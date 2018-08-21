package edu.oregonstate.mist.personsapi

import com.codahale.metrics.annotation.Timed
import edu.oregonstate.mist.api.Error
import edu.oregonstate.mist.api.Resource
import edu.oregonstate.mist.api.jsonapi.ResourceObject
import edu.oregonstate.mist.api.jsonapi.ResultObject
import edu.oregonstate.mist.personsapi.core.MealPlan
import edu.oregonstate.mist.personsapi.core.JobObject
import edu.oregonstate.mist.personsapi.core.PersonObject
import edu.oregonstate.mist.personsapi.core.PersonObjectException
import edu.oregonstate.mist.personsapi.db.PersonsDAO
import edu.oregonstate.mist.personsapi.db.PersonsWriteDAO
import groovy.transform.TypeChecked
import org.apache.commons.lang3.StringUtils

import javax.annotation.security.PermitAll
import javax.imageio.ImageIO
import javax.validation.Valid
import javax.ws.rs.Consumes
import javax.ws.rs.GET
import javax.ws.rs.POST
import javax.ws.rs.Path
import javax.ws.rs.PathParam
import javax.ws.rs.Produces
import javax.ws.rs.QueryParam
import javax.ws.rs.core.MediaType
import javax.ws.rs.core.Response

@Path("persons")
@Produces(MediaType.APPLICATION_JSON)
@PermitAll
@TypeChecked
class PersonsResource extends Resource {
    private final PersonsDAO personsDAO
    private final PersonsWriteDAO personsWriteDAO
    private PersonUriBuilder personUriBuilder
    private final Integer maxImageWidth = 2000

    PersonsResource(PersonsDAO personsDAO, PersonsWriteDAO personsWriteDAO, URI endpointUri) {
        this.personsDAO = personsDAO
        this.personsWriteDAO = personsWriteDAO
        this.endpointUri = endpointUri
        this.personUriBuilder = new PersonUriBuilder(endpointUri)
    }

    @Timed
    @GET
    Response list(@QueryParam('onid') String onid,
                  @QueryParam('osuID') String osuID,
                  @QueryParam('osuUID') String osuUID,
                  @QueryParam('firstName') String firstName,
                  @QueryParam('lastName') String lastName,
                  @QueryParam('searchOldNames') Boolean searchOldNames,
                  @QueryParam('searchOldOsuIDs') Boolean searchOldOsuIDs) {
        // Check for a bad request.
        Closure<Integer> getSize = { List<String> list -> list.findAll { it }.size() }

        Integer idCount = getSize([onid, osuID, osuUID])
        Integer nameCount = getSize([firstName, lastName])

        Boolean validNameRequest = nameCount == 2

        String errorMessage

        if (osuUID && !osuUID.matches("[0-9]+")) {
            errorMessage = "OSU UID can only contain numbers."
        } else if (validNameRequest && idCount != 0) {
            errorMessage = "Cannot search by a name and an ID in the same request."
        } else if (idCount > 1) {
            errorMessage = "onid, osuID, and osuUID cannot be included together."
        } else if (nameCount == 1) {
            errorMessage = "firstName and lastName must be included together."
        } else if (searchOldNames && searchOldOsuIDs) {
            errorMessage = "searchOldNames and searchOldOsuIDs cannot both be true"
        } else if (searchOldOsuIDs && (onid || osuUID)) {
            errorMessage = "searchOldOsuIDs can only be used with OSU ID queries."
        } else if (searchOldOsuIDs && !osuID) {
            errorMessage = "osuID must be included if searchOldOsuIDs is true."
        } else if (searchOldNames && !validNameRequest) {
            errorMessage = "firstName and lastName must be included if searchOldNames is true."
        } else if (!idCount && !nameCount) {
            errorMessage = "No names or IDs were provided in the request."
        }

        if (errorMessage) {
            return badRequest(errorMessage).build()
        }

        // At this point, the request is valid. Proceed with desired data retrieval.
        List<PersonObject> persons

        if (!nameCount && idCount == 1) {
            if (!searchOldOsuIDs) {
                // Search by a current ID.
                persons = personsDAO.getPersons(onid, osuID, osuUID, null, null, false)
            } else {
                // Search current and previous OSU ID's.
                persons = personsDAO.getPersons(null, osuID, null, null, null, true)
            }
        } else if (!idCount && validNameRequest) {
            String formattedFirstName = formatName(firstName)
            String formattedLastName = formatName(lastName)

            if (!searchOldNames) {
                // Search current names.
                persons = personsDAO.getPersons(null, null, null, formattedFirstName,
                        formattedLastName, false)
            } else {
                // Search current and previous names.
                persons = personsDAO.getPersons(null, null, null, formattedFirstName,
                        formattedLastName, true)
            }
        } else {
            return internalServerError("The application encountered an unexpected condition.")
                    .build()
        }

        ResultObject res = personResultObject(persons)
        ok(res).build()
    }

    /**
     * Strip accents and convert to uppercase to prepare for DAO.
     * @param name
     * @return
     */
    String formatName(String name) {
        StringUtils.stripAccents(name).toUpperCase()
    }

    @Timed
    @GET
    @Path('{osuID: [0-9]+}')
    Response getPersonById(@PathParam('osuID') String osuID) {
        def person = personsDAO.getPersons(null, osuID, null, null, null, false)
        if (person) {
            ResultObject res = personResultObject(person?.get(0))
            ok(res).build()
        } else {
            notFound().build()
        }
    }

    ResultObject personResultObject(List<PersonObject> persons) {
        new ResultObject(data: persons.collect { personResourceObject(it) })
    }

    ResultObject personResultObject(PersonObject person) {
        new ResultObject(data: personResourceObject(person))
    }

    ResourceObject personResourceObject(PersonObject person) {
        person.previousRecords = personsDAO.getPreviousRecords(person.internalID)

        new ResourceObject(
                id: person.osuID,
                type: 'person',
                attributes: person,
                links: ['self': personUriBuilder.personUri(person.osuID)]
        )
    }

    @Timed
    @GET
    @Path('{osuID: [0-9]+}/jobs')
    Response getJobs(@PathParam('osuID') String osuID,
                     @QueryParam('positionNumber') String positionNumber,
                     @QueryParam('suffix') String suffix) {
        if (personsDAO.personExist(osuID)) {
            List<JobObject> jobs = personsDAO.getJobsById(osuID, positionNumber, suffix)
            ok(jobResultObject(jobs, osuID)).build()
        } else {
            notFound().build()
        }
    }

    @Timed
    @POST
    @Consumes (MediaType.APPLICATION_JSON)
    @Path('{osuID: [0-9]+}/jobs')
    Response createJob(@PathParam('osuID') String osuID,
                       @Valid ResultObject resultObject) {
        if (!personsDAO.personExist(osuID)) {
            return notFound().build()
        }

        List<Error> errors = newJobErrors(resultObject)

        if (errors) {
            Response.ResponseBuilder responseBuilder = Response.status(Response.Status.BAD_REQUEST)
            return responseBuilder.entity(errors).build()
        }

        // At this point, the submitted job object is valid. Proceed with posting to message queue.
        JobObject job = JobObject.fromResultObject(resultObject)

        String createJobResult = personsWriteDAO.createJob(osuID, job).getString("return_value")

        //TODO: Should we be checking other conditions besides an null/empty string?
        // null/empty string == success
        if (!createJobResult) {
            accepted(new ResultObject(data: new ResourceObject(attributes: job))).build()
        } else {
            internalServerError("Error creating new job: $createJobResult").build()
        }
    }

    ResultObject jobResultObject(List<JobObject> jobs, String osuID) {
        new ResultObject(data: jobs.collect { jobResourceObject(it, osuID)})
    }

    ResourceObject jobResourceObject(JobObject job, String osuID) {
        job.laborDistribution = personsDAO.getJobLaborDistribution(osuID,
                job.positionNumber, job.suffix)

        new ResourceObject(
                type: 'jobs',
                attributes: job,
                links: ['self': personUriBuilder.personJobsUri(
                        osuID, job.positionNumber, job.suffix)]
        )
    }

    private List<Error> newJobErrors(ResultObject resultObject) {
        List<Error> errors = []

        JobObject job

        def addBadRequest = { String message ->
            errors.add(Error.badRequest(message))
        }

        try {
            job = JobObject.fromResultObject(resultObject)
        } catch (PersonObjectException e) {
            addBadRequest("Could not parse job object. " +
                    "Make sure dates are in ISO8601 format: yyyy-MM-dd")
            // if we can't deserialize the job object, no need to proceed
            return errors
        }

        if (!job) {
            addBadRequest("No job object provided.")

            // if there's no job object, no need to proceed
            return errors
        }

        // at this point, we have a job object. Let's validate the fields
        def requiredFields = ["Position number": job.positionNumber,
                              "Begin date": job.beginDate,
                              "Supervisor OSU ID": job.supervisorOsuID,
                              "Supervisor position number": job.supervisorPositionNumber,
                              "Effective date": job.effectiveDate]

        requiredFields.findAll { key, value -> !value }.each { key, value ->
            addBadRequest("${key} is required.")
        }

        def positiveNumberFields = ["Hourly rate": job.hourlyRate,
                                    "Hours per pay": job.hoursPerPay,
                                    "Assignment salary": job.assignmentSalary,
                                    "Annual salary": job.annualSalary,
                                    "Pays per year": job.paysPerYear]

        positiveNumberFields.findAll { key, value ->
            value && value < 0
        }.each { key, value ->
            addBadRequest("${key} cannot be a negative number.")
        }

        if (!job.isActive()) {
            addBadRequest("'${job.activeJobStatus}' is the only valid job status.")
        }

        if (job.beginDate && job.endDate && (job.beginDate >= job.endDate)) {
            addBadRequest("End date must be after begin date.")
        }

        if (job.contractBeginDate && job.contractEndDate &&
                (job.contractBeginDate >= job.contractEndDate)) {
            addBadRequest("Contract end date must be after begin date.")
        }

        if (job.fullTimeEquivalency &&
                (job.fullTimeEquivalency > 1 || job.fullTimeEquivalency <= 0)) {
            addBadRequest("Full time equivalency must range from 0 to 1.")
        }

        if (job.appointmentPercent &&
                (job.appointmentPercent > 100 || job.appointmentPercent < 0)) {
            addBadRequest("Appointment percent must range from 0 to 100.")
        }

        if (job.supervisorOsuID) {
            if (!personsDAO.personExist(job.supervisorOsuID)) {
                addBadRequest("Supervisor OSU ID does not exist.")
            } else if (job.supervisorPositionNumber) {
                Boolean validSupervisorPosition = personsDAO.isValidSupervisorPosition(
                        job.beginDate,
                        job.supervisorOsuID,
                        job.supervisorPositionNumber,
                        job.supervisorSuffix
                )

                if (!validSupervisorPosition) {
                    addBadRequest("Supervisor does not have an active position with position " +
                            "number ${job.supervisorPositionNumber} for the given begin date.")
                }
            }
        }

        if (job.positionNumber &&
                !personsDAO.isValidPositionNumber(job.positionNumber, job.beginDate)) {
            addBadRequest("${job.positionNumber} is not a valid position number " +
                    "for the given begin date.")
        }

        if (job.locationID && !personsDAO.isValidLocation(job.locationID)) {
            addBadRequest("${job.locationID} is not a valid location ID.")
        }

        if (job.timesheetOrganizationCode && !personsDAO.isValidOrganizationCode(
                job.timesheetOrganizationCode)) {
            addBadRequest("${job.timesheetOrganizationCode} is not a valid organization code.")
        }

        if (job.laborDistribution) {
            BigDecimal totalDistributionPercent = 0

            job.laborDistribution.each {
                if (it.distributionPercent) {
                    totalDistributionPercent += it.distributionPercent
                } else {
                    //TODO: Yes, make sure they're rounded
                    addBadRequest("distributionPercent is required for each labor distribution.")
                }

                //TODO: either have account index code or accountcode+activitycode+org+program+fund
                //TODO: require effective date. All effective dates should be same
                if (!it.accountIndexCode) {
                    addBadRequest("accountIndexCode is required for each labor distribution")
                } else if (!personsDAO.isValidAccountIndexCode(it.accountIndexCode)) {
                    addBadRequest("${it.accountIndexCode} is not a valid accountIndexCode.")
                }

                if (it.accountCode && !personsDAO.isValidAccountCode(it.accountCode)) {
                    addBadRequest("${it.accountCode} is not a valid accountCode.")
                }

                if (it.activityCode && !personsDAO.isValidActivityCode(it.activityCode)) {
                    addBadRequest("${it.activityCode} is not a valid activityCode.")
                }

                //todo: validate org code, program code, fund code
            }

            if (totalDistributionPercent != 100) {
                addBadRequest("Total sum of labor distribution percentages must equal 100.")
            }
        }

        errors
    }

    @Timed
    @GET
    @Produces('image/jpeg')
    @Path('{osuID: [0-9]+}/image')
    Response getImageById(@PathParam('osuID') String osuID, @QueryParam('width') Integer width) {
        if (personsDAO.personExist(osuID)) {
            if (width != null && (width <= 0) || (width > maxImageWidth)) {
                String widthError = 'Width must be value from 1 - ' + maxImageWidth
                return badRequest(widthError).type(MediaType.APPLICATION_JSON).build()
            }

            def res
            def image = personsDAO.getImageById(osuID)
            if (image) {
                res = ImageManipulation.getImageStream(ImageIO.read(image.getBinaryStream()), width)
            } else {
                ClassLoader classLoader = Thread.currentThread().getContextClassLoader()
                InputStream imageInputStream = classLoader.getResourceAsStream('defaultImage.jpg')
                res = ImageManipulation.getImageStream(ImageIO.read(imageInputStream), width)
            }
            ok(res).build()
        } else {
            notFound().type(MediaType.APPLICATION_JSON).build()
        }
    }

    @Timed
    @GET
    @Path('{osuID: [0-9]+}/meal-plans')
    Response getMealPlans(@PathParam('osuID') String osuID) {
        if (personsDAO.personExist(osuID)) {
            List<MealPlan> mealPlans = personsDAO.getMealPlans(osuID, null)

            ResultObject resultObject = new ResultObject(
                    data: mealPlans.collect {
                        getMealPlanResourceObject(it, osuID)
                    }
            )

            ok(resultObject).build()
        } else {
            notFound().build()
        }
    }

    @Timed
    @GET
    @Path('{osuID: [0-9]+}/meal-plans/{mealPlanID}')
    Response getMealPlanByID(@PathParam('osuID') String osuID,
                             @PathParam('mealPlanID') String mealPlanID) {
        if (personsDAO.personExist(osuID)) {
            List<MealPlan> mealPlans = personsDAO.getMealPlans(
                    osuID, mealPlanID)

            if (mealPlans) {
                ResultObject resultObject = new ResultObject(
                        data: getMealPlanResourceObject(mealPlans?.get(0), osuID)
                )
                ok(resultObject).build()
            } else {
                notFound().build()
            }
        } else {
            notFound().build()
        }
    }

    private ResourceObject getMealPlanResourceObject(
            MealPlan mealPlan, String osuID) {
        new ResourceObject(
                id: mealPlan.mealPlanID,
                type: "meal-plans",
                attributes: mealPlan,
                links: ['self': personUriBuilder.mealPlanUri(osuID, mealPlan.mealPlanID)]
        )
    }
}

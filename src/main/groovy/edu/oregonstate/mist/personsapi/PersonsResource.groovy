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
import edu.oregonstate.mist.personsapi.db.PersonsStringTemplateDAO
import edu.oregonstate.mist.personsapi.db.PersonsWriteDAO
import groovy.transform.TypeChecked
import org.apache.commons.lang3.StringUtils
import org.slf4j.Logger
import org.slf4j.LoggerFactory

import javax.annotation.security.PermitAll
import javax.imageio.ImageIO
import javax.validation.Valid
import javax.ws.rs.Consumes
import javax.ws.rs.GET
import javax.ws.rs.POST
import javax.ws.rs.PUT
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
    private final PersonsStringTemplateDAO personsStringTemplateDAO
    private final PersonsWriteDAO personsWriteDAO
    private PersonUriBuilder personUriBuilder

    private static final Integer maxImageWidth = 2000

    // The max number of IDs retrieved in a single request
    private static final Integer maxIDListLimit = 50

    private static final String notValidErrorPhrase = "is not a valid"
    private static final String jobIDDelimiter = "-"

    private static final String studentEmploymentType = "student"
    private static final String graduateEmploymentType = "graduate"
    private static final List<String> validEmploymentTypes = [
            studentEmploymentType, graduateEmploymentType
    ]

    private static Logger logger = LoggerFactory.getLogger(this)

    PersonsResource(PersonsDAO personsDAO,
                    PersonsStringTemplateDAO personsStringTemplateDAO,
                    PersonsWriteDAO personsWriteDAO,
                    URI endpointUri) {
        this.personsDAO = personsDAO
        this.personsStringTemplateDAO = personsStringTemplateDAO
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

        List<String> osuIDList = getListFromString(osuID)

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
        } else if (!isValidIDList(osuIDList)) {
            errorMessage = "The number of IDs in a request cannot exceed $maxIDListLimit."
        }

        if (errorMessage) {
            return badRequest(errorMessage).build()
        }

        // At this point, the request is valid. Proceed with desired data retrieval.
        List<PersonObject> persons

        if (!nameCount && idCount == 1) {
            if (!searchOldOsuIDs) {
                // Search by a current ID.
                persons = personsStringTemplateDAO.getPersons(
                        onid, osuIDList, osuUID, null, null, false)
            } else {
                // Search current and previous OSU ID's.
                persons = personsStringTemplateDAO.getPersons(
                        null, osuIDList, null, null, null, true)
            }
        } else if (!idCount && validNameRequest) {
            String formattedFirstName = formatName(firstName)
            String formattedLastName = formatName(lastName)

            if (!searchOldNames) {
                // Search current names.
                persons = personsStringTemplateDAO.getPersons(null, null, null, formattedFirstName,
                        formattedLastName, false)
            } else {
                // Search current and previous names.
                persons = personsStringTemplateDAO.getPersons(null, null, null, formattedFirstName,
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

    /**
     * Get a list of strings from a comma delimited string. Used for searching for multiple IDs in
     * a persons request.
     * @param commaDelimitedList
     * @return
     */
    private static List<String> getListFromString(String commaDelimitedList) {
        commaDelimitedList?.tokenize(",")
    }

    private Boolean isValidIDList(List<String> idList) {
        !idList || idList.size() <= maxIDListLimit
    }

    @Timed
    @GET
    @Path('{osuID: [0-9]+}')
    Response getPersonById(@PathParam('osuID') String osuID) {
        def person = personsStringTemplateDAO.getPersons(null, [osuID], null, null, null, false)
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
            ok(jobsResultObject(jobs, osuID)).build()
        } else {
            notFound().build()
        }
    }

    @Timed
    @POST
    @Consumes (MediaType.APPLICATION_JSON)
    @Path('{osuID: [0-9]+}/jobs')
    Response createJob(@PathParam('osuID') String osuID,
                       @Valid ResultObject resultObject,
                       @QueryParam('employmentType') String employmentType) {
        if (!personsDAO.personExist(osuID)) {
            return notFound().build()
        }

        List<Error> errors = newJobErrors(resultObject, osuID, employmentType, false)

        if (errors) {
            return errorArrayResponse(errors)
        }

        createOrUpdateJobInDB(resultObject, osuID, employmentType, false)
    }

    @Timed
    @GET
    @Path('{osuID: [0-9]+}/jobs/{jobID: [0-9a-zA-Z-]+}')
    Response getJobById(@PathParam('osuID') String osuID,
                        @PathParam('jobID') String jobID) {
        if (!personsDAO.personExist(osuID)) {
            return notFound().build()
        }

        JobObject job = getJobObject(osuID, jobID)

        if (!job) {
            notFound().build()
        } else {
            ok(jobResultObject(job, osuID)).build()
        }
    }

    @Timed
    @PUT
    @Consumes (MediaType.APPLICATION_JSON)
    @Path('{osuID: [0-9]+}/jobs/{jobID: [0-9a-zA-Z-]+}')
    Response updateJob(@PathParam('osuID') String osuID,
                       @PathParam('jobID') String jobID,
                       @Valid ResultObject resultObject,
                       @QueryParam('employmentType') String employmentType) {
        if (!personsDAO.personExist(osuID) || !getJobObject(osuID, jobID)) {
            return notFound().build()
        }

        List<Error> errors = newJobErrors(resultObject, osuID, employmentType, true)

        if (errors) {
            return errorArrayResponse(errors)
        }

        createOrUpdateJobInDB(resultObject, osuID, employmentType, true)
    }

    private Response createOrUpdateJobInDB(ResultObject resultObject,
                                           String osuID,
                                           String employmentType,
                                           Boolean update) {
        JobObject job = JobObject.fromResultObject(resultObject)

        String dbFunctionOutput

        switch (employmentType) {
            case studentEmploymentType:
                if (update) {
                    logger.info("Updating $studentEmploymentType job")
                    dbFunctionOutput = personsWriteDAO.updateStudentJob(osuID, job)
                            .getString(PersonsWriteDAO.outParameter)
                } else {
                    logger.info("Creating $studentEmploymentType job")
                    dbFunctionOutput = personsWriteDAO.createStudentJob(osuID, job)
                            .getString(PersonsWriteDAO.outParameter)
                }
                break
            case graduateEmploymentType:
                if (update) {
                    logger.info("Updating $graduateEmploymentType job")
                    dbFunctionOutput = personsWriteDAO.updateGraduateJob(osuID, job)
                            .getString(PersonsWriteDAO.outParameter)
                } else {
                    logger.info("Creating $graduateEmploymentType job")
                    dbFunctionOutput = personsWriteDAO.createGraduateJob(osuID, job)
                            .getString(PersonsWriteDAO.outParameter)
                }
                break
        }

        //TODO: Should we be checking other conditions besides an null/empty string?
        // null/empty string == success
        if (!dbFunctionOutput) {
            accepted(new ResultObject(data: new ResourceObject(attributes: job))).build()
        } else {
            logger.error("Unexpected database return value: $dbFunctionOutput")
            internalServerError("Error creating new job: $dbFunctionOutput").build()
        }
    }

    private Response errorArrayResponse(List<Error> errors) {
        Response.ResponseBuilder responseBuilder = Response.status(Response.Status.BAD_REQUEST)
        responseBuilder.entity(errors).build()
    }

    private JobObject getJobObject(String osuID, String jobID) {
        def parsedJobID = parseJobID(jobID)

        String positionNumber = parsedJobID.positionNumber
        String suffix = parsedJobID.suffix

        if (!positionNumber || !suffix) {
            return null
        }

        List<JobObject> job = personsDAO.getJobsById(osuID, positionNumber, suffix)

        if (!job) {
            null
        } else {
            job?.get(0)
        }
    }

    /**
     * Parses a hyphen delimited positionNumber and suffix to a map.
     * @param jobID
     * @return
     */
    private static Map<String, String> parseJobID(String jobID) {
        def parsedJobID = [positionNumber: "", suffix: ""]

        // jobID should be in the format of "positionNumber-suffix"
        def splitJobID = jobID.split(jobIDDelimiter, 2)
        if (splitJobID.length != 2) {
            return parsedJobID
        }

        parsedJobID.positionNumber = splitJobID[0]
        parsedJobID.suffix = splitJobID[1]

        parsedJobID
    }

    /**
     * Joins a positionNumber and suffix into a single hyphen delimited string.
     * @param positionNumber
     * @param suffix
     * @return
     */
    private static String joinJobID(String positionNumber, String suffix) {
        positionNumber + jobIDDelimiter + suffix
    }

    ResultObject jobsResultObject(List<JobObject> jobs, String osuID) {
        new ResultObject(data: jobs.collect { jobResourceObject(it, osuID)})
    }

    ResultObject jobResultObject(JobObject job, String osuID) {
        new ResultObject(data: jobResourceObject(job, osuID))
    }

    ResourceObject jobResourceObject(JobObject job, String osuID) {
        job.laborDistribution = personsDAO.getJobLaborDistribution(osuID,
                job.positionNumber, job.suffix)

        new ResourceObject(
                id: joinJobID(job.positionNumber, job.suffix),
                type: 'jobs',
                attributes: job,
                links: ['self': personUriBuilder.personJobsUri(
                        osuID, job.positionNumber, job.suffix)]
        )
    }

    private List<Error> newJobErrors(ResultObject resultObject,
                                     String osuID,
                                     String employmentType,
                                     Boolean update) {
        List<Error> errors = []

        JobObject job

        def addBadRequest = { String message ->
            errors.add(Error.badRequest(message))
        }

        if (!validEmploymentTypes.contains(employmentType)) {
            addBadRequest("Invalid employmentType (query parameter). " +
                    "Valid types are: ${validEmploymentTypes.join(", ")}")
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
        def requiredFields = ["Position number"           : job.positionNumber,
                              "Begin date"                : job.beginDate,
                              "Supervisor OSU ID"         : job.supervisorOsuID,
                              "Supervisor position number": job.supervisorPositionNumber,
                              "Effective date"            : job.effectiveDate]

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

        if (!job.suffix && update) {
            addBadRequest("Suffix is required when updating an existing job.")
        }

        if (!update && job.positionNumber && job.suffix && personsDAO.getJobsById(
                osuID, job.positionNumber, job.suffix)) {
            addBadRequest("Person already has a job with the given position number and suffix.")
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

        if (!update && job.beginDate && job.effectiveDate && (job.beginDate != job.effectiveDate)) {
            addBadRequest("Begin date and effective date must match for new jobs.")
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
            addBadRequest("${job.positionNumber} $notValidErrorPhrase position number " +
                    "for the given begin date.")
        }

        if (job.locationID && !personsDAO.isValidLocation(job.locationID)) {
            addBadRequest("${job.locationID} $notValidErrorPhrase location ID.")
        }

        if (job.timesheetOrganizationCode && !personsDAO.isValidOrganizationCode(
                job.timesheetOrganizationCode)) {
            addBadRequest("${job.timesheetOrganizationCode} $notValidErrorPhrase " +
                    "organization code.")
        }

        if (job.laborDistribution) {
            BigDecimal totalDistributionPercent = 0
            boolean missingDistributionPercent = false
            boolean missingEffectiveDate = false

            job.laborDistribution.each {
                if (it.distributionPercent) {
                    //validate the total later
                    totalDistributionPercent += it.distributionPercent
                } else {
                    //each labor distribution must have a distribution percent
                    missingDistributionPercent = true
                }

                //each labor distribution must have an effective date
                //no need to validate the date format here. jackson does that earlier
                missingEffectiveDate = !it.effectiveDate ? true : missingEffectiveDate

                if (!it.accountIndexCode ||
                        !personsDAO.isValidAccountIndexCode(it.accountIndexCode)) {
                    //accountIndexCode is required
                    addBadRequest("${it.accountIndexCode} $notValidErrorPhrase accountIndexCode.")
                }

                if (it.accountCode && !personsDAO.isValidAccountCode(it.accountCode)) {
                    addBadRequest("${it.accountCode} $notValidErrorPhrase accountCode.")
                }

                if (it.activityCode && !personsDAO.isValidActivityCode(it.activityCode)) {
                    addBadRequest("${it.activityCode} $notValidErrorPhrase activityCode.")
                }

                if (it.organizationCode &&
                        !personsDAO.isValidOrganizationCode(it.organizationCode)) {
                    addBadRequest("${it.organizationCode} $notValidErrorPhrase organizationCode.")
                }

                if (it.programCode && !personsDAO.isValidProgramCode(it.programCode)) {
                    addBadRequest("${it.programCode} $notValidErrorPhrase programCode.")
                }

                if (it.fundCode && !personsDAO.isValidFundCode(it.fundCode)) {
                    addBadRequest("${it.fundCode} $notValidErrorPhrase fundCode.")
                }

                if (it.locationCode && !personsDAO.isValidLocationCode(it.locationCode)) {
                    addBadRequest("${it.locationCode} $notValidErrorPhrase locationCode.")
                }
            }

            if (missingDistributionPercent) {
                addBadRequest("distributionPercent is required for each labor distribution.")
            } else if (totalDistributionPercent != 100) {
                addBadRequest("Total sum of labor distribution percentages must equal 100.")
            }

            if (missingEffectiveDate) {
                addBadRequest("effectiveDate is required for each labor distribution.")
            } else if (job.laborDistribution.collect {
                it.effectiveDate.atStartOfDay() }.unique().size() > 1) {
                addBadRequest("effectiveDate must be the same for each labor distribution.")
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

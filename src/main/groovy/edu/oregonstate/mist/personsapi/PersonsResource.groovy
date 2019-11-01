package edu.oregonstate.mist.personsapi

import com.codahale.metrics.annotation.Timed
import edu.oregonstate.mist.api.Error
import edu.oregonstate.mist.api.Resource
import edu.oregonstate.mist.api.jsonapi.ResourceObject
import edu.oregonstate.mist.api.jsonapi.ResultObject
import edu.oregonstate.mist.personsapi.core.AddressObject
import edu.oregonstate.mist.personsapi.core.AddressRecordObject
import edu.oregonstate.mist.personsapi.core.MealPlan
import edu.oregonstate.mist.personsapi.core.JobObject
import edu.oregonstate.mist.personsapi.core.PersonObject
import edu.oregonstate.mist.personsapi.core.PersonObjectException
import edu.oregonstate.mist.personsapi.core.PhoneObject
import edu.oregonstate.mist.personsapi.core.PhoneRecordObject
import edu.oregonstate.mist.personsapi.db.BannerPersonsReadDAO
import edu.oregonstate.mist.personsapi.db.ODSPersonsReadDAO
import edu.oregonstate.mist.personsapi.db.PersonsStringTemplateDAO
import edu.oregonstate.mist.personsapi.db.BannerPersonsWriteDAO
import groovy.transform.TypeChecked
import org.apache.commons.lang3.StringUtils
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.skife.jdbi.v2.exceptions.UnableToExecuteStatementException

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
import javax.ws.rs.core.Context
import javax.ws.rs.core.MediaType
import javax.ws.rs.core.Response
import javax.ws.rs.core.UriInfo

@Path("persons")
@Produces(MediaType.APPLICATION_JSON)
@PermitAll
@TypeChecked
class PersonsResource extends Resource {
    private final BannerPersonsReadDAO bannerPersonsReadDAO
    private final PersonsStringTemplateDAO personsStringTemplateDAO
    private final BannerPersonsWriteDAO bannerPersonsWriteDAO
    private final ODSPersonsReadDAO odsPersonsReadDAO
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

    PersonsResource(BannerPersonsReadDAO bannerPersonsReadDAO,
                    PersonsStringTemplateDAO personsStringTemplateDAO,
                    BannerPersonsWriteDAO bannerPersonsWriteDAO,
                    ODSPersonsReadDAO odsPersonsReadDAO,
                    URI endpointUri) {
        this.bannerPersonsReadDAO = bannerPersonsReadDAO
        this.personsStringTemplateDAO = personsStringTemplateDAO
        this.bannerPersonsWriteDAO = bannerPersonsWriteDAO
        this.odsPersonsReadDAO = odsPersonsReadDAO
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

    @Timed
    @POST
    @Consumes (MediaType.APPLICATION_JSON)
    Response createPerson(@Valid ResultObject resultObject) {
        PersonObject person
        try {
            person = PersonObject.fromResultObject(resultObject)
            if (!person) {
                return badRequest("No person object provided").build()
            } else if ([
                person.name.firstName,
                person.name.lastName,
                person.sex,
                person.birthDate].contains(null)
            ) {
                return badRequest("Required fields are missing or are null.").build()
            } else if (!(person.sex in ["M", "F", "N"])) {
                return badRequest("Sex must be one of 'M', 'F', 'N'.").build()
            } else if (!(person.citizen in [null, "FN", "N", "R", "S", "C"])) {
                return badRequest("Citizen must be one of 'FN', 'N', 'R', 'S', 'C'.").build()
            }
        } catch (PersonObjectException e) {
            return badRequest(
                "Unable to parse person object or required fields are missing. " +
                "Please make sure all required fields are included and in the corret format."
            ).build()
        }

        String dbFunctionOutput = bannerPersonsWriteDAO.createPerson(person)
                                                 .getString(BannerPersonsWriteDAO.outParameter)

        if (!dbFunctionOutput.startsWith("ERROR")) {
            def createdPerson = personsStringTemplateDAO.getPersons(
                null, [dbFunctionOutput], null, null, null, false
            )
            if (createdPerson) {
                ResultObject res = personResultObject(createdPerson?.get(0))
                accepted(res).build()
            } else {
                internalServerError("Person has been created but not found.").build()
            }
        } else {
            badRequest(dbFunctionOutput).build()
        }
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
        person.previousRecords = bannerPersonsReadDAO.getPreviousRecords(person.internalID)

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
        if (bannerPersonsReadDAO.personExist(osuID)) {
            List<JobObject> jobs = bannerPersonsReadDAO.getJobsById(osuID, positionNumber, suffix)
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
        if (!bannerPersonsReadDAO.personExist(osuID)) {
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
        if (!bannerPersonsReadDAO.personExist(osuID)) {
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
        if (!bannerPersonsReadDAO.personExist(osuID) || !getJobObject(osuID, jobID)) {
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
                    dbFunctionOutput = bannerPersonsWriteDAO.updateStudentJob(osuID, job)
                            .getString(BannerPersonsWriteDAO.outParameter)
                } else {
                    logger.info("Creating $studentEmploymentType job")
                    dbFunctionOutput = bannerPersonsWriteDAO.createStudentJob(osuID, job)
                            .getString(BannerPersonsWriteDAO.outParameter)
                }
                break
            case graduateEmploymentType:
                if (update) {
                    logger.info("Updating $graduateEmploymentType job")
                    dbFunctionOutput = bannerPersonsWriteDAO.updateGraduateJob(osuID, job)
                            .getString(BannerPersonsWriteDAO.outParameter)
                } else {
                    logger.info("Creating $graduateEmploymentType job")
                    dbFunctionOutput = bannerPersonsWriteDAO.createGraduateJob(osuID, job)
                            .getString(BannerPersonsWriteDAO.outParameter)
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

        List<JobObject> job = bannerPersonsReadDAO.getJobsById(osuID, positionNumber, suffix)

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
        job.laborDistribution = bannerPersonsReadDAO.getJobLaborDistribution(osuID,
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
            addBadRequest(
                "Could not parse job object. Please make sure all required fields are included " +
                "and in the corret format."
            )
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

        if (!update && job.positionNumber && job.suffix && bannerPersonsReadDAO.getJobsById(
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

        def earnFields = [
            job.earnCode,
            job.earnCodeEffectiveDate,
            job.earnCodeHours,
            job.earnCodeShift
        ]

        if (!(earnFields.every {it} || earnFields.every {!it})) {
            addBadRequest("earnCode, earnCodeEffectiveDate, earnCodeHours, earnCodeShift" +
                          " should be all null or all not null.")
        }

        if (job.supervisorOsuID) {
            if (!bannerPersonsReadDAO.personExist(job.supervisorOsuID)) {
                addBadRequest("Supervisor OSU ID does not exist.")
            } else if (job.supervisorPositionNumber) {
                Boolean validSupervisorPosition = bannerPersonsReadDAO.isValidSupervisorPosition(
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

        if (job.positionNumber) {
            if (!bannerPersonsReadDAO.isValidPositionNumber(job.positionNumber, job.beginDate)) {
                addBadRequest("${job.positionNumber} $notValidErrorPhrase position number " +
                        "for the given begin date.")
            }
            if (employmentType == studentEmploymentType && !job.isValidStudentPositionNumber()) {
                addBadRequest("Student position numbers must begin with one of these prefixes: " +
                        "${JobObject.validStudentPositionNumberPrefixes.join(", ")}")
            }
        }

        if (job.locationID && !bannerPersonsReadDAO.isValidLocation(job.locationID)) {
            addBadRequest("${job.locationID} $notValidErrorPhrase location ID.")
        }

        if (job.timesheetOrganizationCode && !bannerPersonsReadDAO.isValidOrganizationCode(
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
                        !bannerPersonsReadDAO.isValidAccountIndexCode(it.accountIndexCode)) {
                    //accountIndexCode is required
                    addBadRequest("${it.accountIndexCode} $notValidErrorPhrase accountIndexCode.")
                }

                if (it.accountCode && !bannerPersonsReadDAO.isValidAccountCode(it.accountCode)) {
                    addBadRequest("${it.accountCode} $notValidErrorPhrase accountCode.")
                }

                if (it.activityCode && !bannerPersonsReadDAO.isValidActivityCode(it.activityCode)) {
                    addBadRequest("${it.activityCode} $notValidErrorPhrase activityCode.")
                }

                if (it.organizationCode &&
                        !bannerPersonsReadDAO.isValidOrganizationCode(it.organizationCode)) {
                    addBadRequest("${it.organizationCode} $notValidErrorPhrase organizationCode.")
                }

                if (it.programCode && !bannerPersonsReadDAO.isValidProgramCode(it.programCode)) {
                    addBadRequest("${it.programCode} $notValidErrorPhrase programCode.")
                }

                if (it.fundCode && !bannerPersonsReadDAO.isValidFundCode(it.fundCode)) {
                    addBadRequest("${it.fundCode} $notValidErrorPhrase fundCode.")
                }

                if (it.locationCode && !bannerPersonsReadDAO.isValidLocationCode(it.locationCode)) {
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
        if (bannerPersonsReadDAO.personExist(osuID)) {
            if (width != null && (width <= 0) || (width > maxImageWidth)) {
                String widthError = 'Width must be value from 1 - ' + maxImageWidth
                return badRequest(widthError).type(MediaType.APPLICATION_JSON).build()
            }

            def res
            def image = bannerPersonsReadDAO.getImageById(osuID)
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
        if (odsPersonsReadDAO.personExist(osuID)) {
            List<MealPlan> mealPlans = odsPersonsReadDAO.getMealPlans(osuID, null)

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
        if (odsPersonsReadDAO.personExist(osuID)) {
            List<MealPlan> mealPlans = odsPersonsReadDAO.getMealPlans(
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

    @Timed
    @GET
    @Path('{osuID: [0-9]+}/addresses')
    Response getAddresses(@PathParam('osuID') String osuID,
                          @QueryParam('addressType') String addressType) {
        if (bannerPersonsReadDAO.personExist(osuID)) {
            List<Error> errors = validateTypeParams(addressType, null)
            if (errors) {
                return errorArrayResponse(errors)
            }

            List<AddressObject> addresses = bannerPersonsReadDAO.getAddresses(osuID, addressType)

            ResultObject resultObject = new ResultObject(
                    data: addresses.collect {
                        new ResourceObject(
                                id: it.id,
                                type: "addresses",
                                attributes: it
                        )
                    }
            )

            ok(resultObject).build()
        } else {
            notFound().build()
        }
    }

    // Validate new address object
    private List<Error> newAddressErrors(AddressObject address) {
        List<Error> errors = []
        Closure addBadRequest = { String message ->
            errors.add(Error.badRequest(message))
        }

        [
            [true, "addressType", 2,
             { String addressType -> bannerPersonsReadDAO.isValidAddressType(addressType) }
            ],
            [false, "houseNumber", 10, null],
            [false, "addressLine1", 75, null],
            [false, "addressLine2", 75, null],
            [false, "addressLine3", 75, null],
            [false, "addressLine4", 75, null],
            [true, "city", 50, null],
            [false, "countyCode", 5,
             { String countyCode -> bannerPersonsReadDAO.isValidCountyCode(countyCode) }
            ],
            [false, "stateCode", 3,
             { String stateCode -> bannerPersonsReadDAO.isValidStateCode(stateCode) }
            ],
            [false, "postalCode", 30, null],
            [false, "nationCode", 5,
             { String nationCode -> bannerPersonsReadDAO.isValidNationCode(nationCode) }
            ]
        ].each {
            Boolean isRequired = it.get(0)
            String fieldName = it.get(1)
            String fieldValue = address[fieldName]
            Integer length = it.get(2)
            Closure validateFunction = it.get(3)

            // Check if required field is missing
            if (isRequired && !fieldValue) {
                addBadRequest("Required field $fieldName is missing or null.")
            }

            // Check if input field is over the buffer size
            if (fieldValue?.length() > length) {
                addBadRequest("$fieldName can't be more than $length characters.")
            }

            // Check if input field is valid
            if (fieldValue && validateFunction && !validateFunction(fieldValue)) {
                addBadRequest("$fieldName is not valid.")
            }
        }

        errors
    }

    @Timed
    @POST
    @Consumes (MediaType.APPLICATION_JSON)
    @Path('{osuID: [0-9]+}/addresses')
    Response createAddress(@PathParam('osuID') String osuID,
                           @Valid ResultObject resultObject) {
        String pidm = bannerPersonsReadDAO.personExist(osuID)
        if (!pidm) {
            return notFound().build()
        }
        AddressObject address
        try {
            address = AddressObject.fromResultObject(resultObject)
            if (!address) {
                return badRequest("No address object provided.").build()
            }

            List<Error> errors = newAddressErrors(address)
            if (errors) {
                return errorArrayResponse(errors)
            }
        } catch (PersonObjectException e) {
            return badRequest(
                "Unable to parse address object or required fields are missing. " +
                "Please make sure all required fields are included and in the correct format."
            ).build()
        }

        try {
            String addressType = resultObject.data['attributes']['addressType']
            AddressRecordObject addressRecord = bannerPersonsReadDAO.hasSameAddressType(
                pidm, addressType
            )
            // query phoneRecord early because deactivateAddress will set the status to inactive
            PhoneRecordObject phoneRecord = bannerPersonsReadDAO.phoneHasSameAddressType(
                pidm, addressType
            )
            if (addressRecord?.rowID) {
                logger.info("Address with the same type exist. Deactivate the current one.")
                bannerPersonsWriteDAO.deactivateAddress(pidm, addressRecord)
            }

            try {
                logger.info("Creating new address.")
                bannerPersonsWriteDAO.createAddress(pidm, address)
            } catch (Exception e) {
                logger.info("Unable to create new address record. Reactivate the current one.")
                bannerPersonsWriteDAO.reactivateAddress(pidm, addressRecord)
                throw new Exception("Unable to create new address record.")
            }

            List<AddressObject> addresses = bannerPersonsReadDAO.getAddresses(osuID, addressType)

            if (addresses.size() != 1) {
                throw new Exception("New record created but more than one records are valid.")
            }

            updatePhoneAddrSeqno(pidm, addressRecord, phoneRecord)

            accepted(new ResultObject(
                data: new ResourceObject(
                    id: addresses[0].id,
                    type: "addresses",
                    attributes: addresses[0]
                )
            )).build()
        } catch (UnableToExecuteStatementException e) {
            internalServerError("Unable to execute SQL query.").build()
        } catch (Exception e) {
            internalServerError(
                "Internal Server Error, please contact API support team for further assistance."
            ).build()
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

    @Timed
    @POST
    @Consumes (MediaType.APPLICATION_JSON)
    @Path('{osuID: [0-9]+}/ssn')
    Response createSSN(@PathParam('osuID') String osuID,
                       @Valid ResultObject resultObject) {

        String pidm = bannerPersonsReadDAO.personExist(osuID)
        if (!pidm) {
            return notFound().build()
        }

        if (bannerPersonsReadDAO.ssnIsNotNull(osuID) == "Y") {
            return badRequest("Persons's SSN is not null or in vault").build()
        }

        String ssn = resultObject.data['attributes']['ssn']
        if (!ssn?.matches(/^\d{9}$/)) {
            return badRequest("SSN must be 9 digits").build()
        }

        try {
            logger.info("Creating SSN")
            if (bannerPersonsReadDAO.hasSPBPERS(pidm)) {
                bannerPersonsWriteDAO.updateSSN(pidm, ssn)
            } else {
                bannerPersonsWriteDAO.createSSN(pidm, ssn)
            }

            accepted(new ResourceObject(
                id: ssn,
                type: "ssn",
                attributes: ["ssn": ssn]
            )).build()
        } catch (UnableToExecuteStatementException e) {
            internalServerError("Unable to execute SQL query").build()
        }
    }

    private List<Error> validateTypeParams(String addressType, String phoneType) {
        List<Error> errors = []
        [
            ["phoneType", phoneType && !bannerPersonsReadDAO.isValidPhoneType(phoneType)],
            ["addressType", addressType && !bannerPersonsReadDAO.isValidAddressType(addressType)]
        ].each {
          String fieldName = it.get(0)
          Boolean invalid = it.get(1)

          if (invalid) {
            errors.add(Error.badRequest("Invalid $fieldName parameter"))
          }
        }
        errors
    }

    @Timed
    @GET
    @Path('{osuID: [0-9]+}/phones')
    Response getPhones(@PathParam('osuID') String osuID,
                       @QueryParam('addressType') String addressType,
                       @QueryParam('phoneType') String phoneType,
                       @Context UriInfo uri) {
        if (bannerPersonsReadDAO.personExist(osuID)) {
            // validate query parameters
            List<Error> errors = validateTypeParams(addressType, phoneType)
            if (errors) {
                return errorArrayResponse(errors)
            }

            List<PhoneObject> phones = bannerPersonsReadDAO.getPhones(osuID, addressType, phoneType)

            ResultObject resultObject = new ResultObject(
                    links: uri.getRequestUri(),
                    data: phones.collect {
                        new ResourceObject(
                                id: it.id,
                                type: "phones",
                                attributes: it,
                                links: ['self': personUriBuilder.phoneUri(
                                    osuID, it.phoneType)]
                        )
                    }
            )

            ok(resultObject).build()
        } else {
            notFound().build()
        }
    }

    // Validate new phone object
    private List<Error> newPhoneErrors(PhoneObject phone) {
        List<Error> errors = []
        Closure addBadRequest = { String message ->
            errors.add(Error.badRequest(message))
        }

        [
            [true, "addressType", 2,
             { String addressType -> bannerPersonsReadDAO.isValidAddressType(addressType) }
            ],
            [true, "phoneType", 2,
             { String phoneType -> bannerPersonsReadDAO.isValidPhoneType(phoneType) }
            ],
            [true, "areaCode", 3, 
             { String areaCode -> areaCode =~ /^[0-9]{1,3}$/ }
            ],
            [true, "phoneNumber", 7,
             { String phoneNumber -> phoneNumber =~ /^[0-9]{1,7}$/ }
            ],
            [false, "phoneExtension", 4,
             { String phoneExtension -> phoneExtension =~ /^[0-9]{1,4}$/ }
            ],
            [true, "primaryIndicator", 5, null],
        ].each {
            Boolean isRequired = it.get(0)
            String fieldName = it.get(1)
            String fieldValue = phone[fieldName]
            Integer length = it.get(2)
            Closure validateFunction = it.get(3)

            // Check if required field is missing
            if (isRequired && !fieldValue) {
                addBadRequest("Required field $fieldName is missing or null.")
            }

            // Check if input field is over the buffer size
            if (fieldValue?.length() > length) {
                addBadRequest("$fieldName can't be more than $length characters.")
            }

            // Check if input field is valid
            if (fieldValue && validateFunction && !validateFunction(fieldValue)) {
                addBadRequest("$fieldName is not valid.")
            }
        }

        errors
    }

    @Timed
    @POST
    @Consumes (MediaType.APPLICATION_JSON)
    @Path('{osuID: [0-9]+}/phones')
    Response createPhones(@PathParam('osuID') String osuID,
                          @Valid ResultObject resultObject) {
        String pidm = bannerPersonsReadDAO.personExist(osuID)
        if (!pidm) {
            return notFound().build()
        }
        PhoneObject phone
        try {
            phone = PhoneObject.fromResultObject(resultObject)
            if (!phone) {
                return badRequest("No phone object provided.").build()
            }

            List<Error> errors = newPhoneErrors(phone)
            if (errors) {
                return errorArrayResponse(errors)
            }
        } catch (PersonObjectException e) {
            return badRequest(
                "Unable to parse phone object or required fields are missing. " +
                "Please make sure all required fields are included and in the correct format."
            ).build()
        }

        String phoneType = resultObject.data['attributes']['phoneType']
        String addressType = resultObject.data['attributes']['addressType']
        PhoneRecordObject phoneRecord = bannerPersonsReadDAO.hasSamePhoneType(
            pidm, phoneType
        )
        AddressRecordObject addressRecord = bannerPersonsReadDAO.hasSameAddressType(
            pidm, addressType
        )
        if(!addressRecord) {
            return badRequest("No address record found with the $addressType address code").build()
        }

        try {
            if (phoneRecord?.id) {
                logger.info("Phone with the same type exists. Deactivate the current one.")
                bannerPersonsWriteDAO.deactivatePhone(pidm, phoneRecord)
            }

            try {
                logger.info("Creating new phone.")
                bannerPersonsWriteDAO.createPhone(pidm, phone, addressRecord)
            } catch (Exception e) {
                e.printStackTrace()
                logger.info("Unable to create new phone record. Reactivating the current one.")
                bannerPersonsWriteDAO.reactivatePhone(pidm, phoneRecord)
                throw new Exception("Unable to create new phone record.")
            }

            List<PhoneObject> phones = bannerPersonsReadDAO.getPhones(osuID, addressType, phoneType)

            if (phones.size() != 1) {
                throw new Exception("New record created but more than one records are valid.")
            }
            accepted(new ResultObject(
                data: new ResourceObject(
                    id: phones[0].id,
                    type: "phones",
                    attributes: phones[0]
                )
            )).build()
        } catch (UnableToExecuteStatementException e) {
            e.printStackTrace()
            internalServerError("Unable to execute SQL query.").build()
        } catch (Exception e) {
            e.printStackTrace()
            internalServerError(
                "Internal Server Error, please contact API support team for further assistance."
            ).build()
        }
    }

    private void updatePhoneAddrSeqno(String pidm,
                                      AddressRecordObject addressRecord,
                                      PhoneRecordObject phoneRecord) {
        if (phoneRecord?.id) {
            logger.info("""
                Phone record found with the given pidm and address type.
                Updating address seqno on phone record.
            """)
            // query address record to get updated seqno
            AddressRecordObject updatedAddressRecord = bannerPersonsReadDAO.hasSameAddressType(
                                                pidm, addressRecord.addressType
            )

            try {
                bannerPersonsWriteDAO.updatePhoneAddrSeqno(pidm,
                                                           updatedAddressRecord.seqno,
                                                           phoneRecord)
            } catch(Exception e) {
                logger.info("Unable to update phone record. Rolling back address changes.")
                bannerPersonsWriteDAO.deleteAddress(pidm, updatedAddressRecord)
                bannerPersonsWriteDAO.reactivateAddress(pidm, addressRecord)
                throw new Exception("Unable to update phone record.")
            }
        }
    }
}

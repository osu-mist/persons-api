package edu.oregonstate.mist.persons

import edu.oregonstate.mist.api.Error
import edu.oregonstate.mist.api.jsonapi.ResourceObject
import edu.oregonstate.mist.api.jsonapi.ResultObject
import edu.oregonstate.mist.personsapi.core.JobObject
import edu.oregonstate.mist.personsapi.core.LaborDistribution
import edu.oregonstate.mist.personsapi.core.Name
import edu.oregonstate.mist.personsapi.core.PersonObject
import edu.oregonstate.mist.personsapi.PersonsResource
import edu.oregonstate.mist.personsapi.db.PersonsDAO
import edu.oregonstate.mist.personsapi.db.PersonsStringTemplateDAO
import edu.oregonstate.mist.personsapi.db.PersonsWriteDAO
import groovy.mock.interceptor.StubFor
import org.junit.Before
import org.junit.Test
import org.skife.jdbi.v2.OutParameters

import javax.ws.rs.core.Response
import java.time.LocalDate

import static org.junit.Assert.assertEquals
import static org.junit.Assert.assertNotNull

class PersonsResourceTest {
    private final URI endpointUri = new URI('https://www.foo.com/')
    private final String graduateEmploymentType = "graduate"
    private final String studentEmploymentType = "student"

    PersonObject fakePerson
    JobObject fakeJob
    ResultObject fakeJobResultObject
    LocalDate sampleDate

    @Before
    void setup() {
        fakePerson = new PersonObject(
                osuID: '123456789',
                name: new Name(
                        lastName: 'Doe',
                        firstName: 'John'
                ),
                alternatePhone: null,
                osuUID: '987654321',
                birthDate: sampleDate,
                currentStudent: false,
                currentEmployee: false,
                mobilePhone: '+15411234567',
                primaryPhone: '+15411234567',
                homePhone: '15411234567',
                email: 'johndoe@oregonstate.edu',
                username: 'johndoe',
                confidential: false
        )

        //sampleDate = Date.parse('yyyy-MM-dd','2018-01-01')
        sampleDate = LocalDate.parse('2018-01-01')

        fakeJob = new JobObject(
                positionNumber: 'C50345',
                suffix: '00',
                effectiveDate: sampleDate,
                beginDate: sampleDate,
                endDate: null,
                accruesLeave: true,
                contractBeginDate: sampleDate,
                contractEndDate: null,
                contractType: "foo",
                locationID: '1A',
                status: 'Active',
                description: 'Fake Programmer',
                personnelChangeDate: sampleDate,
                changeReasonCode: null,
                fullTimeEquivalency: 1,
                appointmentPercent: 30.2,
                salaryStep: 2,
                salaryGroupCode: 'foo',
                strsAssignmentCode: 'bar',
                supervisorOsuID: '12345678',
                supervisorPositionNumber: 'C65432',
                supervisorSuffix: '01',
                timesheetOrganizationCode: '20394',
                hourlyRate: 12.5,
                hoursPerPay: 173.333,
                assignmentSalary: 2000,
                paysPerYear: 12,
                employeeClassificationCode: 'foobar',
                annualSalary: 24000,
                earnCodeEffectiveDate: sampleDate,
                earnCode: 'some-earn-code',
                earnCodeHours: 300,
                earnCodeShift: 'foo',
                laborDistribution: [new LaborDistribution(
                        effectiveDate: LocalDate.now(),
                        accountIndexCode: 'FFB333',
                        fundCode: null,
                        organizationCode: null,
                        accountCode: null,
                        programCode: null,
                        activityCode: null,
                        locationCode: null,
                        distributionPercent: 100
                )]
        )

        fakeJobResultObject = new ResultObject(
                data: new ResourceObject(attributes: fakeJob))
    }

    private static void checkErrorResponse(Response res, Integer errorCode) {
        assertNotNull(res)
        assertEquals(errorCode, res.status)

        def responseEntity = res.getEntity()

        if (responseEntity instanceof ArrayList) {
            responseEntity.each {
                assertEquals(Error.class, it.class)
            }
        } else {
            assertEquals(Error.class, responseEntity.class)
        }
    }

    private static void checkErrorResponse(Response res,
                                           Integer errorCode,
                                           String expectedMessage) {
        checkErrorResponse(res, errorCode)

        def responseEntity = res.getEntity()

        if (responseEntity.class == ArrayList.class) {
            List<String> errorMessages = responseEntity.collect { it['developerMessage'] }
            assertEquals(1, errorMessages.size())
            assertEquals(expectedMessage, errorMessages[0])
        } else {
            assertEquals(expectedMessage, responseEntity['developerMessage'])
        }
    }

    private static void checkValidResponse(Response res, Integer statusCode, def object) {
        assertNotNull(res)
        assertEquals(statusCode, res.status)
        assertEquals(ResultObject.class, res.getEntity().class)
        assertEquals(object, res.getEntity()['data']['attributes'])
    }

    private static StubFor getPersonsDAOStub() {
        new StubFor(PersonsDAO)
    }

    private static StubFor getPersonsStringTemplateDAOStub() {
        new StubFor(PersonsStringTemplateDAO)
    }

    @Test
    void shouldReturn400() {
        PersonsResource personsResource = new PersonsResource(null, null, null, endpointUri)

        // OSU UID can only contain numbers
        checkErrorResponse(personsResource.list(null, null, 'badOSUUID', null, null, null, null),
                400)

        // Can't search by names and IDs in the same request
        checkErrorResponse(personsResource.list(null, null, '123456789', 'Jane', 'Doe', null, null),
                400)
        checkErrorResponse(personsResource.list(null, '931234567', null, 'Jane', 'Doe', null, null),
                400)
        checkErrorResponse(personsResource.list('doej', null, null, 'Jane', 'Doe', null, null),
                400)

        // Only one ID parameter should be included in a request
        checkErrorResponse(personsResource.list('doej', '93123456', null, null, null, null, null),
                400)
        checkErrorResponse(personsResource.list('doej', null, '12345678', null, null, null, null),
                400)
        checkErrorResponse(personsResource.list(null, '931236', '1238', null, null, null, null),
                400)
        checkErrorResponse(personsResource.list('doej', '931236', '1238', null, null, null, null),
                400)

        // First and last name must be included together
        checkErrorResponse(personsResource.list(null, null, null, 'Jane', null, null, null),
                400)
        checkErrorResponse(personsResource.list(null, null, null, null, 'Doe', null, null),
                400)

        // Can't use searchOldNames and searchOldOsuIDs in one request
        checkErrorResponse(personsResource.list(null, '9322525', null, null, null, true, true), 400)

        // Searching old OSU IDs can only be done with the OSU ID
        checkErrorResponse(personsResource.list('doej', null, null, null, null, null, true), 400)
        checkErrorResponse(personsResource.list(null, null, '12345678', null, null, null, true),
                400)

        // Searching old names must include a valid name request
        checkErrorResponse(personsResource.list(null, null, null, null, null, true, null), 400)
        checkErrorResponse(personsResource.list(null, null, null, "jane", null, true, null), 400)
        checkErrorResponse(personsResource.list(null, null, null, null, "doe", true, null), 400)

        // The request must include an ID or names
        checkErrorResponse(personsResource.list(null, null, null, null, null, null, null), 400)
    }

    @Test
    void shouldReturn404IfBadOSUId() {
        def stubDAO = getPersonsDAOStub()
        stubDAO.demand.with {
            personExist(2..2) { null }
            getJobsById { null }
            getImageById { null }
        }

        def stubStringTemplateDAO = getPersonsStringTemplateDAOStub()
        stubStringTemplateDAO.demand.getPersons { String onid, List<String> osuIDs, String osuUID,
                                 String firstName, String lastName, searchOldVersions -> null }

        PersonsResource personsResource = new PersonsResource(
                stubDAO.proxyInstance(), stubStringTemplateDAO.proxyInstance(), null, endpointUri)
        checkErrorResponse(personsResource.getPersonById('123456789'), 404)
        checkErrorResponse(personsResource.getJobs('123456789', null, null), 404)
        checkErrorResponse(personsResource.getImageById('123456789', null), 404)
    }

    @Test
    void shouldReturnValidResponse() {
        def stubPersonsDAO = getPersonsDAOStub()
        stubPersonsDAO.demand.with {
            personExist { String osuID -> '123456789' }
            getJobsById { String osuID, String positionNumber, String suffix -> [fakeJob] }
            getJobLaborDistribution { String osuID, String positionNumber, String suffix -> null }
            getPreviousRecords(2..2) { String internalID -> null }
        }

        def stubPersonsStringTemplateDAO = getPersonsStringTemplateDAOStub()
        stubPersonsStringTemplateDAO.demand.getPersons(2..2) { String onid, List<String> osuIDs,
                                                               String osuUID, String firstName,
                                                               String lastName, searchOldVersions ->
            [fakePerson] }

        PersonsResource personsResource = new PersonsResource(stubPersonsDAO.proxyInstance(),
                stubPersonsStringTemplateDAO.proxyInstance(), null, endpointUri)
        checkValidResponse(personsResource.list('johndoe', null, null, null, null, null, null), 200,
                [fakePerson])
        checkValidResponse(personsResource.getPersonById('123456789'), 200, fakePerson)
        checkValidResponse(personsResource.getJobs('123456789', null, null), 200, [fakeJob])
    }

    private PersonsResource getPersonsResourceWithGoodMockDAOsForNewJob() {
        new PersonsResource(
                getGoodMockPersonsDAOForNewJob().proxyInstance(),
                null,
                getMockPersonsWriteDAO("").proxyInstance(),
                endpointUri)
    }

    private PersonsResource getPersonsResourceWithGoodMockDAOsForUpdateJob() {
        new PersonsResource(
                getGoodMockPersonsDAOForUpdateJob().proxyInstance(),
                null,
                getMockPersonsWriteDAO("").proxyInstance(),
                endpointUri
        )
    }

    private StubFor getGoodMockPersonsDAO() {
        def personsDAOStub = getPersonsDAOStub()
        personsDAOStub.demand.with {
            personExist(2..2) { String osuID -> '123456789' }
            isValidSupervisorPosition { LocalDate employeeBeginDate, String supervisorOsuID,
                String supervisorPositionNumber, String supervisorSuffix -> true
            }
            isValidPositionNumber { String positionNumber, LocalDate jobBeginDate -> true }
            isValidLocation { String locationID -> true }
            isValidOrganizationCode(2..2) { String organizationCode -> true }
            isValidAccountIndexCode { String accountIndexCode -> true }
            isValidAccountCode { String accountCode -> true }
            isValidActivityCode { String activityCode -> true }
            isValidProgramCode { String programCode -> true }
            isValidFundCode { String fundCode -> true }
        }

        personsDAOStub
    }

    private StubFor getGoodMockPersonsDAOForNewJob() {
        def personsDAOStub = getGoodMockPersonsDAO()
        personsDAOStub.demand.getJobsById { String osuID, String positionNumber, String suffix ->
            []
        }

        personsDAOStub
    }

    private StubFor getGoodMockPersonsDAOForUpdateJob() {
        def personsDAOStub = getGoodMockPersonsDAO()
        personsDAOStub.demand.getJobsById { String osuID, String positionNumber, String suffix ->
            [fakeJob]
        }

        personsDAOStub
    }

    private getMockPersonsWriteDAO(String returnMessage) {
        def outParametersStub = getOutParametersStub(returnMessage)

        def personsWriteDAOStub = new StubFor(PersonsWriteDAO)
        personsWriteDAOStub.demand.createGraduateJob { String osuID, JobObject job ->
            outParametersStub.proxyInstance()
        }
        personsWriteDAOStub
    }

    private getOutParametersStub(String returnMessage) {
        def outParametersStub = new StubFor(OutParameters)
        outParametersStub.demand.getString { String name -> returnMessage }

        outParametersStub
    }

    @Test
    void createJobSuccessful() {
        PersonsResource personsResource = getPersonsResourceWithGoodMockDAOsForNewJob()

        Response response = personsResource.createJob(
                "hello", fakeJobResultObject, graduateEmploymentType
        )
        checkValidResponse(response, 202, fakeJob)
    }

    @Test
    void createJobShouldReturnNotFound() {
        def personsDAOStub = getPersonsDAOStub()
        personsDAOStub.demand.personExist { String osuID -> null }
        PersonsResource personsResource = new PersonsResource(
                personsDAOStub.proxyInstance(), null, null, endpointUri)

        Response response = personsResource.createJob(
                "foo", new ResultObject(), graduateEmploymentType
        )
        checkErrorResponse(response, 404)
    }

    @Test
    void createJobShouldRejectMalformedJobObject() {
        testCreatingBadJobObject("badObject",
                "Could not parse job object. Make sure dates are in ISO8601 format: yyyy-MM-dd")
    }

    @Test
    void createJobShouldRejectBadDate() {
        testCreatingBadJobObject(["beginDate":"5/3/2018"],
                "Could not parse job object. Make sure dates are in ISO8601 format: yyyy-MM-dd")
    }

    @Test
    void createJobShouldRejectNullJob() {
        testCreatingBadJobObject(null, "No job object provided.")
    }

    void testCreatingBadJobObject(def resourceObjectAttributes, String expectedMessage) {
        def personsDAOStub = getPersonsDAOStub()
        personsDAOStub.demand.personExist { String osuID -> "123456789" }
        PersonsResource personsResource = new PersonsResource(
                personsDAOStub.proxyInstance(), null, null, endpointUri)

        ResultObject badJobResultObject = new ResultObject(data: new ResourceObject(
                attributes: resourceObjectAttributes))
        Response response = personsResource.createJob(
                "123456789", badJobResultObject,graduateEmploymentType
        )
        checkErrorResponse(response, 400, expectedMessage)
    }

    @Test
    void createJobRequiredFields() {
        JobObject job = new JobObject(
                positionNumber: null,
                beginDate: sampleDate,
                supervisorOsuID: '123',
                supervisorPositionNumber: fakeJob.supervisorPositionNumber,
                status: 'Active',
                effectiveDate: sampleDate
        )

        ResultObject jobResultObject = new ResultObject(data: new ResourceObject(attributes: job))

        job.with {
            checkCreateJobErrorMessageResponse(it, "Position number is required.")
            positionNumber = "123456"

            beginDate = null
            checkCreateJobErrorMessageResponse(it, "Begin date is required.")
            beginDate = sampleDate

            supervisorOsuID = null
            checkCreateJobErrorMessageResponse(it, "Supervisor OSU ID is required.")
            supervisorOsuID = "123"

            supervisorPositionNumber = null
            checkCreateJobErrorMessageResponse(it, "Supervisor position number is required.")
            supervisorPositionNumber = fakeJob.supervisorPositionNumber

            effectiveDate = null
            checkCreateJobErrorMessageResponse(it, "Effective date is required.")
            effectiveDate = sampleDate

            checkValidResponse(getPersonsResourceWithGoodMockDAOsForNewJob().createJob(
                    "123", jobResultObject, graduateEmploymentType), 202, it)
        }
    }

    @Test
    void createJobPositiveFields() {
        BigDecimal negativeNumber = -20
        BigDecimal positiveNumber = 20

        JobObject job = new JobObject(
                positionNumber: "C12345",
                beginDate: sampleDate,
                supervisorOsuID: '123',
                supervisorPositionNumber: fakeJob.supervisorPositionNumber,
                status: 'Active',
                effectiveDate: sampleDate,
                hourlyRate: negativeNumber
        )

        ResultObject jobResultObject = new ResultObject(data: new ResourceObject(attributes: job))

        job.with {
            checkCreateJobErrorMessageResponse(it, "Hourly rate cannot be a negative number.")
            hourlyRate = positiveNumber

            hoursPerPay = negativeNumber
            checkCreateJobErrorMessageResponse(it, "Hours per pay cannot be a negative number.")
            hoursPerPay = positiveNumber

            assignmentSalary = negativeNumber
            checkCreateJobErrorMessageResponse(it, "Assignment salary cannot be a negative number.")
            assignmentSalary = positiveNumber

            annualSalary = negativeNumber
            checkCreateJobErrorMessageResponse(it, "Annual salary cannot be a negative number.")
            annualSalary = positiveNumber

            paysPerYear = negativeNumber
            checkCreateJobErrorMessageResponse(it, "Pays per year cannot be a negative number.")
            paysPerYear = positiveNumber

            checkValidResponse(getPersonsResourceWithGoodMockDAOsForNewJob().createJob(
                    "123", jobResultObject, graduateEmploymentType), 202, it)
        }
    }

    @Test
    void suffixIsRequiredForUpdate() {
        JobObject job = fakeJob
        job.suffix = null

        PersonsResource personsResource = new PersonsResource(
                getGoodMockPersonsDAOForUpdateJob().proxyInstance(), null, null, endpointUri)

        checkErrorResponse(
                personsResource.updateJob(
                        "123",
                        "foo-bar",
                        new ResultObject(data: new ResourceObject(attributes: job)),
                        graduateEmploymentType),
                400,
                "Suffix is required when updating an existing job."
        )
    }

    @Test
    void jobMustBeActive() {
        JobObject terminatedJob = fakeJob
        terminatedJob.status = 'Terminated'
        checkCreateJobErrorMessageResponse(terminatedJob, "'Active' is the only valid job status.")
    }

    @Test
    void jobEndDateIsAfterBeginDate() {
        JobObject badDateJob = fakeJob
        fakeJob.beginDate = LocalDate.parse('2018-01-01')
        fakeJob.endDate = LocalDate.parse('2017-01-01')
        checkCreateJobErrorMessageResponse(badDateJob, "End date must be after begin date.")
    }

    @Test
    void jobContractEndDateIsAfterBeginDate() {
        JobObject badContractDateJob = fakeJob
        fakeJob.contractBeginDate = LocalDate.parse('2018-01-01')
        fakeJob.contractEndDate = LocalDate.parse('2017-01-01')
        checkCreateJobErrorMessageResponse(badContractDateJob,
                "Contract end date must be after begin date.")
    }

    @Test
    void jobBeginDateAndEffectiveDateMustMatch() {
        JobObject badDateJob = fakeJob
        fakeJob.beginDate = LocalDate.parse('2018-01-01')
        fakeJob.effectiveDate = LocalDate.parse('2017-01-01')
        checkCreateJobErrorMessageResponse(badDateJob, "Begin date and effective date must " +
                "match for new jobs.")
    }

    @Test
    void fteMustBeWithinRange() {
        JobObject badFteJob = fakeJob
        [-0.5, 1.5].each {
            badFteJob.fullTimeEquivalency = it
            checkCreateJobErrorMessageResponse(badFteJob,
                    "Full time equivalency must range from 0 to 1.")
        }
    }

    @Test
    void appointmentPercentMustBeWithinRange() {
        JobObject badAppointmentPercentJob = fakeJob
        [-50.5, 150.5].each {
            badAppointmentPercentJob.appointmentPercent = it
            checkCreateJobErrorMessageResponse(badAppointmentPercentJob,
                    "Appointment percent must range from 0 to 100.")
        }
    }

    void checkCreateJobErrorMessageResponse(JobObject job, String expectedMessage) {
        checkErrorResponse(
                getPersonsResourceWithGoodMockDAOsForNewJob().createJob(
                        "123",
                        new ResultObject(data: new ResourceObject(attributes: job)),
                        graduateEmploymentType),
                400,
                expectedMessage
        )
    }

    @Test
    void supervisorIdDoesNotExist() {
        def personsDAOStub = getPersonsDAOStub()
        personsDAOStub.demand.with {
            personExist(2..2) { String osuID ->
                if (osuID == fakeJob.supervisorOsuID) {
                    null
                } else {
                    '123456789'
                }
            }
            isValidPositionNumber { String positionNumber, LocalDate jobBeginDate -> true }
            getJobsById { String osuID, String positionNumber, String suffix -> [] }
            isValidLocation { String locationID -> true }
            isValidOrganizationCode { String organizationCode -> true }
            isValidAccountIndexCode { String accountIndexCode -> true }
            isValidAccountCode { String accountCode -> true }
            isValidActivityCode { String activityCode -> true }
        }

        PersonsResource personsResource = new PersonsResource(
                personsDAOStub.proxyInstance(), null, null, endpointUri)

        checkErrorResponse(
                personsResource.createJob(
                        "123",
                        fakeJobResultObject,
                        graduateEmploymentType),
                400,
                "Supervisor OSU ID does not exist."
        )
    }

    @Test
    void supervisorPositionIsNotActive() {
        def personsDAOStub = getPersonsDAOStub()
        personsDAOStub.demand.with {
            personExist(2..2) { String osuID -> '123456789' }
            isValidSupervisorPosition { LocalDate employeeBeginDate, String supervisorOsuID,
                                        String supervisorPositionNumber, String supervisorSuffix ->
                false
            }
            isValidPositionNumber { String positionNumber, LocalDate jobBeginDate -> true }
            getJobsById { String osuID, String positionNumber, String suffix -> [] }
            isValidLocation { String locationID -> true }
            isValidOrganizationCode { String organizationCode -> true }
            isValidAccountIndexCode { String accountIndexCode -> true }
            isValidAccountCode { String accountCode -> true }
            isValidActivityCode { String activityCode -> true }
            isValidFundCode { String fundCode -> true }
            isValidProgramCode { String programCode -> true }
        }

        PersonsResource personsResource = new PersonsResource(
                personsDAOStub.proxyInstance(), null, null, endpointUri)

        checkErrorResponse(
                personsResource.createJob(
                        "123",
                        fakeJobResultObject,
                        graduateEmploymentType),
                400,
                "Supervisor does not have an active position with position number " +
                        "${fakeJob.supervisorPositionNumber} for the given begin date."
        )
    }

    @Test
    void invalidPositionNumber() {
        def personsDAOStub = getPersonsDAOStub()
        personsDAOStub.demand.with {
            personExist(2..2) { String osuID -> '123456789' }
            isValidSupervisorPosition { LocalDate employeeBeginDate, String supervisorOsuID,
                                        String supervisorPositionNumber, String supervisorSuffix ->
                true
            }
            isValidPositionNumber { String positionNumber, LocalDate jobBeginDate -> false }
            getJobsById { String osuID, String positionNumber, String suffix -> [] }
            isValidLocation { String locationID -> true }
            isValidOrganizationCode { String organizationCode -> true }
            isValidAccountIndexCode { String accountIndexCode -> true }
            isValidAccountCode { String accountCode -> true }
            isValidActivityCode { String activityCode -> true }
        }

        PersonsResource personsResource = new PersonsResource(
                personsDAOStub.proxyInstance(), null, null, endpointUri)

        checkErrorResponse(
                personsResource.createJob(
                        "123",
                        fakeJobResultObject,
                        graduateEmploymentType),
                400,
                "${fakeJob.positionNumber} is not a valid position number for the given begin date."
        )
    }

    @Test
    void jobAlreadyExists() {
        def personsDAOStub = getPersonsDAOStub()
        personsDAOStub.demand.with {
            personExist(2..2) { String osuID -> '123456789' }
            isValidSupervisorPosition { LocalDate employeeBeginDate, String supervisorOsuID,
                                        String supervisorPositionNumber, String supervisorSuffix ->
                true
            }
            isValidPositionNumber { String positionNumber, LocalDate jobBeginDate -> true }
            getJobsById { String osuID, String positionNumber, String suffix -> [fakeJob] }
            isValidLocation { String locationID -> true }
            isValidOrganizationCode { String organizationCode -> true }
            isValidAccountIndexCode { String accountIndexCode -> true }
            isValidAccountCode { String accountCode -> true }
            isValidActivityCode { String activityCode -> true }
        }

        PersonsResource personsResource = new PersonsResource(
                personsDAOStub.proxyInstance(), null, null, endpointUri)

        checkErrorResponse(
                personsResource.createJob(
                        "123",
                        fakeJobResultObject,
                        graduateEmploymentType),
                400,
                "Person already has a job with the given position number and suffix."
        )
    }

    @Test
    void invalidLocationID() {
        def personsDAOStub = getPersonsDAOStub()
        personsDAOStub.demand.with {
            personExist(2..2) { String osuID -> '123456789' }
            isValidSupervisorPosition { LocalDate employeeBeginDate, String supervisorOsuID,
                                        String supervisorPositionNumber, String supervisorSuffix ->
                true
            }
            isValidPositionNumber { String positionNumber, LocalDate jobBeginDate -> true }
            getJobsById { String osuID, String positionNumber, String suffix -> [] }
            isValidLocation { String locationID -> false }
            isValidOrganizationCode { String organizationCode -> true }
            isValidAccountIndexCode { String accountIndexCode -> true }
            isValidAccountCode { String accountCode -> true }
            isValidActivityCode { String activityCode -> true }
        }

        PersonsResource personsResource = new PersonsResource(
                personsDAOStub.proxyInstance(), null, null, endpointUri)

        checkErrorResponse(
                personsResource.createJob(
                        "123",
                        fakeJobResultObject,
                        graduateEmploymentType),
                400,
                "${fakeJob.locationID} is not a valid location ID."
        )
    }

    @Test
    void invalidOrgCode() {
        def personsDAOStub = getPersonsDAOStub()
        personsDAOStub.demand.with {
            personExist(2..2) { String osuID -> '123456789' }
            isValidSupervisorPosition { LocalDate employeeBeginDate, String supervisorOsuID,
                                        String supervisorPositionNumber, String supervisorSuffix ->
                true
            }
            isValidPositionNumber { String positionNumber, LocalDate jobBeginDate -> true }
            getJobsById { String osuID, String positionNumber, String suffix -> [] }
            isValidLocation { String locationID -> true }
            isValidOrganizationCode { String organizationCode -> false }
            isValidAccountIndexCode { String accountIndexCode -> true }
            isValidAccountCode { String accountCode -> true }
            isValidActivityCode { String activityCode -> true }
        }

        PersonsResource personsResource = new PersonsResource(
                personsDAOStub.proxyInstance(), null, null, endpointUri)

        checkErrorResponse(
                personsResource.createJob(
                        "123",
                        fakeJobResultObject,
                        graduateEmploymentType),
                400,
                "${fakeJob.timesheetOrganizationCode} is not a valid organization code."
        )
    }

    @Test
    void invalidLaborDistributionPercentage() {
        def personsDAOStub = getPersonsDAOStub()
        personsDAOStub.demand.with {
            personExist(2..2) { String osuID -> '123456789' }
            isValidSupervisorPosition { LocalDate employeeBeginDate, String supervisorOsuID,
                                        String supervisorPositionNumber, String supervisorSuffix ->
                true
            }
            isValidPositionNumber { String positionNumber, LocalDate jobBeginDate -> true }
            getJobsById { String osuID, String positionNumber, String suffix -> [] }
            isValidLocation { String locationID -> true }
            isValidOrganizationCode { String organizationCode -> true }
            isValidAccountIndexCode(2..2) { String accountIndexCode -> true }
        }

        PersonsResource personsResource = new PersonsResource(
                personsDAOStub.proxyInstance(), null, null, endpointUri)

        JobObject job = fakeJob
        LaborDistribution laborDistribution = new LaborDistribution(
                effectiveDate: LocalDate.now(), accountIndexCode: "foo", distributionPercent: 55)

        job.laborDistribution.with {
            clear()
            2.times { add(laborDistribution) }
        }

        checkErrorResponse(
                personsResource.createJob(
                        "123",
                        new ResultObject(data: new ResourceObject(attributes: job)),
                        graduateEmploymentType),
                400,
                "Total sum of labor distribution percentages must equal 100."
        )
    }

    @Test
    void accountIndexCodeCantBeNull() {
        JobObject job = fakeJob
        job.laborDistribution[0].accountIndexCode = null
        checkCreateJobErrorMessageResponse(job, "null is not a valid accountIndexCode.")
    }

    @Test
    void allLaborFieldsCanBeUsed() {
        JobObject job = fakeJob
        job.laborDistribution[0].with {
            fundCode = 'example'
            organizationCode = 'example'
            accountCode = 'example'
            programCode = 'example'
            activityCode = 'example'
        }

        ResultObject newJobResultObject = new ResultObject(
                data: new ResourceObject(
                        attributes: job
                )
        )

        PersonsResource personsResource = getPersonsResourceWithGoodMockDAOsForNewJob()
        Response response = personsResource.createJob(
                "hello", newJobResultObject, graduateEmploymentType
        )

        checkValidResponse(response, 202, job)
    }

    @Test
    void invalidAccountIndexCode() {
        def personsDAOStub = getPersonsDAOStub()
        personsDAOStub.demand.with {
            personExist(2..2) { String osuID -> '123456789' }
            isValidSupervisorPosition { LocalDate employeeBeginDate, String supervisorOsuID,
                                        String supervisorPositionNumber, String supervisorSuffix ->
                true
            }
            getJobsById { String osuID, String positionNumber, String suffix -> [] }
            isValidPositionNumber { String positionNumber, LocalDate jobBeginDate -> true }
            isValidLocation { String locationID -> true }
            isValidOrganizationCode { String organizationCode -> true }
            isValidAccountIndexCode { String accountIndexCode -> false }
        }

        PersonsResource personsResource = new PersonsResource(
                personsDAOStub.proxyInstance(), null, null, endpointUri)

        checkErrorResponse(
                personsResource.createJob(
                        "123",
                        fakeJobResultObject,
                        graduateEmploymentType),
                400,
                "${fakeJob.laborDistribution[0].accountIndexCode} is not a valid accountIndexCode."
        )
    }

    @Test
    void invalidAccountCode() {
        def personsDAOStub = getPersonsDAOStub()
        personsDAOStub.demand.with {
            personExist(2..2) { String osuID -> '123456789' }
            isValidSupervisorPosition { LocalDate employeeBeginDate, String supervisorOsuID,
                                        String supervisorPositionNumber, String supervisorSuffix ->
                true
            }
            isValidPositionNumber { String positionNumber, LocalDate jobBeginDate -> true }
            getJobsById { String osuID, String positionNumber, String suffix -> [] }
            isValidLocation { String locationID -> true }
            isValidOrganizationCode(2..2) { String organizationCode -> true }
            isValidAccountIndexCode { String accountIndexCode -> true }
            isValidAccountCode { String accountCode -> false }
            isValidActivityCode { String activityCode -> true }
            isValidProgramCode { String programCode -> true }
            isValidFundCode { String fundCode -> true }
            isValidLocationCode { String locationCode -> true }
        }

        PersonsResource personsResource = new PersonsResource(
                personsDAOStub.proxyInstance(), null, null, endpointUri)

        JobObject job = fakeJob
        job.laborDistribution[0].with {
            accountIndexCode = 'example'
            fundCode = 'example'
            organizationCode = 'example'
            accountCode = 'example'
            programCode = 'example'
            activityCode = 'example'
            locationCode = 'example'
        }

        checkErrorResponse(
                personsResource.createJob(
                        "123",
                        new ResultObject(data: new ResourceObject(attributes: job)),
                        graduateEmploymentType),
                400,
                "${fakeJob.laborDistribution[0].accountCode} is not a valid accountCode."
        )
    }

    @Test
    void invalidActivityCode() {
        def personsDAOStub = getPersonsDAOStub()
        personsDAOStub.demand.with {
            personExist(2..2) { String osuID -> '123456789' }
            isValidSupervisorPosition { LocalDate employeeBeginDate, String supervisorOsuID,
                                        String supervisorPositionNumber, String supervisorSuffix ->
                true
            }
            isValidPositionNumber { String positionNumber, LocalDate jobBeginDate -> true }
            getJobsById { String osuID, String positionNumber, String suffix -> [] }
            isValidLocation { String locationID -> true }
            isValidOrganizationCode(2..2) { String organizationCode -> true }
            isValidAccountIndexCode { String accountIndexCode -> true }
            isValidAccountCode { String accountCode -> true }
            isValidActivityCode { String activityCode -> false }
            isValidProgramCode { String programCode -> true }
            isValidFundCode { String fundCode -> true }
            isValidLocationCode { String locationCode -> true }
        }

        JobObject job = fakeJob
        job.laborDistribution[0].with {
            accountIndexCode = 'example'
            fundCode = 'example'
            organizationCode = 'example'
            accountCode = 'example'
            programCode = 'example'
            activityCode = 'example'
            locationCode = 'example'
        }

        PersonsResource personsResource = new PersonsResource(
                personsDAOStub.proxyInstance(), null, null, endpointUri)

        checkErrorResponse(
                personsResource.createJob(
                        "123",
                        new ResultObject(data: new ResourceObject(attributes: job)),
                        graduateEmploymentType),
                400,
                "${fakeJob.laborDistribution[0].activityCode} is not a valid activityCode."
        )
    }

    @Test
    void invalidLaborOrganizationCode() {
        String badOrganizationCode = "bad"
        def personsDAOStub = getPersonsDAOStub()
        personsDAOStub.demand.with {
            personExist(2..2) { String osuID -> '123456789' }
            isValidSupervisorPosition { LocalDate employeeBeginDate, String supervisorOsuID,
                                        String supervisorPositionNumber, String supervisorSuffix ->
                true
            }
            isValidPositionNumber { String positionNumber, LocalDate jobBeginDate -> true }
            getJobsById { String osuID, String positionNumber, String suffix -> [] }
            isValidLocation { String locationID -> true }
            isValidOrganizationCode(2..2) { String organizationCode ->
                if (organizationCode == badOrganizationCode) {
                    false
                } else {
                    true
                }
            }
            isValidAccountIndexCode { String accountIndexCode -> true }
            isValidAccountCode { String accountCode -> true }
            isValidActivityCode { String activityCode -> true }
            isValidProgramCode { String programCode -> true }
            isValidFundCode { String fundCode -> true }
            isValidLocationCode { String locationCode -> true }
        }

        JobObject job = fakeJob
        job.laborDistribution[0].with {
            accountIndexCode = 'example'
            fundCode = 'example'
            organizationCode = badOrganizationCode
            accountCode = 'example'
            programCode = 'example'
            activityCode = 'example'
            locationCode = 'example'
        }

        PersonsResource personsResource = new PersonsResource(
                personsDAOStub.proxyInstance(), null, null, endpointUri)

        checkErrorResponse(
                personsResource.createJob(
                        "123",
                        new ResultObject(data: new ResourceObject(attributes: job)),
                        graduateEmploymentType),
                400,
                "${fakeJob.laborDistribution[0].organizationCode} is not a valid organizationCode."
        )
    }

    @Test
    void invalidProgramCode() {
        def personsDAOStub = getPersonsDAOStub()
        personsDAOStub.demand.with {
            personExist(2..2) { String osuID -> '123456789' }
            isValidSupervisorPosition { LocalDate employeeBeginDate, String supervisorOsuID,
                                        String supervisorPositionNumber, String supervisorSuffix ->
                true
            }
            isValidPositionNumber { String positionNumber, LocalDate jobBeginDate -> true }
            getJobsById { String osuID, String positionNumber, String suffix -> [] }
            isValidLocation { String locationID -> true }
            isValidOrganizationCode(2..2) { String organizationCode -> true }
            isValidAccountIndexCode { String accountIndexCode -> true }
            isValidAccountCode { String accountCode -> true }
            isValidActivityCode { String activityCode -> true }
            isValidProgramCode { String programCode -> false }
            isValidFundCode { String fundCode -> true }
            isValidLocationCode { String locationCode -> true }
        }

        JobObject job = fakeJob
        job.laborDistribution[0].with {
            accountIndexCode = 'example'
            fundCode = 'example'
            organizationCode = 'example'
            accountCode = 'example'
            programCode = 'example'
            activityCode = 'example'
            locationCode = 'example'
        }

        PersonsResource personsResource = new PersonsResource(
                personsDAOStub.proxyInstance(), null, null, endpointUri)

        checkErrorResponse(
                personsResource.createJob(
                        "123",
                        new ResultObject(data: new ResourceObject(attributes: job)),
                        graduateEmploymentType),
                400,
                "${fakeJob.laborDistribution[0].programCode} is not a valid programCode."
        )
    }

    @Test
    void invalidFundCode() {
        def personsDAOStub = getPersonsDAOStub()
        personsDAOStub.demand.with {
            personExist(2..2) { String osuID -> '123456789' }
            isValidSupervisorPosition { LocalDate employeeBeginDate, String supervisorOsuID,
                                        String supervisorPositionNumber, String supervisorSuffix ->
                true
            }
            isValidPositionNumber { String positionNumber, LocalDate jobBeginDate -> true }
            getJobsById { String osuID, String positionNumber, String suffix -> [] }
            isValidLocation { String locationID -> true }
            isValidOrganizationCode(2..2) { String organizationCode -> true }
            isValidAccountIndexCode { String accountIndexCode -> true }
            isValidAccountCode { String accountCode -> true }
            isValidActivityCode { String activityCode -> true }
            isValidProgramCode { String programCode -> true }
            isValidFundCode { String fundCode -> false }
            isValidLocationCode { String locationCode -> true }
        }

        JobObject job = fakeJob
        job.laborDistribution[0].with {
            accountIndexCode = 'example'
            fundCode = 'example'
            organizationCode = 'example'
            accountCode = 'example'
            programCode = 'example'
            activityCode = 'example'
            locationCode = 'example'
        }

        PersonsResource personsResource = new PersonsResource(
                personsDAOStub.proxyInstance(), null, null, endpointUri)

        checkErrorResponse(
                personsResource.createJob(
                        "123",
                        new ResultObject(data: new ResourceObject(attributes: job)),
                        graduateEmploymentType),
                400,
                "${fakeJob.laborDistribution[0].fundCode} is not a valid fundCode."
        )
    }

    @Test
    void invalidLocationCode() {
        def personsDAOStub = getPersonsDAOStub()
        personsDAOStub.demand.with {
            personExist(2..2) { String osuID -> '123456789' }
            isValidSupervisorPosition { LocalDate employeeBeginDate, String supervisorOsuID,
                                        String supervisorPositionNumber, String supervisorSuffix ->
                true
            }
            isValidPositionNumber { String positionNumber, LocalDate jobBeginDate -> true }
            getJobsById { String osuID, String positionNumber, String suffix -> [] }
            isValidLocation { String locationID -> true }
            isValidOrganizationCode(2..2) { String organizationCode -> true }
            isValidAccountIndexCode { String accountIndexCode -> true }
            isValidAccountCode { String accountCode -> true }
            isValidActivityCode { String activityCode -> true }
            isValidProgramCode { String programCode -> true }
            isValidFundCode { String fundCode -> true }
            isValidLocationCode { String locationCode -> false }
        }

        JobObject job = fakeJob
        job.laborDistribution[0].with {
            accountIndexCode = 'example'
            fundCode = 'example'
            organizationCode = 'example'
            accountCode = 'example'
            programCode = 'example'
            activityCode = 'example'
            locationCode = 'example'
        }

        PersonsResource personsResource = new PersonsResource(
                personsDAOStub.proxyInstance(), null, null, endpointUri)

        checkErrorResponse(
                personsResource.createJob(
                        "123",
                        new ResultObject(data: new ResourceObject(attributes: job)),
                        graduateEmploymentType),
                400,
                "${fakeJob.laborDistribution[0].locationCode} is not a valid locationCode."
        )
    }

    @Test
    void laborEffectiveDateIsRequired() {
        JobObject job = fakeJob
        fakeJob.laborDistribution[0].effectiveDate = null
        checkCreateJobErrorMessageResponse(job,
                "effectiveDate is required for each labor distribution.")
    }

    @Test
    void laborEffectiveDatesMustMatch() {
        List<LaborDistribution> laborDistributions = [
                new LaborDistribution(
                        effectiveDate: LocalDate.now(), accountIndexCode: "foo",
                        distributionPercent: 50),
                new LaborDistribution(
                        effectiveDate: LocalDate.now().plusDays(1), accountIndexCode: "foo",
                        distributionPercent: 50)
        ]

        JobObject job = fakeJob
        job.laborDistribution = laborDistributions

        def personsDAOStub = getPersonsDAOStub()
        personsDAOStub.demand.with {
            personExist(2..2) { String osuID -> '123456789' }
            isValidSupervisorPosition { LocalDate employeeBeginDate, String supervisorOsuID,
                                        String supervisorPositionNumber, String supervisorSuffix ->
                true
            }
            isValidPositionNumber { String positionNumber, LocalDate jobBeginDate -> true }
            getJobsById { String osuID, String positionNumber, String suffix -> [] }
            isValidLocation { String locationID -> true }
            isValidOrganizationCode(2..2) { String organizationCode -> true }
            isValidAccountIndexCode(2..2) { String accountIndexCode -> true }
            isValidAccountCode { String accountCode -> true }
            isValidActivityCode { String activityCode -> true }
            isValidProgramCode { String programCode -> true }
            isValidFundCode { String fundCode -> true }
        }

        PersonsResource personsResource = new PersonsResource(
                personsDAOStub.proxyInstance(), null, null, endpointUri)

        checkErrorResponse(
                personsResource.createJob(
                        "123",
                        new ResultObject(data: new ResourceObject(attributes: job)),
                        graduateEmploymentType),
                400,
                "effectiveDate must be the same for each labor distribution."
        )
    }

    @Test
    void laborDistributionPercentIsRequired() {
        JobObject job = fakeJob
        fakeJob.laborDistribution[0].distributionPercent = null
        checkCreateJobErrorMessageResponse(job,
                "distributionPercent is required for each labor distribution.")
    }

    @Test
    void notNullDAOResponseShouldThrowError() {
        String personsWriteDAOResponse = "Something broke!"

        PersonsResource personsResource = new PersonsResource(
                getGoodMockPersonsDAOForNewJob().proxyInstance(),
                null,
                getMockPersonsWriteDAO(personsWriteDAOResponse).proxyInstance(),
                endpointUri)

        checkErrorResponse(
                personsResource.createJob(
                        "123",
                        fakeJobResultObject,
                        graduateEmploymentType),
                500,
                "Error creating new job: $personsWriteDAOResponse"
        )
    }

    @Test
    void getJobByIdReturnsNotFoundIfPersonDoesNotExist() {
        def personsDAOStub = getPersonsDAOStub()
        personsDAOStub.demand.personExist { String osuID -> null }
        PersonsResource personsResource = new PersonsResource(
                personsDAOStub.proxyInstance(), null, null, endpointUri)

        Response response = personsResource.getJobById("foo", "bar")
        checkErrorResponse(response, 404)
    }

    @Test
    void getJobByIdReturnsNotFoundIfJobDoesNotExist() {
        def personsDAOStub = getPersonsDAOStub()
        personsDAOStub.demand.with {
            personExist { String osuID -> "foo" }
            getJobsById { String osuID, String positionNumber, String suffix -> [] }
        }
        PersonsResource personsResource = new PersonsResource(
                personsDAOStub.proxyInstance(), null, null, endpointUri)

        Response response = personsResource.getJobById("foo", "foo-bar")
        checkErrorResponse(response, 404)
    }

    @Test
    void updateJobReturnsNotFoundIfJobDoesNotExist() {
        def personsDAOStub = getPersonsDAOStub()
        personsDAOStub.demand.with {
            personExist(2..2) { String osuID -> '123456789' }
            getJobsById { String osuID, String positionNumber, String suffix -> [] }
        }

        PersonsResource personsResource = new PersonsResource(
                personsDAOStub.proxyInstance(), null, null, endpointUri)

        checkErrorResponse(
                personsResource.updateJob(
                        "123",
                        "foo-bar",
                        fakeJobResultObject,
                        graduateEmploymentType),
                404
        )
    }

    @Test
    void invalidEmploymentTypeReturnsBadRequest() {
        String expectedMessage = "Invalid employmentType (query parameter). " +
                "Valid types are: student, graduate"

        List<String> invalidEmploymentTypes = ["Classified", "", null]

        invalidEmploymentTypes.each {
            checkErrorResponse(
                    getPersonsResourceWithGoodMockDAOsForNewJob().createJob(
                            "123456789",
                            fakeJobResultObject,
                            it),
                    400,
                    expectedMessage
            )

            checkErrorResponse(
                    getPersonsResourceWithGoodMockDAOsForUpdateJob().updateJob(
                            "123",
                            "foo-bar",
                            fakeJobResultObject,
                            it),
                    400,
                    expectedMessage
            )
        }
    }

    @Test
    void studentPositionsMustUseValidPrefix() {
        String expectedMessage = "Student position numbers must begin with one " +
                "of these prefixes: C50, C51, C52"

        checkErrorResponse(
                getPersonsResourceWithGoodMockDAOsForNewJob().createJob(
                        "123456789",
                        fakeJobResultObject,
                        studentEmploymentType),
                400,
                expectedMessage
        )

        checkErrorResponse(
                getPersonsResourceWithGoodMockDAOsForUpdateJob().updateJob(
                        "123",
                        "foo-bar",
                        fakeJobResultObject,
                        studentEmploymentType),
                400,
                expectedMessage
        )
    }

    @Test
    void correctDAOMethodsShouldBeCalledForCreateJob() {
        def outParametersStub = getOutParametersStub("")
        def personsWriteDAOStub = new StubFor(PersonsWriteDAO)

        personsWriteDAOStub.demand.with {
            createStudentJob { String osuID, JobObject job ->
                outParametersStub.proxyInstance()
            }
            createGraduateJob { String osuID, JobObject job ->
                outParametersStub.proxyInstance()
            }
        }

        [studentEmploymentType, graduateEmploymentType].each {
            PersonsResource personsResource = new PersonsResource(
                    getGoodMockPersonsDAOForNewJob().proxyInstance(),
                    null,
                    personsWriteDAOStub.proxyInstance(),
                    endpointUri
            )

            checkValidResponse(
                    personsResource.createJob("foo", fakeJobResultObject, it),
                    202,
                    fakeJob
            )
        }
    }

    @Test
    void correctDAOMethodsShouldBeCalledForUpdateJob() {
        def outParametersStub = getOutParametersStub("")
        def personsWriteDAOStub = new StubFor(PersonsWriteDAO)

        personsWriteDAOStub.demand.with {
            updateStudentJob { String osuID, JobObject job ->
                outParametersStub.proxyInstance()
            }
            updateGraduateJob { String osuID, JobObject job ->
                outParametersStub.proxyInstance()
            }
        }

        [studentEmploymentType, graduateEmploymentType].each {
            PersonsResource personsResource = new PersonsResource(
                    getGoodMockPersonsDAOForUpdateJob().proxyInstance(),
                    null,
                    personsWriteDAOStub.proxyInstance(),
                    endpointUri
            )

            checkValidResponse(
                    personsResource.updateJob("foo", "foo-bar", fakeJobResultObject, it),
                    202,
                    fakeJob
            )
        }
    }
}

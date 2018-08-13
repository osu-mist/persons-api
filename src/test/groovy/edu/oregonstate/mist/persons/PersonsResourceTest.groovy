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
import edu.oregonstate.mist.personsapi.db.PersonsWriteDAO
import groovy.mock.interceptor.StubFor
import org.junit.Test
import org.skife.jdbi.v2.OutParameters

import javax.ws.rs.core.Response
import java.time.LocalDate

import static org.junit.Assert.assertEquals
import static org.junit.Assert.assertNotNull

class PersonsResourceTest {
    private final URI endpointUri = new URI('https://www.foo.com/')

    PersonObject fakePerson = new PersonObject(
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

    JobObject fakeJob = new JobObject(
        positionNumber: 'C12345',
        suffix: '00',
        effectiveDate: sampleDate,
        beginDate: sampleDate,
        endDate: null,
        contractType: 'A good contract!',
        accruesLeave: true,
        contractBeginDate: sampleDate,
        contractEndDate: sampleDate,
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
                fundCode: 'fake-fund-code',
                organizationCode: 'fake-org-code',
                accountCode: '23',
                programCode: 'fake-program-code',
                activityCode: '343A',
                distributionPercent: 100
        )]
    )

    ResultObject fakeJobResultObject = new ResultObject(
            data: new ResourceObject(attributes: fakeJob))

    Date sampleDate = Date.parse('yyyy-MM-dd','2018-01-01')

    private static void checkErrorResponse(Response res, Integer errorCode) {
        assertNotNull(res)
        assertEquals(errorCode, res.status)

        def responseEntity = res.getEntity()

        if (responseEntity.class == ArrayList.class) {
            responseEntity.each {
                assertEquals(Error.class, it.class)
            }
        } else {
            assertEquals(Error.class, responseEntity.class)
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

    @Test
    void shouldReturn400() {
        PersonsResource personsResource = new PersonsResource(null, null, endpointUri)

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
        def stub = getPersonsDAOStub()
        stub.demand.with {
            getPersons { String onid, String osuID, String osuUID,
                         String firstName, String lastName, searchOldVersions -> null }
            personExist(2..2) { null }
            getJobsById { null }
            getImageById { null }
        }

        PersonsResource personsResource = new PersonsResource(
                stub.proxyInstance(), null, endpointUri)
        checkErrorResponse(personsResource.getPersonById('123456789'), 404)
        checkErrorResponse(personsResource.getJobs('123456789', null, null), 404)
        checkErrorResponse(personsResource.getImageById('123456789', null), 404)
    }

    @Test
    void shouldReturnValidResponse() {
        def stub = getPersonsDAOStub()
        stub.demand.with {
            getPersons(2..2) { String onid, String osuID, String osuUID,
                         String firstName, String lastName, searchOldVersions -> [fakePerson] }
            personExist { String osuID -> '123456789' }
            getJobsById { String osuID, String positionNumber, String suffix -> [fakeJob] }
            getJobLaborDistribution { String osuID, String positionNumber, String suffix -> null }
            getPreviousRecords(2..2) { String internalID -> null }
        }
        PersonsResource personsResource = new PersonsResource(
                stub.proxyInstance(), null, endpointUri)
        checkValidResponse(personsResource.list('johndoe', null, null, null, null, null, null), 200,
                [fakePerson])
        checkValidResponse(personsResource.getPersonById('123456789'), 200, fakePerson)
        checkValidResponse(personsResource.getJobs('123456789', null, null), 200, [fakeJob])
    }

    private PersonsResource getPersonsResourceWithGoodMockDAOs() {
        List<JobObject> supervisorJobs = [new JobObject(
                positionNumber: fakeJob.supervisorPositionNumber, status: 'Active')]

        def personsDAOStub = getPersonsDAOStub()
        personsDAOStub.demand.with {
            personExist(2..2) { String osuID -> '123456789' }
            getJobsById { String osuID, String positionNumber, String suffix -> supervisorJobs }
            isValidPositionNumber { String positionNumber -> true }
            isValidLocation { String locationID -> true }
            isValidOrganizationCode { String organizationCode -> true }
            isValidAccountIndexCode { String accountIndexCode -> true }
            isValidAccountCode { String accountCode -> true }
            isValidActivityCode { String activityCode -> true }
        }

        def outParametersStub = new StubFor(OutParameters)
        outParametersStub.demand.getString { String name -> "" }

        def personsWriteDAOStub = new StubFor(PersonsWriteDAO)
        personsWriteDAOStub.demand.createJob { String osuID, JobObject job ->
            outParametersStub.proxyInstance()
        }

        new PersonsResource(
                personsDAOStub.proxyInstance(), personsWriteDAOStub.proxyInstance(), endpointUri)
    }

    @Test
    void createJobSuccessful() {
        PersonsResource personsResource = getPersonsResourceWithGoodMockDAOs()

        Response response = personsResource.createJob("hello", fakeJobResultObject)
        checkValidResponse(response, 202, fakeJob)
    }

    @Test
    void createJobShouldReturnNotFound() {
        def personsDAOStub = getPersonsDAOStub()
        personsDAOStub.demand.personExist { String osuID -> null }
        PersonsResource personsResource = new PersonsResource(
                personsDAOStub.proxyInstance(), null, endpointUri)

        Response response = personsResource.createJob("foo", new ResultObject())
        checkErrorResponse(response, 404)
    }

    @Test
    void createJobShouldRejectMalformedJobObject() {
        testCreatingBadJobObject("badObject")
    }

    @Test
    void createJobShouldRejectBadDate() {
        testCreatingBadJobObject(["beginDate":"5/3/2018"])
    }

    @Test
    void createJobShouldRejectNullJob() {
        testCreatingBadJobObject(null)
    }

    @Test
    void createJobRequiredFields() {
        JobObject job = new JobObject(
                positionNumber: null,
                beginDate: sampleDate,
                supervisorOsuID: '123',
                supervisorPositionNumber: '456'
        )

        ResultObject jobResultObject = new ResultObject(data: new ResourceObject(attributes: job))

        job.with {
            checkErrorResponse(getPersonsResourceWithGoodMockDAOs().createJob(
                    "123", jobResultObject), 400)
            positionNumber = "123456"
            beginDate = null
            checkErrorResponse(getPersonsResourceWithGoodMockDAOs().createJob(
                    "123", jobResultObject), 400)
            beginDate = sampleDate
            supervisorOsuID = null
            checkErrorResponse(getPersonsResourceWithGoodMockDAOs().createJob(
                    "123", jobResultObject), 400)
            supervisorOsuID = "123"
            supervisorPositionNumber = null
            checkErrorResponse(getPersonsResourceWithGoodMockDAOs().createJob(
                    "123", jobResultObject), 400)
            supervisorPositionNumber = "123456"
        }
    }

    void testCreatingBadJobObject(def resourceObjectAttributes) {
        def personsDAOStub = getPersonsDAOStub()
        personsDAOStub.demand.personExist { String osuID -> "123456789" }
        PersonsResource personsResource = new PersonsResource(
                personsDAOStub.proxyInstance(), null, endpointUri)

        ResultObject badJobResultObject = new ResultObject(data: new ResourceObject(
                attributes: resourceObjectAttributes))
        Response response = personsResource.createJob("123456789", badJobResultObject)
        checkErrorResponse(response, 400)
    }
}

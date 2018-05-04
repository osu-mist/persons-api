package edu.oregonstate.mist.persons

import edu.oregonstate.mist.api.Error
import edu.oregonstate.mist.api.jsonapi.ResultObject
import edu.oregonstate.mist.personsapi.core.JobObject
import edu.oregonstate.mist.personsapi.core.Name
import edu.oregonstate.mist.personsapi.core.PersonObject
import edu.oregonstate.mist.personsapi.PersonsResource
import edu.oregonstate.mist.personsapi.db.PersonsDAO
import groovy.mock.interceptor.StubFor
import org.junit.Test

import javax.ws.rs.core.Response

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
        birthDate: Date.parse('yyyy-MM-dd','2018-01-01'),
        currentStudent: false,
        mobilePhone: '+15411234567',
        primaryPhone: '+15411234567',
        homePhone: '15411234567',
        email: 'johndoe@oregonstate.edu',
        username: 'johndoe',
        confidential: false
    )

    JobObject fakeJob = new JobObject(
        positionNumber: 'C12345',
        beginDate: Date.parse('yyyy-MM-dd','2018-01-01'),
        endDate: null,
        status: 'Active',
        description: 'Fake Programmer',
        fullTimeEquivalency: 1
    )

    private static void checkErrorResponse (Response res, Integer errorCode) {
        assertNotNull(res)
        assertEquals(res.status, errorCode)
        assertEquals(res.getEntity().class, Error.class)
    }

    private static void checkValidResponse (Response res, Integer statusCode, def object) {
        assertNotNull(res)
        assertEquals(res.status, statusCode)
        assertEquals(res.getEntity().class, ResultObject.class)
        assertEquals(res.getEntity()['data']['attributes'], object)
    }

    @Test
    void shouldReturn400() {
        PersonsResource personsResource = new PersonsResource(null, endpointUri)

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
        def stub = new StubFor(PersonsDAO)
        stub.demand.with {
            getPersons { String onid, String osuID, String osuUID,
                         String firstName, String lastName, searchOldVersions -> null }
            personExist(2..2) { null }
            getJobsById { null }
            getImageById { null }
        }

        PersonsResource personsResource = new PersonsResource(stub.proxyInstance(), endpointUri)
        checkErrorResponse(personsResource.getPersonById('123456789'), 404)
        checkErrorResponse(personsResource.getJobs('123456789'), 404)
        checkErrorResponse(personsResource.getImageById('123456789', null), 404)
    }

   @Test
    void shouldReturnValidResponse() {
        def stub = new StubFor(PersonsDAO)
        stub.demand.with {
            getPersons(2..2) { String onid, String osuID, String osuUID,
                         String firstName, String lastName, searchOldVersions -> [fakePerson] }
            personExist { String osuID -> '123456789' }
            getJobsById { String osuID -> [fakeJob] }
            getJobLaborDistribution { String osuID, String positionNumber, String suffix -> null }
            getPreviousRecords(2..2) { String internalID -> null }
        }
        PersonsResource personsResource = new PersonsResource(stub.proxyInstance(), endpointUri)
        checkValidResponse(personsResource.list('johndoe', null, null, null, null, null, null), 200,
                [fakePerson])
        checkValidResponse(personsResource.getPersonById('123456789'), 200, fakePerson)
        checkValidResponse(personsResource.getJobs('123456789'), 200, [fakeJob])
        //@todo add test for getJobById
    }
}

package edu.oregonstate.mist.persons

import edu.oregonstate.mist.api.Error
import edu.oregonstate.mist.api.jsonapi.ResultObject
import edu.oregonstate.mist.personsapi.core.JobObject
import edu.oregonstate.mist.personsapi.core.PersonObject
import edu.oregonstate.mist.personsapi.PersonsResource
import edu.oregonstate.mist.personsapi.db.PersonsDAO
import groovy.mock.interceptor.MockFor
import groovy.mock.interceptor.StubFor
import org.junit.Test

import javax.ws.rs.core.Response

import static org.junit.Assert.assertEquals
import static org.junit.Assert.assertNotNull

class PersonsResourceTest {
    private final URI endpointUri = new URI('https://www.foo.com/')

    PersonObject fakePerson = new PersonObject(
        osuID: '123456789',
        lastName: 'Doe',
        firstName: 'John',
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
        fte: 1
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
    void shouldReturn400IfInputParamIsNotExactOne() {
        PersonsResource personsResource = new PersonsResource(null, endpointUri)

        checkErrorResponse(personsResource.list(null, null, null), 400)
        checkErrorResponse(personsResource.list('931234567', '123456789', null), 400)
        checkErrorResponse(personsResource.list('931234567', null, 'foobar'),400)
        checkErrorResponse(personsResource.list(null, '123456789', 'foobar'), 400)
        checkErrorResponse(personsResource.list('931234567', '123456789', 'foobar'),400)
    }

    @Test
    void shouldReturn404IfBadOSUId() {
        def stub = new StubFor(PersonsDAO)
        stub.demand.with {
            getPersonById { null }
            personExist { null }
            getJobsById { null }
            personExist { null }
            getImageById { null }
        }
        PersonsResource personsResource = new PersonsResource(stub.proxyInstance(), endpointUri)
        checkErrorResponse(personsResource.getPersonById('123456789'), 404)
        checkErrorResponse(personsResource.getJobsById('123456789'), 404)
        checkErrorResponse(personsResource.getImageById('123456789', null), 404)
    }

    @Test
    void shouldReturnValidResponse() {
        def stub = new StubFor(PersonsDAO)
        stub.demand.with {
            getPersons { String onid, String osuID, String osuUID -> [fakePerson] }
            personExist { String osuID -> '123456789' }
            getPersonById { String osuID -> fakePerson }
            getJobsById { String osuID -> fakeJob }
        }
        PersonsResource personsResource = new PersonsResource(stub.proxyInstance(), endpointUri)
        checkValidResponse(personsResource.list('johndoe', null, null), 200, [fakePerson])
        checkValidResponse(personsResource.getPersonById('123456789'), 200, fakePerson)
        checkValidResponse(personsResource.getJobsById('123456789'), 200, ['jobs': fakeJob])
    }
}

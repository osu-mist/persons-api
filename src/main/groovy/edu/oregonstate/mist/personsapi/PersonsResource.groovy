package edu.oregonstate.mist.personsapi

import com.codahale.metrics.annotation.Timed
import edu.oregonstate.mist.api.Resource
import edu.oregonstate.mist.api.jsonapi.ResourceObject
import edu.oregonstate.mist.api.jsonapi.ResultObject
import edu.oregonstate.mist.personsapi.db.PersonsDAO
import groovy.transform.TypeChecked

import javax.annotation.security.PermitAll
import javax.ws.rs.GET
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
    private PersonUriBuilder personUriBuilder

    PersonsResource(PersonsDAO personsDAO, URI endpointUri) {
        this.personsDAO = personsDAO
        this.endpointUri = endpointUri
        this.personUriBuilder = new PersonUriBuilder(endpointUri)
    }

    @Timed
    @GET
    Response list(@QueryParam('onid') String onid,
                  @QueryParam('osuID') String osuID,
                  @QueryParam('osuUID') Long osuUID) {

        def id = [onid, osuID, osuUID].findAll { it }
        if (id.size() != 1) {
            badRequest('The number of input parameter(s) is not equal to one.').build()
        } else {
            def persons = personsDAO.getPersons(onid, osuID, osuUID)
            ResultObject res = new ResultObject(
                data: persons.collect {
                    new ResourceObject(
                        id: it.osuID,
                        type: 'person',
                        attributes: it,
                        links: ['self': personUriBuilder.personUri(it.osuID)]
                    )
                }
            )
            ok(res).build()
        }
    }

    @Timed
    @GET
    @Path('{osuID: [0-9]{9}}')
    Response getPersonById(@PathParam('osuID') String osuID) {
        def person = personsDAO.getPersonById(osuID)
        ResultObject res = new ResultObject(data: new ResourceObject(
            id: osuID,
            type: 'person',
            attributes: person,
            links: ['self': personUriBuilder.personUri(osuID)]
        ))
        ok(res).build()
    }

    @Timed
    @GET
    @Path('{osuID: [0-9]{9}}/jobs')
    Response getJobsById(@PathParam('osuID') String osuID) {
        def jobs = personsDAO.getJobsById(osuID)

        def res = new ResultObject(
            data: new ResourceObject(
                id: osuID,
                type: 'jobs',
                attributes: jobs,
                links: ['self': personUriBuilder.personJobsUri(osuID)]
            )
        )
        ok(res).build()
    }
}

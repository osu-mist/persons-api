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
    @Path('{osuId: [0-9]{9}}')
    Response getPersonById(@PathParam('osuId') String osuId) {

        def res = new ResultObject(
            data: new ResourceObject(
                id: osuId,
                type: 'person',
                attributes: personsDAO.getPersonById(osuId),
                links: ['self': personUriBuilder.personJobUri(osuId)]
            )
        )
        ok(res).build()
    }

    @Timed
    @GET
    @Path('{osuId: [0-9]{9}}/jobs')
    Response getJobsById(@PathParam('osuId') String osuId) {

        def jobs = personsDAO.getJobsById(osuId)

        if (jobs) {
            def res = new ResultObject(
                data: new ResourceObject(
                    id: osuId,
                    type: 'jobs',
                    attributes: jobs,
                    links: ['self': personUriBuilder.personJobUri(osuId)]
                )
            )
            ok(res).build()
        } else {
            notFound().build()
        }

    }
}

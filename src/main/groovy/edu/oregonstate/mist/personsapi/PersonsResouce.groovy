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

    PersonsResource(PersonsDAO personsDAO) {
        this.personsDAO = personsDAO
    }

    @Timed
    @GET
    @Path('{osu_id: [0-9]{9}}')
    Response getPersonById(@PathParam('osu_id') String osu_id) {
        def res = personsDAO.getPersonById(osu_id)
//        ok(res).build()
        notFound().build() // returning not found for now
    }

    @Timed
    @GET
    @Path('{osu_id: [0-9]{9}}/jobs')
    Response getJobsById(@PathParam('osu_id') String osu_id) {
        def res = new ResultObject(
            data: new ResourceObject(
                id: osu_id,
                type: 'jobs',
                attributes: personsDAO.getJobsById(osu_id),
                links: ['self': osu_id]
            )
        )

        ok(res).build()
    }
}

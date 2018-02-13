package edu.oregonstate.mist.personsapi

import com.codahale.metrics.annotation.Timed
import edu.oregonstate.mist.api.Resource
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

    PersonsResource(PersonsDAO personsDAO) {
        this.personsDAO = personsDAO
    }

    @Timed
    @GET
    @Path('{osu_id: [0-9]{9}}')
    Response getById(@PathParam('osu_id') String osu_id) {
        def res = personsDAO.getById(osu_id)
        ok(res).build()
    }
}

package edu.oregonstate.mist.personsapi

import edu.oregonstate.mist.api.Resource
import groovy.transform.TypeChecked

import javax.annotation.security.PermitAll
import javax.ws.rs.Path
import javax.ws.rs.Produces
import javax.ws.rs.core.MediaType

@Path("persons")
@Produces(MediaType.APPLICATION_JSON)
@PermitAll
@TypeChecked
class PersonsResource extends Resource {
    private final PersonsDAO personsDAO

    PersonsResource(PersonsDAO personsDAO) {
        this.personsDAO = personsDAO
    }

}

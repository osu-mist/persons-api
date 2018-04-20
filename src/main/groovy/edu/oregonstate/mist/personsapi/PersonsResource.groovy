package edu.oregonstate.mist.personsapi

import com.codahale.metrics.annotation.Timed
import edu.oregonstate.mist.api.Resource
import edu.oregonstate.mist.api.jsonapi.ResourceObject
import edu.oregonstate.mist.api.jsonapi.ResultObject
import edu.oregonstate.mist.personsapi.core.PersonObject
import edu.oregonstate.mist.personsapi.db.PersonsDAO
import groovy.transform.TypeChecked
import org.apache.commons.lang3.StringUtils

import javax.annotation.security.PermitAll
import javax.imageio.ImageIO
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
    private final Integer maxImageWidth = 2000

    PersonsResource(PersonsDAO personsDAO, URI endpointUri) {
        this.personsDAO = personsDAO
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
                  @QueryParam('searchOldVersions') Boolean searchOldVersions) {
        // Check for a bad request.
        def ids = [onid, osuID, osuUID].findAll { it }.size()
        def names = [firstName, lastName].findAll { it }.size()

        Boolean validNameRequest = names == 2

        if (validNameRequest && ids != 0) {
            return badRequest('Cannot search by a name and an ID in the same request.').build()
        } else if (ids > 1) {
            return badRequest('onid, osuID, and osuUID cannot be included together.').build()
        } else if (names == 1) {
            return badRequest('firstName and lastName must be included together.').build()
        } else if (searchOldVersions && (onid || osuUID)) {
            return badRequest('searchOldVersions can only be used with ' +
                    'name queries or OSU ID queries.').build()
        } else if (ids + names == 0) {
            return badRequest('No names or IDs were provided in the request.').build()
        }

        // At this point, the request is valid. Proceed with desired data retrieval.
        List<PersonObject> persons = new ArrayList<PersonObject>()

        def addPerson = { PersonObject person ->
            if (person) {
                persons.add(person)
            }
        }

        if (names == 0 && ids == 1 && !searchOldVersions) {
            // Search by a current ID.
            addPerson(personsDAO.getPersonById(onid, osuID, osuUID, null))
        } else if (validNameRequest && ids == 0 && !searchOldVersions) {
            // Search current names.
            persons.addAll(personsDAO.getPersonByName(formatName(lastName),
                    formatName(firstName), false))
        } else if (names == 0 && ids == 1 && osuID && searchOldVersions) {
            // Search current and previous OSU ID's.
            addPerson(personsDAO.getPersonById(null, osuID, null, osuID))
        } else if (validNameRequest && ids == 0 && searchOldVersions) {
            // Search current and previous names.
            persons.addAll(personsDAO.getPersonByName(formatName(lastName),
                    formatName(firstName), true))
        } else {
            return internalServerError("The application encountered an unexpected condition.")
                    .build()
        }

        ResultObject res = personResultObject(persons)
        ok(res).build()
    }

    String formatName(String name) {
        StringUtils.stripAccents(name).toUpperCase()
    }

    @Timed
    @GET
    @Path('{osuID: [0-9]+}')
    Response getPersonById(@PathParam('osuID') String osuID) {
        def person = personsDAO.getPersonById(null, osuID, null, null)
        if (person) {
            ResultObject res = personResultObject(person)
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
        person.previousRecords = personsDAO.getPreviousRecords(person.internalID)

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
    Response getJobsById(@PathParam('osuID') String osuID) {

        if (personsDAO.personExist(osuID)) {
            def jobs = personsDAO.getJobsById(osuID)
            def res = new ResultObject(
                data: new ResourceObject(
                    id: osuID,
                    type: 'jobs',
                    attributes: ['jobs': jobs],
                    links: ['self': personUriBuilder.personJobsUri(osuID)]
                )
            )
            ok(res).build()
        } else {
            notFound().build()
        }
    }

    @Timed
    @GET
    @Produces('image/jpeg')
    @Path('{osuID: [0-9]+}/image')
    Response getImageById(@PathParam('osuID') String osuID, @QueryParam('width') Integer width) {

        if (personsDAO.personExist(osuID)) {
            if (width != null && (width <= 0) || (width > maxImageWidth)) {
                String widthError = 'Width must be value from 1 - ' + maxImageWidth
                return badRequest(widthError).type(MediaType.APPLICATION_JSON).build()
            }

            def res
            def image = personsDAO.getImageById(osuID)
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
}

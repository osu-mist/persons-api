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
                  @QueryParam('searchOldNames') Boolean searchOldNames,
                  @QueryParam('searchOldOsuIDs') Boolean searchOldOsuIDs) {
        // Check for a bad request.
        Closure<Integer> getSize = { List<String> list -> list.findAll { it }.size() }

        Integer idCount = getSize([onid, osuID, osuUID])
        Integer nameCount = getSize([firstName, lastName])

        Boolean validNameRequest = nameCount == 2

        String errorMessage

        if (osuUID && !osuUID.matches("[0-9]+")) {
            errorMessage = "OSU UID can only contain numbers."
        } else if (validNameRequest && idCount != 0) {
            errorMessage = "Cannot search by a name and an ID in the same request."
        } else if (idCount > 1) {
            errorMessage = "onid, osuID, and osuUID cannot be included together."
        } else if (nameCount == 1) {
            errorMessage = "firstName and lastName must be included together."
        } else if (searchOldNames && searchOldOsuIDs) {
            errorMessage = "searchOldNames and searchOldOsuIDs cannot both be true"
        } else if (searchOldOsuIDs && (onid || osuUID)) {
            errorMessage = "searchOldOsuIDs can only be used with OSU ID queries."
        } else if (searchOldOsuIDs && !osuID) {
            errorMessage = "osuID must be included if searchOldOsuIDs is true."
        } else if (searchOldNames && !validNameRequest) {
            errorMessage = "firstName and lastName must be included if searchOldNames is true."
        } else if (!idCount && !nameCount) {
            errorMessage = "No names or IDs were provided in the request."
        }

        if (errorMessage) {
            return badRequest(errorMessage).build()
        }

        // At this point, the request is valid. Proceed with desired data retrieval.
        List<PersonObject> persons

        if (nameCount == 0 && idCount == 1 && !searchOldOsuIDs) {
            // Search by a current ID.
            persons = personsDAO.getPersons(onid, osuID, osuUID, null, null, false)
        } else if (validNameRequest && idCount == 0 && !searchOldNames) {
            // Search current names.
            persons = personsDAO.getPersons(null, null, null,
                    formatName(firstName), formatName(lastName), false)
        } else if (nameCount == 0 && idCount == 1 && osuID && searchOldOsuIDs) {
            // Search current and previous OSU ID's.
            persons = personsDAO.getPersons(null, osuID, null, null, null, true)
        } else if (validNameRequest && idCount == 0 && searchOldNames) {
            // Search current and previous names.
            persons = personsDAO.getPersons(null, null, null,
                    formatName(firstName), formatName(lastName), true)
        } else {
            return internalServerError("The application encountered an unexpected condition.")
                    .build()
        }

        ResultObject res = personResultObject(persons)
        ok(res).build()
    }

    /**
     * Strip accents and convert to uppercase to prepare for DAO.
     * @param name
     * @return
     */
    String formatName(String name) {
        StringUtils.stripAccents(name).toUpperCase()
    }

    @Timed
    @GET
    @Path('{osuID: [0-9]+}')
    Response getPersonById(@PathParam('osuID') String osuID) {
        def person = personsDAO.getPersons(null, osuID, null, null, null, false)
        if (person) {
            ResultObject res = personResultObject(person?.get(0))
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

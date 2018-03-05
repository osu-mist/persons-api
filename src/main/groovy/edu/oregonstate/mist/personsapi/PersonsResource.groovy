package edu.oregonstate.mist.personsapi

import com.codahale.metrics.annotation.Timed
import edu.oregonstate.mist.api.Resource
import edu.oregonstate.mist.api.jsonapi.ResourceObject
import edu.oregonstate.mist.api.jsonapi.ResultObject
import edu.oregonstate.mist.personsapi.db.PersonsDAO
import groovy.transform.TypeChecked

import javax.annotation.security.PermitAll
import javax.imageio.ImageIO
import javax.ws.rs.GET
import javax.ws.rs.Path
import javax.ws.rs.PathParam
import javax.ws.rs.Produces
import javax.ws.rs.QueryParam
import javax.ws.rs.core.MediaType
import javax.ws.rs.core.Response
import java.awt.image.BufferedImage

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
                  @QueryParam('osuUID') String osuUID) {

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
        if (person) {
            ResultObject res = new ResultObject(data: new ResourceObject(
                id: osuID,
                type: 'person',
                attributes: person,
                links: ['self': personUriBuilder.personUri(osuID)]
            ))
            ok(res).build()
        } else {
            notFound().build()
        }
    }

    @Timed
    @GET
    @Path('{osuID: [0-9]{9}}/jobs')
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
    @Path('{osuID: [0-9]{9}}/image')
    Response getImageById(@PathParam('osuID') String osuID, @QueryParam('width') Integer width) {

        if (personsDAO.personExist(osuID)) {
            if (width && (width <= 0) || (width > maxImageWidth)) {
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

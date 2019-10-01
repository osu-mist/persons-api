package edu.oregonstate.mist.personsapi

import edu.oregonstate.mist.api.Application
import edu.oregonstate.mist.personsapi.db.BannerPersonsReadDAO
import edu.oregonstate.mist.personsapi.db.ODSPersonsReadDAO
import edu.oregonstate.mist.personsapi.db.PersonsStringTemplateDAO
import edu.oregonstate.mist.personsapi.db.BannerPersonsWriteDAO
import io.dropwizard.jdbi.DBIFactory
import io.dropwizard.setup.Environment
import org.skife.jdbi.v2.DBI

/**
 * Main application class.
 */
class PersonsApplication extends Application<PersonsApplicationConfiguration> {
    /**
     * Parses command-line arguments and runs the application.
     *
     * @param configuration
     * @param environment
     */
    @Override
    public void run(PersonsApplicationConfiguration configuration, Environment environment) {
        this.setup(configuration, environment)

        DBIFactory factory = new DBIFactory()
        DBI bannerReadJdbi = factory.build(
                environment, configuration.getBannerReadDataSourceFactory(), "bannerReadJdbi")
        BannerPersonsReadDAO bannerPersonsReadDAO = bannerReadJdbi.onDemand(
                BannerPersonsReadDAO.class)
        PersonsStringTemplateDAO personsStringTemplateDAO = bannerReadJdbi.onDemand(
                PersonsStringTemplateDAO.class)

        DBI bannerWriteJdbi = factory.build(
                environment, configuration.getBannerWriteDataSourceFactory(), "bannerWriteJdbi")
        BannerPersonsWriteDAO bannerPersonsWriteDAO = bannerWriteJdbi.onDemand(
                BannerPersonsWriteDAO.class)

        DBI odsReadJdbi = factory.build(
                environment, configuration.getODSReadDataSourceFactory(), "odsReadJdbi")
        ODSPersonsReadDAO odsPersonsReadDAO = odsReadJdbi.onDemand(
                ODSPersonsReadDAO.class)

        environment.jersey().register(
            new PersonsResource(bannerPersonsReadDAO, personsStringTemplateDAO,
                    bannerPersonsWriteDAO, odsPersonsReadDAO, configuration.api.endpointUri)
        )
    }

    /**
     * Instantiates the application class with command-line arguments.
     *
     * @param arguments
     * @throws Exception
     */
    public static void main(String[] arguments) throws Exception {
        new PersonsApplication().run(arguments)
    }
}

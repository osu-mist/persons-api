package edu.oregonstate.mist.personsapi

import edu.oregonstate.mist.api.Application
import edu.oregonstate.mist.personsapi.db.PersonsDAO
import edu.oregonstate.mist.personsapi.db.PersonsStringTemplateDAO
import edu.oregonstate.mist.personsapi.db.PersonsWriteDAO
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
        DBI readJdbi = factory.build(environment, configuration.getReadDataSourceFactory(),
                "readJdbi")
        PersonsDAO personsDAO = readJdbi.onDemand(PersonsDAO.class)
        PersonsStringTemplateDAO personsStringTemplateDAO = readJdbi.onDemand(
                PersonsStringTemplateDAO.class)

        DBI writeJdbi = factory.build(
                environment, configuration.getWriteDataSourceFactory(), "writeJdbi")
        PersonsWriteDAO personsWriteDAO = writeJdbi.onDemand(PersonsWriteDAO.class)

        environment.jersey().register(
            new PersonsResource(personsDAO, personsStringTemplateDAO,
                    personsWriteDAO, configuration.api.endpointUri)
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

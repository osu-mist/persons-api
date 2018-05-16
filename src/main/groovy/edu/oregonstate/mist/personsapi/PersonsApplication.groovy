package edu.oregonstate.mist.personsapi

import edu.oregonstate.mist.api.Application
import edu.oregonstate.mist.personsapi.db.MessageQueueDAO
import edu.oregonstate.mist.personsapi.db.PersonsDAO
import io.dropwizard.client.HttpClientBuilder
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

        def httpClientBuilder = new HttpClientBuilder(environment)

        if (configuration.httpClientConfiguration != null) {
            httpClientBuilder.using(configuration.httpClientConfiguration)
        }

        DBIFactory factory = new DBIFactory()
        DBI jdbi = factory.build(environment, configuration.getDataSourceFactory(), "jdbi")
        PersonsDAO personsDAO = jdbi.onDemand(PersonsDAO.class)
        MessageQueueDAO messageQueueDAO = new MessageQueueDAO(
                httpClientBuilder.build("backend-http-client"),
                configuration.messageQueueConfiguraiton.baseUrl,
                configuration.messageQueueConfiguraiton.apiKey,
                configuration.messageQueueConfiguraiton.newJobEventType)

        environment.jersey().register(
            new PersonsResource(personsDAO, messageQueueDAO, configuration.api.endpointUri)
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

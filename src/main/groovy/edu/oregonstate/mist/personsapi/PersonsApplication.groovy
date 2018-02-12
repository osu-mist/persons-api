package edu.oregonstate.mist.personsapi

import edu.oregonstate.mist.api.Application
import edu.oregonstate.mist.api.Configuration
import io.dropwizard.setup.Environment

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

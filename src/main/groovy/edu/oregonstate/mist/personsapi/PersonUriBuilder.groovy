package edu.oregonstate.mist.personsapi

import javax.ws.rs.core.UriBuilder

class PersonUriBuilder {
    URI endpointUri

    PersonUriBuilder(URI endpointUri) {
        this.endpointUri = endpointUri
    }

    URI personUri(String osuID) {
        UriBuilder.fromUri(this.endpointUri)
                  .path('persons/{osuID}')
                  .build(osuID)
    }

    URI personJobsUri(String osuID) {
        UriBuilder.fromUri(this.endpointUri)
                  .path('persons/{osuID}/jobs')
                  .build(osuID)
    }
}

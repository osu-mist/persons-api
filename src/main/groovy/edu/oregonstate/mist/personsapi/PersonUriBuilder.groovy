package edu.oregonstate.mist.personsapi

import javax.ws.rs.core.UriBuilder

class PersonUriBuilder {
    URI endpointUri

    PersonUriBuilder(URI endpointUri) {
        this.endpointUri = endpointUri
    }

    URI personJobUri(String osuID) {
        UriBuilder.fromUri(this.endpointUri)
            .path('persons/{osuID}/jobs')
            .build(osuID)
    }
}

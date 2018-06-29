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

    URI personJobsUri(String osuID, String positionNumber, String suffix) {
        UriBuilder.fromUri(this.endpointUri)
                  .path('persons/{osuID}/jobs')
                  .queryParam("positionNumber", positionNumber)
                  .queryParam("suffix", suffix)
                  .build(osuID)
    }

    URI mealPlanUri(String osuID, String mealPlanID) {
        UriBuilder.fromUri(this.endpointUri)
                .path('persons/{osuID}/meal-plans/{mealPlanID}')
                .build(osuID, mealPlanID)
    }
}

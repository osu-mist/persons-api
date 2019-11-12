package edu.oregonstate.mist.personsapi

import javax.ws.rs.core.UriBuilder
import javax.ws.rs.core.UriInfo

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

    URI ssnUri(String osuID, String ssnID) {
        UriBuilder.fromUri(this.endpointUri)
                .path('persons/{osuID}/ssn/{ssnID}')
                .build(osuID, ssnID)
    }

    URI addressUri(String osuID, String addressType) {
        UriBuilder.fromUri(this.endpointUri)
                .path('persons/{osuID}/addresses')
                .queryParam('addressType', addressType)
                .build(osuID)
    }

    URI phoneUri(String osuID, String phoneType) {
        UriBuilder.fromUri(this.endpointUri)
                .path('persons/{osuID}/phones')
                .queryParam('phoneType', phoneType)
                .build(osuID)
    }

    URI topLevelUri(UriInfo uri) {
        UriBuilder builder = UriBuilder.fromUri(this.endpointUri)

        if(uri) {
                builder.path(uri.getPath())

            uri.getQueryParameters().each {
                builder.queryParam(it.key, it.value[0])
            }

        }

        builder.build()
    }
}

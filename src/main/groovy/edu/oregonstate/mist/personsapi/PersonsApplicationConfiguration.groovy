package edu.oregonstate.mist.personsapi

import com.fasterxml.jackson.annotation.JsonProperty
import edu.oregonstate.mist.api.Configuration
import io.dropwizard.client.HttpClientConfiguration
import io.dropwizard.db.DataSourceFactory

import javax.validation.Valid
import javax.validation.constraints.NotNull

class PersonsApplicationConfiguration extends Configuration {

    @Valid
    @NotNull
    @JsonProperty("bannerReadDatabase")
    DataSourceFactory bannerReadDatabase = new DataSourceFactory()

    @JsonProperty("bannerReadDatabase")
    public DataSourceFactory getBannerReadDataSourceFactory() {
        bannerReadDatabase
    }

    @Valid
    @NotNull
    @JsonProperty("bannerWriteDatabase")
    DataSourceFactory bannerWriteDatabase = new DataSourceFactory()

    @JsonProperty("bannerWriteDatabase")
    public DataSourceFactory getBannerWriteDataSourceFactory() {
        bannerWriteDatabase
    }
}
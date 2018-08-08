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
    @JsonProperty("readDatabase")
    DataSourceFactory readDatabase = new DataSourceFactory()

    @JsonProperty("readDatabase")
    public DataSourceFactory getReadDataSourceFactory() {
        readDatabase
    }

    @Valid
    @NotNull
    @JsonProperty("writeDatabase")
    DataSourceFactory writeDatabase = new DataSourceFactory()

    @JsonProperty("writeDatabase")
    public DataSourceFactory getWriteDataSourceFactory() {
        writeDatabase
    }

    @JsonProperty("messageQueue")
    @NotNull
    @Valid
    Map<String, String> messageQueueConfiguraiton

    @Valid
    @NotNull
    HttpClientConfiguration httpClientConfiguration
}
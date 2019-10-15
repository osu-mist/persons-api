package edu.oregonstate.mist.personsapi.core
import edu.oregonstate.mist.api.jsonapi.ResultObject
import com.fasterxml.jackson.annotation.JsonFormat
import com.fasterxml.jackson.annotation.JsonIgnore
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.databind.ObjectMapper

import java.time.LocalDate

class AddressObject {
    private static ObjectMapper mapper = new ObjectMapper().registerModule(new JavaTimeModule())

    @JsonIgnore
    String id

    String addressType

    @JsonIgnore
    String addressTypeDescription
    @JsonIgnore
    String addressLine1
    @JsonIgnore
    String addressLine2
    @JsonIgnore
    String addressLine3
    @JsonIgnore
    String addressLine4
    @JsonIgnore
    String houseNumber
    String city
    String stateCode
    @JsonIgnore
    String state
    String postalCode
    @JsonIgnore
    String countyCode
    @JsonIgnore
    String county
    @JsonIgnore
    String nationCode
    @JsonIgnore
    String nation

    @JsonIgnore
    @JsonFormat(shape=JsonFormat.Shape.STRING, pattern="yyyy-MM-dd")
    LocalDate lastModified

    public static AddressObject fromResultObject(ResultObject resultObject) {
        try {
            mapper.convertValue(resultObject.data['attributes'], AddressObject.class)
        } catch (IllegalArgumentException e) {
            println('-------------')
            println(e)
            println('-------------')
            throw new PersonObjectException("Some fields weren't able to map to an address object.")
        } catch (NullPointerException e) {
            throw new PersonObjectException("Could not parse result object.")
        }
    }
}

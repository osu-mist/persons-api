package edu.oregonstate.mist.personsapi.core

import com.fasterxml.jackson.annotation.JsonFormat
import com.fasterxml.jackson.annotation.JsonIgnore
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.databind.ObjectMapper
import edu.oregonstate.mist.api.jsonapi.ResultObject

import java.time.LocalDate

class AddressObject {
    private static ObjectMapper mapper = new ObjectMapper().registerModule(new JavaTimeModule())

    String id
    String addressType
    String addressTypeDescription
    String addressLine1
    String addressLine2
    String addressLine3
    String addressLine4
    String houseNumber
    String city
    String stateCode
    String state
    String postalCode
    String countyCode
    String county
    String nationCode
    String nation

    @JsonFormat(shape=JsonFormat.Shape.STRING, pattern="yyyy-MM-dd")
    LocalDate lastModified

    public static AddressObject fromResultObject(ResultObject resultObject) {
        try {
            mapper.convertValue(resultObject.data['attributes'], AddressObject.class)
        } catch (IllegalArgumentException e) {
            throw new PersonObjectException("Some fields weren't able to map to an address object.")
        } catch (NullPointerException e) {
            throw new PersonObjectException("Could not parse result object.")
        }
    }
}

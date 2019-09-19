package edu.oregonstate.mist.personsapi.core

import com.fasterxml.jackson.annotation.JsonFormat
import com.fasterxml.jackson.annotation.JsonIgnore
import com.fasterxml.jackson.annotation.JsonUnwrapped
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import edu.oregonstate.mist.api.jsonapi.ResultObject
import groovy.transform.InheritConstructors

class PersonObject {
    private static ObjectMapper mapper = new ObjectMapper().registerModule(new JavaTimeModule())
    /*
     * Ignore osuID since it is only used for building link URI and response id
     */
    @JsonIgnore
    String osuID

    /*
     * Ignore internalID since it is an internal identifier and not meant to be exposed
     */
    @JsonIgnore
    String internalID

    @JsonFormat(shape=JsonFormat.Shape.STRING, pattern="yyyy-MM-dd", timezone="America/Los_Angeles")
    Date birthDate

    @JsonUnwrapped
    Name name
    List<PreviousRecord> previousRecords
    String homePhone
    String alternatePhone
    String osuUID
    String primaryPhone
    String mobilePhone
    Boolean currentStudent
    Boolean currentEmployee
    String employeeStatus
    String email
    String username
    Boolean confidential
    String ssnStatus

    public static PersonObject fromResultObject(ResultObject resultObject) {
        try {
            PersonObject personObject = mapper.convertValue(
                resultObject.data['attributes'], PersonObject.class
            )
            personObject.name.firstName = resultObject.data['attributes']['name']['firstName']
            personObject.name.lastName = resultObject.data['attributes']['name']['lastName']
            personObject
        } catch (IllegalArgumentException e) {
            throw new PersonObjectException("Some fields weren't able to map to a person object.")
        } catch (NullPointerException e) {
            throw new PersonObjectException("Could not parse result object.")
        }
    }
}

@InheritConstructors
class PersonObjectException extends Exception {}

class PreviousRecord {
    String osuID
    @JsonUnwrapped
    Name name
}

class Name {
    String firstName
    String middleName
    String lastName
}

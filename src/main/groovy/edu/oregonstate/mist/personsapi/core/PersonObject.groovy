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
    String displayFirstName
    String displayMiddleName
    String displayLastName
    List<PreviousRecord> previousRecords
    String citizen
    String sex
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
            def attributes = resultObject.data['attributes']
            PersonObject personObject = mapper.convertValue(
                attributes, PersonObject.class
            )
            personObject.name.firstName = attributes['name']['firstName']
            personObject.name.lastName = attributes['name']['lastName']
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

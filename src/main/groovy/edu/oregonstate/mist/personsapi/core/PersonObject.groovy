package edu.oregonstate.mist.personsapi.core

import com.fasterxml.jackson.annotation.JsonFormat
import com.fasterxml.jackson.annotation.JsonIgnore
import com.fasterxml.jackson.annotation.JsonUnwrapped

class PersonObject {
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

    @JsonFormat(shape=JsonFormat.Shape.STRING, pattern="yyyy-MM-dd", timezone="UTC")
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
    String email
    String username
    Boolean confidential
}

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
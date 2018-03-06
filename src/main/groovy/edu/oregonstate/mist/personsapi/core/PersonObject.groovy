package edu.oregonstate.mist.personsapi.core

import com.fasterxml.jackson.annotation.JsonFormat
import com.fasterxml.jackson.annotation.JsonIgnore

class PersonObject {
    /*
     * Ignore osuID since it is only used for building link URI and response id
     */
    @JsonIgnore
    String osuID

    @JsonFormat(shape=JsonFormat.Shape.STRING, pattern="yyyy-MM-dd", timezone="UTC")
    Date birthDate

    String lastName
    String homePhone
    String alternatePhone
    String osuUID
    String firstName
    String primaryPhone
    String mobilePhone
    Boolean currentStudent
    String middleName
    String email
    String username
    Boolean confidential
}
package edu.oregonstate.mist.core

import com.fasterxml.jackson.annotation.JsonIgnore

class PersonObject {
    @JsonIgnore
    String osuID
    String lastName
    String homePhone
    String alternatePhone
    Long osuUID
    Date birthDate
    String primaryAffiliation
    String firstName
    String primaryPhone
    Boolean currentUser
    Boolean currentEmployee
    String mobilePhone
    Boolean currentStudent
    String middleName
    String email
    String username
    Boolean confidential
}
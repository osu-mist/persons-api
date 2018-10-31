package edu.oregonstate.mist.personsapi.core

import com.fasterxml.jackson.annotation.JsonFormat
import com.fasterxml.jackson.annotation.JsonIgnore

import java.time.LocalDate

class AddressObject {
    @JsonIgnore
    String id

    String addressType
    String addressTypeDescription
    Integer addressNumber
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

    @JsonFormat(shape=JsonFormat.Shape.STRING, pattern="yyyy-MM-dd")
    LocalDate lastModified
}

package edu.oregonstate.mist.personsapi.core
import edu.oregonstate.mist.api.jsonapi.ResultObject
import com.fasterxml.jackson.annotation.JsonFormat
import com.fasterxml.jackson.annotation.JsonIgnore
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.databind.ObjectMapper

import java.time.LocalDate

class PhoneObject {
  private static ObjectMapper mapper = new ObjectMapper().registerModule(new JavaTimeModule())

  @JsonIgnore
  String id

  String areaCode
  String phoneNumber
  String fullPhoneNumber
  String phoneExtension
  String primaryIndicator
  String phoneType
  String phoneTypeDescription
  String addressType
  String addressTypeDescription

  @JsonFormat(shape=JsonFormat.Shape.STRING, pattern="yyyy-MM-dd")
  LocalDate activityDate

  public static PhoneObject fromResultObject(ResultObject resultObject) {
    try {
        mapper.convertValue(resultObject.data['attributes'], PhoneObject.class)
    } catch (IllegalArgumentException e) {
        throw new PersonObjectException("Some fields weren't able to map to an address object.")
    } catch (NullPointerException e) {
        throw new PersonObjectException("Could not parse result object.")
    }
}
}

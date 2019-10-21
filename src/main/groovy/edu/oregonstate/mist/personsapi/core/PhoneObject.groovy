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
  String addressType

  @JsonFormat(shape=JsonFormat.Shape.STRING, pattern="yyyy-MM-dd")
  LocalDate activityDate
}

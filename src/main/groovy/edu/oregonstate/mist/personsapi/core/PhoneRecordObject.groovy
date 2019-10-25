package edu.oregonstate.mist.personsapi.core
import edu.oregonstate.mist.api.jsonapi.ResultObject
import com.fasterxml.jackson.annotation.JsonFormat
import com.fasterxml.jackson.annotation.JsonIgnore
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.databind.ObjectMapper

class PhoneRecordObject {
  private static ObjectMapper mapper = new ObjectMapper().registerModule(new JavaTimeModule())

  String id
  String phoneSeqno
  String phoneType
  Boolean primaryIndicator
}
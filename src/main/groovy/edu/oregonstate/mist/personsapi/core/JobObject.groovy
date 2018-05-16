package edu.oregonstate.mist.personsapi.core

import com.fasterxml.jackson.annotation.JsonFormat
import com.fasterxml.jackson.annotation.JsonIgnore
import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.databind.ObjectMapper
import edu.oregonstate.mist.api.jsonapi.ResultObject
import groovy.transform.InheritConstructors

@JsonIgnoreProperties(ignoreUnknown=true) //when deserializing, ignore unknown fields
class JobObject {
    String positionNumber
    String suffix

    @JsonFormat(shape=JsonFormat.Shape.STRING, pattern="yyyy-MM-dd", timezone="UTC")
    Date beginDate

    @JsonFormat(shape=JsonFormat.Shape.STRING, pattern="yyyy-MM-dd", timezone="UTC")
    Date endDate

    String locationID
    String status
    String description
    BigDecimal fullTimeEquivalency
    BigDecimal appointmentPercent

    String supervisorOsuID
    String supervisorPositionNumber
    String supervisorSuffix

    String timesheetOrganizationCode
    BigDecimal hourlyRate
    BigDecimal hoursPerPay
    BigDecimal assignmentSalary
    BigDecimal paysPerYear
    BigDecimal annualSalary
    List<LaborDistribution> laborDistribution

    public static JobObject fromResultObject(ResultObject resultObject) {
        ObjectMapper mapper = new ObjectMapper()
        try {
            mapper.convertValue(resultObject.data['attributes'], JobObject.class)
        } catch (IllegalArgumentException e) {
            throw new PersonObjectException("Some fields weren't able to map to a person object.")
        } catch (NullPointerException e) {
            throw new PersonObjectException("Could not parse result object.")
        }
    }

    @JsonIgnore
    public Boolean isActive() {
        this.status == 'Active'
    }
}

@InheritConstructors
class PersonObjectException extends Exception {}

@JsonIgnoreProperties(ignoreUnknown=true) //when deserializing, ignore unknown fields
class LaborDistribution {
    String accountIndexCode
    String accountCode
    String activityCode
    BigDecimal distributionPercent
}
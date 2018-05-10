package edu.oregonstate.mist.personsapi.core

import com.fasterxml.jackson.annotation.JsonFormat
import com.fasterxml.jackson.annotation.JsonIgnore
import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.databind.ObjectMapper
import edu.oregonstate.mist.api.jsonapi.ResultObject

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
        mapper.convertValue(resultObject.data['attributes'], JobObject.class)
    }

    @JsonIgnore
    public Boolean isActive() {
        this.status == 'Active'
    }
}

@JsonIgnoreProperties(ignoreUnknown=true) //when deserializing, ignore unknown fields
class LaborDistribution {
    String accountIndexCode
    String accountCode
    String activityCode
    BigDecimal distributionPercent
}
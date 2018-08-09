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

    @JsonFormat(shape=JsonFormat.Shape.STRING, pattern="yyyy-MM-dd")
    Date effectiveDate

    @JsonFormat(shape=JsonFormat.Shape.STRING, pattern="yyyy-MM-dd")
    Date beginDate

    @JsonFormat(shape=JsonFormat.Shape.STRING, pattern="yyyy-MM-dd")
    Date endDate

    String contractType
    Boolean accruesLeave

    @JsonFormat(shape=JsonFormat.Shape.STRING, pattern="yyyy-MM-dd")
    Date contractBeginDate

    @JsonFormat(shape=JsonFormat.Shape.STRING, pattern="yyyy-MM-dd")
    Date contractEndDate

    String locationID
    String status
    String description

    @JsonFormat(shape=JsonFormat.Shape.STRING, pattern="yyyy-MM-dd")
    Date personnelChangeDate

    String changeReasonCode

    BigDecimal fullTimeEquivalency
    BigDecimal appointmentPercent
    Integer salaryStep
    String salaryGroupCode
    String strsAssignmentCode

    String supervisorOsuID
    String supervisorPositionNumber
    String supervisorSuffix

    String timesheetOrganizationCode
    BigDecimal hourlyRate
    BigDecimal hoursPerPay
    BigDecimal assignmentSalary
    BigDecimal paysPerYear
    String employeeClassificationCode
    String employerCode
    BigDecimal annualSalary

    @JsonFormat(shape=JsonFormat.Shape.STRING, pattern="yyyy-MM-dd")
    Date earnCodeEffectiveDate

    String earnCode
    BigDecimal earnCodeHours
    String earnCodeShift

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
    @JsonFormat(shape=JsonFormat.Shape.STRING, pattern="yyyy-MM-dd")
    Date effectiveDate

    String accountIndexCode
    String fundCode
    String organizationCode
    String accountCode
    String programCode
    String activityCode
    BigDecimal distributionPercent
}
package edu.oregonstate.mist.personsapi.core

import com.fasterxml.jackson.annotation.JsonFormat
import com.fasterxml.jackson.annotation.JsonIgnore
import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import edu.oregonstate.mist.api.jsonapi.ResultObject
import groovy.transform.InheritConstructors

import java.time.LocalDate
import java.time.format.DateTimeFormatter

@JsonIgnoreProperties(ignoreUnknown=true) //when deserializing, ignore unknown fields
class JobObject {
    private static ObjectMapper mapper = new ObjectMapper().registerModule(new JavaTimeModule())

    private static final String accruesLeaveTrue = 'Y'
    private static final String accruesLeaveFalse = 'N'

    public static final String activeJobStatus = "Active"
    // Hard code job statuses and those descriptions here since there is no validation table
    private static final def jobStatusDict = [
            'A': activeJobStatus,
            'B': 'Leave without pay but with benefits',
            'L': 'Leave without pay and benefits',
            'F': 'Leave with full pay and benefits',
            'P': 'Leave with partial pay and benefits',
            'T': 'Terminated'
    ].withDefault { key -> 'New job status. Please contact API support for further assistance' }

    String positionNumber
    String suffix

    @JsonFormat(shape=JsonFormat.Shape.STRING, pattern="yyyy-MM-dd")
    LocalDate effectiveDate

    @JsonFormat(shape=JsonFormat.Shape.STRING, pattern="yyyy-MM-dd")
    LocalDate beginDate

    @JsonFormat(shape=JsonFormat.Shape.STRING, pattern="yyyy-MM-dd")
    LocalDate endDate

    Boolean accruesLeave

    @JsonIgnore
    public void setAccruesLeaveFromDbValue(String dbValue) {
        switch (dbValue) {
            case accruesLeaveTrue: this.accruesLeave = true
                break
            case accruesLeaveFalse: this.accruesLeave = false
                break
            default:
                this.accruesLeave = null
                break
        }
    }

    @JsonIgnore
    public String getAccruesLeaveForDb() {
        if (this.accruesLeave == null) {
            null
        } else {
            this.accruesLeave ? accruesLeaveTrue : accruesLeaveFalse
        }
    }

    @JsonFormat(shape=JsonFormat.Shape.STRING, pattern="yyyy-MM-dd")
    LocalDate contractBeginDate

    @JsonFormat(shape=JsonFormat.Shape.STRING, pattern="yyyy-MM-dd")
    LocalDate contractEndDate

    String contractType
    String locationID
    String status

    @JsonIgnore
    public void setStatusFromDbValue(String dbValue) {
        this.status = jobStatusDict[dbValue]
    }

    @JsonIgnore
    public String getStatusForDb() {
        jobStatusDict.find { it.value == this.status }?.key
    }

    @JsonIgnore
    public Boolean isActive() {
        this.status == activeJobStatus
    }

    String description

    @JsonFormat(shape=JsonFormat.Shape.STRING, pattern="yyyy-MM-dd")
    LocalDate personnelChangeDate

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
    BigDecimal annualSalary

    @JsonFormat(shape=JsonFormat.Shape.STRING, pattern="yyyy-MM-dd")
    LocalDate earnCodeEffectiveDate

    String earnCode
    BigDecimal earnCodeHours
    String earnCodeShift

    List<LaborDistribution> laborDistribution

    @JsonIgnore
    public LaborDistributionForDb getLaborDistrubtionForDb() {
        LaborDistributionForDb.getLaborDistributionForDb(this.laborDistribution)
    }

    public static JobObject fromResultObject(ResultObject resultObject) {
        try {
            mapper.convertValue(resultObject.data['attributes'], JobObject.class)
        } catch (IllegalArgumentException e) {
            throw new PersonObjectException("Some fields weren't able to map to a person object.")
        } catch (NullPointerException e) {
            throw new PersonObjectException("Could not parse result object.")
        }
    }
}

@InheritConstructors
class PersonObjectException extends Exception {}

@JsonIgnoreProperties(ignoreUnknown=true) //when deserializing, ignore unknown fields
class LaborDistribution {
    @JsonFormat(shape=JsonFormat.Shape.STRING, pattern="yyyy-MM-dd")
    LocalDate effectiveDate

    String accountIndexCode
    String fundCode
    String organizationCode
    String accountCode
    String programCode
    String activityCode
    String locationCode
    BigDecimal distributionPercent
}

/**
 * For creating new jobs, labor distributions are combined into a single object
 * with concatenated values. This class represents the values for inserting into the database.
 */
class LaborDistributionForDb {
    private static final String delimiter = "|"
    private static final DateTimeFormatter dbDateFormat =
            DateTimeFormatter.ofPattern("dd-MMM-yyyy")//example: 01-AUG-2018

    Integer count //the number of labor distributions
    String effectiveDates
    String accountIndexCodes
    String fundCodes
    String organizationCodes
    String accountCodes
    String programCodes
    String activityCodes
    String locationCodes
    String distributionPercentages

    public static LaborDistributionForDb getLaborDistributionForDb(
            List<LaborDistribution> laborDistribution) {
        new LaborDistributionForDb(
                count: laborDistribution ? laborDistribution.size() : 0,
                effectiveDates: concatenateList(laborDistribution.collect {
                    it.effectiveDate.format(dbDateFormat).toUpperCase()
                }),
                accountIndexCodes: concatenateList(laborDistribution.collect {
                    it.accountIndexCode
                }),
                fundCodes: concatenateList(laborDistribution.collect { it.fundCode }),
                organizationCodes: concatenateList(laborDistribution.collect {
                    it.organizationCode
                }),
                accountCodes: concatenateList(laborDistribution.collect { it.accountCode }),
                programCodes: concatenateList(laborDistribution.collect { it.programCode }),
                activityCodes: concatenateList(laborDistribution.collect { it.activityCode }),
                locationCodes: concatenateList(laborDistribution.collect { it.locationCode }),
                distributionPercentages: concatenateList(laborDistribution.collect {
                    it.distributionPercent.toString()
                })
        )
    }

    /**
     * Formats a list of strings for database insert.
     * @param List<String> ["foo", "bar", null, "eggplant"]
     * @return String "foo|bar||eggplant|"
     */
    private static String concatenateList(List<String> list) {
        "${list.collect { it ?: "" }.join(delimiter)}${delimiter}"
    }
}
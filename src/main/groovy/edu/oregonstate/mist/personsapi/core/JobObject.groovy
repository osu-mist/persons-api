package edu.oregonstate.mist.personsapi.core

import com.fasterxml.jackson.annotation.JsonFormat

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
}

class LaborDistribution {
    @JsonFormat(shape=JsonFormat.Shape.STRING, pattern="yyyy-MM-dd", timezone="UTC")
    Date effectiveDate

    String chartOfAccountsCode
    String accountIndex
    String fund
    String organizationCode
    String accountCode
    String programCode
    BigDecimal distributionPercent
}
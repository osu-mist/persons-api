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
    Integer appointmentBasis
    String status
    String description
    BigDecimal fte
    Integer appointmentPercent

    String supervisorID
    String supervisorPositionNumber
    String homeOrganizationCode
    BigDecimal hourlyRate
    BigDecimal hoursPerPay
    BigDecimal assignmentSalary
    Integer paysPerYear
    BigDecimal annualSalary
    BigDecimal appointmentSalary
    String step
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
    BigDecimal distributionPercentage
}
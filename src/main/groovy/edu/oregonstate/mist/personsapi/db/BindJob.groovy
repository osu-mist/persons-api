package edu.oregonstate.mist.personsapi.db

import edu.oregonstate.mist.personsapi.core.JobObject
import edu.oregonstate.mist.personsapi.core.LaborDistributionForDb
import org.skife.jdbi.v2.SQLStatement
import org.skife.jdbi.v2.sqlobject.Binder
import org.skife.jdbi.v2.sqlobject.BinderFactory
import org.skife.jdbi.v2.sqlobject.BindingAnnotation

import java.lang.annotation.Annotation
import java.lang.annotation.ElementType
import java.lang.annotation.Retention
import java.lang.annotation.RetentionPolicy
import java.lang.annotation.Target

@BindingAnnotation(BindJob.EventBinderFactor.class)
@Retention(RetentionPolicy.RUNTIME)
@Target([ElementType.PARAMETER])
public @interface BindJob {
    public static class EventBinderFactor implements BinderFactory {
        public Binder build(Annotation annotation) {
            new Binder<BindJob, JobObject>() {
                public void bind(SQLStatement q, BindJob bind, JobObject job) {
                    q.bind("positionNumber", job.positionNumber)
                    q.bind("suffix", job.suffix)
                    q.bind("effectiveDate", job.effectiveDate)
                    q.bind("beginDate", job.beginDate)
                    q.bind("endDate", job.endDate)
                    q.bind("accruesLeave", job.getAccruesLeaveForDb())
                    q.bind("contractBeginDate", job.contractBeginDate)
                    q.bind("contractEndDate", job.contractEndDate)
                    q.bind("contractType", job.contractType)
                    q.bind("locationID", job.locationID)
                    q.bind("status", job.getStatusForDb())
                    q.bind("description", job.description)
                    q.bind("personnelChangeDate", job.personnelChangeDate)
                    q.bind("changeReasonCode", job.changeReasonCode)
                    q.bind("fullTimeEquivalency", job.fullTimeEquivalency)
                    q.bind("appointmentPercent", job.appointmentPercent)
                    q.bind("salaryStep", job.salaryStep)
                    q.bind("salaryGroupCode", job.salaryGroupCode)
                    q.bind("strsAssignmentCode", job.strsAssignmentCode)
                    q.bind("supervisorOsuID", job.supervisorOsuID)
                    q.bind("supervisorPositionNumber", job.supervisorPositionNumber)
                    q.bind("supervisorSuffix", job.supervisorSuffix)
                    q.bind("timesheetOrganizationCode", job.timesheetOrganizationCode)
                    q.bind("hourlyRate", job.hourlyRate)
                    q.bind("hoursPerPay", job.hoursPerPay)
                    q.bind("assignmentSalary", job.assignmentSalary)
                    q.bind("paysPerYear", job.paysPerYear)
                    q.bind("employeeClassificationCode", job.employeeClassificationCode)
                    q.bind("annualSalary", job.annualSalary)
                    q.bind("earnCodeEffectiveDate", job.earnCodeEffectiveDate)
                    q.bind("earnCode", job.earnCode)
                    q.bind("earnCodeHours", job.earnCodeHours)
                    q.bind("earnCodeShift", job.earnCodeShift)

                    LaborDistributionForDb labor = job.laborDistrubtionForDb

                    println("labor effective dates: " + labor.effectiveDates)
                    println("job effectiveDate: " + job.effectiveDate)
                    println("job beginDate: " + job.beginDate)
                    
                    q.bind("laborCount", labor.count)
                    q.bind("laborEffectiveDates", labor.effectiveDates)
                    q.bind("laborAccountIndexCodes", labor.accountIndexCodes)
                    q.bind("laborFundCodes", labor.fundCodes)
                    q.bind("laborOrganizationCodes", labor.organizationCodes)
                    q.bind("laborAccountCodes", labor.accountCodes)
                    q.bind("laborProgramCodes", labor.programCodes)
                    q.bind("laborActivityCodes", labor.activityCodes)
                    q.bind("laborLocationCodes", labor.locationCodes)
                    q.bind("laborDistributionPercentages", labor.distributionPercentages)
                }
            }
        }
    }
}
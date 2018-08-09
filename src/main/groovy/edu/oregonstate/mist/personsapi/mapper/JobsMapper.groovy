package edu.oregonstate.mist.personsapi.mapper

import edu.oregonstate.mist.personsapi.core.JobObject
import org.skife.jdbi.v2.StatementContext
import org.skife.jdbi.v2.tweak.ResultSetMapper

import java.sql.ResultSet
import java.sql.SQLException

public class JobsMapper implements ResultSetMapper<JobObject> {
    public JobObject map(int i, ResultSet rs, StatementContext sc) throws SQLException {

        /*
         * Hard code job statuses and those descriptions here since there is no validation table
         */
        def jobStatusDict = [
            'A': 'Active',
            'B': 'Leave without pay but with benefits',
            'L': 'Leave without pay and benefits',
            'F': 'Leave with full pay and benefits',
            'P': 'Leave with partial pay and benefits',
            'T': 'Terminated'
        ].withDefault { key -> 'New job status. Please contact API support for further assistance' }

        new JobObject(
            positionNumber: rs.getString('POSITION_NUMBER'),
            suffix: rs.getString('SUFFIX'),
            effectiveDate: rs.getDate('EFFECTIVE_DATE'),
            beginDate: rs.getDate('BEGIN_DATE'),
            endDate: rs.getDate('END_DATE'),
            contractType: rs.getString('CONTRACT_TYPE'),
            accruesLeave: rs.getString('ACCRUE_LEAVE_INDICATOR') == 'Y',
            contractBeginDate: rs.getDate('CONTRACT_BEGIN_DATE'),
            contractEndDate: rs.getDate('CONTRACT_END_DATE'),
            locationID: rs.getString('LOCATION_ID'),
            status: jobStatusDict[rs.getString('STATUS')],
            personnelChangeDate: rs.getDate('PERSONNEL_CHANGE_DATE'),
            changeReasonCode: rs.getString('CHANGE_REASON_CODE'),
            description: rs.getString('DESCRIPTION'),
            fullTimeEquivalency: rs.getBigDecimal('FTE'),
            appointmentPercent: rs.getBigDecimal('APPOINTMENT_PERCENT'),
            salaryStep: rs.getInt('SALARY_STEP'),
            salaryGroupCode: rs.getString('SALARY_GROUP_CODE'),
            strsAssignmentCode: rs.getString('STRS_ASSIGNMENT_CODE'),
            supervisorOsuID: rs.getString('SUPERVISOR_ID'),
            supervisorPositionNumber: rs.getString('SUPERVISOR_POSITION_NUMBER'),
            supervisorSuffix: rs.getString('SUPERVISOR_SUFFIX'),
            timesheetOrganizationCode: rs.getString('TIMESHEET_ORGANIZATION_CODE'),
            hourlyRate: rs.getBigDecimal('HOURLY_RATE'),
            hoursPerPay: rs.getBigDecimal('HOURS_PER_PAY'),
            assignmentSalary: rs.getBigDecimal('ASSIGNMENT_SALARY'),
            paysPerYear: rs.getBigDecimal('PAYS_PER_YEAR'),
            employeeClassificationCode: rs.getString('EMPLOYEE_CLASSIFICATION_CODE'),
            employerCode: rs.getString('EMPLOYER_CODE'),
            annualSalary: rs.getBigDecimal('ANNUAL_SALARY'),
            earnCodeEffectiveDate: rs.getDate('EARN_CODE_EFFECTIVE_DATE'),
            earnCode: rs.getString('EARN_CODE'),
            earnCodeHours: rs.getBigDecimal('EARN_CODE_HOURS'),
            earnCodeShift: rs.getString('EARN_CODE_SHIFT')
        )
    }
}
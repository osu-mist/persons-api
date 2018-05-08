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
            beginDate: rs.getDate('BEGIN_DATE'),
            endDate: rs.getDate('END_DATE'),
            locationID: rs.getString('LOCATION_ID'),
            status: jobStatusDict[rs.getString('STATUS')],
            description: rs.getString('DESCRIPTION'),
            fullTimeEquivalency: rs.getBigDecimal('FTE'),
            appointmentPercent: rs.getBigDecimal('APPOINTMENT_PERCENT'),
            supervisorOsuID: rs.getString('SUPERVISOR_ID'),
            supervisorPositionNumber: rs.getString('SUPERVISOR_POSITION_NUMBER'),
            supervisorSuffix: rs.getString('SUPERVISOR_SUFFIX'),
            timesheetOrganizationCode: rs.getString('TIMESHEET_ORGANIZATION_CODE'),
            hourlyRate: rs.getBigDecimal('HOURLY_RATE'),
            hoursPerPay: rs.getBigDecimal('HOURS_PER_PAY'),
            assignmentSalary: rs.getBigDecimal('ASSIGNMENT_SALARY'),
            paysPerYear: rs.getBigDecimal('PAYS_PER_YEAR'),
            annualSalary: rs.getBigDecimal('ANNUAL_SALARY')
        )
    }
}
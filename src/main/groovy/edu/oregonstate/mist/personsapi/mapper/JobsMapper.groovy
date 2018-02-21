package edu.oregonstate.mist.personsapi.mapper

import edu.oregonstate.mist.core.JobObject
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
            'B': 'Leave without pay with benefits',
            'L': 'Leave without pay without benefits',
            'F': 'Leave with full pay and benefits',
            'P': 'Leave with partial pay and benefits',
            'T': 'Terminated'
        ]

        new JobObject(
            positionNumber: rs.getString('POSITION_NUMBER'),
            fte: rs.getFloat('FTE'),
            description: rs.getString('DESCRIPTION'),
            effectiveDate: rs.getDate('EFFECTIVE_DATE'),
            status: jobStatusDict[rs.getString('STATUS')]
        )
    }
}
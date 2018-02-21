package edu.oregonstate.mist.personsapi.mapper

import edu.oregonstate.mist.core.JobObject
import org.skife.jdbi.v2.StatementContext
import org.skife.jdbi.v2.tweak.ResultSetMapper

import java.sql.ResultSet
import java.sql.SQLException

public class JobsMapper implements ResultSetMapper<JobObject> {
    public JobObject map(int i, ResultSet rs, StatementContext sc) throws SQLException {
        new JobObject(
            positionNumber: rs.getString('POSITION_NUMBER')
        )
    }
}

package edu.oregonstate.mist.personsapi.mapper

import edu.oregonstate.mist.personsapi.core.Name

import edu.oregonstate.mist.personsapi.core.PreviousRecord
import org.skife.jdbi.v2.StatementContext
import org.skife.jdbi.v2.tweak.ResultSetMapper

import java.sql.ResultSet
import java.sql.SQLException

public class PreviousRecordMapper implements ResultSetMapper<PreviousRecord> {
    public PreviousRecord map(int i, ResultSet rs, StatementContext sc) throws SQLException {
        new PreviousRecord (
                osuID: rs.getString('osu_id'),
                name: new Name(
                        firstName: rs.getString('first_name'),
                        middleName: rs.getString('middle_name'),
                        lastName: rs.getString('last_name')
                )
        )
    }
}

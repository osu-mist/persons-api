package edu.oregonstate.mist.personsapi.mapper

import edu.oregonstate.mist.personsapi.core.AddressRecordObject
import edu.oregonstate.mist.personsapi.core.AddressObject
import org.skife.jdbi.v2.StatementContext
import org.skife.jdbi.v2.tweak.ResultSetMapper

import java.sql.ResultSet
import java.sql.SQLException

public class AddressRecordMapper implements ResultSetMapper<AddressRecordObject> {
    public AddressRecordObject map(int i, ResultSet rs, StatementContext sc) throws SQLException {
        new AddressRecordObject(
            rowID: rs.getString('ROWID'),
            addressType: rs.getString('SPRADDR_ATYP_CODE'),
            seqno: rs.getString('SPRADDR_SEQNO')
        )
    }
}

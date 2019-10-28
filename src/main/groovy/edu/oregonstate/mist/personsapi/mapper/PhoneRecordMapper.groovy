package edu.oregonstate.mist.personsapi.mapper

import edu.oregonstate.mist.personsapi.core.PhoneRecordObject
import org.skife.jdbi.v2.StatementContext
import org.skife.jdbi.v2.tweak.ResultSetMapper

import java.sql.ResultSet
import java.sql.SQLException

public class PhoneRecordMapper implements ResultSetMapper<PhoneRecordObject> {
  public PhoneRecordObject map(int i, ResultSet rs, StatementContext sc) throws SQLException {
    new PhoneRecordObject(
      id: rs.getString('ID'),
      phoneSeqno: rs.getString('PHONE_SEQNO'),
      phoneType: rs.getString('PHONE_TYPE'),
      primaryIndicator: rs.getBoolean('PRIMARY_INDICATOR')
    )
  }
}

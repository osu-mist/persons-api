package edu.oregonstate.mist.personsapi.mapper

import edu.oregonstate.mist.personsapi.core.PhoneRecordObject
import org.skife.jdbi.v2.StatementContext
import org.skife.jdbi.v2.tweak.ResultSetMapper

import java.sql.ResultSet
import java.sql.SQLException

public class PhoneMapper implements ResultSetMapper<PhoneRecordObject> {
  public PhoneRecordObject map(int i, ResultSet rs, StatementContext sc) throws SQLException {
    new PhoneRecordObject(
      addressSeqno: rs.getString('SPRTELE_ADDR_SEQNO'),
      addressType: rs.getString('ADDRESS_TYPE')
    )
  }
}

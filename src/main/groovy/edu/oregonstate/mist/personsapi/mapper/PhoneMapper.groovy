package edu.oregonstate.mist.personsapi.mapper

import edu.oregonstate.mist.personsapi.core.PhoneObject
import org.skife.jdbi.v2.StatementContext
import org.skife.jdbi.v2.tweak.ResultSetMapper

import java.sql.ResultSet
import java.sql.SQLException

public class PhoneMapper implements ResultSetMapper<PhoneObject> {
  /* public PhoneObject map(int i, ResultSet rs, StatementContext sc) throws SQLException {
    new PhoneObject(
      id: rs.getString('SPRTELE_PIDM'),
      areaCode: rs.getString('SPRTELE_PHONE_AREA')
    )
  } */
}

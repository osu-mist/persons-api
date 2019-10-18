package edu.oregonstate.mist.personsapi.mapper

import edu.oregonstate.mist.personsapi.core.PhoneObject
import org.skife.jdbi.v2.StatementContext
import org.skife.jdbi.v2.tweak.ResultSetMapper

import java.sql.ResultSet
import java.sql.SQLException

public class PhoneMapper implements ResultSetMapper<PhoneObject> {
  public PhoneObject map(int i, ResultSet rs, StatementContext sc) throws SQLException {
    new PhoneObject(
      id: rs.getString('ID'),
      phoneNumber: rs.getString('AREA_CODE') + rs.getString('PHONE_NUMBER'),
      phoneExtension: rs.getString('PHONE_EXTENSION'),
      primaryIndicator: rs.getString('PRIMARY_IND'),
      addressCode: rs.getString('ADDRESS_CODE'),
      activityDate: rs.getDate('ACTIVITY_DATE')?.toLocalDate()
    )
  }
}

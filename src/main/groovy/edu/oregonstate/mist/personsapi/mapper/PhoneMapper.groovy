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
      areaCode: rs.getString('AREA_CODE'),
      phoneNumber: rs.getString('PHONE_NUMBER'),
      fullPhoneNumber: rs.getString('AREA_CODE') + rs.getString('PHONE_NUMBER'),
      phoneExtension: rs.getString('PHONE_EXTENSION'),
      primaryIndicator: rs.getBoolean('PRIMARY_IND'),
      phoneType: rs.getString('PHONE_TYPE'),
      phoneTypeDescription: rs.getString('PHONE_TYPE_DESC'),
      addressType: rs.getString('ADDRESS_TYPE'),
      addressTypeDescription: rs.getString('ADDRESS_TYPE_DESC'),
      lastModified: rs.getDate('ACTIVITY_DATE')?.toLocalDate()
    )
  }
}

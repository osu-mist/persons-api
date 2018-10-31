package edu.oregonstate.mist.personsapi.mapper

import edu.oregonstate.mist.personsapi.core.AddressObject
import org.skife.jdbi.v2.StatementContext
import org.skife.jdbi.v2.tweak.ResultSetMapper

import java.sql.ResultSet
import java.sql.SQLException

public class AddressMapper implements ResultSetMapper<AddressObject> {
    public AddressObject map(int i, ResultSet rs, StatementContext sc) throws SQLException {
        new AddressObject(
                id: rs.getString('ADDRESS_ID'),
                addressType: rs.getString('ADDRESS_TYPE'),
                addressTypeDescription: rs.getString('ADDRESS_TYPE_DESCRIPTION'),
                addressNumber: rs.getInt('ADDRESS_NUMBER'),
                addressLine1: rs.getString('ADDRESS_LINE_1'),
                addressLine2: rs.getString('ADDRESS_LINE_2'),
                addressLine3: rs.getString('ADDRESS_LINE_3'),
                addressLine4: rs.getString('ADDRESS_LINE_4'),
                houseNumber: rs.getString('HOUSE_NUMBER'),
                city: rs.getString('CITY'),
                stateCode: rs.getString('STATE_CODE'),
                state: rs.getString('STATE'),
                postalCode: rs.getString('POSTAL_CODE'),
                countyCode: rs.getString('COUNTY_CODE'),
                county: rs.getString('COUNTY'),
                lastModified: rs.getDate('LAST_MODIFIED')?.toLocalDate()
        )
    }
}

package edu.oregonstate.mist.personsapi.mapper

import edu.oregonstate.mist.core.PersonObject
import edu.oregonstate.mist.personsapi.PhoneFormatter
import org.skife.jdbi.v2.StatementContext
import org.skife.jdbi.v2.tweak.ResultSetMapper

import java.sql.ResultSet
import java.sql.SQLException

public class PersonMapper implements ResultSetMapper<PersonObject> {
    PhoneFormatter phoneFormatter = new PhoneFormatter()
    public PersonObject map(int i, ResultSet rs, StatementContext sc) throws SQLException {
        new PersonObject (
            osuID: rs.getString('OSU_ID'),
            firstName: rs.getString('FIRST_NAME'),
            lastName: rs.getString('LAST_NAME'),
            middleName: rs.getString('MIDDLE_NAME'),
            birthDate: rs.getDate('BIRTH_DATE'),
            primaryAffiliation: rs.getString('PRIMARY_AFFILIATION'),
            email: rs.getString('EMAIL_ADDRESS'),
            username: rs.getString('USERNAME'),
            osuUID: rs.getString('OSUUID'),
            confidential: rs.getBoolean('CONFIDENTIAL'),
            currentUser: rs.getBoolean('CURRENT_USER'),
            currentStudent: rs.getBoolean('CURRENT_STUDENT'),
            homePhone: phoneFormatter.toE164(rs.getString('HOME_PHONE')),
            alternatePhone: phoneFormatter.toE164(rs.getString('ALTERNATE_PHONE')),
            primaryPhone: phoneFormatter.toE164(rs.getString('PRIMARY_PHONE')),
            mobilePhone: phoneFormatter.toE164(rs.getString('MOBILE_PHONE'))
        )
    }
}

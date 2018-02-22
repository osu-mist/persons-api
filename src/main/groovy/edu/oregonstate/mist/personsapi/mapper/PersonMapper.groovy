package edu.oregonstate.mist.personsapi.mapper

import com.google.i18n.phonenumbers.PhoneNumberUtil
import com.google.i18n.phonenumbers.Phonenumber.PhoneNumber
import edu.oregonstate.mist.api.jsonapi.ResourceObject
import edu.oregonstate.mist.core.PersonObject
import org.skife.jdbi.v2.StatementContext
import org.skife.jdbi.v2.tweak.ResultSetMapper

import java.sql.ResultSet
import java.sql.SQLException

public class PersonMapper implements ResultSetMapper<ResourceObject> {

    static phoneNumberToE164(String phoneStr) {
        if (!phoneStr) {
            return null
        }
        PhoneNumberUtil phoneUtil = PhoneNumberUtil.getInstance()
        PhoneNumber phoneProto = phoneUtil.parse(phoneStr, 'US')
        phoneUtil.format(phoneProto, PhoneNumberUtil.PhoneNumberFormat.E164)
    }

    public ResourceObject map(int i, ResultSet rs, StatementContext sc) throws SQLException {
        new ResourceObject(
            id: rs.getString('OSU_ID'),
            type: 'jobs',
            attributes: new PersonObject(
                firstName: rs.getString('FIRST_NAME'),
                lastName: rs.getString('LAST_NAME'),
                middleName: rs.getString('MIDDLE_NAME'),
                birthDate: rs.getString('BIRTH_DATE'),
                primaryAffiliation: rs.getString('PRIMARY_AFFILIATION'),
                email: rs.getString('EMAIL_ADDRESS'),
                username: rs.getString('USERNAME'),
                osuUID: rs.getDouble('OSUUID'),
                confidential: rs.getBoolean('CONFIDENTIAL'),
                currentUser: rs.getBoolean('CURRENT_USER'),
                currentEmployee: rs.getBoolean('CURRENT_EMPLOYEE'),
                currentStudent: rs.getBoolean('CURRENT_STUDENT'),
                homePhone: phoneNumberToE164(rs.getString('HOME_PHONE')),
                alternatePhone: phoneNumberToE164(rs.getString('ALTERNATE_PHONE')),
                primaryPhone: phoneNumberToE164(rs.getString('PRIMARY_PHONE')),
                mobilePhone: phoneNumberToE164(rs.getString('MOBILE_PHONE'))
            ),
            links: ["self": rs.getString('OSU_ID')]
        )
    }
}

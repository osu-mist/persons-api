package edu.oregonstate.mist.personsapi.mapper

import com.google.i18n.phonenumbers.NumberParseException
import edu.oregonstate.mist.personsapi.core.PersonObject
import edu.oregonstate.mist.personsapi.core.Name

import edu.oregonstate.mist.personsapi.PhoneFormatter
import org.skife.jdbi.v2.StatementContext
import org.skife.jdbi.v2.tweak.ResultSetMapper

import java.sql.ResultSet
import java.sql.SQLException

public class PersonMapper implements ResultSetMapper<PersonObject> {
    PhoneFormatter phoneFormatter = new PhoneFormatter()
    public PersonObject map(int i, ResultSet rs, StatementContext sc) throws SQLException {
        String rawSSN = rs.getString('SSN')
        String ssnStatus = 'invalid'

        if (rawSSN?.startsWith('VAULT')) {
            ssnStatus = 'valut'
        } else if (rawSSN == 'REDACTED' || rawSSN == 'ARCHIVED') {
            ssnStatus = rawSSN.toLowerCase()
        } else if (rawSSN?.matches(/^(?!0{9})\d{9}$/)) {
            ssnStatus = 'valid'
        }

        new PersonObject (
            osuID: rs.getString('OSU_ID'),
            internalID: rs.getString('INTERNAL_ID'),
            name: new Name(
                    firstName: rs.getString('FIRST_NAME'),
                    middleName: rs.getString('MIDDLE_NAME'),
                    lastName: rs.getString('LAST_NAME')
            ),
            birthDate: rs.getDate('BIRTH_DATE'),
            email: rs.getString('EMAIL_ADDRESS'),
            username: rs.getString('USERNAME'),
            osuUID: rs.getString('OSUUID'),
            confidential: rs.getString('CONFIDENTIAL') == 'Y',
            currentStudent: rs.getString('CURRENT_STUDENT') == 'Y',
            currentEmployee: rs.getString('CURRENT_EMPLOYEE') == 'A',
            employeeStatus: rs.getString('CURRENT_EMPLOYEE'),
            homePhone: formatPhoneNumber(rs.getString('HOME_PHONE')),
            alternatePhone: formatPhoneNumber(rs.getString('ALTERNATE_PHONE')),
            primaryPhone: formatPhoneNumber(rs.getString('PRIMARY_PHONE')),
            mobilePhone: formatPhoneNumber(rs.getString('MOBILE_PHONE')),
            ssnStatus: ssnStatus
        )
    }

    /**
     * The backend datasource has some poorly entered phone numbers.
     * This method will return the unformatted phone number if it's unable to be formatted.
     * @param unformattedPhoneNumber
     * @return
     */
    private String formatPhoneNumber(String unformattedPhoneNumber) {
        String phoneNumber

        try {
            phoneNumber = phoneFormatter.toE164(unformattedPhoneNumber)
        } catch (NumberParseException e) {
            phoneNumber = unformattedPhoneNumber.trim()
        }

        phoneNumber
    }
}

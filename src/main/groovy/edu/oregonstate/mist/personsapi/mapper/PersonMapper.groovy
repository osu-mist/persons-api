package edu.oregonstate.mist.personsapi.mapper

import edu.oregonstate.mist.api.jsonapi.ResourceObject
import edu.oregonstate.mist.core.PersonAttributes
import org.skife.jdbi.v2.StatementContext
import org.skife.jdbi.v2.tweak.ResultSetMapper

import java.sql.ResultSet
import java.sql.SQLException

public class PersonMapper implements ResultSetMapper<ResourceObject> {
    public ResourceObject map(int i, ResultSet rs, StatementContext sc) throws SQLException {
        new ResourceObject(
            id: rs.getString('OSU_ID'),
            type: 'jobs',
            attributes: new PersonAttributes(
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
                homePhone: rs.getString('HOME_PHONE'),
                alternatePhone: rs.getString('ALTERNATE_PHONE'),
                primaryPhone: rs.getString('PRIMARY_PHONE'),
                mobilePhone: rs.getString('MOBILE_PHONE')
            ),
            links: ["self": rs.getString('OSU_ID')]
        )
    }
}

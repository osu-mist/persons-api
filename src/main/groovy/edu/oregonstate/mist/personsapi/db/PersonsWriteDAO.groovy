package edu.oregonstate.mist.personsapi.db

import edu.oregonstate.mist.contrib.AbstractPersonsDAO
import edu.oregonstate.mist.personsapi.core.JobObject
import edu.oregonstate.mist.personsapi.core.PersonObject
import org.skife.jdbi.v2.OutParameters
import org.skife.jdbi.v2.sqlobject.Bind
import org.skife.jdbi.v2.sqlobject.SqlCall
import org.skife.jdbi.v2.sqlobject.customizers.OutParameter

import java.sql.Types

public interface PersonsWriteDAO extends Closeable {
    static final String outParameter = "return_value"

    @SqlCall(AbstractPersonsDAO.personNewFunction)
    @OutParameter(name = "return_value", sqlType = Types.VARCHAR)
    OutParameters personNewFunction(@BindPerson PersonObject person)

    @SqlCall(AbstractPersonsDAO.studentNewFunction)
    @OutParameter(name = "return_value", sqlType = Types.VARCHAR)
    OutParameters createStudentJob(@Bind('osuID') String osuID,
                                   @BindJob JobObject job)

    @SqlCall(AbstractPersonsDAO.studentUpdateFunction)
    @OutParameter(name = "return_value", sqlType = Types.VARCHAR)
    OutParameters updateStudentJob(@Bind('osuID') String osuID,
                                   @BindJob JobObject job)

    @SqlCall(AbstractPersonsDAO.graduateNewFunction)
    @OutParameter(name = "return_value", sqlType = Types.VARCHAR)
    OutParameters createGraduateJob(@Bind('osuID') String osuID,
                                    @BindJob JobObject job)

    @SqlCall(AbstractPersonsDAO.graduateUpdateFunction)
    @OutParameter(name = "return_value", sqlType = Types.VARCHAR)
    OutParameters updateGraduateJob(@Bind('osuID') String osuID,
                                    @BindJob JobObject job)

    @SqlCall(AbstractPersonsDAO.updateSSN)
    void updateSSN(@Bind('pidm') String pidm,
                   @Bind('ssn') String ssn)
}

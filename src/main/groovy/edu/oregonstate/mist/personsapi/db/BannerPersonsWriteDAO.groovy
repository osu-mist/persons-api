package edu.oregonstate.mist.personsapi.db

import edu.oregonstate.mist.contrib.AbstractPersonsDAO
import edu.oregonstate.mist.personsapi.core.AddressObject
import edu.oregonstate.mist.personsapi.core.AddressRecordObject
import edu.oregonstate.mist.personsapi.core.JobObject
import edu.oregonstate.mist.personsapi.core.PersonObject
import edu.oregonstate.mist.personsapi.core.PhoneObject
import edu.oregonstate.mist.personsapi.core.PhoneRecordObject
import org.skife.jdbi.v2.OutParameters
import org.skife.jdbi.v2.sqlobject.Bind
import org.skife.jdbi.v2.sqlobject.SqlCall
import org.skife.jdbi.v2.sqlobject.customizers.OutParameter

import java.sql.Types

public interface BannerPersonsWriteDAO extends Closeable {
    static final String outParameter = "return_value"

    @SqlCall(AbstractPersonsDAO.personNewFunction)
    @OutParameter(name = "return_value", sqlType = Types.VARCHAR)
    OutParameters createPerson(@BindPerson PersonObject person)

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

    @SqlCall(AbstractPersonsDAO.createAddress)
    void createAddress(@Bind('pidm') String pidm,
                       @BindAddress AddressObject address)

    @SqlCall(AbstractPersonsDAO.deactivateAddress)
    void deactivateAddress(@Bind('pidm') String pidm,
                           @BindAddressRecord AddressRecordObject addressrecord)

    @SqlCall(AbstractPersonsDAO.reactivateAddress)
    void reactivateAddress(@Bind('pidm') String pidm,
                           @BindAddressRecord AddressRecordObject addressrecord)

    @SqlCall(AbstractPersonsDAO.createSSN)
    void createSSN(@Bind('pidm') String pidm,
                   @Bind('ssn') String ssn)

    @SqlCall(AbstractPersonsDAO.updateSSN)
    void updateSSN(@Bind('pidm') String pidm,
                   @Bind('ssn') String ssn)

    @SqlCall(AbstractPersonsDAO.deactivatePhone)
    void deactivatePhone(@Bind('pidm') String pidm,
                         @BindPhoneRecord PhoneRecordObject phoneRecord)

    @SqlCall(AbstractPersonsDAO.reactivatePhone)
    void reactivatePhone(@Bind('pidm') String pidm,
                         @BindPhoneRecord PhoneRecordObject phoneRecord)
}

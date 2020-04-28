package edu.oregonstate.mist.personsapi.db

import edu.oregonstate.mist.personsapi.core.AddressObject
import edu.oregonstate.mist.personsapi.core.AddressRecordObject
import edu.oregonstate.mist.personsapi.core.JobObject
import edu.oregonstate.mist.personsapi.core.LaborDistribution
import edu.oregonstate.mist.personsapi.core.PhoneObject
import edu.oregonstate.mist.personsapi.core.PhoneRecordObject
import edu.oregonstate.mist.personsapi.core.PreviousRecord
import edu.oregonstate.mist.personsapi.mapper.AddressMapper
import edu.oregonstate.mist.personsapi.mapper.AddressRecordMapper
import edu.oregonstate.mist.personsapi.mapper.ImageMapper
import edu.oregonstate.mist.personsapi.mapper.JobsMapper
import edu.oregonstate.mist.personsapi.mapper.LaborDistributionMapper
import edu.oregonstate.mist.personsapi.mapper.PhoneMapper
import edu.oregonstate.mist.personsapi.mapper.PhoneRecordMapper
import edu.oregonstate.mist.contrib.AbstractPersonsDAO
import edu.oregonstate.mist.personsapi.mapper.PreviousRecordMapper
import org.skife.jdbi.v2.sqlobject.Bind
import org.skife.jdbi.v2.sqlobject.SqlQuery
import org.skife.jdbi.v2.sqlobject.customizers.Mapper

import java.sql.Blob
import java.time.LocalDate

public interface BannerPersonsReadDAO extends Closeable {
    @SqlQuery(AbstractPersonsDAO.personExist)
    String personExist(@Bind('osuID') String osuID)

    @SqlQuery(AbstractPersonsDAO.hasSPBPERS)
    Boolean hasSPBPERS(@Bind('pidm') String pidm)

    @SqlQuery(AbstractPersonsDAO.ssnIsNotNull)
    String ssnIsNotNull(@Bind('osuID') String osuID)

    @SqlQuery(AbstractPersonsDAO.ssnStatus)
    String ssnStatus(@Bind('osuID') String osuID)

    @SqlQuery(AbstractPersonsDAO.validatePositionNumber)
    Boolean isValidPositionNumber(@Bind('positionNumber') String positionNumber,
                                  @Bind('jobBeginDate') LocalDate jobBeginDate)

    @SqlQuery(AbstractPersonsDAO.validateSupervisorPosition)
    Boolean isValidSupervisorPosition(@Bind('employeeBeginDate') LocalDate employeeBeginDate,
                                      @Bind('supervisorOsuID') String supervisorOsuID,
                                      @Bind('supervisorPositionNumber')
                                              String supervisorPositionNumber,
                                      @Bind('supervisorSuffix') String supervisorSuffix)

    // Validate location ID that represents a physical location a job is performed
    @SqlQuery(AbstractPersonsDAO.validateLocationID)
    Boolean isValidLocation(@Bind('locationID') String locationID)

    @SqlQuery(AbstractPersonsDAO.validateAccountIndexCode)
    Boolean isValidAccountIndexCode(@Bind('accountIndexCode') String accountIndexCode)

    @SqlQuery(AbstractPersonsDAO.validateAccountCode)
    Boolean isValidAccountCode(@Bind('accountCode') String accountCode)

    @SqlQuery(AbstractPersonsDAO.validateActivityCode)
    Boolean isValidActivityCode(@Bind('activityCode') String activityCode)

    @SqlQuery(AbstractPersonsDAO.validateOrganizationCode)
    Boolean isValidOrganizationCode(@Bind('organizationCode') String organizationCode)

    @SqlQuery(AbstractPersonsDAO.validateProgramCode)
    Boolean isValidProgramCode(@Bind('programCode') String programCode)

    @SqlQuery(AbstractPersonsDAO.validateFundCode)
    Boolean isValidFundCode(@Bind('fundCode') String fundCode)

    // Validate financial location codes for labor distributions
    @SqlQuery(AbstractPersonsDAO.validateLocationCode)
    Boolean isValidLocationCode(@Bind('locationCode') String locationCode)

    @SqlQuery(AbstractPersonsDAO.validateChangeReasonCode)
    Boolean isValidChangeReasonCode(@Bind('changeReasonCode') String changeReasonCode)

    @SqlQuery(AbstractPersonsDAO.getPreviousRecords)
    @Mapper(PreviousRecordMapper)
    List<PreviousRecord> getPreviousRecords(@Bind('id') String internalID)

    @SqlQuery(AbstractPersonsDAO.getJobsById)
    @Mapper(JobsMapper)
    List<JobObject> getJobsById(@Bind('osuID') String osuID,
                                @Bind('positionNumber') String positionNumber,
                                @Bind('suffix') String suffix)

    @SqlQuery(AbstractPersonsDAO.getJobLaborDistribution)
    @Mapper(LaborDistributionMapper)
    List<LaborDistribution> getJobLaborDistribution(@Bind('osuID') String osuID,
                                                    @Bind('positionNumber') String positionNumber,
                                                    @Bind('suffix') String suffix)

    @SqlQuery(AbstractPersonsDAO.getImageById)
    @Mapper(ImageMapper)
    Blob getImageById(@Bind('osuID') String osuID)

    @SqlQuery(AbstractPersonsDAO.getAddresses)
    @Mapper(AddressMapper)
    List<AddressObject> getAddresses(@Bind('osuID') String osuID,
                                     @Bind('addressType') String addressType)

    @SqlQuery(AbstractPersonsDAO.hasSameAddressType)
    @Mapper(AddressRecordMapper)
    List<AddressRecordObject> hasSameAddressType(@Bind('pidm') String pidm,
                                           @Bind('addressType') String addressType)

    @SqlQuery(AbstractPersonsDAO.validateAddressType)
    Boolean isValidAddressType(@Bind('addressType') String addressType)

    @SqlQuery(AbstractPersonsDAO.validateStateCode)
    Boolean isValidStateCode(@Bind('stateCode') String stateCode)

    @SqlQuery(AbstractPersonsDAO.validateCountyCode)
    Boolean isValidCountyCode(@Bind('countyCode') String countyCode)

    @SqlQuery(AbstractPersonsDAO.validateNationCode)
    Boolean isValidNationCode(@Bind('nationCode') String nationCode)

    @SqlQuery(AbstractPersonsDAO.validatePhoneType)
    Boolean isValidPhoneType(@Bind('phoneType') String phoneType)

    @SqlQuery(AbstractPersonsDAO.getPhones)
    @Mapper(PhoneMapper)
    List<PhoneObject> getPhones(@Bind('osuID') String osuID,
                                @Bind('addressType') String addressType,
                                @Bind('phoneType') String phoneType)

    @SqlQuery(AbstractPersonsDAO.hasSamePhoneType)
    @Mapper(PhoneRecordMapper)
    List<PhoneRecordObject> hasSamePhoneType(@Bind('pidm') String pidm,
                                       @Bind('phoneType') String phoneType)

    @SqlQuery(AbstractPersonsDAO.phoneHasSameAddressType)
    @Mapper(PhoneRecordMapper)
    List<PhoneRecordObject> phoneHasSameAddressType(@Bind('pidm') String pidm,
                                              @Bind('addressType') String addressType)
}

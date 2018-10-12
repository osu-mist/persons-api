package edu.oregonstate.mist.personsapi.db

import edu.oregonstate.mist.personsapi.core.MealPlan
import edu.oregonstate.mist.personsapi.core.JobObject
import edu.oregonstate.mist.personsapi.core.LaborDistribution
import edu.oregonstate.mist.personsapi.core.PersonObject
import edu.oregonstate.mist.personsapi.core.PreviousRecord
import edu.oregonstate.mist.personsapi.mapper.MealPlanMapper
import edu.oregonstate.mist.personsapi.mapper.ImageMapper
import edu.oregonstate.mist.personsapi.mapper.JobsMapper
import edu.oregonstate.mist.personsapi.mapper.LaborDistributionMapper
import edu.oregonstate.mist.personsapi.mapper.PersonMapper
import edu.oregonstate.mist.contrib.AbstractPersonsDAO
import edu.oregonstate.mist.personsapi.mapper.PreviousRecordMapper
import org.skife.jdbi.v2.sqlobject.Bind
import org.skife.jdbi.v2.sqlobject.SqlQuery
import org.skife.jdbi.v2.sqlobject.customizers.Mapper
import org.skife.jdbi.v2.unstable.BindIn

import java.sql.Blob

public interface PersonsDAO extends Closeable {
    @SqlQuery(AbstractPersonsDAO.personExist)
    String personExist(@Bind('osuID') String osuID)

    @SqlQuery(AbstractPersonsDAO.validatePositionNumber)
    Boolean isValidPositionNumber(@Bind('positionNumber') String positionNumber,
                                  @Bind('jobBeginDate') Date jobBeginDate)

    @SqlQuery(AbstractPersonsDAO.validateSupervisorPosition)
    Boolean isValidSupervisorPosition(@Bind('employeeBeginDate') Date employeeBeginDate,
                                      @Bind('supervisorOsuID') String supervisorOsuID,
                                      @Bind('supervisorPositionNumber')
                                              String supervisorPositionNumber,
                                      @Bind('supervisorSuffix') String supervisorSuffix)

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

    @SqlQuery(AbstractPersonsDAO.getPersons)
    @Mapper(PersonMapper)
    List<PersonObject> getPersons(@Bind('onid') String onid,
                                  @BindIn(value = 'osuIDs',
                                          onEmpty = BindIn.EmptyHandling.NULL) List<String> osuIDs,
                                  @Bind('osuUID') String osuUID,
                                  @Bind('firstName') String firstName,
                                  @Bind('lastName') String lastName,
                                  @Bind('searchOldVersions') Boolean searchOldVersions)

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

    @SqlQuery(AbstractPersonsDAO.getMealPlans)
    @Mapper(MealPlanMapper)
    List<MealPlan> getMealPlans(@Bind('osuID') String osuID,
                                @Bind('mealPlanID') String mealPlanID)
}

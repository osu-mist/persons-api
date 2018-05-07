package edu.oregonstate.mist.personsapi.db

import edu.oregonstate.mist.personsapi.core.JobObject
import edu.oregonstate.mist.personsapi.core.LaborDistribution
import edu.oregonstate.mist.personsapi.core.PersonObject
import edu.oregonstate.mist.personsapi.core.PreviousRecord
import edu.oregonstate.mist.personsapi.mapper.ImageMapper
import edu.oregonstate.mist.personsapi.mapper.JobsMapper
import edu.oregonstate.mist.personsapi.mapper.LaborDistributionMapper
import edu.oregonstate.mist.personsapi.mapper.PersonMapper
import edu.oregonstate.mist.contrib.AbstractPersonsDAO
import edu.oregonstate.mist.personsapi.mapper.PreviousRecordMapper
import org.skife.jdbi.v2.sqlobject.Bind
import org.skife.jdbi.v2.sqlobject.SqlQuery
import org.skife.jdbi.v2.sqlobject.customizers.Mapper

import java.sql.Blob

public interface PersonsDAO extends Closeable {
    @SqlQuery(AbstractPersonsDAO.personExist)
    String personExist(@Bind('osuID') String osuID)

    @SqlQuery(AbstractPersonsDAO.getPersons)
    @Mapper(PersonMapper)
    List<PersonObject> getPersons(@Bind('onid') String onid,
                                  @Bind('osuID') String osuID,
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
}

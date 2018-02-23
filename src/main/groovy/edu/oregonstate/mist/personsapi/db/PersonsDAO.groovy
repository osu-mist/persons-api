package edu.oregonstate.mist.personsapi.db

import edu.oregonstate.mist.core.JobObject
import edu.oregonstate.mist.core.PersonObject
import edu.oregonstate.mist.personsapi.mapper.JobsMapper
import edu.oregonstate.mist.personsapi.mapper.PersonMapper
import edu.oregonstate.mist.contrib.AbstractPersonsDAO
import org.skife.jdbi.v2.sqlobject.Bind
import org.skife.jdbi.v2.sqlobject.SqlQuery
import org.skife.jdbi.v2.sqlobject.customizers.Mapper

public interface PersonsDAO extends Closeable {
    @SqlQuery(AbstractPersonsDAO.getPersons)
    @Mapper(PersonMapper)
    PersonObject getPersons(@Bind('id') id)

    @SqlQuery(AbstractPersonsDAO.getPersonById)
    @Mapper(PersonMapper)
    PersonObject getPersonById(@Bind('osuID') String osuID)

    @SqlQuery(AbstractPersonsDAO.getJobsById)
    @Mapper(JobsMapper)
    List<JobObject> getJobsById(@Bind('osuID') String osuID)
}

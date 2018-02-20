package edu.oregonstate.mist.personsapi

import edu.oregonstate.mist.personsapi.PersonMapper
import edu.oregonstate.mist.api.jsonapi.ResourceObject
import edu.oregonstate.mist.contrib.AbstractPersonsDAO
import org.skife.jdbi.v2.sqlobject.Bind
import org.skife.jdbi.v2.sqlobject.SqlQuery
import org.skife.jdbi.v2.sqlobject.customizers.Mapper

public interface PersonsDAO extends Closeable {
    @SqlQuery("SELECT 1 FROM dual")
    Integer checkHealth()

    @SqlQuery(AbstractPersonsDAO.getPersonById)
    @Mapper(PersonMapper)
    ResourceObject getPersonById(@Bind('osu_id') String osu_id)
}

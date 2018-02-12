package edu.oregonstate.mist.personsapi

import org.skife.jdbi.v2.sqlobject.SqlQuery

public interface PersonsDAO extends Closeable {
    @SqlQuery("SELECT 1 FROM dual")
    Integer checkHealth()
}

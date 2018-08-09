package edu.oregonstate.mist.personsapi.db

import edu.oregonstate.mist.contrib.AbstractPersonsDAO
import edu.oregonstate.mist.personsapi.core.JobObject
import org.skife.jdbi.v2.OutParameters
import org.skife.jdbi.v2.sqlobject.Bind
import org.skife.jdbi.v2.sqlobject.SqlCall
import org.skife.jdbi.v2.sqlobject.customizers.OutParameter

import java.sql.Types

public interface PersonsWriteDAO extends Closeable {
    @SqlCall(AbstractPersonsDAO.createJobFunction)
    @OutParameter(name = "return_value", sqlType = Types.VARCHAR)
    OutParameters createJob(@Bind('osuID') String osuID,
                            @BindJob JobObject job)
}

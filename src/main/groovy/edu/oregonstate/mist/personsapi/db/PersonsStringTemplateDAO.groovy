package edu.oregonstate.mist.personsapi.db

import edu.oregonstate.mist.contrib.AbstractPersonsDAO
import edu.oregonstate.mist.personsapi.core.PersonObject
import edu.oregonstate.mist.personsapi.mapper.PersonMapper
import org.skife.jdbi.v2.sqlobject.Bind
import org.skife.jdbi.v2.sqlobject.SqlQuery
import org.skife.jdbi.v2.sqlobject.customizers.Mapper
import org.skife.jdbi.v2.sqlobject.stringtemplate.UseStringTemplate3StatementLocator
import org.skife.jdbi.v2.unstable.BindIn

/**
 * getPersons uses the @BindIn annotation, which requires a special annotation on the class to
 * tell JDBI to use string templates. Lists/arrays/collections are bound with the syntax <variable>
 * instead of :variable
 */
@UseStringTemplate3StatementLocator
public interface PersonsStringTemplateDAO extends Closeable {
    @SqlQuery(AbstractPersonsDAO.getPersons)
    @Mapper(PersonMapper)
    List<PersonObject> getPersons(@Bind('onid') String onid,
                                  @BindIn(value = 'osuIDs',
                                          onEmpty = BindIn.EmptyHandling.NULL) List<String> osuIDs,
                                  @Bind('osuUID') String osuUID,
                                  @Bind('firstName') String firstName,
                                  @Bind('lastName') String lastName,
                                  @Bind('searchOldVersions') Boolean searchOldVersions)
}

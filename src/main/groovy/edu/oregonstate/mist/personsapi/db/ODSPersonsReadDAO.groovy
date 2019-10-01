package edu.oregonstate.mist.personsapi.db

import edu.oregonstate.mist.contrib.AbstractPersonsDAO
import edu.oregonstate.mist.personsapi.core.MealPlan
import edu.oregonstate.mist.personsapi.mapper.MealPlanMapper
import org.skife.jdbi.v2.sqlobject.Bind
import org.skife.jdbi.v2.sqlobject.SqlQuery
import org.skife.jdbi.v2.sqlobject.customizers.Mapper

public interface ODSPersonsReadDAO extends Closeable {
    @SqlQuery(AbstractPersonsDAO.personExist)
    String personExist(@Bind('osuID') String osuID)

    @SqlQuery(AbstractPersonsDAO.getMealPlans)
    @Mapper(MealPlanMapper)
    List<MealPlan> getMealPlans(@Bind('osuID') String osuID,
                                @Bind('mealPlanID') String mealPlanID)

}

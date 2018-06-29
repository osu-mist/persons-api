package edu.oregonstate.mist.personsapi.mapper

import edu.oregonstate.mist.personsapi.core.MealPlan
import org.skife.jdbi.v2.StatementContext
import org.skife.jdbi.v2.tweak.ResultSetMapper

import java.sql.ResultSet
import java.sql.SQLException

public class MealPlanMapper implements ResultSetMapper<MealPlan> {
    public MealPlan map(int i, ResultSet rs, StatementContext sc) throws SQLException {
        new MealPlan(
                mealPlanID: rs.getString("MEALPLAN_ID"),
                mealPlan: getMealPlan(rs.getString("MEALPLAN_DESC")),
                balance: rs.getBigDecimal("BALANCE"),
                lastUsedDate: rs.getTimestamp("LAST_USED_DATE")?.toInstant(),
                lastUsedPlace: rs.getString("LAST_USED_PLACE")?.replace("_", " ")
        )
    }

    private static String getMealPlan(String dbMealPlan) {
        if (dbMealPlan == "!OrangeCash") {
            "Orange Cash"
        } else {
            dbMealPlan
        }
    }
}

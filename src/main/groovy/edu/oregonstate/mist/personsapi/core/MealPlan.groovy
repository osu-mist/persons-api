package edu.oregonstate.mist.personsapi.core

import com.fasterxml.jackson.annotation.JsonFormat
import com.fasterxml.jackson.annotation.JsonIgnore

import java.time.Instant

class MealPlan {
    @JsonIgnore
    String mealPlanID

    String mealPlan
    BigDecimal balance

    @JsonFormat(shape=JsonFormat.Shape.STRING, timezone="UTC")
    Instant lastUsedDate
    String lastUsedPlace
}

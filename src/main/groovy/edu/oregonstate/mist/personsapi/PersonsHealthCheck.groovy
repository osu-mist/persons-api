package edu.oregonstate.mist.personsapi

import com.codahale.metrics.health.HealthCheck
import com.codahale.metrics.health.HealthCheck.Result

class PersonsHealthCheck extends HealthCheck {
    private PersonsDAO personsDAO

    PersonsHealthCheck(PersonsDAO personsDAO) {
        this.personsDAO = personsDAO
    }

    @Override
    protected Result check() {
        try {
            String status = personsDAO.checkHealth()

            if (status != null) {
                return Result.healthy()
            }
            Result.unhealthy("status: ${status}")
        } catch(Exception e) {
            Result.unhealthy(e.message)
        }
    }
}

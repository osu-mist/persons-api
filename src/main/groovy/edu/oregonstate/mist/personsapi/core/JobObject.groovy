package edu.oregonstate.mist.personsapi.core

import com.fasterxml.jackson.annotation.JsonFormat

class JobObject {
    @JsonFormat(shape=JsonFormat.Shape.STRING, pattern="yyyy-MM-dd", timezone="UTC")
    Date beginDate

    @JsonFormat(shape=JsonFormat.Shape.STRING, pattern="yyyy-MM-dd", timezone="UTC")
    Date endDate

    String positionNumber
    String status
    String description
    Float fte
}

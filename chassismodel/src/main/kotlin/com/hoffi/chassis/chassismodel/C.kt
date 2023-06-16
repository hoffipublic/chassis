package com.hoffi.chassis.chassismodel

import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import java.util.*

object C {
    val LOCALE = Locale.getDefault()
    val NULLSTRING = "<NULL>"
    val DEFAULTSTRING = "<DEFAULT>"
    val DEFAULT = "default"

    val DEFAULT_INT = -1
    val DEFAULT_LONG = -1L
    val DEFAULT_STRING_DUMMY = "<Dummy>"
    //val DEFAULT_UUID = Uuid.fromString("00000000-0000-0000-0000-000000000001")
    val DEFAULT_INSTANT = Instant.fromEpochMilliseconds(1L)
    val DEFAULT_LOCALDATETIME = DEFAULT_INSTANT.toLocalDateTime(TimeZone.UTC)
    val DEFAULT_LOCALDATETIME_DB = DEFAULT_LOCALDATETIME

    val DEFAULT_VARCHAR_LENGTH = 512

    val DEFAULT_USER = "<System>"
    val DEFAULT_OPTIMISTIC_LOCK_ID = 0

}

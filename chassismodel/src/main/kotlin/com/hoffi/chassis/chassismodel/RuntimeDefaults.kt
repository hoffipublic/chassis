package com.hoffi.chassis.chassismodel

import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.MemberName
import com.squareup.kotlinpoet.MemberName.Companion.member
import com.squareup.kotlinpoet.asTypeName
import com.squareup.kotlinpoet.buildCodeBlock
import okio.Path.Companion.toPath

object RuntimeDefaults {
    val UNIVERSE__BASEDIR = "../generated/examples/src/main/kotlin/".toPath()
    const val UNIVERSE___PACKAGE = "com.hoffi.generated.universe"
    const val UNIVERSE___MODELGROUP = "Universe"
    const val UNIVERSE___DEFAULTS = "Defaults"
    const val UNIVERSE___GENERATED = "WasGenerated"

    val WAS_GENERATED_INTERFACE_ClassName = ClassName(UNIVERSE___PACKAGE, UNIVERSE___GENERATED)
    val ANNOTATION_DTO_CLASSNAME = ClassName(UNIVERSE___PACKAGE, "TABLEsDTO")
    val ANNOTATION_TABLE_CLASSNAME = ClassName(UNIVERSE___PACKAGE, "DTOsTABLE")
    val UUIDTABLE_CLASSNAME = ClassName(UNIVERSE___PACKAGE, "UuidTable")
    val UUIDDTO_INTERFACE_CLASSNAME = ClassName(UNIVERSE___PACKAGE, "IUuidDto")
    val UUID_PROPNAME = "uuid"
    val ANNOTATION_FKFROM_CLASSNAME = ClassName(UNIVERSE___PACKAGE, "FKFROM")

    val DEFAULTS_CLASSNAME = ClassName(UNIVERSE___PACKAGE, UNIVERSE___DEFAULTS)
    val DEFAULT_MEMBER_INT =              DEFAULTS_CLASSNAME.member("DEFAULT_INT")
    val DEFAULT_MEMBER_LONG =             DEFAULTS_CLASSNAME.member("DEFAULT_LONG")
    val DEFAULT_MEMBER_STRING =           DEFAULTS_CLASSNAME.member("DEFAULT_STRING")
    val DEFAULT_MEMBER_UUID =             DEFAULTS_CLASSNAME.member("DEFAULT_UUID")
    val DEFAULT_MEMBER_INSTANT =          DEFAULTS_CLASSNAME.member("DEFAULT_INSTANT")
    val DEFAULT_MEMBER_LOCALDATETIME =    DEFAULTS_CLASSNAME.member("DEFAULT_LOCALDATETIME")
    val DEFAULT_MEMBER_LOCALDATETIME_DB = DEFAULTS_CLASSNAME.member("DEFAULT_LOCALDATETIME_DB")
    val NULL_MEMBER_INT =              DEFAULTS_CLASSNAME.member("NULL_INT")
    val NULL_MEMBER_LONG =             DEFAULTS_CLASSNAME.member("NULL_LONG")
    val NULL_MEMBER_STRING =           DEFAULTS_CLASSNAME.member("NULL_STRING")
    val NULL_MEMBER_UUID =             DEFAULTS_CLASSNAME.member("NULL_UUID")
    val NULL_MEMBER_INSTANT =          DEFAULTS_CLASSNAME.member("NULL_INSTANT")
    val NULL_MEMBER_LOCALDATETIME =    DEFAULTS_CLASSNAME.member("NULL_LOCALDATETIME")

    val classNameUUID = ClassName("java.util", "UUID")
    val classNameUUID_randomUUID = classNameUUID.member("randomUUID")
    val classNameTimeZone = ClassName("kotlinx.datetime", "TimeZone")
    val classNameInstant = ClassName("kotlinx.datetime", "Instant")
    val classNameInstant_toLocalDateTime = MemberName("kotlinx.datetime","toLocalDateTime") // extension Function
    val classNameLocalDateTime = ClassName("kotlinx.datetime", "LocalDateTime")
    val classNameClockSystem = ClassName("kotlinx.datetime", "Clock", "System")
    val classNameClockSystem_now = classNameClockSystem.member("now")

    val classNameSerializable = ClassName("kotlinx.serialization", "Serializable")
    val DEFAULT_INITCODE_UUID =          buildCodeBlock { add("%T.%M()", classNameUUID, classNameUUID_randomUUID) }
    val DEFAULT_INITCODE_INSTANT =       buildCodeBlock { add("%T.%M()", classNameClockSystem, classNameClockSystem_now) }
    val DEFAULT_INITCODE_LOCALDATETIME = buildCodeBlock { add("%T.%M().%M(%T.UTC)", classNameClockSystem, classNameClockSystem_now, classNameInstant_toLocalDateTime, classNameTimeZone) }

    val DEFAULT_INITIALIZER_DUMMY = Initializer.of("%T()", Any::class.asTypeName())
    val DEFAULT_INITIALIZER_INT = Initializer.of("%M", DEFAULT_MEMBER_INT)
    val DEFAULT_INITIALIZER_LONG = Initializer.of("%M", DEFAULT_MEMBER_LONG)
    val DEFAULT_INITIALIZER_STRING = Initializer.of("%M", DEFAULT_MEMBER_STRING)
    val DEFAULT_INITIALIZER_BOOL = Initializer.of("%L", false)
    val DEFAULT_INITIALIZER_UUID = Initializer.of("%M", DEFAULT_MEMBER_UUID)
    val DEFAULT_INITIALIZER_INSTANT = Initializer.of("%M", DEFAULT_MEMBER_INSTANT)
    val DEFAULT_INITIALIZER_LOCALDATETIME = Initializer.of("%M", DEFAULT_MEMBER_LOCALDATETIME)
    val NULL_INITIALIZER_INT = Initializer.of("%M", NULL_MEMBER_INT)
    val NULL_INITIALIZER_LONG = Initializer.of("%M", NULL_MEMBER_LONG)
    val NULL_INITIALIZER_STRING = Initializer.of("%M", NULL_MEMBER_STRING)
    val NULL_INITIALIZER_BOOL = Initializer.of("%L", false)
    val NULL_INITIALIZER_UUID = Initializer.of("%M", NULL_MEMBER_UUID)
    val NULL_INITIALIZER_INSTANT = Initializer.of("%M", NULL_MEMBER_INSTANT)
    val NULL_INITIALIZER_LOCALDATETIME = Initializer.of("%M", NULL_MEMBER_LOCALDATETIME)

    // kotlin exposed types from kotlinx-datetime are extension functions and not part of the superClass "DslTable"
    val DB_MEMBER_timestamp = MemberName("org.jetbrains.exposed.sql.kotlin.datetime", "timestamp")
    val DB_MEMBER_datetime =  MemberName("org.jetbrains.exposed.sql.kotlin.datetime", "datetime")
}

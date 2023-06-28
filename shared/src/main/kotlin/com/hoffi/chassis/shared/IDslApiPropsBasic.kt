package com.hoffi.chassis.shared

import com.hoffi.chassis.chassismodel.C
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.asClassName
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlin.reflect.KClass

@JvmInline value class Mutable(val bool: Boolean)
val immutable = Mutable(false)
val mutable = Mutable(true)

sealed class COLLECTIONTYPE {
    override fun toString() = this::class.simpleName!!
    class NONE : COLLECTIONTYPE()
    class LIST : COLLECTIONTYPE()
    class SET : COLLECTIONTYPE()
    class COLLECTION : COLLECTIONTYPE()
    class ITERABLE : COLLECTIONTYPE()
    companion object {
        val NONE = NONE()
        val LIST = LIST()
        val SET = SET()
        val COLLECTION = COLLECTION()
        val ITERABLE = ITERABLE()
    }
}

sealed class TYP(val poetType: ClassName) {
    override fun toString() = this::class.simpleName!!
    class CLASS : TYP(KClass::class.asClassName())
    class MODEL : TYP(Any::class.asClassName())
    class INT : TYP(Integer::class.asClassName())
    class LONG : TYP(Long::class.asClassName())
    class STRING : TYP(String::class.asClassName())
    class BOOL : TYP(Boolean::class.asClassName())
    class UUID : TYP(java.util.UUID::class.asClassName())
    class INSTANT : TYP(Instant::class.asClassName())
    class LOCALDATETIME : TYP(LocalDateTime::class.asClassName())
    companion object {
        val DEFAULT = STRING()
        val CLASS = CLASS()
        val MODEL = MODEL()
        val INT = INT()
        val LONG = LONG()
        val STRING = STRING()
        val BOOL = BOOL()
        val UUID = UUID()
        val INSTANT = INSTANT()
        val LOCALDATETIME = LOCALDATETIME()
        val DEFAULT_INT = -1
        val DEFAULT_LONG = -1L
        val DEFAULT_STRING = C.DEFAULTSTRING
        val DEFAULT_STRING_DUMMY = C.DEFAULT
        val DEFAULT_UUID = java.util.UUID.fromString("00000000-0000-0000-0000-000000000001")
        val DEFAULT_INSTANT = Instant.fromEpochMilliseconds(1L)
        val DEFAULT_LOCALDATETIME = DEFAULT_INSTANT.toLocalDateTime(TimeZone.UTC)
        val DEFAULT_LOCALDATETIME_DB = DEFAULT_LOCALDATETIME
        val DEFAULT_VARCHAR_LENGTH = 512
        val DEFAULT_USER = "<System>"
        val DEFAULT_OPTIMISTIC_LOCK_ID = 0

    }
}

class Initializer private constructor(var format: String, val args: MutableList<Any> = mutableListOf()) {
    override fun toString() = "\"$format\"->${args.joinToString()}"
    constructor(format: String, arg: Any, vararg args: Any) : this(format, mutableListOf(arg, *args))
    fun isEmpty() = format.isBlank()
    fun isNotEmpty() = !format.isBlank()
    companion object {
        val EMPTY = Initializer("")
        fun of(format: String, vararg args: Any) = Initializer(format, args)
        fun of(format: String, args: List<Any>) = Initializer(format, args.toTypedArray())
    }
    fun copy(format: String = this.format, args: MutableList<Any> = this.args) = Initializer(format, args)
}

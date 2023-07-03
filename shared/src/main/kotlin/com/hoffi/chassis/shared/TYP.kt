package com.hoffi.chassis.shared

import com.hoffi.chassis.chassismodel.C
import com.hoffi.chassis.shared.fix.RuntimeDefaults
import com.hoffi.chassis.shared.shared.Initializer
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import com.squareup.kotlinpoet.TypeName
import com.squareup.kotlinpoet.asClassName
import com.squareup.kotlinpoet.asTypeName
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

@JvmInline value class Mutable(val bool: Boolean)
val immutable = Mutable(false)
val mutable = Mutable(true)

sealed class COLLECTIONTYP {
    override fun toString() = this::class.simpleName!!
    class NONE : COLLECTIONTYP()
    class LIST : COLLECTIONTYP()
    class SET : COLLECTIONTYP()
    class COLLECTION : COLLECTIONTYP()
    class ITERABLE : COLLECTIONTYP()
    companion object {
        val NONE = NONE()
        val LIST = LIST()
        val SET = SET()
        val COLLECTION = COLLECTION()
        val ITERABLE = ITERABLE()
    }
}

sealed class TYP(val poetType: ClassName, val defaultInitializer: Initializer) {
    override fun toString() = this::class.simpleName!!

    //class CLASS : TYP(KClass::class.asClassName(), RuntimeDefaults.DEFAULT_INITIALIZER_CLASS)
    //class MODEL : TYP(Any::class.asClassName(), RuntimeDefaults.DEFAULT_INITIALIZER_MODEL)
    class INT : TYP(Integer::class.asClassName(), RuntimeDefaults.DEFAULT_INITIALIZER_INT)
    class LONG : TYP(Long::class.asClassName(), RuntimeDefaults.DEFAULT_INITIALIZER_LOCALDATETIME)
    class STRING : TYP(String::class.asClassName(), RuntimeDefaults.DEFAULT_INITIALIZER_STRING)
    class BOOL : TYP(Boolean::class.asClassName(), RuntimeDefaults.DEFAULT_INITIALIZER_BOOL)
    class UUID : TYP(java.util.UUID::class.asClassName(), RuntimeDefaults.DEFAULT_INITIALIZER_UUID)
    class INSTANT : TYP(Instant::class.asClassName(), RuntimeDefaults.DEFAULT_INITIALIZER_INSTANT)
    class LOCALDATETIME : TYP(LocalDateTime::class.asClassName(), RuntimeDefaults.DEFAULT_INITIALIZER_LOCALDATETIME)
    companion object {
        val DEFAULT = STRING()
        //val CLASS = CLASS()
        //val MODEL = MODEL()
        val INT = INT()
        val LONG = LONG()
        val STRING = STRING()
        val BOOL = BOOL()
        val UUID = UUID()
        val INSTANT = INSTANT()
        val LOCALDATETIME = LOCALDATETIME()
        val DEFAULT_INT = C.DEFAULT_INT
        val DEFAULT_LONG = C.DEFAULT_LONG
        val DEFAULT_STRING = C.DEFAULTSTRING
        val DEFAULT_STRING_DUMMY = C.DEFAULT
        val DEFAULT_UUID = C.DEFAULT_UUID
        val DEFAULT_INSTANT = C.DEFAULT_INSTANT
        val DEFAULT_LOCALDATETIME = DEFAULT_INSTANT.toLocalDateTime(TimeZone.UTC)
        val DEFAULT_LOCALDATETIME_DB = DEFAULT_LOCALDATETIME
        val DEFAULT_VARCHAR_LENGTH = C.DEFAULT_VARCHAR_LENGTH
        val DEFAULT_USER = C.DEFAULT_USER
        val DEFAULT_OPTIMISTIC_LOCK_ID = C.DEFAULT_OPTIMISTIC_LOCK_ID
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is TYP) return false
        return poetType == other.poetType
    }
    override fun hashCode() = poetType.hashCode()
}

data class TypWrapper(val typeName: TypeName, val initializer: Initializer) {
    companion object {
        fun of(COLLECTIONTYP: COLLECTIONTYP, mutable: Mutable, genericType: TypeName) =
            when (COLLECTIONTYP) {
                is COLLECTIONTYP.LIST ->       if (mutable.bool) TypWrapper(ClassName("kotlin.collections", "MutableList").parameterizedBy(genericType), Initializer.of("mutableListOf()", "")) else TypWrapper(List::class      .asTypeName().parameterizedBy(genericType), Initializer.of("listOf()", ""))
                is COLLECTIONTYP.SET ->        if (mutable.bool) TypWrapper(ClassName("kotlin.collections", "MutableSet") .parameterizedBy(genericType), Initializer.of("mutableSetOf()", ""))  else TypWrapper(Set::class       .asTypeName().parameterizedBy(genericType), Initializer.of("setOf()", ""))
                is COLLECTIONTYP.COLLECTION -> if (mutable.bool) TypWrapper(ClassName("kotlin.collections", "MutableList").parameterizedBy(genericType), Initializer.of("mutableListOf()", "")) else TypWrapper(Collection::class.asTypeName().parameterizedBy(genericType), Initializer.of("listOf()", ""))
                is COLLECTIONTYP.ITERABLE ->   if (mutable.bool) TypWrapper(ClassName("kotlin.collections", "MutableList").parameterizedBy(genericType), Initializer.of("mutableListOf()", "")) else TypWrapper(Iterable::class  .asTypeName().parameterizedBy(genericType), Initializer.of("listOf()", ""))
                is COLLECTIONTYP.NONE -> TODO()
            }
    }
}

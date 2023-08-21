package com.hoffi.chassis.chassismodel.typ

import com.hoffi.chassis.chassismodel.C
import com.hoffi.chassis.chassismodel.Initializer
import com.hoffi.chassis.chassismodel.PoetHelpers.nullable
import com.hoffi.chassis.chassismodel.RuntimeDefaults
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.ParameterizedTypeName
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import com.squareup.kotlinpoet.TypeName
import com.squareup.kotlinpoet.asTypeName
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlin.reflect.KClass

@JvmInline value class Mutable(val bool: Boolean)
val immutable = Mutable(false)
val mutable = Mutable(true)

sealed class COLLECTIONTYP {
    override fun toString() = when (this) { is NONE -> "1To1" else -> this::class.simpleName!! }
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

data class TYPTranslation(val typeName: ParameterizedTypeName, val initializer: Initializer)

sealed class TYP(val kClass: KClass<*>, val defaultInitializer: Initializer, val defaultNull: Initializer) {
    override fun toString() = this::class.simpleName!!
    val isInterface = kClass.java.isInterface
    //val kType = kClass.createType()
    val poetType = kClass.asTypeName()

    //class CLASS : TYP(KClass::class.asClassName(), RuntimeDefaults.DEFAULT_INITIALIZER_CLASS)
    //class MODEL : TYP(Any::class.asClassName(), RuntimeDefaults.DEFAULT_INITIALIZER_MODEL)
    class INT : TYP(Integer::class, RuntimeDefaults.DEFAULT_INITIALIZER_INT, RuntimeDefaults.NULL_INITIALIZER_INT)
    class LONG : TYP(Long::class, RuntimeDefaults.DEFAULT_INITIALIZER_LONG, RuntimeDefaults.NULL_INITIALIZER_LONG)
    class STRING : TYP(String::class, RuntimeDefaults.DEFAULT_INITIALIZER_STRING, RuntimeDefaults.NULL_INITIALIZER_STRING)
    class BOOL : TYP(Boolean::class, RuntimeDefaults.DEFAULT_INITIALIZER_BOOL, RuntimeDefaults.NULL_INITIALIZER_BOOL)
    class UUID : TYP(java.util.UUID::class, RuntimeDefaults.DEFAULT_INITIALIZER_UUID, RuntimeDefaults.NULL_INITIALIZER_UUID)
    class INSTANT : TYP(Instant::class, RuntimeDefaults.DEFAULT_INITIALIZER_INSTANT, RuntimeDefaults.NULL_INITIALIZER_INSTANT)
    class LOCALDATETIME : TYP(LocalDateTime::class, RuntimeDefaults.DEFAULT_INITIALIZER_LOCALDATETIME, RuntimeDefaults.NULL_INITIALIZER_LOCALDATETIME)
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
        val NULL_INT = -2
        val NULL_LONG = -2L
        val NULL_STRING = "<NULL>"
        val NULL_BOOL = false
        val NULL_UUID = java.util.UUID.fromString("00000000-0000-0000-0000-000000000002")
        val NULL_INSTANT = Instant.fromEpochMilliseconds(2L)
        val NULL_LOCALDATETIME = NULL_INSTANT.toLocalDateTime(TimeZone.UTC)
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is TYP) return false
        return poetType == other.poetType
    }
    override fun hashCode() = poetType.hashCode()
}

class CollectionTypWrapper private constructor(val typeName: TypeName, val initializer: Initializer) {
    override fun toString() = "${this::class.simpleName}($typeName, $initializer)"

    companion object {
        fun of(COLLECTIONTYP: COLLECTIONTYP, mutable: Mutable, isNullable: Boolean, genericType: TypeName): CollectionTypWrapper {
            val classNameAndInitializerPair = when (COLLECTIONTYP) {
                is COLLECTIONTYP.LIST ->       if (mutable.bool) Pair(ClassName("kotlin.collections", "MutableList").parameterizedBy(genericType), Initializer.of("mutableListOf()", "")) else Pair(List::class      .asTypeName().parameterizedBy(genericType), Initializer.of("listOf()", ""))
                is COLLECTIONTYP.SET ->        if (mutable.bool) Pair(ClassName("kotlin.collections", "MutableSet") .parameterizedBy(genericType), Initializer.of("mutableSetOf()", ""))  else Pair(Set::class       .asTypeName().parameterizedBy(genericType), Initializer.of("setOf()", ""))
                is COLLECTIONTYP.COLLECTION -> if (mutable.bool) Pair(ClassName("kotlin.collections", "MutableList").parameterizedBy(genericType), Initializer.of("mutableListOf()", "")) else Pair(Collection::class.asTypeName().parameterizedBy(genericType), Initializer.of("listOf()", ""))
                is COLLECTIONTYP.ITERABLE ->   if (mutable.bool) Pair(ClassName("kotlin.collections", "MutableList").parameterizedBy(genericType), Initializer.of("mutableListOf()", "")) else Pair(Iterable::class  .asTypeName().parameterizedBy(genericType), Initializer.of("listOf()", ""))
                is COLLECTIONTYP.NONE -> TODO()
            }
            return if (isNullable) {
                CollectionTypWrapper(classNameAndInitializerPair.first.nullable(), classNameAndInitializerPair.second)
            } else {
                CollectionTypWrapper(classNameAndInitializerPair.first, classNameAndInitializerPair.second)
            }
        }
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is CollectionTypWrapper) return false
        if (typeName != other.typeName) return false
        return initializer == other.initializer
    }
    override fun hashCode() = 31 * typeName.hashCode() + initializer.hashCode()
}

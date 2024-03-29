package com.hoffi.chassis.dsl.internal

import kotlin.reflect.KClass

/** DSL Contributing funcs and props (to get "DSL scoped"/@DslMarker marked) */
@ChassisDslMarker
interface IDslApi
/** all classes participating in the chassis DSL language must have this annotation for scope control</br>
 * see https://kotlinlang.org/docs/type-safe-builders.html#scope-control-dslmarker */
@DslMarker annotation class ChassisDslMarker(vararg val impls: KClass<out IDslParticipator>)

/** a (maybe not very useful) explicit marker to say that the instance is NOT a ChassisDslMarker class */
@Target(AnnotationTarget.FUNCTION, AnnotationTarget.PROPERTY)
@Retention(AnnotationRetention.SOURCE)
@MustBeDocumented
annotation class NonDsl

/** a (maybe not very useful) explicit marker for DSL Top Level functions */
@Target(AnnotationTarget.FUNCTION, AnnotationTarget.PROPERTY)
@Retention(AnnotationRetention.SOURCE)
@MustBeDocumented
annotation class DslTopLevel

/** convenience annotation to be able to just "click on" for IDE navigation directly to implementing IDslXxxClass'es */
@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.SOURCE)
@MustBeDocumented
annotation class DslBlockOn(vararg val impls: KClass<out IDslParticipator>)

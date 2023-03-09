package com.hoffi.chassis.dsl.internal

import com.hoffi.chassis.chassismodel.dsl.ChassisDsl
import com.hoffi.chassis.shared.dsl.IDslParticipator
import kotlin.reflect.KClass

@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.SOURCE)
@MustBeDocumented
@ChassisDsl
annotation class DslBlockOn(vararg val impls: KClass<out IDslParticipator>)

@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.SOURCE)
@MustBeDocumented
@ChassisDsl
annotation class DslDelegators(vararg val impls: KClass<out IDslParticipator>)

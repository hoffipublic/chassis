package com.hoffi.chassis.dsl.internal

import com.hoffi.chassis.shared.dsl.IDslClass

@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.SOURCE)
@MustBeDocumented
annotation class DslBlockOn<T: IDslClass>()

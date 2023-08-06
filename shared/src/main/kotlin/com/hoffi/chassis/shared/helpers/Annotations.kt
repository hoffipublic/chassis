package com.hoffi.chassis.shared.helpers

import kotlin.reflect.KClass

/** just IDE navigation "click on" helpers to get directly to implementing IDslClass'es */
@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.SOURCE)
@MustBeDocumented
annotation class CodeLink(val desc: String = "", vararg val impls: KClass<*>)

package com.hoffi.chassis.shared.helpers

import kotlin.reflect.KClass

/** just IDE navigation "click on" helpers to get directly to implementing IDslClass'es */
@Target(AnnotationTarget.FUNCTION, AnnotationTarget.EXPRESSION, AnnotationTarget.CLASS, AnnotationTarget.TYPE, AnnotationTarget.FILE)
@Retention(AnnotationRetention.SOURCE)
@MustBeDocumented
annotation class CodeLink(val desc: String = "", vararg val impls: KClass<*>)


/** convenience no op Class (to Ctrl-click on) */
@CodeLink
class See(val desc: String = "", vararg val impls: KClass<*>) {
    constructor(vararg impls: KClass<*>) : this("", *impls)
}

//package com.hoffi.chassis.dsl.internal
//
//import kotlin.reflect.KClass
//
//fun IDslClass.assertNotUsedIn(vararg forbidden: KClass<out IDslClass>) = assert(parent::class !in forbidden)
//fun <T> IDslClass.assertNotUsedIn(vararg forbidden: KClass<out IDslClass>, initializerBlock: IDslClass.() -> T): T {
//    assert(parent::class !in forbidden)
//    return initializerBlock()
//}
//fun IDslClass.assertEmptyIfUsedIn(array: Array<out Any>, vararg forbidden: KClass<out IDslClass>) = assert (parent::class in forbidden && array.isEmpty())

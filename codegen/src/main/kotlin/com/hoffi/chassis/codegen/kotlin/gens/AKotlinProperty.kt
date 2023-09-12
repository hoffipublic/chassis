package com.hoffi.chassis.codegen.kotlin.gens

import com.hoffi.chassis.codegen.kotlin.GenCtxWrapper
import com.hoffi.chassis.shared.parsedata.ModelClassDataFromDsl
import com.hoffi.chassis.shared.parsedata.Property
import com.squareup.kotlinpoet.PropertySpec
import org.slf4j.LoggerFactory

context(GenCtxWrapper)
abstract class AKotlinProperty(val property: Property, val modelClassDataFromDslPropIsIn: ModelClassDataFromDsl) {
    val log = LoggerFactory.getLogger(javaClass)
    abstract val builder: PropertySpec.Builder
    override fun toString(): String = "Property(${property.name()}) of $modelClassDataFromDslPropIsIn\n${builder.build().toString()}"

    fun mergePropertyIntoConstructor(): AKotlinProperty { builder.initializer(property.name()) ; return this }

    fun build(): PropertySpec {
        return builder.build()
    }
}

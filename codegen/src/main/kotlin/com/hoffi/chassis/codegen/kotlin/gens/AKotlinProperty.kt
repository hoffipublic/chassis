package com.hoffi.chassis.codegen.kotlin.gens

import com.hoffi.chassis.shared.parsedata.ModelClassData
import com.hoffi.chassis.shared.parsedata.Property
import com.squareup.kotlinpoet.PropertySpec

abstract class AKotlinProperty(val property: Property, val genModel: ModelClassData) {
    abstract val builder: PropertySpec.Builder
    override fun toString(): String = "Property(${property.name}) of ${genModel}\n${builder.build().toString()}"
    fun mergePropertyIntoConstructor(): AKotlinProperty { builder.initializer(property.name) ; return this }
    fun build(): PropertySpec {
        return builder.build()
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other is Property) return this.property == other
        if (other !is AKotlinProperty) return false
        return property == other.property
    }

    override fun hashCode() = property.hashCode()
}
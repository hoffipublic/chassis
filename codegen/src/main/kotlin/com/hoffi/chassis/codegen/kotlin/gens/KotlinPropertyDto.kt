package com.hoffi.chassis.codegen.kotlin.gens

import com.hoffi.chassis.shared.COLLECTIONTYP
import com.hoffi.chassis.shared.TypWrapper
import com.hoffi.chassis.shared.immutable
import com.hoffi.chassis.shared.mutable
import com.hoffi.chassis.shared.parsedata.Property
import com.hoffi.chassis.shared.shared.Initializer
import com.hoffi.chassis.shared.shared.Tag
import com.squareup.kotlinpoet.PropertySpec

class KotlinPropertyDto(val property: Property, val genModel: AHasPropertys) {
    override fun toString(): String = "Property(${property.name}) of ${genModel}\n${builder.build().toString()}"

    private val builder: PropertySpec.Builder
    lateinit var initializer: Initializer

    init {
        when (property.collectionType) {
            is COLLECTIONTYP.NONE -> {
                builder = PropertySpec.builder(property.name, property.eitherTypModelOrClass.modelClassName.poetType, property.modifiers)
                if (property.eitherTypModelOrClass.initializer.format.isNotBlank()) {
                    if (property.eitherTypModelOrClass.initializer.format.count { it == '%' } != property.eitherTypModelOrClass.initializer.args.size) {
                        throw Exception("$property imbalanced number of initializer format variables and given args: ${property.eitherTypModelOrClass.initializer} in $property")
                    }
                    initializer = Initializer.of(property.eitherTypModelOrClass.initializer.format, property.eitherTypModelOrClass.initializer.args)
                    builder.initializer(property.eitherTypModelOrClass.initializer.format, *property.eitherTypModelOrClass.initializer.args.toTypedArray())
                }
            }
            is COLLECTIONTYP.COLLECTION, is COLLECTIONTYP.ITERABLE, is COLLECTIONTYP.LIST, is COLLECTIONTYP.SET -> {
                val collMutable = if (Tag.COLLECTION_IMMUTABLE in property.tags) immutable else mutable
                val collTypWrapper = TypWrapper.of(property.collectionType, collMutable, property.eitherTypModelOrClass.modelClassName.poetType)
                builder = PropertySpec.builder(property.name, collTypWrapper.typeName, property.modifiers)
                initializer = Initializer.of(collTypWrapper.initializer.format, collTypWrapper.initializer.args)
                builder.initializer(collTypWrapper.initializer.format, collTypWrapper.initializer.args)
            }
        }
        if (property.mutable.bool) builder.mutable() // val or var
    }

    fun mergePropertyIntoConstructor(): KotlinPropertyDto { builder.initializer(property.name) ; return this }

    fun build(): PropertySpec {
        return builder.build()
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other is Property) return this.property == other
        if (other !is KotlinPropertyDto) return false
        return property == other.property
    }
    override fun hashCode() = property.hashCode()
}

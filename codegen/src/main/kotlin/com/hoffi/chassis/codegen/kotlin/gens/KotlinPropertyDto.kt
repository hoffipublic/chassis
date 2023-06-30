package com.hoffi.chassis.codegen.kotlin.gens

import com.hoffi.chassis.shared.COLLECTIONTYPE
import com.hoffi.chassis.shared.parsedata.Property
import com.squareup.kotlinpoet.PropertySpec

class KotlinPropertyDto(val property: Property) {
    override fun toString(): String = builder.build().toString()

    private val builder: PropertySpec.Builder

    init {
        builder = when (property.collectionType) {
            is COLLECTIONTYPE.NONE -> {
                PropertySpec.builder(property.name, property.eitherTypModelOrClass.modelClassName.poetType, property.modifiers)
            }
            is COLLECTIONTYPE.COLLECTION -> {
                PropertySpec.builder(property.name, property.eitherTypModelOrClass.modelClassName.poetType, property.modifiers)
            }
            is COLLECTIONTYPE.ITERABLE -> {
                PropertySpec.builder(property.name, property.eitherTypModelOrClass.modelClassName.poetType, property.modifiers)
            }
            is COLLECTIONTYPE.LIST -> {
                PropertySpec.builder(property.name, property.eitherTypModelOrClass.modelClassName.poetType, property.modifiers)
            }
            is COLLECTIONTYPE.SET -> {
                PropertySpec.builder(property.name, property.eitherTypModelOrClass.modelClassName.poetType, property.modifiers)
            }
        }
        if (property.mutable.bool) builder.mutable()
    }

    fun build(): PropertySpec {
        return builder.build()
    }
}

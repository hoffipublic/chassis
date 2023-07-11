package com.hoffi.chassis.codegen.kotlin.gens

import com.hoffi.chassis.chassismodel.Initializer
import com.hoffi.chassis.chassismodel.RuntimeDefaults.UUID_PROPNAME
import com.hoffi.chassis.chassismodel.typ.CollectionTypWrapper
import com.hoffi.chassis.chassismodel.typ.immutable
import com.hoffi.chassis.chassismodel.typ.mutable
import com.hoffi.chassis.codegen.kotlin.GenCtxWrapper
import com.hoffi.chassis.codegen.kotlin.whens.WhensGen
import com.hoffi.chassis.shared.parsedata.ModelClassData
import com.hoffi.chassis.shared.parsedata.Property
import com.hoffi.chassis.shared.shared.Tag
import com.squareup.kotlinpoet.CodeBlock
import com.squareup.kotlinpoet.KModifier
import com.squareup.kotlinpoet.PropertySpec

context(GenCtxWrapper)
class KotlinPropertyDto(property: Property, genModel: ModelClassData) : AKotlinProperty(property, genModel) {
    override val builder: PropertySpec.Builder = whenInit()

    fun whenInit(): PropertySpec.Builder {
        if (property.initializer.format.count { it == '%' } != property.initializer.args.size) {
            throw Exception("$property imbalanced number of initializer format variables and given args: ${property.initializer} in $property")
        }
        lateinit var initBuilder: PropertySpec.Builder
        val initializerCodeBlockBuilder = CodeBlock.builder()
        WhensGen.whenTypeAndCollectionType(property.eitherTypModelOrClass, property.collectionType,
            preFunc = {},
            preNonCollection = {
                initBuilder = PropertySpec.builder(property.name, property.poetType, property.modifiers)
                if (property.initializer.hasOriginalInitializer()) {
                    initializerCodeBlockBuilder.add(property.initializer.codeBlockFull())
                }
            },
            preCollection = {
                val collMutable = if (Tag.COLLECTION_IMMUTABLE in property.tags) immutable else mutable
                val collCollectionTypWrapper = CollectionTypWrapper.of(property.collectionType, collMutable, property.isNullable, property.poetType)
                initBuilder = PropertySpec.builder(property.name, collCollectionTypWrapper.typeName, property.modifiers)
                if (Tag.NO_DEFAULT_INITIALIZER !in property.tags) {
                    initializerCodeBlockBuilder.add(Initializer.of(collCollectionTypWrapper.initializer.format, collCollectionTypWrapper.initializer.args).codeBlockFull())
                }
            },
            isModel = {
                if ( ( ! property.initializer.hasOriginalInitializer()) && (Tag.NO_DEFAULT_INITIALIZER !in property.tags) ) {
                    val defaultInitializer = Initializer.of("%T.%L", this.modelClassName.poetType, "NULL")
                    initializerCodeBlockBuilder.add(defaultInitializer.codeBlockFull())
                }
            },
            isPoetType = {
                if ( ( ! property.initializer.hasOriginalInitializer()) && (Tag.NO_DEFAULT_INITIALIZER !in property.tags) ) {
                    val defaultInitializer = Initializer.of("%T.%L", this.modelClassName.poetType, "NULL")
                    initializerCodeBlockBuilder.add(defaultInitializer.codeBlockFull())
                }
            },
            isTyp = {
                if (Tag.NO_DEFAULT_INITIALIZER !in property.tags) {
                    val defaultInitializer = typ.defaultInitializer
                    initializerCodeBlockBuilder.add(defaultInitializer.codeBlockFull())
                }
            },
            isModelList = {},
            isModelSet = {},
            postNonCollection = { if (property.mutable.bool) initBuilder.mutable() /* val or var */ },
            isModelCollection = {},
            isModelIterable = {},
            isPoetTypeList = {},
            isPoetTypeSet = {},
            isPoetTypeCollection = {},
            isPoetTypeIterable = {},
            isTypList = {},
            isTypSet = {},
            isTypCollection = {},
            isTypIterable = {},
            postCollection = {},
        )
        if (property.initializer.hasInitializerAddendum()) {
            initializerCodeBlockBuilder.add(property.initializer.codeBlockAddendum())
        }
        initBuilder.initializer(initializerCodeBlockBuilder.build())
        if (genModel.isUuidPrimary && property.name == UUID_PROPNAME) { initBuilder.addModifiers(KModifier.OVERRIDE) }
        return initBuilder
    }
}

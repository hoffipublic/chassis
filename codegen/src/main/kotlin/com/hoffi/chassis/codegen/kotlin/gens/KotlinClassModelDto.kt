package com.hoffi.chassis.codegen.kotlin.gens

import com.hoffi.chassis.chassismodel.C
import com.hoffi.chassis.chassismodel.RuntimeDefaults
import com.hoffi.chassis.chassismodel.RuntimeDefaults.ANNOTATION_TABLE_CLASSNAME
import com.hoffi.chassis.chassismodel.RuntimeDefaults.UUID_PROPNAME
import com.hoffi.chassis.chassismodel.dsl.GenCtxException
import com.hoffi.chassis.chassismodel.typ.COLLECTIONTYP
import com.hoffi.chassis.chassismodel.typ.CollectionTypWrapper
import com.hoffi.chassis.chassismodel.typ.immutable
import com.hoffi.chassis.chassismodel.typ.mutable
import com.hoffi.chassis.shared.EitherTypOrModelOrPoetType
import com.hoffi.chassis.shared.codegen.GenCtxWrapper
import com.hoffi.chassis.shared.dsl.DslRef
import com.hoffi.chassis.shared.parsedata.GenModel
import com.hoffi.chassis.shared.parsedata.Property
import com.hoffi.chassis.shared.shared.Tag
import com.squareup.kotlinpoet.*

context(GenCtxWrapper)
class KotlinClassModelDto(val dtoModel: GenModel.DtoModel)
    : AKotlinClass(dtoModel)
{
    fun build(): TypeSpec.Builder {
        builder.addModifiers(dtoModel.classModifiers)
        buildExtends()
        buildConstructorsAndPropertys()
        buildFeatures()
        buildFunctions()
        buildAuxiliaryFunctions()
        buildAnnotations()
        return builder
    }

    fun buildExtends() {
        val extends = modelClassData.extends["default"]
        if (extends != null && extends.typeClassOrDslRef != EitherTypOrModelOrPoetType.NOTHING) {
            builder.superclass(extends.typeClassOrDslRef.modelClassName.poetType)
            when (extends.typeClassOrDslRef) {
                is EitherTypOrModelOrPoetType.EitherModel -> {
                    val eitherModel = extends.typeClassOrDslRef as EitherTypOrModelOrPoetType.EitherModel
                    val reffedModel = genCtx.genModel(eitherModel.modelSubElementRef)
                    for (superConstrProp: Property in reffedModel.allProps.values.filter { Tag.CONSTRUCTOR in it.tags }) {
                        builder.addSuperclassConstructorParameter(superConstrProp.name)
                    }
                }
                is EitherTypOrModelOrPoetType.EitherPoetType -> {
                    TODO()
                }
                is EitherTypOrModelOrPoetType.EitherTyp -> {
                    TODO()
                }
                is EitherTypOrModelOrPoetType.NOTHING -> {}
            }
        }
        for (superinterface in extends?.superInterfaces ?: mutableSetOf()) {
            builder.addSuperinterface(superinterface.modelClassName.poetType)
        }
        val isUuidDto = propsInclSuperclassPropsMap.values.filter { Tag.Companion.PRIMARY in it.tags }
        if (isUuidDto.size == 1 && isUuidDto.first().name == UUID_PROPNAME) {
            builder.addSuperinterface(RuntimeDefaults.UUIDDTO_INTERFACE_CLASSNAME)
            dtoModel.isUuidPrimary = true
        }
    }

    fun buildConstructorsAndPropertys() {
        if ( ! dtoModel.constructorVisibility) {
            constructorBuilder.addModifiers(KModifier.PROTECTED)
        }

        val constrParamsWithInitializersForCompanionCreate = mutableSetOf<ParameterSpec.Builder>()

        val superConstructorProps: MutableSet<Property> = mutableSetOf()
        val superModelEither = dtoModel.extends[C.DEFAULT]?.typeClassOrDslRef ?: EitherTypOrModelOrPoetType.NOTHING
        when (superModelEither) {
            is EitherTypOrModelOrPoetType.EitherModel -> {
                val superModel = genCtx.genModel(superModelEither.modelSubElementRef)
                superConstructorProps.addAll(superModel.allProps.values.filter { Tag.CONSTRUCTOR in it.tags })
            }
            is EitherTypOrModelOrPoetType.EitherPoetType -> TODO()
            is EitherTypOrModelOrPoetType.EitherTyp -> TODO()
            is EitherTypOrModelOrPoetType.NOTHING, null -> {}
        }

        // add primary constructor propertys
        for (theProp in dtoModel.allProps.values) {
            if (Tag.CONSTRUCTOR in theProp.tags) {
                if (theProp in superConstructorProps) {
                    val paramBuilder = paramBuilder(theProp)
                    constructorBuilder.addParameter(paramBuilder.build())
                    //constrParamsWithInitializersForCompanionCreate.add(paramBuilder)
                } else {
                    val kotlinProp = KotlinPropertyDto(theProp, this.modelClassData)
                    kotlinProp.mergePropertyIntoConstructor()
                    builder.addProperty(kotlinProp.build())
                    val paramBuilder = paramBuilder(theProp)
                    constructorBuilder.addParameter(paramBuilder.build())
                    //constrParamsWithInitializersForCompanionCreate.add(paramBuilder) // ?
                }
            } else {
                val kotlinProp = KotlinPropertyDto(theProp, this.modelClassData)
                builder.addProperty(kotlinProp.build())
            }
        }

        if (dtoModel.kind == TypeSpec.Kind.CLASS) {
            builder.primaryConstructor(constructorBuilder.build())
        }
    }

    private fun paramBuilder(
        theProp: Property
    ): ParameterSpec.Builder {
        val paramBuilder: ParameterSpec.Builder
        val initializerCodeBlockBuilder = CodeBlock.builder()
        when (theProp.collectionType) {
            is COLLECTIONTYP.NONE -> {
                paramBuilder = ParameterSpec.builder(theProp.name, theProp.poetType)
                if (theProp.eitherTypModelOrClass.initializer.hasOriginalInitializer()) {
                    //paramBuilder.defaultValue(theProp.initializer.format, theProp.initializer.args)
                    paramBuilder.defaultValue(theProp.initializer.codeBlockFull())
                } else if (Tag.DEFAULT_INITIALIZER in theProp.tags) {
                    val eitherTypOfProp = theProp.eitherTypModelOrClass
                    val defaultInitializer = when (eitherTypOfProp) {
                        is EitherTypOrModelOrPoetType.EitherModel -> TODO()
                        is EitherTypOrModelOrPoetType.EitherPoetType -> TODO()
                        is EitherTypOrModelOrPoetType.EitherTyp -> eitherTypOfProp.typ.defaultInitializer
                        is EitherTypOrModelOrPoetType.NOTHING -> TODO()
                    }
                    //paramBuilder.defaultValue(defaultInitializer.codeBlockFull())
                    initializerCodeBlockBuilder.add(defaultInitializer.codeBlockFull())
                    initializerCodeBlockBuilder.add(theProp.initializer.codeBlockAddendum())
                    paramBuilder.defaultValue(initializerCodeBlockBuilder.build())
                }
            }
            is COLLECTIONTYP.LIST, is COLLECTIONTYP.SET, is COLLECTIONTYP.COLLECTION, is COLLECTIONTYP.ITERABLE -> {
                val collMutable = if (Tag.COLLECTION_IMMUTABLE in theProp.tags) immutable else mutable
                val collTypeWrapper = CollectionTypWrapper.of(theProp.collectionType, collMutable, theProp.isNullable, theProp.poetType)
                paramBuilder = ParameterSpec.builder(theProp.name, collTypeWrapper.typeName)
                if (Tag.DEFAULT_INITIALIZER in theProp.tags) {
                    //paramBuilder.defaultValue(collTypeWrapper.initializer.format, collTypeWrapper.initializer.args)
                    initializerCodeBlockBuilder.add(collTypeWrapper.initializer.format, collTypeWrapper.initializer.args)
                    initializerCodeBlockBuilder.add(theProp.initializer.codeBlockAddendum())
                    paramBuilder.defaultValue(initializerCodeBlockBuilder.build())
                }
            }
        }
        return paramBuilder
    }

//    fun buildPropertys(setOfPropertys: Set<Property>) {
//        for (prop in modelClassData.propertys.values) {
//            prop.validate(this)
//            val kotlinProp = KotlinPropertyDto(prop, this)
//            val propSpec = kotlinProp.build()
//            builder.addProperty(propSpec)
//        }
//        for (prop in modelClassData.gatheredPropertys.values) {
//            prop.validate(this)
//            val kotlinProp = KotlinPropertyDto(prop, this)
//            val propSpec = kotlinProp.build()
//            builder.addProperty(propSpec)
//        }
//    }

    fun buildFeatures() {
        if (modelClassData.modelClassName.modelOrTypeNameString == "Entity") {
            println(modelClassData.poetType)
            propsInclSuperclassPropsMap.values.forEach {
                println(it)
            }

        }
    }

    fun buildFunctions() {

    }

    fun buildAuxiliaryFunctions() {

    }

    fun buildAnnotations() {
        val tableModel = try { genCtx.genModel(DslRef.table(C.DEFAULT, dtoModel.modelSubElRef.parentDslRef)) } catch(e: GenCtxException) { null }
        if (tableModel != null) {
            builder.addAnnotation(
                AnnotationSpec.builder(ANNOTATION_TABLE_CLASSNAME)
                    .addMember("%T::class", modelClassData.poetType)
                    .addMember("targetTable = %T::class", tableModel.modelClassName.poetType)
                    .build()
            )
        }
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is KotlinClassModelDto) return false
        return dtoModel == other.dtoModel
    }

    override fun hashCode() = dtoModel.hashCode()
}

package com.hoffi.chassis.codegen.kotlin.gens

import com.hoffi.chassis.chassismodel.C
import com.hoffi.chassis.shared.*
import com.hoffi.chassis.shared.codegen.GenCtxWrapper
import com.hoffi.chassis.shared.fix.ENV
import com.hoffi.chassis.shared.fix.RuntimeDefaults.WAS_GENERATED_INTERFACE_ClassName
import com.hoffi.chassis.shared.parsedata.GenModel
import com.hoffi.chassis.shared.parsedata.Property
import com.hoffi.chassis.shared.shared.Tag
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.KModifier
import com.squareup.kotlinpoet.ParameterSpec
import com.squareup.kotlinpoet.TypeSpec

context(GenCtxWrapper)
class KotlinClassModelDto(val dtoModel: GenModel.DtoModel)
    : AKotlinClass(dtoModel)
{
    override fun toString() = "${this::class.simpleName}(${dtoModel})"

    override var builder = when (dtoModel.kind) {
        TypeSpec.Kind.OBJECT -> {
            TypeSpec.objectBuilder(dtoModel.poetType)
        }
        TypeSpec.Kind.INTERFACE -> {
            TypeSpec.interfaceBuilder(dtoModel.poetType)
        }
        else -> {
            TypeSpec.classBuilder(dtoModel.poetType)
        }
    }.apply {
        if (dtoModel.kind != TypeSpec.Kind.OBJECT && dtoModel.kind != TypeSpec.Kind.INTERFACE) {
            //addNullCompanion() // TODO addNullCompanion()
        }
    }
    val constructorBuilder = FunSpec.constructorBuilder()

    fun build(): TypeSpec.Builder {
        builder.addModifiers(dtoModel.classModifiers)
        buildExtends()
        buildConstructorsAndPropertys()
        buildAuxiliaryFunctions()
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
        builder.addSuperinterface(WAS_GENERATED_INTERFACE_ClassName)
        builder.addKdoc("generated at %L on %L", ENV.generationLocalDateTime, ENV.hostname)
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
                    val kotlinProp = KotlinPropertyDto(theProp, this)
                    kotlinProp.mergePropertyIntoConstructor()
                    builder.addProperty(kotlinProp.build())
                    val paramBuilder = paramBuilder(theProp)
                    constructorBuilder.addParameter(paramBuilder.build())
                    //constrParamsWithInitializersForCompanionCreate.add(paramBuilder) // ?
                }
            } else {
                val kotlinProp = KotlinPropertyDto(theProp, this)
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
        when (theProp.collectionType) {
            is COLLECTIONTYP.NONE -> {
                paramBuilder = ParameterSpec.builder(theProp.name, theProp.poetType)
                if (theProp.eitherTypModelOrClass.initializer.format.isNotBlank()) {
                    paramBuilder.defaultValue(theProp.initializer.format, theProp.initializer.args)
                } else if (Tag.DEFAULT_INITIALIZER in theProp.tags) {
                    val eitherTypOfProp = theProp.eitherTypModelOrClass
                    val defaultInitializer = when (eitherTypOfProp) {
                        is EitherTypOrModelOrPoetType.EitherModel -> TODO()
                        is EitherTypOrModelOrPoetType.EitherPoetType -> TODO()
                        is EitherTypOrModelOrPoetType.EitherTyp -> eitherTypOfProp.typ.defaultInitializer
                        is EitherTypOrModelOrPoetType.NOTHING -> TODO()
                    }
                    paramBuilder.defaultValue(defaultInitializer.codeBlock())
                }
            }
            is COLLECTIONTYP.LIST, is COLLECTIONTYP.SET, is COLLECTIONTYP.COLLECTION, is COLLECTIONTYP.ITERABLE -> {
                val collMutable = if (Tag.COLLECTION_IMMUTABLE in theProp.tags) immutable else mutable
                val collTypeWrapper = CollectionTypWrapper.of(
                    theProp.collectionType,
                    collMutable,
                    theProp.isNullable,
                    theProp.poetType
                )
                paramBuilder = ParameterSpec.builder(theProp.name, collTypeWrapper.typeName)
                if (Tag.DEFAULT_INITIALIZER in theProp.tags) {
                    paramBuilder.defaultValue(collTypeWrapper.initializer.format, collTypeWrapper.initializer.args)
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

    fun buildFunctions() {

    }

    fun buildAuxiliaryFunctions() {

    }


    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is KotlinClassModelDto) return false
        return dtoModel == other.dtoModel
    }

    override fun hashCode() = dtoModel.hashCode()
}

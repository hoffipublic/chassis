package com.hoffi.chassis.codegen.kotlin.gens

import com.hoffi.chassis.chassismodel.C
import com.hoffi.chassis.shared.*
import com.hoffi.chassis.shared.codegen.GenCtxWrapper
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
                    for (superConstrProp: Property in reffedModel.propertys.values.filter { Tag.CONSTRUCTOR in it.tags }) {
                        builder.addSuperclassConstructorParameter(superConstrProp.name)
                    }
                    for (superConstrProp: Property in reffedModel.gatheredPropertys.values.filter { Tag.CONSTRUCTOR in it.tags }) {
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
            // TODO add preGenerated fix interface GENERATED
            builder.addSuperinterface(superinterface.modelClassName.poetType)
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
                superConstructorProps.addAll(superModel.propertys.values.filter { Tag.CONSTRUCTOR in it.tags })
                superConstructorProps.addAll(superModel.gatheredPropertys.values.filter { Tag.CONSTRUCTOR in it.tags })
            }
            is EitherTypOrModelOrPoetType.EitherPoetType -> TODO()
            is EitherTypOrModelOrPoetType.EitherTyp -> TODO()
            is EitherTypOrModelOrPoetType.NOTHING, null -> {}
        }

        val allProps = dtoModel.propertys.values + dtoModel.gatheredPropertys.values

        // add primary constructor propertys
        for (theProp in allProps) {
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
                paramBuilder = ParameterSpec.builder(theProp.name, theProp.eitherTypModelOrClass.modelClassName.poetType)
                if (theProp.eitherTypModelOrClass.initializer.format.isNotBlank()) {
                    paramBuilder.defaultValue(theProp.eitherTypModelOrClass.initializer.format, theProp.eitherTypModelOrClass.initializer.args)
                }
            }
            is COLLECTIONTYP.LIST, is COLLECTIONTYP.SET, is COLLECTIONTYP.COLLECTION, is COLLECTIONTYP.ITERABLE -> {
                val collMutable = if (Tag.COLLECTION_IMMUTABLE in theProp.tags) immutable else mutable
                val collTypeWrapper = TypWrapper.of(theProp.collectionType, collMutable, theProp.eitherTypModelOrClass.modelClassName.poetType)
                paramBuilder = ParameterSpec.builder(theProp.name, collTypeWrapper.typeName)
                paramBuilder.defaultValue(collTypeWrapper.initializer.format, collTypeWrapper.initializer.args)
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

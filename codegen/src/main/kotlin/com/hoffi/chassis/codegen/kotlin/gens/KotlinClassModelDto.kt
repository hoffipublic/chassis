package com.hoffi.chassis.codegen.kotlin.gens

import com.hoffi.chassis.chassismodel.C
import com.hoffi.chassis.chassismodel.Initializer
import com.hoffi.chassis.chassismodel.RuntimeDefaults
import com.hoffi.chassis.chassismodel.RuntimeDefaults.ANNOTATION_TABLE_CLASSNAME
import com.hoffi.chassis.chassismodel.RuntimeDefaults.UUID_PROPNAME
import com.hoffi.chassis.chassismodel.RuntimeDefaults.classNameUUID
import com.hoffi.chassis.chassismodel.RuntimeDefaults.classNameUUID_randomUUID
import com.hoffi.chassis.chassismodel.dsl.GenCtxException
import com.hoffi.chassis.chassismodel.typ.COLLECTIONTYP
import com.hoffi.chassis.chassismodel.typ.CollectionTypWrapper
import com.hoffi.chassis.chassismodel.typ.immutable
import com.hoffi.chassis.chassismodel.typ.mutable
import com.hoffi.chassis.codegen.kotlin.GenCtxWrapper
import com.hoffi.chassis.shared.EitherTypOrModelOrPoetType
import com.hoffi.chassis.shared.dsl.DslRef
import com.hoffi.chassis.shared.parsedata.GenModel
import com.hoffi.chassis.shared.parsedata.Property
import com.hoffi.chassis.shared.shared.Tag
import com.squareup.kotlinpoet.*

context(GenCtxWrapper)
class KotlinClassModelDto(val dtoModel: GenModel.DtoModel)
    : AKotlinClass(dtoModel)
{
    data class ConstrParam(val propName: String, val poetType: TypeName, val defaultInitializer: Initializer, val nullInitializer: Initializer)
    val constrParamsWithInitializersForCompanionCreate: MutableSet<ConstrParam> = mutableSetOf()
    val constrLikeParams = mutableListOf<ParameterSpec>()

    fun build(): TypeSpec.Builder {
        builder.addModifiers(dtoModel.classModifiers)
        buildExtends()
        buildConstructorsAndPropertys()
        buildFunctions()
        buildAuxiliaryFunctions()
        buildAnnotations()
        buildFeatures()
        buildCompanion()
        if (companionBuilder != null && modelClassData.kind == TypeSpec.Kind.CLASS) {
            builder.addType(companionBuilder!!.build())
        }
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
        val isUuidDto = modelClassData.propsInclSuperclassPropsMap.values.filter { Tag.Companion.PRIMARY in it.tags }
        if (isUuidDto.size == 1 && isUuidDto.first().name == UUID_PROPNAME) {
            builder.addSuperinterface(RuntimeDefaults.UUIDDTO_INTERFACE_CLASSNAME)
            dtoModel.isUuidPrimary = true
        }
    }

    fun buildConstructorsAndPropertys() {
        if ( ! dtoModel.constructorVisibility) {
            constructorBuilder.addModifiers(KModifier.PROTECTED)
        }

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
                    val paramBuilder = constrParamBuilder(theProp)
                    constructorBuilder.addParameter(paramBuilder.build())
                    constrLikeParams.add(paramBuilder.build())
                } else {
                    val kotlinProp = KotlinPropertyDto(theProp, this)
                    kotlinProp.mergePropertyIntoConstructor()
                    builder.addProperty(kotlinProp.build())
                    val paramBuilder = constrParamBuilder(theProp)
                    constructorBuilder.addParameter(paramBuilder.build())
                    constrLikeParams.add(paramBuilder.build())
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

    private fun constrParamBuilder(
        theProp: Property,
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
                        is EitherTypOrModelOrPoetType.EitherModel -> Initializer.of("%T.%L", theProp.poetType, "NULL")
                        is EitherTypOrModelOrPoetType.EitherPoetType -> Initializer.of(theProp.initializer.format, theProp.initializer.args.toMutableList())
                        is EitherTypOrModelOrPoetType.EitherTyp -> eitherTypOfProp.typ.defaultInitializer
                        is EitherTypOrModelOrPoetType.NOTHING -> TODO()
                    }
                    initializerCodeBlockBuilder.add(defaultInitializer.codeBlockFull())
                    initializerCodeBlockBuilder.add(theProp.initializer.codeBlockAddendum())
                    paramBuilder.defaultValue(initializerCodeBlockBuilder.build())
                }
                val eitherTypOfProp = theProp.eitherTypModelOrClass
                val constructorParam: ConstrParam = when (eitherTypOfProp) {
                    is EitherTypOrModelOrPoetType.EitherModel -> ConstrParam(theProp.name, theProp.poetType, Initializer.of("%T.%L", theProp.poetType, "NULL"), Initializer.of("%T.%L", theProp.poetType, "NULL"))
                    is EitherTypOrModelOrPoetType.EitherPoetType -> ConstrParam(theProp.name, theProp.poetType, Initializer.of(theProp.initializer.format, theProp.initializer.args.toMutableList()), Initializer.of(theProp.initializer.format, theProp.initializer.args.toMutableList()))
                    is EitherTypOrModelOrPoetType.EitherTyp -> ConstrParam(theProp.name, theProp.poetType, eitherTypOfProp.typ.defaultInitializer, eitherTypOfProp.typ.defaultNull)
                    is EitherTypOrModelOrPoetType.NOTHING -> TODO()
                }
                constrParamsWithInitializersForCompanionCreate.add(constructorParam)
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
                constrParamsWithInitializersForCompanionCreate.add(ConstrParam(theProp.name, collTypeWrapper.typeName, collTypeWrapper.initializer, collTypeWrapper.initializer))
            }
        }
        return paramBuilder
    }

    fun buildFeatures() {
        buildEqualsAndHashCodeFunction()

        buildToStringFunction()
    }

    private fun buildToStringFunction() {
        if (KModifier.ABSTRACT in modelClassData.classModifiers || modelClassData.kind in listOf(TypeSpec.Kind.OBJECT, TypeSpec.Kind.INTERFACE)) return
        val toStringMembers = modelClassData.propsInclSuperclassPropsMap.values.filter { prop -> Tag.PRIMARY in prop.tags }.toMutableSet() // primaries first
        toStringMembers.addAll(modelClassData.propsInclSuperclassPropsMap.values.filter { prop -> Tag.TO_STRING_MEMBER in prop.tags || prop.name in modelClassData.additionalToStringMemberProps }.toMutableSet())
        toStringMembers.removeIf { prop -> prop.name in modelClassData.removeToStringMemberProps }
        if (toStringMembers.isNotEmpty()) {
            val funSpecBuilder =
                FunSpec.builder("toString").returns(String::class.asTypeName()).addModifiers(KModifier.OVERRIDE)
            val toStringPropsFormatList = mutableListOf<String>()
            for (toStringProp in toStringMembers) {
                toStringPropsFormatList.add("${toStringProp.name}='\$${toStringProp.name}'")
            }
            funSpecBuilder.addStatement(
                "return %P",
                "${(modelClassData.poetType as ClassName).simpleName}(${toStringPropsFormatList.joinToString()})"
            )
            builder.addFunction(funSpecBuilder.build())
        }
    }

    private fun buildEqualsAndHashCodeFunction() {
        if (KModifier.ABSTRACT in modelClassData.classModifiers || modelClassData.kind in listOf(TypeSpec.Kind.OBJECT, TypeSpec.Kind.INTERFACE)) return
        val equalsAndHashCodeMembers = modelClassData.propsInclSuperclassPropsMap.values.filter { Tag.PRIMARY in it.tags   }.toMutableList()
        if (equalsAndHashCodeMembers.isEmpty()) { equalsAndHashCodeMembers.addAll(modelClassData.propsInclSuperclassPropsMap.values.filter { Tag.HASH_MEMBER in it.tags }) }
        if (equalsAndHashCodeMembers.isNotEmpty()) {
            var funName = "equals"
            var funBuilder = FunSpec.builder(funName)
                .addModifiers(KModifier.PUBLIC, KModifier.OVERRIDE)
                .returns(Boolean::class.asTypeName())
                .addParameter("other", ANY.copy(nullable = true))
            funBuilder
                .addStatement("%L", "if (this === other) return true")
                .addStatement("if (other !is %T) return false", modelClassData.poetType)
            for (p in equalsAndHashCodeMembers) {
                val primaryComment = if (p.name == UUID_PROPNAME) " /* PRIMARY */" else ""
                val codeBlock = CodeBlock.builder().addStatement("if (%L != other.%L) return false%L", p.name, p.name, primaryComment)
                funBuilder.addCode(codeBlock.build())
            }
            funBuilder.addStatement("%L", "return true")
            builder.addFunction(funBuilder.build())

            funName = "hashCode"
            funBuilder = FunSpec.builder(funName)
                .addModifiers(KModifier.PUBLIC, KModifier.OVERRIDE)
                .returns(Int::class.asTypeName())
            val theFirstProp =  equalsAndHashCodeMembers.removeFirst()
            val primaryComment = if (theFirstProp.name == UUID_PROPNAME) " /* PRIMARY */" else ""
            funBuilder.addStatement("var result = %L.hashCode()%L", theFirstProp.name, primaryComment)
            for (p in equalsAndHashCodeMembers) {
                val primaryComment = if (p.name == UUID_PROPNAME) " /* PRIMARY */" else ""
                funBuilder.addStatement("result = 31 * result + %L.hashCode()%L", p.name, primaryComment)
            }
            funBuilder.addStatement("%L", "return result")
            builder.addFunction(funBuilder.build())
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

    fun buildCompanion() {
        if (KModifier.ABSTRACT in modelClassData.classModifiers || modelClassData.kind in listOf(TypeSpec.Kind.OBJECT, TypeSpec.Kind.INTERFACE)) return
        val companionBuilder = getOrCreateCompanion()
        val nullCodeBlocks = mutableListOf<CodeBlock>()
        for (constrParam in constrParamsWithInitializersForCompanionCreate) {
            nullCodeBlocks.add(constrParam.nullInitializer.codeBlockFull())
        }
        val nullInitializerBlock = CodeBlock.of("%T(%L)", modelClassData.poetType, nullCodeBlocks.joinToCode(", "))
        val defaultCodeBlocks = mutableListOf<CodeBlock>()
        for (constrParam in constrParamsWithInitializersForCompanionCreate) {
            defaultCodeBlocks.add(constrParam.defaultInitializer.codeBlockFull())
        }
        val defaultInitializerBlock = CodeBlock.of("return %T(%L)", modelClassData.poetType, defaultCodeBlocks.joinToCode(", "))
        companionBuilder
            .addProperty(PropertySpec.builder("NULL", modelClassData.poetType)
                .initializer(nullInitializerBlock)
                .build())

        //@JvmStatic /public fun _internal_create(): SimpleEntityDto = SimpleEntityDto(DEFAULT_STRING, DEFAULT_STRING, DEFAULT_STRING, mutableListOf(), mutableSetOf())
            .addFunction(FunSpec.builder("_internal_create")
                .returns(modelClassData.poetType)
                .addAnnotation(JvmStatic::class)
                .addCode(defaultInitializerBlock)
                .build())

        //@JvmStatic public fun _internal_createWithUuid(): SimpleEntityDto = _internal_create().apply { uuid = Uuid.randomUUID() }
        if ( modelClassData.isUuidPrimary) {
            buildCodeBlock { add(".apply { %L = %T.%M() }", UUID_PROPNAME, classNameUUID, classNameUUID_randomUUID) }
            companionBuilder
                .addFunction(FunSpec.builder("_internal_createWithUuid")
                    .returns(modelClassData.poetType)
                    .addAnnotation(JvmStatic::class)
                    .addCode("return _internal_create().apply { %L = %T.%M() }", UUID_PROPNAME, classNameUUID, classNameUUID_randomUUID)
                    .build())
        }

        //@JvmStatic public fun createShallowWithNewEmptyModels(): SimpleEntityDto
        var funSpecBuilder = FunSpec.builder("createShallowWithNewEmptyModels")
            .returns(modelClassData.poetType)
            .addAnnotation(JvmStatic::class)
            .addStatement("val %L = _internal_createWithUuid()", modelClassData.asVarName)
        for (prop in modelClassData.directProps.values) {
            if (prop.eitherTypModelOrClass !is EitherTypOrModelOrPoetType.EitherModel) continue
            when (prop.collectionType) {
                is COLLECTIONTYP.NONE -> funSpecBuilder.addStatement("%L.%L = %T._internal_createWithUuid()", modelClassData.asVarName, prop.name, prop.poetType)
                else -> {}
            }
        }
        funSpecBuilder.addStatement("return %L", modelClassData.asVarName)
        companionBuilder.addFunction(funSpecBuilder.build())

        //@JvmStatic public fun createDeepWithNewEmptyModels(): SimpleEntityDto
        funSpecBuilder = FunSpec.builder("createDeepWithNewEmptyModels")
            .returns(modelClassData.poetType)
            .addAnnotation(JvmStatic::class)
            .addStatement("val %L = _internal_createWithUuid()", modelClassData.asVarName)
        for (prop in modelClassData.directProps.values) {
            if (prop.eitherTypModelOrClass !is EitherTypOrModelOrPoetType.EitherModel) continue
            when (prop.collectionType) {
                is COLLECTIONTYP.NONE -> {
                    funSpecBuilder.addStatement("/* beware of recursive calls, if Type or some submodel of it has a reference to this */")
                    funSpecBuilder.addStatement("%L.%L = %T.createDeepWithNewEmptyModels()", modelClassData.asVarName, prop.name, prop.poetType)
                }
                else -> { }
            }
        }
        funSpecBuilder.addStatement("return %L", modelClassData.asVarName)
        companionBuilder.addFunction(funSpecBuilder.build())

        if ( ! modelClassData.constructorVisibility) {
            companionBuilder
                .addFunction(FunSpec.builder("create")
                    .returns(modelClassData.poetType)
                    .addAnnotation(JvmStatic::class)
                    .addParameters(constrLikeParams)
                    .apply {
                        if ( ! modelClassData.constructorVisibility) {
                            this.addStatement("return %T(%L).apply { %L = %T.%M() }", modelClassData.poetType, constrLikeParams.joinToString { it.name }, UUID_PROPNAME, classNameUUID, classNameUUID_randomUUID)
                        } else {
                            this.addStatement("return %T(%L)", modelClassData.poetType, constrLikeParams.joinToString { it.name })
                        }
                    }
                    .build())
        }
    }
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is KotlinClassModelDto) return false
        return dtoModel == other.dtoModel
    }

    override fun hashCode() = dtoModel.hashCode()
}

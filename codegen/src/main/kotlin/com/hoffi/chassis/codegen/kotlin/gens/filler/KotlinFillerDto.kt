package com.hoffi.chassis.codegen.kotlin.gens.filler

import com.hoffi.chassis.chassismodel.C
import com.hoffi.chassis.chassismodel.dsl.GenException
import com.hoffi.chassis.chassismodel.typ.COLLECTIONTYP
import com.hoffi.chassis.codegen.kotlin.GenCtxWrapper
import com.hoffi.chassis.shared.EitherTypOrModelOrPoetType
import com.hoffi.chassis.shared.shared.FillerData
import com.hoffi.chassis.shared.shared.SynthFillerData
import com.hoffi.chassis.shared.shared.Tag
import com.hoffi.chassis.shared.shared.reffing.MODELKIND
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.KModifier

context(GenCtxWrapper)
class KotlinFillerDto(fillerData: FillerData): AKotlinFiller(fillerData, MODELKIND.DTOKIND) {
    private var cloneFillersNotCreated = true

    override fun build(modelkind: MODELKIND, fillerData: FillerData) {
        currentBuildFillerData = fillerData
        if (alreadyCreated.contains(fillerData.sourceDslRef)) return
        else alreadyCreated.add(fillerData.sourceDslRef)

        val intersectPropsData = IntersectPropertys.intersectPropsOf(
            modelkind,
            genCtx.genModel(fillerData.targetDslRef),
            genCtx.genModel(fillerData.sourceDslRef), "", ""
        )

        if (cloneFillersNotCreated) {
            cloneFillersNotCreated = false
            cloneFillerFunctions(intersectPropsData)
        }

        copyShallowIgnoreModelsInto(intersectPropsData)
        copyShallowWithNewModelsInto(intersectPropsData)
        copyShallowAndTakeSameModelsInto(intersectPropsData)
        deepCopyAndDeepClone(intersectPropsData)
    }

    private fun copyShallowIgnoreModelsInto(i: IntersectPropertys.CommonPropData) {
        val funSpec = FunSpec.builder("copyShallowIgnoreModelsInto")
            .addParameter(i.targetVarName, i.targetGenModel.poetType)
            .addParameter(i.sourceVarName, i.sourceGenModel.poetType)
            .returns(i.targetGenModel.poetType)
        nullSentinel(funSpec, i.targetVarName, i.targetGenModel)
        for (nonModelProp in i.intersectPropSet.filter {
            it.eitherTypModelOrClass !is EitherTypOrModelOrPoetType.EitherModel &&
                    Tag.PRIMARY !in it.tags &&
                    it.collectionType == COLLECTIONTYP.NONE && Tag.PRIMARY !in it.tags
        }) {
            funSpec.addStatement("%L.%L = %L.%L", i.targetVarName, nonModelProp.name, i.sourceVarName, nonModelProp.name)
        }
        funSpec.addStatement("return %L", i.targetVarName)
        builder.addFunction(funSpec.build())
    }

    private fun copyShallowWithNewModelsInto(i: IntersectPropertys.CommonPropData) {
        val funSpec = FunSpec.builder("copyShallowWithNewModelsInto")
            .addParameter(i.targetVarName, i.targetGenModel.poetType)
            .addParameter(i.sourceVarName, i.sourceGenModel.poetType)
            .returns(i.targetGenModel.poetType)
        nullSentinel(funSpec, i.targetVarName, i.targetGenModel)
        funSpec.addStatement("%L(%L, %L)", "copyShallowIgnoreModelsInto", i.targetVarName, i.sourceVarName)
        for (prop in i.intersectPropSet.filter { Tag.PRIMARY !in it.tags }) {
            val propEither = prop.eitherTypModelOrClass
            when (propEither) {
                is EitherTypOrModelOrPoetType.EitherModel -> {
                    when (prop.collectionType) {
                        is COLLECTIONTYP.NONE -> {
                            funSpec.addComment("beware of recursive calls, if Type or some submodel of it has a reference to this")
                            funSpec.addStatement("%L.%L = %T.createDeepWithNewEmptyModels()", i.targetVarName, prop.name, prop.poetType)
                        }
                        is COLLECTIONTYP.LIST, is COLLECTIONTYP.SET, is COLLECTIONTYP.COLLECTION, is COLLECTIONTYP.ITERABLE -> {
                            funSpec.addCode(clearCollection(i.targetVarName, prop))
                        }
                    }
                }
                is EitherTypOrModelOrPoetType.EitherPoetType -> {
                    when (prop.collectionType) {
                        is COLLECTIONTYP.NONE -> {}
                        is COLLECTIONTYP.LIST, is COLLECTIONTYP.SET, is COLLECTIONTYP.COLLECTION, is COLLECTIONTYP.ITERABLE -> {
                            funSpec.addCode(clearCollection(i.targetVarName, prop))
                        }
                    }
                }
                is EitherTypOrModelOrPoetType.EitherTyp -> {
                    when (prop.collectionType) {
                        is COLLECTIONTYP.NONE -> { }
                        is COLLECTIONTYP.LIST, is COLLECTIONTYP.SET, is COLLECTIONTYP.COLLECTION, is COLLECTIONTYP.ITERABLE -> {
                            funSpec.addCode(clearCollection(i.targetVarName, prop))
                        }
                    }
                }
                is EitherTypOrModelOrPoetType.NOTHING -> throw GenException("EitherTypOrModelOrPoetType is still NOTHING")
            }
        }
        funSpec.addStatement("return %L", i.targetVarName)
        builder.addFunction(funSpec.build())
    }

    private fun copyShallowAndTakeSameModelsInto(i: IntersectPropertys.CommonPropData) {
        val funSpec = FunSpec.builder("copyShallowAndTakeSameModelsInto")
            .addParameter(i.targetVarName, i.targetGenModel.poetType)
            .addParameter(i.sourceVarName, i.sourceGenModel.poetType)
            .returns(i.targetGenModel.poetType)
        nullSentinel(funSpec, i.targetVarName, i.targetGenModel)
        funSpec.addStatement("%L(%L, %L)", "copyShallowIgnoreModelsInto", i.targetVarName, i.sourceVarName)
        for (prop in i.intersectPropSet.filter { Tag.PRIMARY !in it.tags }) {
            val propEither = prop.eitherTypModelOrClass
            when (propEither) {
                is EitherTypOrModelOrPoetType.EitherModel -> {
                    when (prop.collectionType) {
                        is COLLECTIONTYP.NONE -> {
                            funSpec.addStatement("%L.%L = %L.%L", i.targetVarName, prop.name, i.sourceVarName, prop.name)
                        }
                        is COLLECTIONTYP.LIST, is COLLECTIONTYP.COLLECTION, is COLLECTIONTYP.ITERABLE -> {
                            funSpec.addStatement("// %L.%L.clear()", i.targetVarName, prop.name)
                            funSpec.addStatement("// %L.%L.addAll(%L.%L)", i.targetVarName, prop.name, i.sourceVarName, prop.name)
                        }
                        is COLLECTIONTYP.SET -> {
                            funSpec.addStatement("// %L.%L.clear()", i.targetVarName, prop.name)
                            funSpec.addStatement("// %L.%L.addAll(%L.%L.toList())", i.targetVarName, prop.name, i.sourceVarName, prop.name)
                        }
                    }
                }
                is EitherTypOrModelOrPoetType.EitherPoetType -> {
                    when (prop.collectionType) {
                        is COLLECTIONTYP.NONE -> {}
                        is COLLECTIONTYP.LIST, is COLLECTIONTYP.COLLECTION, is COLLECTIONTYP.ITERABLE -> {
                            funSpec.addStatement("// %L.%L.clear()", i.targetVarName, prop.name)
                            funSpec.addStatement("// %L.%L.addAll(%L.%L)", i.targetVarName, prop.name, i.sourceVarName, prop.name)
                        }
                        is COLLECTIONTYP.SET -> {
                            funSpec.addStatement("// %L.%L.clear()", i.targetVarName, prop.name)
                            funSpec.addStatement("// %L.%L.addAll(%L.%L.toList())", i.targetVarName, prop.name, i.sourceVarName, prop.name)
                        }
                    }
                }
                is EitherTypOrModelOrPoetType.EitherTyp -> {
                    when (prop.collectionType) {
                        is COLLECTIONTYP.NONE -> { }
                        is COLLECTIONTYP.LIST, is COLLECTIONTYP.COLLECTION, is COLLECTIONTYP.ITERABLE -> {
                            funSpec.addStatement("// %L.%L.clear()", i.targetVarName, prop.name)
                            funSpec.addStatement("// %L.%L.addAll(%L.%L)", i.targetVarName, prop.name, i.sourceVarName, prop.name)
                        }
                        is COLLECTIONTYP.SET -> {
                            funSpec.addStatement("// %L.%L.clear()", i.targetVarName, prop.name)
                            funSpec.addStatement("// %L.%L.addAll(%L.%L.toList())", i.targetVarName, prop.name, i.sourceVarName, prop.name)
                        }
                    }
                }
                is EitherTypOrModelOrPoetType.NOTHING -> throw GenException("EitherTypOrModelOrPoetType is still NOTHING")
            }
        }
        funSpec.addStatement("return %L", i.targetVarName)
        builder.addFunction(funSpec.build())
    }

    private fun deepCopyAndDeepClone(i: IntersectPropertys.CommonPropData) {
        val funNames = listOf("copyDeepInto", "cloneDeep")
        for (funName in funNames) {
            if (funName == "cloneDeep" && KModifier.ABSTRACT in i.targetGenModel.classModifiers) continue
            val funSpec = if (funName == "copyDeepInto") {
                val theFunSpec = FunSpec.builder("copyDeepInto")
                    .addParameter(i.targetVarName, i.targetGenModel.poetType)
                    .addParameter(i.sourceVarName, i.sourceGenModel.poetType)
                nullSentinel(theFunSpec, i.targetVarName, i.targetGenModel)
                theFunSpec
            } else {
                val theFunSpec = FunSpec.builder("cloneDeep")
                    .addParameter(i.sourceVarName, i.sourceGenModel.poetType)
                if (i.targetGenModel.isUuidPrimary) {
                    theFunSpec.addStatement("val %L = %T._internal_createWithUuid()", i.targetVarName, i.targetGenModel.poetType)
                } else {
                    val allPKs = i.sourceGenModel.allProps.values.filter { Tag.PRIMARY in it.tags }
                    theFunSpec.addStatement("val %L = %T._internal_create()", i.targetVarName, i.targetGenModel.poetType)
                    for (pkProp in allPKs) {
                        theFunSpec.addStatement("%L.%L = %L.%L", i.targetVarName, pkProp.name, i.sourceVarName, pkProp.name)
                    }
                }
                theFunSpec
            }
            funSpec.returns(i.targetGenModel.poetType)

            funSpec.addStatement("%L(%L, %L)", "copyShallowIgnoreModelsInto", i.targetVarName, i.sourceVarName)
            for (prop in i.intersectPropSet.filter { Tag.PRIMARY !in it.tags }) {
                val propEither = prop.eitherTypModelOrClass
                when (propEither) {
                    is EitherTypOrModelOrPoetType.EitherModel -> {
                        genCtx.syntheticFillerDatas.add(SynthFillerData(propEither.modelSubElementRef, propEither.modelSubElementRef, via = "propTypeEither is Model in deepCopyAndClone of $currentBuildFillerData"))
                        val propEitherModelFillerClassName = prop.eitherTypModelOrClass.modelClassName.fillerPoetType
                        when (prop.collectionType) {
                            is COLLECTIONTYP.NONE -> {
                                funSpec.addCode(
                                    """if (${i.sourceVarName}.${prop.name} === %T.NULL)
                                        |    ${i.targetVarName}.${prop.name} = ${i.sourceVarName}.${prop.name}
                                        |else
                                        |    %T.copyDeepInto(${i.targetVarName}.${prop.name}, ${i.sourceVarName}.${prop.name})
                                        |""".trimMargin(),
                                    prop.poetType, propEitherModelFillerClassName
                                )
                            }
                            is COLLECTIONTYP.SET, is COLLECTIONTYP.LIST, is COLLECTIONTYP.COLLECTION, is COLLECTIONTYP.ITERABLE -> {
                                funSpec.addCode(clearCollection(i.targetVarName, prop))
                                if (funName == "cloneDeep") {
                                    funSpec.addCode(addAllMapped(i.targetVarName, prop, i.sourceVarName, propEitherModelFillerClassName, funName))
                                } else {
                                    funSpec.addCode(addAll(i.targetVarName, prop, i.sourceVarName))
                                }
                            }
                        }
                    }
                    is EitherTypOrModelOrPoetType.EitherPoetType -> {
                        when (prop.collectionType) {
                            is COLLECTIONTYP.NONE -> {}
                            is COLLECTIONTYP.LIST, is COLLECTIONTYP.SET, is COLLECTIONTYP.COLLECTION, is COLLECTIONTYP.ITERABLE -> {
                                funSpec.addCode(clearCollection(i.targetVarName, prop))
                                funSpec.addCode(addAll(i.targetVarName, prop, i.sourceVarName))
                            }
                        }
                    }
                    is EitherTypOrModelOrPoetType.EitherTyp -> {
                        when (prop.collectionType) {
                            is COLLECTIONTYP.NONE -> {}
                            is COLLECTIONTYP.SET, is COLLECTIONTYP.LIST, is COLLECTIONTYP.COLLECTION, is COLLECTIONTYP.ITERABLE -> {
                                funSpec.addCode(clearCollection(i.targetVarName, prop))
                                funSpec.addCode(addAll(i.targetVarName, prop, i.sourceVarName))
                            }
                        }
                    }
                    is EitherTypOrModelOrPoetType.NOTHING -> TODO()
                }
            }
//            if (i.additionalDslRefsInSourceSuperclasses.isNotEmpty()) {
//                if (i.additionalDslRefsInSourceSuperclasses.size > 1) {
//                    val x = 42 // TODO breakpoint
//                }
                val targetExtendsModelEither = i.targetGenModel.extends[C.DEFAULT]?.typeClassOrDslRef
                if ( targetExtendsModelEither != null && targetExtendsModelEither is EitherTypOrModelOrPoetType.EitherModel) {
                    val targetExtendsModel = genCtx.genModel(targetExtendsModelEither.modelSubElementRef)
                    val sourceGenModelExtendsModelEither = i.sourceGenModel.extends[C.DEFAULT]?.typeClassOrDslRef
                    if (sourceGenModelExtendsModelEither != null && sourceGenModelExtendsModelEither is EitherTypOrModelOrPoetType.EitherModel) {
                        genCtx.syntheticFillerDatas.add(SynthFillerData(targetExtendsModelEither.modelSubElementRef, sourceGenModelExtendsModelEither.modelSubElementRef, via = "superExtends of target is Model in deepCopyAndClone of $currentBuildFillerData"))

                        funSpec.addStatement("%T.copyDeepInto(%L, %L)",  targetExtendsModel.modelClassName.fillerPoetType, i.targetVarName, i.sourceVarName)

                    }
                }
//            }
//            val targetExtendsModel = targetGenModel.extends[C.DEFAULT]?.typeClassOrDslRef
//            if ( targetExtendsModel != null && targetExtendsModel is EitherTypOrModelOrPoetType.EitherModel) {
//                val extendsTargetGenModel = genCtx.genModel(targetExtendsModel.modelSubElementRef)
//                val superClassPropIntersect = targetGenModel.superclassProps.values.intersect(extendsTargetGenModel.superclassProps.values)
//
//                ;xxx;
//                            genCtx . syntheticFillerDatas . add (FillerData(
//                        targetExtendsModel.modelSubElementRef,
//                        targetExtendsModel.modelSubElementRef
//                    ))
//                    val extendsClassName = targetExtendsModel . modelClassName . poetType as ClassName
//                val extendsFillerClassName = ClassName("${extendsClassName.packageName}.filler", "Filler${extendsClassName.simpleName}")
//                funSpec.addStatement("%T.copyDeepInto(%L, %L)",  extendsFillerClassName, i.targetVarName, i.sourceVarName)
//                genCtx.syntheticFillerDatas.add(FillerData(targetExtendsModel.modelSubElementRef, i.sourceGenModel.modelSubElRef))
//            }
//            val sourceExtendsModel = i.sourceGenModel.extends[C.DEFAULT]?.typeClassOrDslRef
//            if (sourceExtendsModel != null && sourceExtendsModel is EitherTypOrModelOrPoetType.EitherModel) {
//                genCtx.syntheticFillerDatas.add(FillerData(sourceExtendsModel.modelSubElementRef, sourceExtendsModel.modelSubElementRef))
//            }
            funSpec.addStatement("return %L", i.targetVarName)
            builder.addFunction(funSpec.build())
        }
    }

}

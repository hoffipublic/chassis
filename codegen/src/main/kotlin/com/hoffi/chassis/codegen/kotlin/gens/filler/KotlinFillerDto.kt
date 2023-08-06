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
        currentFillerData = fillerData
        if (alreadyCreated(fillerData)) return
        //log.trace("build({}, {})", modelkind, currentFillerData)

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
        val funName = funNameFiller("copyShallowIgnoreModelsInto", currentFillerData)
        val funSpec = FunSpec.builder(funName.funName)
            .addParameter(i.targetVarName, i.targetGenModel.poetType)
            .addParameter(i.sourceVarName, i.sourceGenModel.poetType)
            .returns(i.targetGenModel.poetType)
        nullSentinel(funSpec, i.targetVarName, i.targetGenModel)
        for (nonModelProp in i.allIntersectPropSet.filter {
            it.eitherTypModelOrClass !is EitherTypOrModelOrPoetType.EitherModel &&
                    Tag.PRIMARY !in it.tags && // just a copy, so do NOT copy the "identity" over
                    it.collectionType == COLLECTIONTYP.NONE
        }) {
            funSpec.addStatement("%L.%L = %L.%L", i.targetVarName, nonModelProp.name, i.sourceVarName, nonModelProp.name)
        }
        funSpec.addStatement("return %L", i.targetVarName)
        builder.addFunction(funSpec.build())
    }

    private fun copyShallowWithNewModelsInto(i: IntersectPropertys.CommonPropData) {
        val funName = funNameFiller("copyShallowWithNewModelsInto", currentFillerData)
        val funSpec = FunSpec.builder(funName.funName)
            .addParameter(i.targetVarName, i.targetGenModel.poetType)
            .addParameter(i.sourceVarName, i.sourceGenModel.poetType)
            .returns(i.targetGenModel.poetType)
        nullSentinel(funSpec, i.targetVarName, i.targetGenModel)
        funSpec.addStatement("%L(%L, %L)", funName.swapOutOriginalFunNameWith("copyShallowIgnoreModelsInto"), i.targetVarName, i.sourceVarName)
        for (prop in i.allIntersectPropSet.filter { Tag.PRIMARY !in it.tags }) { // just a copy, so do NOT copy the "identity" over
            val propEither = prop.eitherTypModelOrClass
            when (propEither) {
                is EitherTypOrModelOrPoetType.EitherModel -> {
                    when (prop.collectionType) {
                        is COLLECTIONTYP.NONE -> {
                            funSpec.addComment("beware of recursive calls, if Type or some submodel of it has a reference to this")
                            funSpec.addStatement("%L.%L = %T.%L()", i.targetVarName, prop.name, prop.poetType, "createDeepWithNewEmptyModels")
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
        val funName = funNameFiller("copyShallowAndTakeSameModelsInto", currentFillerData)
        val funSpec = FunSpec.builder(funName.funName)
            .addParameter(i.targetVarName, i.targetGenModel.poetType)
            .addParameter(i.sourceVarName, i.sourceGenModel.poetType)
            .returns(i.targetGenModel.poetType)
        nullSentinel(funSpec, i.targetVarName, i.targetGenModel)
        funSpec.addStatement("%L(%L, %L)", funName.swapOutOriginalFunNameWith("copyShallowIgnoreModelsInto"), i.targetVarName, i.sourceVarName)
        for (prop in i.allIntersectPropSet.filter { Tag.PRIMARY !in it.tags }) { // just a copy, so do NOT copy the "identity" over
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
                is EitherTypOrModelOrPoetType.NOTHING -> throw GenException("EitherTypOrModelOrPoetType is shouldn't still be NOTHING")
            }
        }
        funSpec.addStatement("return %L", i.targetVarName)
        builder.addFunction(funSpec.build())
    }

    private fun deepCopyAndDeepClone(i: IntersectPropertys.CommonPropData) {
        val funNames = listOf("copyDeepInto", "cloneDeep")
        for (theFunName in funNames) {
            if (theFunName == "cloneDeep" && (KModifier.ABSTRACT in i.targetGenModel.classModifiers || currentFillerData.businessName != C.DEFAULT) ) continue
            val funName = funNameFiller(theFunName, currentFillerData)
            val funSpec = if (theFunName == "copyDeepInto") {
                val theFunSpec = FunSpec.builder(funName.funName)
                    .addParameter(i.targetVarName, i.targetGenModel.poetType)
                    .addParameter(i.sourceVarName, i.sourceGenModel.poetType)
                nullSentinel(theFunSpec, i.targetVarName, i.targetGenModel)
                theFunSpec
            } else {
                val theFunSpec = FunSpec.builder(funName.funName)
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

            funSpec.addStatement("%L(%L, %L)", funName.swapOutOriginalFunNameWith("copyShallowIgnoreModelsInto"), i.targetVarName, i.sourceVarName)
            for (prop in i.allIntersectPropSet.filter { Tag.PRIMARY !in it.tags }) {
                val propEither = prop.eitherTypModelOrClass
                when (propEither) {
                    is EitherTypOrModelOrPoetType.EitherModel -> {
                        genCtx.syntheticFillerDatas.add(SynthFillerData(currentFillerData.businessName, propEither.modelSubElementRef, propEither.modelSubElementRef, via = "propTypeEither is Model in deepCopyAndClone of $currentFillerData"))
                        val propEitherModelFillerClassName = prop.eitherTypModelOrClass.modelClassName.fillerPoetType
                        when (prop.collectionType) {
                            is COLLECTIONTYP.NONE -> {
                                funSpec.addCode(
                                    """if (${i.sourceVarName}.${prop.name} === %T.NULL)
                                        |    ${i.targetVarName}.${prop.name} = ${i.sourceVarName}.${prop.name}
                                        |else
                                        |    %T.%L(${i.targetVarName}.${prop.name}, ${i.sourceVarName}.${prop.name})
                                        |""".trimMargin(),
                                    prop.poetType, propEitherModelFillerClassName, "copyDeepInto"
                                )
                            }
                            is COLLECTIONTYP.SET, is COLLECTIONTYP.LIST, is COLLECTIONTYP.COLLECTION, is COLLECTIONTYP.ITERABLE -> {
                                funSpec.addCode(clearCollection(i.targetVarName, prop))
                                if (theFunName == "cloneDeep") {
                                    funSpec.addCode(addAllMapped(i.targetVarName, prop, i.sourceVarName, propEitherModelFillerClassName, funName.funName))
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
            //val targetExtendsModelEither = i.targetGenModel.extends[C.DEFAULT]?.typeClassOrDslRef
            //if ( targetExtendsModelEither != null && targetExtendsModelEither is EitherTypOrModelOrPoetType.EitherModel) {
            //    val targetExtendsModel = genCtx.genModel(targetExtendsModelEither.modelSubElementRef)
            //    val sourceGenModelExtendsModelEither = i.sourceGenModel.extends[C.DEFAULT]?.typeClassOrDslRef
            //    if (sourceGenModelExtendsModelEither != null && sourceGenModelExtendsModelEither is EitherTypOrModelOrPoetType.EitherModel) {
            //        genCtx.syntheticFillerDatas.add(SynthFillerData(C.DEFAULT, targetExtendsModelEither.modelSubElementRef, sourceGenModelExtendsModelEither.modelSubElementRef, via = "superExtends of target is Model in deepCopyAndClone of $currentFillerData"))
            //        funSpec.addStatement("%T.copyDeepInto(%L, %L)",  targetExtendsModel.modelClassName.fillerPoetType, i.targetVarName, i.sourceVarName)
            //    }
            //}
            funSpec.addStatement("return %L", i.targetVarName)
            builder.addFunction(funSpec.build())
        }
    }

}

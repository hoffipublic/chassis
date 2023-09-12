package com.hoffi.chassis.codegen.kotlin.gens.filler

import com.hoffi.chassis.chassismodel.C
import com.hoffi.chassis.chassismodel.dsl.GenException
import com.hoffi.chassis.chassismodel.typ.COLLECTIONTYP
import com.hoffi.chassis.codegen.kotlin.GenCtxWrapper
import com.hoffi.chassis.codegen.kotlin.IntersectPropertys
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
        currentAHasCopyBoundryData = fillerData
        if (alreadyCreated(currentFillerData)) {
            if (currentFillerData !is SynthFillerData) log.warn("Already Created specific $modelkind, FILLER: $currentFillerData for $this")
            return
        }
        //log.trace("build({}, {})", modelkind, currentFillerData)

        val intersectPropsData = IntersectPropertys.intersectPropsOf(
            genCtx,
            genCtx.genModelFromDsl(fillerData.targetDslRef),
            genCtx.genModelFromDsl(fillerData.sourceDslRef), "", ""
        )

        if (cloneFillersNotCreated) {
            cloneFillersNotCreated = false
            cloneFillerFunctions(intersectPropsData)
        }

        copyShallowIgnoreModelsInto(intersectPropsData)
        copyShallowWithNewModelsInto(intersectPropsData)
        copyShallowAndTakeSameModelsInto(intersectPropsData)
        deepCopyAndDeepClone(intersectPropsData)
        super.alreadyCreated = true
    }

    private fun copyShallowIgnoreModelsInto(i: IntersectPropertys.CommonPropData) {
        val funName = funNameExpanded("copyShallowIgnoreModelsInto", currentFillerData)
        val funSpec = FunSpec.builder(funName.funName)
            .addParameter(i.targetVarName, i.targetGenModelFromDsl.poetType)
            .addParameter(i.sourceVarName, i.sourceGenModelFromDsl.poetType)
            .returns(i.targetGenModelFromDsl.poetType)
        nullSentinel(funSpec, i.targetVarName, i.targetGenModelFromDsl)
        for (prop in i.allIntersectPropSet.filter {
            it.eitherTypModelOrClass !is EitherTypOrModelOrPoetType.EitherModel &&
                    Tag.PRIMARY !in it.tags && // just a copy, so do NOT copy the "identity" over
                    it.collectionType == COLLECTIONTYP.NONE
        }) {
            propBoundry(prop,
                noPropBoundry = {
                    funSpec.addStatement("%L%L.%L = %L.%L", if (prop.immutable) "// " else "", i.targetVarName, prop.name(), i.sourceVarName, prop.name())
                },
                IGNORE = { copyBoundry -> funSpec.addComment("copyBoundry ${copyBoundry.copyType} ${copyBoundry.boundryType} ${prop.name()}") },
                ELSE = { copyBoundry -> funSpec.addComment("TODO ${copyBoundry.copyType} ${prop.name()} ${prop.propTypeSimpleNameCap} of ${prop.poetType}") },
            )
        }
        funSpec.addStatement("return %L", i.targetVarName)
        builder.addFunction(funSpec.build())
    }

    private fun copyShallowWithNewModelsInto(i: IntersectPropertys.CommonPropData) {
        val funName = funNameExpanded("copyShallowWithNewModelsInto", currentFillerData)
        val funSpec = FunSpec.builder(funName.funName)
            .addParameter(i.targetVarName, i.targetGenModelFromDsl.poetType)
            .addParameter(i.sourceVarName, i.sourceGenModelFromDsl.poetType)
            .returns(i.targetGenModelFromDsl.poetType)
        nullSentinel(funSpec, i.targetVarName, i.targetGenModelFromDsl)
        funSpec.addStatement("%L(%L, %L)", funName.swapOutOriginalFunNameWith("copyShallowIgnoreModelsInto"), i.targetVarName, i.sourceVarName)
        for (prop in i.allIntersectPropSet.filter { Tag.PRIMARY !in it.tags }) { // just a copy, so do NOT copy the "identity" over
            val propEither = prop.eitherTypModelOrClass
            when (propEither) {
                is EitherTypOrModelOrPoetType.EitherModel -> {
                    when (prop.collectionType) {
                        is COLLECTIONTYP.NONE -> {
                            propBoundry(prop,
                                noPropBoundry = {
                                    funSpec.addComment("beware of recursive calls, if Type or some submodel of it has a reference to this")
                                    funSpec.addStatement("%L%L.%L = %T.%L()", if (prop.immutable) "// " else "", i.targetVarName, prop.name(), prop.poetType, "createDeepWithNewEmptyModels")
                                },
                                IGNORE = { copyBoundry -> funSpec.addComment("copyBoundry ${copyBoundry.copyType} ${copyBoundry.boundryType} ${prop.name()}") },
                                ELSE = { copyBoundry -> funSpec.addComment("TODO ${copyBoundry.copyType} ${prop.name()} ${prop.propTypeSimpleNameCap} of ${prop.poetType}") },
                            )
                        }
                        is COLLECTIONTYP.LIST, is COLLECTIONTYP.SET, is COLLECTIONTYP.COLLECTION, is COLLECTIONTYP.ITERABLE -> {
                            propBoundry(prop,
                                noPropBoundry = {
                                    funSpec.addCode(clearCollection(i.targetVarName, prop))
                                },
                                IGNORE = { copyBoundry -> funSpec.addComment("copyBoundry ${copyBoundry.copyType} ${copyBoundry.boundryType} ${prop.name()}") },
                                ELSE = { copyBoundry -> funSpec.addComment("TODO ${copyBoundry.copyType} ${prop.name()} ${prop.propTypeSimpleNameCap} of ${prop.poetType}") },
                            )
                        }
                    }
                }
                is EitherTypOrModelOrPoetType.EitherPoetType -> {
                    when (prop.collectionType) {
                        is COLLECTIONTYP.NONE -> {}
                        is COLLECTIONTYP.LIST, is COLLECTIONTYP.SET, is COLLECTIONTYP.COLLECTION, is COLLECTIONTYP.ITERABLE -> {
                            propBoundry(prop,
                                noPropBoundry = {
                                    funSpec.addCode(clearCollection(i.targetVarName, prop))
                                },
                                IGNORE = { copyBoundry -> funSpec.addComment("copyBoundry ${copyBoundry.copyType} ${copyBoundry.boundryType} ${prop.name()}") },
                                ELSE = { copyBoundry -> funSpec.addComment("TODO ${copyBoundry.copyType} ${prop.name()} ${prop.propTypeSimpleNameCap} of ${prop.poetType}") },
                            )
                        }
                    }
                }
                is EitherTypOrModelOrPoetType.EitherTyp -> {
                    when (prop.collectionType) {
                        is COLLECTIONTYP.NONE -> { }
                        is COLLECTIONTYP.LIST, is COLLECTIONTYP.SET, is COLLECTIONTYP.COLLECTION, is COLLECTIONTYP.ITERABLE -> {
                            propBoundry(prop,
                                noPropBoundry = {
                                    funSpec.addCode(clearCollection(i.targetVarName, prop))
                                },
                                IGNORE = { copyBoundry -> funSpec.addComment("copyBoundry ${copyBoundry.copyType} ${copyBoundry.boundryType} ${prop.name()}") },
                                ELSE = { copyBoundry -> funSpec.addComment("TODO ${copyBoundry.copyType} ${prop.name()} ${prop.propTypeSimpleNameCap} of ${prop.poetType}") },
                            )
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
        val funName = funNameExpanded("copyShallowAndTakeSameModelsInto", currentFillerData)
        val funSpec = FunSpec.builder(funName.funName)
            .addParameter(i.targetVarName, i.targetGenModelFromDsl.poetType)
            .addParameter(i.sourceVarName, i.sourceGenModelFromDsl.poetType)
            .returns(i.targetGenModelFromDsl.poetType)
        nullSentinel(funSpec, i.targetVarName, i.targetGenModelFromDsl)
        funSpec.addStatement("%L(%L, %L)", funName.swapOutOriginalFunNameWith("copyShallowIgnoreModelsInto"), i.targetVarName, i.sourceVarName)
        for (prop in i.allIntersectPropSet.filter { Tag.PRIMARY !in it.tags }) { // just a copy, so do NOT copy the "identity" over
            val propEither = prop.eitherTypModelOrClass
            when (propEither) {
                is EitherTypOrModelOrPoetType.EitherModel -> {
                    when (prop.collectionType) {
                        is COLLECTIONTYP.NONE -> {
                            propBoundry(prop,
                                noPropBoundry = {
                                    funSpec.addStatement("%L%L.%L = %L.%L", if (prop.immutable) "// " else "", i.targetVarName, prop.name(), i.sourceVarName, prop.name())
                                },
                                IGNORE = { copyBoundry -> funSpec.addComment("copyBoundry ${copyBoundry.copyType} ${copyBoundry.boundryType} ${prop.name()}") },
                                ELSE = { copyBoundry -> funSpec.addComment("TODO ${copyBoundry.copyType} ${prop.name()} ${prop.propTypeSimpleNameCap} of ${prop.poetType}") },
                            )
                        }
                        is COLLECTIONTYP.LIST, is COLLECTIONTYP.COLLECTION, is COLLECTIONTYP.ITERABLE -> {
                            propBoundry(prop,
                                noPropBoundry = {
                                    funSpec.addStatement("%L%L.%L%L.clear()", if (Tag.COLLECTION_IMMUTABLE in prop.tags) "// " else "", i.targetVarName, prop.name(), if (prop.isNullable) "?" else "")
                                    funSpec.addStatement("%L%L.%%LL.addAll(%L.%L)", if (Tag.COLLECTION_IMMUTABLE in prop.tags) "// " else "", i.targetVarName, prop.name(), if (prop.isNullable) "?" else "", i.sourceVarName, prop.name())
                                },
                                IGNORE = { copyBoundry -> funSpec.addComment("copyBoundry ${copyBoundry.copyType} ${copyBoundry.boundryType} ${prop.name()}") },
                                ELSE = { copyBoundry -> funSpec.addComment("TODO ${copyBoundry.copyType} ${prop.name()} ${prop.propTypeSimpleNameCap} of ${prop.poetType}") },
                            )
                        }
                        is COLLECTIONTYP.SET -> {
                            propBoundry(prop,
                                noPropBoundry = {
                                    funSpec.addStatement("%L%L.%L%L.clear()", if (Tag.COLLECTION_IMMUTABLE in prop.tags) "// " else "", i.targetVarName, prop.name(), if (prop.isNullable) "?" else "")
                                    funSpec.addStatement("%L%L.%L%L.addAll(%L.%L%L.toList()%L)", if (Tag.COLLECTION_IMMUTABLE in prop.tags) "// " else "", i.targetVarName, prop.name(), if (prop.isNullable) "?" else "", i.sourceVarName, prop.name(), if (prop.isNullable) "?" else "", if (prop.isNullable) " ?: emptyList()" else "")
                                },
                                IGNORE = { copyBoundry -> funSpec.addComment("copyBoundry ${copyBoundry.copyType} ${copyBoundry.boundryType} ${prop.name()}") },
                                ELSE = { copyBoundry -> funSpec.addComment("TODO ${copyBoundry.copyType} ${prop.name()} ${prop.propTypeSimpleNameCap} of ${prop.poetType}") },
                            )
                        }
                    }
                }
                is EitherTypOrModelOrPoetType.EitherPoetType -> {
                    when (prop.collectionType) {
                        is COLLECTIONTYP.NONE -> {}
                        is COLLECTIONTYP.LIST, is COLLECTIONTYP.COLLECTION, is COLLECTIONTYP.ITERABLE -> {
                            propBoundry(prop,
                                noPropBoundry = {
                                    funSpec.addStatement("%L%L.%L%L.clear()", if (Tag.COLLECTION_IMMUTABLE in prop.tags) "// " else "", i.targetVarName, prop.name(), if (prop.isNullable) "?" else "")
                                    funSpec.addStatement("%L%L.%L%L.addAll(%L.%L)", if (Tag.COLLECTION_IMMUTABLE in prop.tags) "// " else "", i.targetVarName, prop.name(), if (prop.isNullable) "?" else "", i.sourceVarName, prop.name())
                                },
                                IGNORE = { copyBoundry -> funSpec.addComment("copyBoundry ${copyBoundry.copyType} ${copyBoundry.boundryType} ${prop.name()}") },
                                ELSE = { copyBoundry -> funSpec.addComment("TODO ${copyBoundry.copyType} ${prop.name()} ${prop.propTypeSimpleNameCap} of ${prop.poetType}") },
                            )
                        }
                        is COLLECTIONTYP.SET -> {
                            propBoundry(prop,
                                noPropBoundry = {
                                    funSpec.addStatement("%L%L.%L%L.clear()", if (Tag.COLLECTION_IMMUTABLE in prop.tags) "// " else "", i.targetVarName, prop.name(), if (prop.isNullable) "?" else "")
                                    funSpec.addStatement("%L%L.%L%L.addAll(%L.%L%L.toList()%L)", if (Tag.COLLECTION_IMMUTABLE in prop.tags) "// " else "", i.targetVarName, prop.name(), if (prop.isNullable) "?" else "", i.sourceVarName, prop.name(), if (prop.isNullable) "?" else "", if (prop.isNullable) " ?: emptyList()" else "")
                                },
                                IGNORE = { copyBoundry -> funSpec.addComment("copyBoundry ${copyBoundry.copyType} ${copyBoundry.boundryType} ${prop.name()}") },
                                ELSE = { copyBoundry -> funSpec.addComment("TODO ${copyBoundry.copyType} ${prop.name()} ${prop.propTypeSimpleNameCap} of ${prop.poetType}") },
                            )
                        }
                    }
                }
                is EitherTypOrModelOrPoetType.EitherTyp -> {
                    when (prop.collectionType) {
                        is COLLECTIONTYP.NONE -> { }
                        is COLLECTIONTYP.LIST, is COLLECTIONTYP.COLLECTION, is COLLECTIONTYP.ITERABLE -> {
                            propBoundry(prop,
                                noPropBoundry = {
                                    funSpec.addStatement("%L%L.%L%L.clear()", if (Tag.COLLECTION_IMMUTABLE in prop.tags) "// " else "", i.targetVarName, prop.name(), if (prop.isNullable) "?" else "")
                                    funSpec.addStatement("%L%L.%L%L.addAll(%L.%L)", if (Tag.COLLECTION_IMMUTABLE in prop.tags) "// " else "", i.targetVarName, prop.name(), if (prop.isNullable) "?" else "", i.sourceVarName, prop.name())
                                },
                                IGNORE = { copyBoundry -> funSpec.addComment("copyBoundry ${copyBoundry.copyType} ${copyBoundry.boundryType} ${prop.name()}") },
                                ELSE = { copyBoundry -> funSpec.addComment("TODO ${copyBoundry.copyType} ${prop.name()} ${prop.propTypeSimpleNameCap} of ${prop.poetType}") },
                            )
                        }
                        is COLLECTIONTYP.SET -> {
                            propBoundry(prop,
                                noPropBoundry = {
                                    funSpec.addStatement("%L%L.%L%L.clear()", if (Tag.COLLECTION_IMMUTABLE in prop.tags) "// " else "", i.targetVarName, prop.name(), if (prop.isNullable) "?" else "")
                                    funSpec.addStatement("%L%L.%L%L.addAll(%L.%L.toList())", if (Tag.COLLECTION_IMMUTABLE in prop.tags) "// " else "", i.targetVarName, prop.name(), if (prop.isNullable) "?" else "", i.sourceVarName, prop.name())
                                },
                                IGNORE = { copyBoundry -> funSpec.addComment("copyBoundry ${copyBoundry.copyType} ${copyBoundry.boundryType} ${prop.name()}") },
                                ELSE = { copyBoundry -> funSpec.addComment("TODO ${copyBoundry.copyType} ${prop.name()} ${prop.propTypeSimpleNameCap} of ${prop.poetType}") },
                            )
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
            if (theFunName == "cloneDeep" && (KModifier.ABSTRACT in i.targetGenModelFromDsl.classModifiers || currentFillerData.businessName != C.DEFAULT) ) continue
            val funName = funNameExpanded(theFunName, currentFillerData)
            val funSpec = if (theFunName == "copyDeepInto") {
                val theFunSpec = FunSpec.builder(funName.funName)
                    .addParameter(i.targetVarName, i.targetGenModelFromDsl.poetType)
                    .addParameter(i.sourceVarName, i.sourceGenModelFromDsl.poetType)
                nullSentinel(theFunSpec, i.targetVarName, i.targetGenModelFromDsl)
                theFunSpec
            } else {
                val theFunSpec = FunSpec.builder(funName.funName)
                    .addParameter(i.sourceVarName, i.sourceGenModelFromDsl.poetType)
                if (i.targetGenModelFromDsl.isUuidPrimary) {
                    theFunSpec.addStatement("val %L = %T._internal_createWithUuid()", i.targetVarName, i.targetGenModelFromDsl.poetType)
                } else {
                    val allPKs = i.sourceGenModelFromDsl.allProps.values.filter { Tag.PRIMARY in it.tags }
                    theFunSpec.addStatement("val %L = %T._internal_create()", i.targetVarName, i.targetGenModelFromDsl.poetType)
                    for (pkProp in allPKs) {
                        theFunSpec.addStatement("%L.%L = %L.%L", i.targetVarName, pkProp.name(), i.sourceVarName, pkProp.name())
                    }
                }
                theFunSpec
            }
            funSpec.returns(i.targetGenModelFromDsl.poetType)

            funSpec.addStatement("%L(%L, %L)", funName.swapOutOriginalFunNameWith("copyShallowIgnoreModelsInto"), i.targetVarName, i.sourceVarName)
            for (prop in i.allIntersectPropSet.filter { Tag.PRIMARY !in it.tags }) {
                val propEither = prop.eitherTypModelOrClass
                when (propEither) {
                    is EitherTypOrModelOrPoetType.EitherModel -> {
                        genCtx.addSyntheticFillerData(SynthFillerData.create(propEither.modelSubElementRef, propEither.modelSubElementRef, currentFillerData, via = "propTypeEither is Model in deepCopyAndClone of $currentFillerData"))
                        val propEitherModelFillerClassName = propEither.modelClassName.fillerPoetType
                        when (prop.collectionType) {
                            is COLLECTIONTYP.NONE -> {
                                if (prop.immutable) {
                                    funSpec.addComment("%L.%L is immutable", i.targetVarName, prop.name())
                                } else {
                                    propBoundry(prop,
                                        noPropBoundry = {
                                            funSpec.addCode(
                                                """if (${i.sourceVarName}.${prop.name()} === %T.NULL)
                                                    |    ${i.targetVarName}.${prop.name()} = ${i.sourceVarName}.${prop.name()}
                                                    |else
                                                    |    %T.%L(${i.targetVarName}.${prop.name()}, ${i.sourceVarName}.${prop.name()})
                                                    |""".trimMargin(),
                                                prop.poetType, propEitherModelFillerClassName, "copyDeepInto"
                                            )
                                        },
                                        IGNORE = { copyBoundry -> funSpec.addComment("copyBoundry ${copyBoundry.copyType} ${copyBoundry.boundryType} ${prop.name()}") },
                                        ELSE = { copyBoundry -> funSpec.addComment("TODO ${copyBoundry.copyType} ${prop.name()} ${prop.propTypeSimpleNameCap} of ${prop.poetType}") },
                                    )
                                }
                            }
                            is COLLECTIONTYP.SET, is COLLECTIONTYP.LIST, is COLLECTIONTYP.COLLECTION, is COLLECTIONTYP.ITERABLE -> {
                                propBoundry(prop,
                                    noPropBoundry = {
                                        funSpec.addCode(clearCollection(i.targetVarName, prop))
                                        if (theFunName == "cloneDeep") {
                                            funSpec.addCode(addAllMapped(i.targetVarName, prop, i.sourceVarName, propEitherModelFillerClassName, funName.funName))
                                        } else {
                                            funSpec.addCode(addAll(i.targetVarName, prop, i.sourceVarName))
                                        }
                                    },
                                    IGNORE = { copyBoundry -> funSpec.addComment("copyBoundry ${copyBoundry.copyType} ${copyBoundry.boundryType} ${prop.name()}") },
                                    ELSE = { copyBoundry -> funSpec.addComment("TODO ${copyBoundry.copyType} ${prop.name()} ${prop.propTypeSimpleNameCap} of ${prop.poetType}") },
                                )
                            }
                        }
                    }
                    is EitherTypOrModelOrPoetType.EitherPoetType -> {
                        when (prop.collectionType) {
                            is COLLECTIONTYP.NONE -> {}
                            is COLLECTIONTYP.LIST, is COLLECTIONTYP.SET, is COLLECTIONTYP.COLLECTION, is COLLECTIONTYP.ITERABLE -> {
                                propBoundry(prop,
                                    noPropBoundry = {
                                        funSpec.addCode(clearCollection(i.targetVarName, prop))
                                        funSpec.addCode(addAll(i.targetVarName, prop, i.sourceVarName))
                                    },
                                    IGNORE = { copyBoundry -> funSpec.addComment("copyBoundry ${copyBoundry.copyType} ${copyBoundry.boundryType} ${prop.name()}") },
                                    ELSE = { copyBoundry -> funSpec.addComment("TODO ${copyBoundry.copyType} ${prop.name()} ${prop.propTypeSimpleNameCap} of ${prop.poetType}") },
                                )
                            }
                        }
                    }
                    is EitherTypOrModelOrPoetType.EitherTyp -> {
                        when (prop.collectionType) {
                            is COLLECTIONTYP.NONE -> {}
                            is COLLECTIONTYP.SET, is COLLECTIONTYP.LIST, is COLLECTIONTYP.COLLECTION, is COLLECTIONTYP.ITERABLE -> {
                                propBoundry(prop,
                                    noPropBoundry = {
                                        funSpec.addCode(clearCollection(i.targetVarName, prop))
                                        funSpec.addCode(addAll(i.targetVarName, prop, i.sourceVarName))
                                    },
                                    IGNORE = { copyBoundry -> funSpec.addComment("copyBoundry ${copyBoundry.copyType} ${copyBoundry.boundryType} ${prop.name()}") },
                                    ELSE = { copyBoundry -> funSpec.addComment("TODO ${copyBoundry.copyType} ${prop.name()} ${prop.propTypeSimpleNameCap} of ${prop.poetType}") },
                                )
                            }
                        }
                    }
                    is EitherTypOrModelOrPoetType.NOTHING -> TODO()
                }
            }
            //val targetExtendsModelEither = i.targetGenModelFromDsl.extends[C.DEFAULT]?.typeClassOrDslRef
            //if ( targetExtendsModelEither != null && targetExtendsModelEither is EitherTypOrModelOrPoetType.EitherModel) {
            //    val targetExtendsModel = genCtx.genModelFromDsl(targetExtendsModelEither.modelSubElementRef)
            //    val sourceGenModelExtendsModelEither = i.sourceGenModelFromDsl.extends[C.DEFAULT]?.typeClassOrDslRef
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

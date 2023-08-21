package com.hoffi.chassis.codegen.kotlin.gens

import com.hoffi.chassis.chassismodel.C
import com.hoffi.chassis.chassismodel.Cap
import com.hoffi.chassis.chassismodel.RuntimeDefaults
import com.hoffi.chassis.chassismodel.decap
import com.hoffi.chassis.chassismodel.dsl.GenException
import com.hoffi.chassis.chassismodel.typ.COLLECTIONTYP
import com.hoffi.chassis.codegen.kotlin.GenCtxWrapper
import com.hoffi.chassis.codegen.kotlin.IntersectPropertys
import com.hoffi.chassis.codegen.kotlin.gens.filler.KotlinFillerTablePoetStatements
import com.hoffi.chassis.shared.EitherTypOrModelOrPoetType
import com.hoffi.chassis.shared.dsl.DslRef
import com.hoffi.chassis.shared.dsl.IDslRef
import com.hoffi.chassis.shared.parsedata.GenModel
import com.hoffi.chassis.shared.shared.*
import com.hoffi.chassis.shared.shared.reffing.MODELKIND
import com.hoffi.chassis.shared.shared.reffing.MODELREFENUM
import com.squareup.kotlinpoet.*
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy

context(GenCtxWrapper)
abstract class ABaseForCrudAndFiller(val originalAHasCopyBoundrysData: AHasCopyBoundrysData, val modelkind: MODELKIND) {
    var alreadyCreated: Boolean = false
    var currentAHasCopyBoundryData = originalAHasCopyBoundrysData

    data class FunName(val funName: String, val originalFunName:String) {
        enum class FUNNAMEMODE { DEFAULT, JUSTPREPOSTFIX}
        var prefix: String = ""  // set in funNameExpanded..() as "what eventually was prefixed"
        var postfix: String = "" // set in funNameExpanded..() as "what eventually was postfixed"
        fun swapOutOriginalFunNameWith(otherFunName: String): String = if (prefix.isNotBlank()) {
            prefix + otherFunName.Cap() + postfix.Cap()
        } else {
            otherFunName + postfix.Cap()
        }
        fun itOrThis(): String   = if (originalFunName.startsWith("batch")) "this" else "it"
        fun sourceOrIt(sourceVarName: String): String = if (originalFunName.startsWith("batch")) "it" else sourceVarName
    }
    private fun funNameExpandedDefault(
        aHasCopyBoundrysData: AHasCopyBoundrysData,
        funNameMode: FunName.FUNNAMEMODE,
        origFunName: String,
        prefix: String,  // additional/other!! prefix than FunName.prefix
        postfix: String // additional/other!! prefix than FunName.prefix
    ) = when (modelkind) {
        MODELKIND.DTOKIND -> {
            if (aHasCopyBoundrysData.sourceDslRef.parentDslRef == aHasCopyBoundrysData.targetDslRef.parentDslRef || funNameMode == FunName.FUNNAMEMODE.JUSTPREPOSTFIX) {
                // keep funName as is
                FunName(origFunName, origFunName).also { it.prefix = prefix.decap(); it.postfix = postfix.Cap() }
            } else {
                // C.DEFAULT fill for "some other" Model
                val sourceGenModel: GenModel = genCtx.genModel(aHasCopyBoundrysData.sourceDslRef)
                val thePrefix = sourceGenModel.asVarName + prefix.Cap()
                val thePostfix = postfix.Cap()
                val theFunName = thePrefix + origFunName.Cap() + thePostfix
                FunName(theFunName, origFunName).also { it.prefix = thePrefix; it.postfix = thePostfix }
            }
        }
        MODELKIND.TABLEKIND -> {
            if (aHasCopyBoundrysData.sourceDslRef.parentDslRef == aHasCopyBoundrysData.targetDslRef.parentDslRef || funNameMode == FunName.FUNNAMEMODE.JUSTPREPOSTFIX) {
                FunName(origFunName, origFunName).also { it.prefix = prefix; it.postfix = postfix }
            } else {
                // C.DEFAULT fill for "some other" Model
                val sourceGenModel: GenModel = genCtx.genModel(aHasCopyBoundrysData.sourceDslRef)
                if (aHasCopyBoundrysData.targetDslRef !is DslRef.table) {
                    // special case e.g. DTO <-- TABLE
                    val targetGenModel: GenModel = genCtx.genModel(aHasCopyBoundrysData.targetDslRef)
                    val thePrefix = targetGenModel.asVarName + prefix.Cap()
                    val thePostfix = postfix.Cap()
                    val theFunName = thePrefix + origFunName.Cap() + thePostfix
                    FunName(theFunName, origFunName).also { it.prefix = thePrefix; it.postfix = thePostfix }
                } else {
                    val thePrefix = sourceGenModel.asVarName + prefix.Cap()
                    val thePostfix = postfix.Cap()
                    val theFunName = thePrefix + origFunName.Cap() + thePostfix
                    FunName(theFunName, origFunName).also { it.prefix = thePrefix; it.postfix = thePostfix }
                }
            }
        }
    }
    fun funNameExpanded(origFunName: String, aHasCopyBoundrysData: AHasCopyBoundrysData, funNameMode: FunName.FUNNAMEMODE = FunName.FUNNAMEMODE.DEFAULT, prefix: String = "", postfix: String = ""): FunName {
        val funName = if (aHasCopyBoundrysData.businessName == C.DEFAULT || funNameMode == FunName.FUNNAMEMODE.JUSTPREPOSTFIX) {
            funNameExpandedDefault(aHasCopyBoundrysData, funNameMode, origFunName, prefix, postfix)
        } else {
            when (modelkind) {
                MODELKIND.DTOKIND -> {
                    val defaultFunName = funNameExpandedDefault(aHasCopyBoundrysData, funNameMode, origFunName, prefix, postfix)
                    val thePrefix = aHasCopyBoundrysData.businessName.decap() + defaultFunName.prefix.Cap()
                    val thePostfix = defaultFunName.postfix
                    val theFunName = thePrefix + origFunName.Cap() + thePostfix
                    FunName(theFunName, origFunName).also { it.prefix = thePrefix; it.postfix = thePostfix }
                }

                MODELKIND.TABLEKIND -> {
                val defaultFunName = funNameExpandedDefault(aHasCopyBoundrysData, funNameMode, origFunName, prefix, postfix)
                    val thePrefix = aHasCopyBoundrysData.businessName.decap() + defaultFunName.prefix.Cap()
                    val thePostfix = defaultFunName.postfix
                    val theFunName = thePrefix + origFunName.Cap() + thePostfix
                    FunName(theFunName, origFunName).also { it.prefix = thePrefix; it.postfix = thePostfix }
                }
            }
        }
        return funName
    }

    fun propFiller(targetDslRef: IDslRef, modelrefenum: MODELREFENUM): ClassName {
        val swappedDslRef = when (modelrefenum) {
            MODELREFENUM.MODEL -> throw GenException("MODELs do not have fillers themselves")
            MODELREFENUM.DTO -> DslRef.dto(C.DEFAULT, targetDslRef.parentDslRef)
            MODELREFENUM.TABLE -> DslRef.table(C.DEFAULT, targetDslRef.parentDslRef)
        }
        val swappedGenModel = genCtx.genModel(swappedDslRef)
        return ClassName((swappedGenModel.poetType as ClassName).packageName + ".filler", "Filler" + (swappedGenModel.poetType as ClassName).simpleName)
    }
    fun propCrud(targetDslRef: IDslRef, crud: CrudData.CRUD): ClassName {
        // TODO remove sentinel?
        if (targetDslRef !is DslRef.ISubElementLevel) throw GenException("targetDslRef for propCrud($targetDslRef) always should be a (model) subelement (DTO, TABLE, ...)")
        val swappedDslRef = DslRef.table(C.DEFAULT, targetDslRef.parentDslRef)
        val swappedGenModel = genCtx.genModel(swappedDslRef)
        //CrudData.CRUD.CREATE -> ClassName((swappedGenModel.poetType as ClassName).packageName + ".sql", swappedGenModel.modelClassName.crudBasePoetTypeForAllCruds + crud.toString())
        return ClassName((swappedGenModel.poetType as ClassName).packageName + ".sql", swappedGenModel.modelClassName.crudBasePoetTypeForAllCruds.simpleName + crud.toString())
    }

    //protected fun FunSpec.Builder.addOne2ManyIncomingFKParamUuidMaps(outgoingFKs: MutableSet<FK>, kotlinGenClassTable: KotlinClassModelTable): FunSpec.Builder {
    //    var none = true
    //    for (fk in outgoingFKs) {
    //        when (fk.COLLECTIONTYP) {
    //            is COLLECTIONTYP.NONE -> {
    //                continue
    //            }
    //            is COLLECTIONTYP.COLLECTION, is COLLECTIONTYP.ITERABLE, is COLLECTIONTYP.LIST, is COLLECTIONTYP.SET -> {
    //                addParameter(ParameterSpec.builder(fk.toProp.name() + RuntimeDefaults.UUID_PROPNAME.Cap() + "ToParentUuid", Map::class.asClassName().parameterizedBy(RuntimeDefaults.classNameUUID, RuntimeDefaults.classNameUUID)).build())
    //            }
    //        }
    //    }
    //    return this
    //}

    protected fun FunSpec.Builder.addOutgoingFKParams(outgoingFKs: MutableSet<FK>, kotlinGenClassTable: KotlinClassModelTable, collectionType: COLLECTIONTYP, funNameInsertOrBatch: FunName): FunSpec.Builder {
        var none = true
        for (fk: FK in outgoingFKs) {
            // only UUID
            //val fkParamBuilder = ParameterSpec.builder(kotlinGenClassTable.fkPropVarNameUUIDs(fk).first, RuntimeDefaults.classNameUUID, fk.toProp.modifiers)
            // Dto itself
            if ( (collectionType == COLLECTIONTYP.NONE && fk.COLLECTIONTYP != COLLECTIONTYP.NONE) ||
                (collectionType != COLLECTIONTYP.NONE && fk.COLLECTIONTYP == COLLECTIONTYP.NONE) )
                continue
            val toTableDtoGenModel = genCtx.genModel(DslRef.dto(fk.toTableRef.simpleName, fk.toTableRef.parentDslRef))
            when (collectionType) {
                is COLLECTIONTYP.NONE -> {
                    val fkParamBuilder = ParameterSpec.builder(fk.toProp.name(), toTableDtoGenModel.poetType, fk.toProp.modifiers)
                    this.addParameter(fkParamBuilder.build())
                    none = false
                }
                is COLLECTIONTYP.COLLECTION, is COLLECTIONTYP.ITERABLE, is COLLECTIONTYP.LIST, is COLLECTIONTYP.SET -> {
                    //val fkParamBuilder = ParameterSpec.builder(kotlinGenClassTable.fkPropVarName(fk).first, toTableDtoGenModel.poetType, fk.toProp.modifiers)
                    val fkParamBuilder =if (funNameInsertOrBatch.originalFunName.startsWith("batch")) {
                        ParameterSpec.builder(fk.toProp.name() + RuntimeDefaults.UUID_PROPNAME.Cap() + "ToParentUuid", Map::class.asClassName().parameterizedBy(RuntimeDefaults.classNameUUID, RuntimeDefaults.classNameUUID))
                    } else {
                        ParameterSpec.builder(kotlinGenClassTable.fkPropVarName(fk).first, toTableDtoGenModel.poetType, fk.toProp.modifiers)
                    }
                    this.addParameter(fkParamBuilder.build())
                    none = false
                }
            }
        }
        if (none) addComment("NONE")
        return this
    }
    protected fun FunSpec.Builder.insertOutgoingFKPropUUIDs(outgoingFKs: MutableSet<FK>, funNameInsertOrBatch: FunName, i: IntersectPropertys.CommonPropData, collectionType: COLLECTIONTYP): FunSpec.Builder {
        this.addCode(buildCodeBlock { this.insertOutgoingFKPropUUIDs(outgoingFKs, funNameInsertOrBatch, i, collectionType)})
        return this
    }
    protected fun CodeBlock.Builder.insertOutgoingFKPropUUIDs(outgoingFKs: MutableSet<FK>, funNameInsertOrBatch: FunName, i: IntersectPropertys.CommonPropData, collectionType: COLLECTIONTYP): CodeBlock.Builder {
        for (fk in outgoingFKs) {
            if ( (collectionType == COLLECTIONTYP.NONE && fk.COLLECTIONTYP != COLLECTIONTYP.NONE) ||
                (collectionType != COLLECTIONTYP.NONE && fk.COLLECTIONTYP == COLLECTIONTYP.NONE) )
                continue
            when (collectionType) {
                is COLLECTIONTYP.NONE -> {
                    KotlinFillerTablePoetStatements.fillTablePropOne2OneModelUuid(this,
                        funNameInsertOrBatch,
                        i.targetPoetType,
                        i.sourceVarName,
                        fk.toProp
                    )
                    //this.addStatement(
                    //    "%L[%T.%L] = %L.%L.%L",
                    //    funNameInsertOrBatch.itOrThis(),
                    //    i.targetPoetType,
                    //    fk.toProp.name(postfix = RuntimeDefaults.UUID_PROPNAME),
                    //    funNameInsertOrBatch.sourceOrIt(i.sourceVarName), fk.toProp.name(), RuntimeDefaults.UUID_PROPNAME
                    //)
                }
                is COLLECTIONTYP.COLLECTION, is COLLECTIONTYP.ITERABLE, is COLLECTIONTYP.LIST, is COLLECTIONTYP.SET -> {
                    throw GenException("Not Here!")
                }
            }
        }
        return this
    }
    protected fun FunSpec.Builder.insertOutgoing1To1Props(outgoingFKs: MutableSet<FK>, funNameInsertOrBatch: FunName, currentCrudData: CrudData, i: IntersectPropertys.CommonPropData, collectionType: COLLECTIONTYP): FunSpec.Builder {
        this.addCode(buildCodeBlock { this.insertOutgoing1To1Props(outgoingFKs, funNameInsertOrBatch, currentCrudData, i, collectionType) })
        return this
    }
    protected fun CodeBlock.Builder.insertOutgoing1To1Props(outgoingFKs: MutableSet<FK>, funNameInsertOrBatch: FunName, currentCrudData: CrudData, i: IntersectPropertys.CommonPropData, collectionType: COLLECTIONTYP): CodeBlock.Builder {
        var none = true
        if (funNameInsertOrBatch.originalFunName.startsWith("batch")) {
            for (fk in outgoingFKs.filter { it.toProp.collectionType == COLLECTIONTYP.NONE }) {
                none = false
                beginControlFlow("val %LTo%L = %L.associateWith", i.targetGenModel.asVarName, fk.toProp.name().Cap(), i.sourceVarName + "s")
                addStatement("%L -> %L.%L", i.sourceVarName, i.sourceVarName, fk.toProp.name())
                endControlFlow()
                addStatement("%T.%L(%LTo%L.values)", propCrud(fk.toTableRef, CrudData.CRUD.CREATE), funNameInsertOrBatch.swapOutOriginalFunNameWith("batchInsertDb"), i.targetGenModel.asVarName, fk.toProp.name().Cap())
            }
        } else {
            for (fk in outgoingFKs.filter { it.toProp.collectionType == COLLECTIONTYP.NONE }) {
                none = false
                addStatement("%T.%L(%L.%L)", propCrud(fk.toTableRef, CrudData.CRUD.CREATE), funNameInsertOrBatch.swapOutOriginalFunNameWith("insertDb"), i.sourceVarName, fk.toProp.name())
            }
        }
        if (none) addStatement("// NONE")
        return this
    }
    protected fun FunSpec.Builder.addOutgoingFKProps(outgoingFKs: MutableSet<FK>, funNameInsertOrBatch: FunName, currentCrudData: CrudData, i: IntersectPropertys.CommonPropData): FunSpec.Builder {
        this.addCode(buildCodeBlock { this.addOutgoingFKProps(outgoingFKs, funNameInsertOrBatch, currentCrudData, i)})
        return this
    }
    protected fun CodeBlock.Builder.addOutgoingFKProps(outgoingFKs: MutableSet<FK>, funNameInsertOrBatch: FunName, currentCrudData: CrudData, i: IntersectPropertys.CommonPropData): CodeBlock.Builder {
        var none = true
        for (fk in outgoingFKs) {
            val kotlinGenClassTable: KotlinClassModelTable = kotlinGenCtx.kotlinGenClass(i.targetGenModel.modelSubElRef) as KotlinClassModelTable
            when (fk.toProp.collectionType) {
                is COLLECTIONTYP.NONE -> {
                   // KotlinFillerTablePoetStatements.fillTablePropTypOrPoetType(this, funNameInsertOrBatch, i.targetPoetType kotlinGenClassTable.fkPropVarNameUUID(fk).first,
                   //     fk.toProp.name(),
                   //     RuntimeDefaults.UUID_PROPNAME)
                    none = false
                    if (funNameInsertOrBatch.originalFunName.startsWith("batch")) {
                        this.addStatement(
                            "%L[%T.%L] = %LTo%L[it]!!.%L",
                            funNameInsertOrBatch.itOrThis(),
                            i.targetPoetType,
                            kotlinGenClassTable.fkPropVarNameUUID(fk).first,
                            i.targetGenModel.asVarName, fk.toProp.name().Cap(),
                            RuntimeDefaults.UUID_PROPNAME
                        )
                    } else {
                        this.addStatement(
                            "%L[%T.%L] = %L.%L.%L",
                            funNameInsertOrBatch.itOrThis(),
                            i.targetPoetType,
                            kotlinGenClassTable.fkPropVarNameUUID(fk).first,
                            i.sourceVarName,
                            fk.toProp.name(),
                            RuntimeDefaults.UUID_PROPNAME
                        )
                    }
                }
                is COLLECTIONTYP.COLLECTION, is COLLECTIONTYP.ITERABLE, is COLLECTIONTYP.LIST, is COLLECTIONTYP.SET -> {
                    none = false
                    if (funNameInsertOrBatch.originalFunName.startsWith("batch")) {
                        this.addStatement(
                            "%L[%T.%L] = %L[it.%L]!!",
                            funNameInsertOrBatch.itOrThis(),
                            i.targetPoetType,
                            kotlinGenClassTable.fkPropVarNameUUID(fk).first,
                            fk.toProp.name() + RuntimeDefaults.UUID_PROPNAME.Cap() + "ToParentUuid",
                            RuntimeDefaults.UUID_PROPNAME
                        )
                    } else {
                        this.addStatement(
                            "%L[%T.%L] = %L.%L",
                            funNameInsertOrBatch.itOrThis(),
                            i.targetPoetType,
                            kotlinGenClassTable.fkPropVarNameUUID(fk).first,
                            kotlinGenClassTable.fkPropVarName(fk).first,
                            RuntimeDefaults.UUID_PROPNAME
                        )
                    }
                }
            }
        }
        if (none) addStatement("// NONE")
        return this
    }
    /** helper for multiple analogous params from a lambda */
    protected fun <T> FunSpec.Builder.addFunCall(prefixCodeBlock: CodeBlock, iterable: Iterable<T>, postfixCodeBlock: CodeBlock, preBlockCode: String = "", iterableMapBlock: CodeBlock.Builder.(T) -> CodeBlock.Builder): FunSpec.Builder {
        this.addCode(buildCodeBlock { this.addFunCall(prefixCodeBlock, iterable, postfixCodeBlock, preBlockCode, iterableMapBlock)})
        return this
    }
    /** helper for multiple analogous params from a lambda */
    protected fun <T> CodeBlock.Builder.addFunCall(prefixCodeBlock: CodeBlock, iterable: Iterable<T>, postfixCodeBlock: CodeBlock, preBlockCode: String = "", iterableMapBlock: CodeBlock.Builder.(T) -> CodeBlock.Builder): CodeBlock.Builder {
        add(prefixCodeBlock)
        add(preBlockCode)
        for (el in iterable) {
            iterableMapBlock.invoke(this, el)
        }
        add(postfixCodeBlock)
        add("\n")
        return this
    }

    protected fun addSyntheticFillersForTableModelProp(propEitherModel: EitherTypOrModelOrPoetType.EitherModel, currentFillerData: FillerData, via: String) {
        val propModelRefSimpleName = propEitherModel.modelSubElementRef.simpleName
        val propModelParentRef = propEitherModel.modelSubElementRef.parentDslRef
        val propDtoModelDslRef = DslRef.dto(propModelRefSimpleName, propModelParentRef)
        val propTableModelDslRef = DslRef.table(propModelRefSimpleName, propModelParentRef)
        genCtx.addSyntheticFillerData(SynthFillerData.create(propTableModelDslRef, propDtoModelDslRef, currentFillerData, via = via))
        genCtx.addSyntheticFillerData(SynthFillerData.create(propDtoModelDslRef, propTableModelDslRef, currentFillerData, via = via))
    }

    protected fun addSyntheticCrudForTableModelProp(propEitherModel: EitherTypOrModelOrPoetType.EitherModel, currentCrudData: CrudData, via: String) {
        val propModelRefSimpleName = propEitherModel.modelSubElementRef.simpleName
        val propModelParentRef = propEitherModel.modelSubElementRef.parentDslRef
        val propDtoModelDslRef = DslRef.dto(propModelRefSimpleName, propModelParentRef)
        val propTableModelDslRef = DslRef.table(propModelRefSimpleName, propModelParentRef)
        genCtx.addSyntheticCrudData(SynthCrudData.create(propTableModelDslRef, propDtoModelDslRef, currentCrudData, via = via))
    }
}

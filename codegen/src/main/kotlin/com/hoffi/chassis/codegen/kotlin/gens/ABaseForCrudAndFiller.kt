package com.hoffi.chassis.codegen.kotlin.gens

import com.hoffi.chassis.chassismodel.C
import com.hoffi.chassis.chassismodel.Cap
import com.hoffi.chassis.chassismodel.PoetHelpers.addComment
import com.hoffi.chassis.chassismodel.RuntimeDefaults
import com.hoffi.chassis.chassismodel.decap
import com.hoffi.chassis.chassismodel.dsl.GenException
import com.hoffi.chassis.chassismodel.typ.COLLECTIONTYP
import com.hoffi.chassis.codegen.kotlin.GenClassNames
import com.hoffi.chassis.codegen.kotlin.GenCtxWrapper
import com.hoffi.chassis.codegen.kotlin.GenNaming
import com.hoffi.chassis.codegen.kotlin.IntersectPropertys
import com.hoffi.chassis.codegen.kotlin.gens.filler.KotlinFillerTablePoetStatements
import com.hoffi.chassis.shared.EitherTypOrModelOrPoetType
import com.hoffi.chassis.shared.dsl.DslRef
import com.hoffi.chassis.shared.parsedata.GenModel
import com.hoffi.chassis.shared.parsedata.Property
import com.hoffi.chassis.shared.shared.*
import com.hoffi.chassis.shared.shared.reffing.MODELKIND
import com.squareup.kotlinpoet.*
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import org.slf4j.LoggerFactory

context(GenCtxWrapper)
abstract class ABaseForCrudAndFiller(val originalAHasCopyBoundrysData: AHasCopyBoundrysData, val modelkind: MODELKIND) {
    protected val log = LoggerFactory.getLogger(this::class.java)
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
                val sourceGenModelFromDsl: GenModel = genCtx.genModelFromDsl(aHasCopyBoundrysData.sourceDslRef)
                val thePrefix = sourceGenModelFromDsl.asVarName + prefix.Cap()
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
                val sourceGenModelFromDsl: GenModel = genCtx.genModelFromDsl(aHasCopyBoundrysData.sourceDslRef)
                if (aHasCopyBoundrysData.targetDslRef !is DslRef.table) {
                    // special case e.g. DTO <-- TABLE
                    val targetGenModelFromDsl: GenModel = genCtx.genModelFromDsl(aHasCopyBoundrysData.targetDslRef)
                    val thePrefix = targetGenModelFromDsl.asVarName + prefix.Cap()
                    val thePostfix = postfix.Cap()
                    val theFunName = thePrefix + origFunName.Cap() + thePostfix
                    FunName(theFunName, origFunName).also { it.prefix = thePrefix; it.postfix = thePostfix }
                } else {
                    val thePrefix = sourceGenModelFromDsl.asVarName + prefix.Cap()
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

    protected fun FunSpec.Builder.addOutgoingFKParams(outgoingFKs: MutableSet<FK>, collectionType: COLLECTIONTYP, funNameInsertOrBatch: FunName): FunSpec.Builder {
        var none = true
        for (fk: FK in outgoingFKs) {
            // only UUID
            //val fkParamBuilder = ParameterSpec.builder(kotlinGenClassTable.fkPropVarNameUUIDs(fk).first, RuntimeDefaults.classNameUUID, fk.toProp.modifiers)
            // Dto itself
            if ( (collectionType == COLLECTIONTYP.NONE && fk.COLLECTIONTYP != COLLECTIONTYP.NONE) ||
                (collectionType != COLLECTIONTYP.NONE && fk.COLLECTIONTYP == COLLECTIONTYP.NONE) )
                continue
            val toTableDtoGenModel = genCtx.genModelFromDsl(DslRef.dto(fk.toTableRef.simpleName, fk.toTableRef.parentDslRef))
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
                        ParameterSpec.builder(GenNaming.fkPropVarName(fk), toTableDtoGenModel.poetType, fk.toProp.modifiers)
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
    protected fun FunSpec.Builder.insertOutgoing1To1Props(outgoingFKs: MutableSet<FK>, funNameInsertOrBatch: FunName, i: IntersectPropertys.CommonPropData): FunSpec.Builder {
        this.addCode(buildCodeBlock { this.insertOutgoing1To1Props(outgoingFKs, funNameInsertOrBatch, i) })
        return this
    }
    protected fun CodeBlock.Builder.insertOutgoing1To1Props(outgoingFKs: MutableSet<FK>, funNameInsertOrBatch: FunName, i: IntersectPropertys.CommonPropData): CodeBlock.Builder {
        var none = true
        if (funNameInsertOrBatch.originalFunName.startsWith("batch")) {
            for (fk in outgoingFKs.filter { it.toProp.collectionType == COLLECTIONTYP.NONE }) {
                none = false
                beginControlFlow("val %LTo%L = %L.associateWith", i.targetGenModelFromDsl.asVarName, fk.toProp.name().Cap(), i.sourceVarName + "s")
                addStatement("%L -> %L.%L", i.sourceVarName, i.sourceVarName, fk.toProp.name())
                endControlFlow()
                addStatement("%T.%L(%LTo%L.values)", GenClassNames.crudFor(fk.toTableRef, CrudData.CRUD.CREATE), funNameInsertOrBatch.swapOutOriginalFunNameWith("batchInsertDb"), i.targetGenModelFromDsl.asVarName, fk.toProp.name().Cap())
            }
        } else {
            for (fk in outgoingFKs.filter { it.toProp.collectionType == COLLECTIONTYP.NONE }) {
                none = false
                addStatement("%T.%L(%L.%L)", GenClassNames.crudFor(fk.toTableRef, CrudData.CRUD.CREATE), funNameInsertOrBatch.swapOutOriginalFunNameWith("insertDb"), i.sourceVarName, fk.toProp.name())
            }
        }
        if (none) addComment("NONE")
        return this
    }
    protected fun FunSpec.Builder.addOutgoingFKProps(outgoingFKs: MutableSet<FK>, funNameInsertOrBatch: FunName, i: IntersectPropertys.CommonPropData): FunSpec.Builder {
        this.addCode(buildCodeBlock { this.addOutgoingFKProps(outgoingFKs, funNameInsertOrBatch, i)})
        return this
    }
    protected fun CodeBlock.Builder.addOutgoingFKProps(outgoingFKs: MutableSet<FK>, funNameInsertOrBatch: FunName, i: IntersectPropertys.CommonPropData): CodeBlock.Builder {
        var none = true
        for (fk in outgoingFKs) {
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
                            GenNaming.fkPropVarNameUUID(fk),
                            i.targetGenModelFromDsl.asVarName, fk.toProp.name().Cap(),
                            RuntimeDefaults.UUID_PROPNAME
                        )
                    } else {
                        this.addStatement(
                            "%L[%T.%L] = %L.%L.%L",
                            funNameInsertOrBatch.itOrThis(),
                            i.targetPoetType,
                            GenNaming.fkPropVarNameUUID(fk),
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
                            GenNaming.fkPropVarNameUUID(fk),
                            fk.toProp.name() + RuntimeDefaults.UUID_PROPNAME.Cap() + "ToParentUuid",
                            RuntimeDefaults.UUID_PROPNAME
                        )
                    } else {
                        this.addStatement(
                            "%L[%T.%L] = %L.%L",
                            funNameInsertOrBatch.itOrThis(),
                            i.targetPoetType,
                            GenNaming.fkPropVarNameUUID(fk),
                            GenNaming.fkPropVarName(fk),
                            RuntimeDefaults.UUID_PROPNAME
                        )
                    }
                }
            }
        }
        if (none) addComment("NONE")
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

    /** if prop has (any) boundry it will be the one and only element contained in returned list</br>
     * if prop has no boundry at all, returned list will be empty */
    protected fun propHasBoundry(prop: Property): List<CopyBoundry> {
        val resultList = mutableListOf<CopyBoundry>()
        propBoundry(prop,
            ELSE = { copyBoundry -> resultList.add(copyBoundry) }
        ) { }
        return resultList
    }
    /** if the given prop has a boundry, the corresponding lambda will be called,</br>
     * if the corresponding lambda is not given/null then the ELSE lambda will be called (if given).</br>
     * if the given pop has no boundry at all, the noPropBoundry lambda will be called */
    protected fun propBoundry(
        prop: Property,
        IGNORE:   ((CopyBoundry) -> Unit)? = null,
        INSTANCE: ((CopyBoundry) -> Unit)? = null,
        NEW:      ((CopyBoundry) -> Unit)? = null,
        DEEP:     ((CopyBoundry) -> Unit)? = null,
        DEEPNEW:  ((CopyBoundry) -> Unit)? = null,
        ELSE:     ((CopyBoundry) -> Unit)? = null,
        noPropBoundry: () -> Unit
    ) {
        val pName = prop.name()
        val copyBoundrysForProp: MutableList<CopyBoundry> = mutableListOf()
        var propCopyBoundry = currentAHasCopyBoundryData.propNameCopyBoundrys[prop.name()]
        if (propCopyBoundry != null) copyBoundrysForProp.add(propCopyBoundry)
        propCopyBoundry = currentAHasCopyBoundryData.propRefCopyBoundrys[prop.propRef]
        if (propCopyBoundry != null) copyBoundrysForProp.add(propCopyBoundry)
        propCopyBoundry = currentAHasCopyBoundryData.modelRefCopyBoundrys[prop.containedInSubelementRef]
        if (propCopyBoundry == null) { propCopyBoundry = currentAHasCopyBoundryData.modelRefCopyBoundrys[prop.containedInSubelementRef.parentDslRef] }
        if (propCopyBoundry != null) copyBoundrysForProp.add(propCopyBoundry)
        propCopyBoundry = currentAHasCopyBoundryData.classNameCopyBoundrys[prop.poetType]
        if (propCopyBoundry != null) copyBoundrysForProp.add(propCopyBoundry)
        if (copyBoundrysForProp.isEmpty()) {
            noPropBoundry()
            return
        }
        if (copyBoundrysForProp.size > 1) {
            log.warn("more than one copyBoundrysForProp {} of {} : {}", prop, prop.containedInSubelementRef, copyBoundrysForProp.joinToString("', '", "'", "'"))
        }

        val copyBoundry = copyBoundrysForProp.first()
        when (copyBoundry.copyType) {
            COPYTYPE.IGNORE ->   if (IGNORE != null)   IGNORE(copyBoundry)   else if (ELSE != null) ELSE(copyBoundry)
            COPYTYPE.INSTANCE -> if (INSTANCE != null) INSTANCE(copyBoundry) else if (ELSE != null) ELSE(copyBoundry)
            COPYTYPE.NEW ->      if (NEW != null)      NEW(copyBoundry)      else if (ELSE != null) ELSE(copyBoundry)
            COPYTYPE.DEEP ->     if (DEEP != null)     DEEP(copyBoundry)     else if (ELSE != null) ELSE(copyBoundry)
            COPYTYPE.DEEPNEW ->  if (DEEPNEW != null)  DEEPNEW(copyBoundry)  else if (ELSE != null) ELSE(copyBoundry)
        }
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

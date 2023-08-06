package com.hoffi.chassis.dsl.modelgroup

import com.hoffi.chassis.chassismodel.C
import com.hoffi.chassis.chassismodel.dsl.DslException
import com.hoffi.chassis.dsl.internal.ADslDelegateClass
import com.hoffi.chassis.dsl.internal.ChassisDslMarker
import com.hoffi.chassis.dsl.internal.DslCtxWrapper
import com.hoffi.chassis.dsl.modelgroup.copyboundry.DslImplCopyBoundryOn
import com.hoffi.chassis.dsl.modelgroup.copyboundry.IDslApiCopyBoundry
import com.hoffi.chassis.dsl.modelgroup.copyboundry.IDslApiPrefixedCopyBoundry
import com.hoffi.chassis.shared.dsl.DslRef
import com.hoffi.chassis.shared.dsl.IDslRef
import com.hoffi.chassis.shared.shared.AHasCopyBoundrysData
import com.hoffi.chassis.shared.shared.FillerData
import com.hoffi.chassis.shared.shared.reffing.MODELREFENUM
import org.slf4j.LoggerFactory

// ======== API

@ChassisDslMarker
interface IDslApiOuterFillerBlock : IDslApiFillerBlock {
    fun prefixed(businessName: String, prefixedFillerBlock: IDslApiPrefixedFillerScopeBlock.() -> Unit)
}

@ChassisDslMarker
interface IDslApiPrefixedFillerScopeBlock : IDslApiFillerBlock, IDslApiPrefixedCopyBoundry

@ChassisDslMarker
interface IDslApiFillerBlock
    :   IDslApiModelReffing
{
    operator fun MODELREFENUM.unaryPlus(): List<FillerData>
    infix fun MODELREFENUM.mutual(other: MODELREFENUM): List<FillerData>
    infix fun MODELREFENUM.mutual(other: IDslRef): List<FillerData>
    infix fun MODELREFENUM.mutual(other: String): List<FillerData>
    infix fun IDslRef.mutual(other: MODELREFENUM): List<FillerData>
    infix fun IDslRef.mutual(other: IDslRef): List<FillerData>
    infix fun IDslRef.mutual(other: String): List<FillerData>
    infix fun String.mutual(other: MODELREFENUM): List<FillerData>
    infix fun String.mutual(other: IDslRef): List<FillerData>
    infix fun String.mutual(other: String): List<FillerData>
    infix fun MODELREFENUM.from(other: MODELREFENUM): List<FillerData>
    infix fun MODELREFENUM.from(other: IDslRef): List<FillerData>
    infix fun MODELREFENUM.from(other: String): List<FillerData>
    infix fun IDslRef.from(other: MODELREFENUM): List<FillerData>
    infix fun IDslRef.from(other: IDslRef): List<FillerData>
    infix fun IDslRef.from(other: String): List<FillerData>
    infix fun String.from(other: MODELREFENUM): List<FillerData>
    infix fun String.from(other: IDslRef): List<FillerData>
    infix fun String.from(other: String): List<FillerData>
}

@ChassisDslMarker
interface IDslApiFillerDelegate {
    fun filler(simpleName: String = C.DEFAULT, block: IDslApiOuterFillerBlock.() -> Unit)
}

// ======== Impl

context(DslCtxWrapper)
class DslFillerDelegateImpl(simpleNameOfDelegator: String, delegatorRef: IDslRef)
    : ADslDelegateClass(simpleNameOfDelegator, delegatorRef),
    IDslApiFillerDelegate
{
    val log = LoggerFactory.getLogger(javaClass)
    override val selfDslRef = DslRef.filler(simpleNameOfDelegator, delegatorRef)

    var theFillerBlocks: MutableMap<String, DslImplOuterFillerBlock> = mutableMapOf()

    // TODO really uuid needed for theFillerDatas Map??
    private val theFillerDatas: MutableMap<String, MutableMap<String, MutableSet<FillerData>>> = mutableMapOf()
    fun getOrCreateFillerData(simpleName: String, businessName: String, targetDslRef: IDslRef, sourceDslRef: IDslRef): FillerData {
        val fillerData = FillerData(businessName, targetDslRef, sourceDslRef)
        val allForSimpleName = theFillerDatas.getOrPut(simpleName) { mutableMapOf() }
        val allForBusinessName = allForSimpleName.getOrPut(businessName) { mutableSetOf() }
        val existingFillerData: FillerData? = allForBusinessName.firstOrNull{it == fillerData}
        return if (existingFillerData != null) {
            return existingFillerData
        } else {
            allForBusinessName.add(fillerData) ; fillerData
        }
    }
    fun finishedFillerDatas(): MutableMap<String, MutableSet<FillerData>> {
        val resultMap: MutableMap<String, MutableSet<FillerData>> = mutableMapOf()
        for (fillersForSimpleNameEntry in theFillerDatas) {
            val setOfFillerData: MutableSet<FillerData> = mutableSetOf()
            resultMap[fillersForSimpleNameEntry.key] = setOfFillerData
            for (fillerData in fillersForSimpleNameEntry.value.flatMap { it.value }) {
                // TODO check if we can remove the test if already exists in setOfFillerData
                if (!setOfFillerData.add(fillerData)) log.error("FIRST FILLER WON -> $selfDslRef filler '$fillerData' for ${fillerData.businessName} there already was a filler from/to $fillerData")
            }
        }
        return resultMap
    }

    override fun filler(simpleName: String, block: IDslApiOuterFillerBlock.() -> Unit) {
        log.info("fun {}(\"{}\") { ... } in PASS {}", object{}.javaClass.enclosingMethod.name, simpleName, dslCtx.currentPASS)
        when (dslCtx.currentPASS) {
            dslCtx.PASS_ERROR -> TODO()
            dslCtx.PASS_FINISH -> { /* TODO implement me! */ }
            dslCtx.PASS_5_REFERENCING -> {
                val dslImpl: DslImplOuterFillerBlock = theFillerBlocks.getOrPut(simpleName) { DslImplOuterFillerBlock(simpleName, selfDslRef) }
                dslImpl.apply(block)
            }
            else -> {}
        }
    }
}

//class DslImplFillerCopyBoundry(val simpleName: String, val fillerName: String, val theDslFillerDatas: List<FillerData>) : IDslApiCopyBoundry {
//    override fun copyBoundry(copyType: COPYTYPE, vararg propName: String) {
//        for (fillerData: FillerData in theDslFillerDatas) {
//            fillerData.businessName = fillerName
//            val copyBoundry = fillerData.theCopyBoundrys[copyType]!!
//            copyBoundry.eitherPropNames.propNames.addAll(propName)
//        }
//    }
//}

context(DslCtxWrapper)
@ChassisDslMarker
class DslImplOuterFillerBlock(val simpleName: String, val selfDslRef: IDslRef)
    : IDslApiOuterFillerBlock
{
    //override fun toString() = "${this::class.simpleName}(${dslFillerDelegateImpl.theFillerBlocks[C.DEFAULT]})"
    val log = LoggerFactory.getLogger(javaClass)

    val dslFillerDelegateImpl: DslFillerDelegateImpl = dslCtx.ctxObj(selfDslRef)

    private val defaultBlock = DslImplInnerFillerBlock(C.DEFAULT, this, simpleName, selfDslRef)
    private val theInnerFillerBlockImpls: MutableMap<String, DslImplInnerFillerBlock> = mutableMapOf()

    override fun prefixed(businessName: String, prefixedFillerBlock: IDslApiPrefixedFillerScopeBlock.() -> Unit) {
        val innerImpl = theInnerFillerBlockImpls.getOrPut(businessName) {
            DslImplInnerFillerBlock(businessName, this, simpleName, selfDslRef)
        }
        innerImpl.apply(prefixedFillerBlock)
    }


    //override fun <T : AHasCopyBoundrysData> List<T>.prefixed(businessName: String, dslApiCopyBoundryBlock: IDslApiCopyBoundry.() -> Unit) {
    //    for (hasCopyBoundryData in this) {
    //        hasCopyBoundryData.businessName = businessName
    //        dslApiCopyBoundryBlock.invoke(DslImplCopyBoundryOn(hasCopyBoundryData))
    //    }
    //}
    //override fun IDslRef.prefixed(businessName: String, dslApiCopyBoundryBlock: IDslApiCopyBoundry.() -> Unit) {
    //    TODO("Not yet implemented")
    //}
    //override fun MODELREFENUM.prefixed(businessName: String, dslApiCopyBoundryBlock: IDslApiCopyBoundry.() -> Unit) {
    //    (+this).prefixed(businessName, dslApiCopyBoundryBlock)
    //}


    override fun MODELREFENUM.unaryPlus(): List<FillerData>                 = with(defaultBlock) { this@unaryPlus.unaryPlus() }
    override fun MODELREFENUM.mutual(other: MODELREFENUM): List<FillerData> = with(defaultBlock) { this@mutual.mutual(other) }
    override fun MODELREFENUM.mutual(other: IDslRef): List<FillerData>      = with(defaultBlock) { this@mutual.mutual(other) }
    override fun MODELREFENUM.mutual(other: String): List<FillerData>       = with(defaultBlock) { this@mutual.mutual(other) }
    override fun IDslRef.mutual(other: MODELREFENUM): List<FillerData>      = with(defaultBlock) { this@mutual.mutual(other) }
    override fun IDslRef.mutual(other: IDslRef): List<FillerData>           = with(defaultBlock) { this@mutual.mutual(other) }
    override fun IDslRef.mutual(other: String): List<FillerData>            = with(defaultBlock) { this@mutual.mutual(other) }
    override fun String.mutual(other: MODELREFENUM): List<FillerData>       = with(defaultBlock) { this@mutual.mutual(other) }
    override fun String.mutual(other: IDslRef): List<FillerData>            = with(defaultBlock) { this@mutual.mutual(other) }
    override fun String.mutual(other: String): List<FillerData>             = with(defaultBlock) { this@mutual.mutual(other) }

    override fun MODELREFENUM.from(other: MODELREFENUM) = with(defaultBlock) { this@from.from(other) }
    override fun MODELREFENUM.from(other: IDslRef)      = with(defaultBlock) { this@from.from(other) }
    override fun MODELREFENUM.from(other: String)       = with(defaultBlock) { this@from.from(other) }
    override fun IDslRef.from(other: MODELREFENUM)      = with(defaultBlock) { this@from.from(other) }
    override fun IDslRef.from(other: IDslRef)           = with(defaultBlock) { this@from.from(other) }
    override fun IDslRef.from(other: String)            = with(defaultBlock) { this@from.from(other) }
    override fun String.from(other: MODELREFENUM)       = with(defaultBlock) { this@from.from(other) }
    override fun String.from(other: IDslRef)            = with(defaultBlock) { this@from.from(other) }
    override fun String.from(other: String)             = with(defaultBlock) { this@from.from(other) }


    // ====================
    // === ModelReffing ===
    // ====================

    val modelReffing = DslImplModelReffing(dslFillerDelegateImpl)

    override fun MODELREFENUM.of(thisModelgroupSubElementRef: IDslRef): IDslRef {
        return modelReffing.fakeOf(this, thisModelgroupSubElementRef)
    }

    override fun MODELREFENUM.of(thisModelgroupSubElementSimpleName: String): IDslRef {
        return modelReffing.fakeOf(this, thisModelgroupSubElementSimpleName)
    }

    override fun MODELREFENUM.inModelgroup(otherModelgroupSimpleName: String): OtherModelgroupSubelementWithSimpleNameDefault {
        return modelReffing.fakeInModelgroup(this, otherModelgroupSimpleName)
    }

    override fun OtherModelgroupSubelementWithSimpleNameDefault.withModelName(modelName: String): IDslRef {
        return modelReffing.fakeWithModelName(this, modelName)
    }
}



context(DslCtxWrapper)
@ChassisDslMarker
class DslImplInnerFillerBlock(val businessName: String, val dslOuterFillerBlockImpl: DslImplOuterFillerBlock, val simpleName: String, val selfDslRef: IDslRef)
    : IDslApiPrefixedFillerScopeBlock
{
    //override fun toString() = "${this::class.simpleName}(${dslFillerDelegateImpl.theFillerBlocks[C.DEFAULT]})"
    val log = LoggerFactory.getLogger(javaClass)

    val dslFillerDelegateImpl: DslFillerDelegateImpl = dslCtx.ctxObj(selfDslRef)

    override fun <E : AHasCopyBoundrysData> List<E>.shallowRestrictions(dslApiCopyBoundryBlock: IDslApiCopyBoundry.() -> Unit) {
        for (hasCopyBoundryData in this) {
            with(DslImplCopyBoundryOn(hasCopyBoundryData)) { this.apply(dslApiCopyBoundryBlock) }
        }
    }

    override fun <E : AHasCopyBoundrysData> List<E>.deepRestrictions(dslApiCopyBoundryBlock: IDslApiCopyBoundry.() -> Unit) {
        for (hasCopyBoundryData in this) {
            with(DslImplCopyBoundryOn(hasCopyBoundryData)) { this.apply(dslApiCopyBoundryBlock) }
        }
    }

    override fun <E : AHasCopyBoundrysData> FOR(vararg aHasCopyBoundryDataList: List<E>): List<E> = aHasCopyBoundryDataList.flatMap { it }

    override fun MODELREFENUM.unaryPlus(): List<FillerData> {
        val (selfGroupRef, selfElementRef, selfSubelRef) = DslImplModelReffing.groupElementAndSubelementLevelDslRef(dslFillerDelegateImpl)
        val fillerData =  when (this) {
            MODELREFENUM.MODEL -> throw DslException("filler on '${selfDslRef}' unaryPlus not allowed to a 'MODEL'")
            MODELREFENUM.DTO ->   dslFillerDelegateImpl.getOrCreateFillerData(simpleName, businessName, DslRef.dto(C.DEFAULT, selfElementRef), DslRef.dto(C.DEFAULT, selfElementRef))
            MODELREFENUM.TABLE -> dslFillerDelegateImpl.getOrCreateFillerData(simpleName, businessName, DslRef.table(C.DEFAULT, selfElementRef), DslRef.table(C.DEFAULT, selfElementRef))
        }
        return listOf(fillerData)
    }

    //override fun IDslRef.unaryPlus(): FillerData {
    //    //val (selfGroupRef, selfElementRef, selfSubelRef) = DslImplModelReffing.grouplementAndSubelementLevelDslRef(dslFillerDelegateImpl)
    //    val (targetGroupRef, targetElementRef, targetSubelRef) = DslImplModelReffing.grouplementAndSubelementLevelDslRef(dslCtx.ctxObj(this))
    //    targetSubelRef ?: throw  DslException("filler on '${selfDslRef}' unaryPlus('${this}') not a model subelement (dto, table, ...)")
    //    val fillerData: FillerData = WhensDslRef.whenModelSubelement (targetSubelRef,
    //        isDtoRef = { fillerData(targetSubelRef, targetSubelRef) },
    //        isTableRef = { fillerData(targetSubelRef, targetSubelRef) },
    //    ) {
    //        DslException("filler on '${selfDslRef}' unaryPlus('$this') not a model subelement (dto, table, ...)")
    //    }
    //    return fillerData
    //}

    override fun MODELREFENUM.mutual(other: MODELREFENUM): List<FillerData> {
        val fillerData = (this from other).first()
        val mutualFillerData = dslFillerDelegateImpl.getOrCreateFillerData(simpleName, fillerData.businessName, fillerData.sourceDslRef, fillerData.targetDslRef)
        return listOf(fillerData, mutualFillerData)
    }

    override fun MODELREFENUM.mutual(other: IDslRef): List<FillerData> {
        val fillerData = (this from other).first()
        val mutualFillerData = dslFillerDelegateImpl.getOrCreateFillerData(simpleName, fillerData.businessName, fillerData.sourceDslRef, fillerData.targetDslRef)
        return listOf(fillerData, mutualFillerData)
    }

    override fun MODELREFENUM.mutual(other: String): List<FillerData> {
        val fillerData = (this from other).first()
        val mutualFillerData = dslFillerDelegateImpl.getOrCreateFillerData(simpleName, fillerData.businessName, fillerData.sourceDslRef, fillerData.targetDslRef)
        return listOf(fillerData, mutualFillerData)
    }

    override fun IDslRef.mutual(other: MODELREFENUM): List<FillerData> {
        val fillerData = (this from other).first()
        val mutualFillerData = dslFillerDelegateImpl.getOrCreateFillerData(simpleName, fillerData.businessName, fillerData.sourceDslRef, fillerData.targetDslRef)
        return listOf(fillerData, mutualFillerData)
    }

    override fun IDslRef.mutual(other: IDslRef): List<FillerData> {
        val fillerData = (this from other).first()
        val mutualFillerData = dslFillerDelegateImpl.getOrCreateFillerData(simpleName, fillerData.businessName, fillerData.sourceDslRef, fillerData.targetDslRef)
        return listOf(fillerData, mutualFillerData)
    }

    override fun IDslRef.mutual(other: String): List<FillerData> {
        // other String must be a complete DslRefString (as we here refer to a modelSubelement definitely outside of this modelgroup
        TODO("Not yet implemented")
    }

    override fun String.mutual(other: MODELREFENUM): List<FillerData> {
        // this String must be a complete DslRefString (as we here refer to a modelSubelement definitely outside of this modelgroup
        TODO("Not yet implemented")
    }

    override fun String.mutual(other: IDslRef): List<FillerData> {
        // this String must be a complete DslRefString (as we here refer to a modelSubelement definitely outside of this modelgroup
        TODO("Not yet implemented")
    }

    override fun String.mutual(other: String): List<FillerData> {
        // this String and other String must be a complete DslRefString (as we here refer to a modelSubelement definitely outside of this modelgroup
        TODO("Not yet implemented")
    }

    override fun MODELREFENUM.from(other: MODELREFENUM): List<FillerData> {
        val (_, selfElementRef, _) = DslImplModelReffing.groupElementAndSubelementLevelDslRef(dslFillerDelegateImpl)

        val targetDslRef =  when (this) {
            MODELREFENUM.MODEL -> throw DslException("$this: filling a MODEL is not allowed")
            MODELREFENUM.DTO ->   DslRef.dto(C.DEFAULT, selfElementRef)
            MODELREFENUM.TABLE -> DslRef.table(C.DEFAULT, selfElementRef)
        }
        val sourceDslRef =  when (other) {
            MODELREFENUM.MODEL -> throw DslException("$this: filling a MODEL is not allowed")
            MODELREFENUM.DTO ->   DslRef.dto(C.DEFAULT, selfElementRef)
            MODELREFENUM.TABLE -> DslRef.table(C.DEFAULT, selfElementRef)
        }

        return listOf(dslFillerDelegateImpl.getOrCreateFillerData(simpleName, businessName, targetDslRef, sourceDslRef))
    }

    override fun MODELREFENUM.from(other: IDslRef): List<FillerData> {
        if (other !is DslRef.ISubElementLevel) { throw DslException("$this: filling a MODEL is not allowed") }
        val (_, selfElementRef, _) = DslImplModelReffing.groupElementAndSubelementLevelDslRef(dslFillerDelegateImpl)

        val toRef =  when (this) {
            MODELREFENUM.MODEL -> throw DslException("$this: filling a MODEL is not allowed")
            MODELREFENUM.DTO ->   DslRef.dto(C.DEFAULT, selfElementRef)
            MODELREFENUM.TABLE -> DslRef.table(C.DEFAULT, selfElementRef)
        }

        return listOf(dslFillerDelegateImpl.getOrCreateFillerData(simpleName, businessName, toRef, other))
    }

    override fun MODELREFENUM.from(other: String): List<FillerData> {
        if (this == MODELREFENUM.MODEL) { throw DslException("$this: filling a MODEL is not allowed") }
        val (_, _, selfSubelRef) = DslImplModelReffing.groupElementAndSubelementLevelDslRef(dslFillerDelegateImpl)
        if (selfSubelRef == null) throw DslException("$this filler not directly on a model/api element")

        val thisRef = this of selfSubelRef
        val otherRef = DslImplModelReffing.defaultSubElementWithName(other, dslFillerDelegateImpl)

        return listOf(dslFillerDelegateImpl.getOrCreateFillerData(simpleName, businessName, thisRef, otherRef))
    }

    override fun IDslRef.from(other: MODELREFENUM): List<FillerData> {
        if (this !is DslRef.ISubElementLevel) throw DslException("$this not sub(!)element")
        val (_, elementRef, _) = DslImplModelReffing.groupElementAndSubelementLevelDslRef(dslFillerDelegateImpl)

        val fromRef =  when (other) {
            MODELREFENUM.MODEL -> throw DslException("$this: filling a MODEL is not allowed")
            MODELREFENUM.DTO ->   DslRef.dto(C.DEFAULT, elementRef)
            MODELREFENUM.TABLE -> DslRef.table(C.DEFAULT, elementRef)
        }

        return listOf(dslFillerDelegateImpl.getOrCreateFillerData(simpleName, businessName, this, fromRef))
    }

    override fun IDslRef.from(other: IDslRef): List<FillerData> {
        if (this !is DslRef.ISubElementLevel || other !is DslRef.ISubElementLevel) throw DslException("$this or $other not sub(!)element")

        return listOf(dslFillerDelegateImpl.getOrCreateFillerData(simpleName, businessName, this, other))
    }

    override fun IDslRef.from(other: String): List<FillerData> {
        // other String must be a complete DslRefString (as we here refer to a modelSubelement definitely outside of this modelgroup
        TODO("Not yet implemented")
    }

    override fun String.from(other: MODELREFENUM): List<FillerData> {
        // this String must be a complete DslRefString (as we here refer to a modelSubelement definitely outside of this modelgroup
        TODO("Not yet implemented")
    }

    override fun String.from(other: IDslRef): List<FillerData> {
        // this String must be a complete DslRefString (as we here refer to a modelSubelement definitely outside of this modelgroup
        TODO("Not yet implemented")
    }

    override fun String.from(other: String): List<FillerData> {
        // this String must be a complete DslRefString (as we here refer to a modelSubelement definitely outside of this modelgroup
        TODO("Not yet implemented")
    }


//    override fun FillerData.copyBoundry(named: String, copyType: COPYTYPE, vararg propRef: DslRef.prop): FillerData {
//        this.theCopyBoundrys.add(CopyBoundry(named, copyType, *propRef))
//        return this
//    }
//
//    override fun FillerData.copyBoundry(named: String, copyType: COPYTYPE, vararg modelRef: DslRef.IModelOrModelSubelement): FillerData {
//        this.theCopyBoundrys.add(CopyBoundry(named, copyType, *modelRef))
//        return this
//    }
//
//    override fun FillerData.copyBoundry(named: String, copyType: COPYTYPE, vararg propNames: String): FillerData {
//        this.theCopyBoundrys.add(CopyBoundry(named, copyType, *propNames))
//        return this
//    }

    // ====================
    // === ModelReffing ===
    // ====================

    val modelReffing = DslImplModelReffing(dslFillerDelegateImpl)

    override fun MODELREFENUM.of(thisModelgroupSubElementRef: IDslRef): IDslRef {
        return modelReffing.fakeOf(this, thisModelgroupSubElementRef)
    }

    override fun MODELREFENUM.of(thisModelgroupSubElementSimpleName: String): IDslRef {
        return modelReffing.fakeOf(this, thisModelgroupSubElementSimpleName)
    }

    override fun MODELREFENUM.inModelgroup(otherModelgroupSimpleName: String): OtherModelgroupSubelementWithSimpleNameDefault {
        return modelReffing.fakeInModelgroup(this, otherModelgroupSimpleName)
    }

    override fun OtherModelgroupSubelementWithSimpleNameDefault.withModelName(modelName: String): IDslRef {
        return modelReffing.fakeWithModelName(this, modelName)
    }
}

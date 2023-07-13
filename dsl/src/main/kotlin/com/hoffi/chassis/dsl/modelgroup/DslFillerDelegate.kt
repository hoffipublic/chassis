package com.hoffi.chassis.dsl.modelgroup

import com.hoffi.chassis.chassismodel.C
import com.hoffi.chassis.chassismodel.dsl.DslException
import com.hoffi.chassis.dsl.internal.ADslClass
import com.hoffi.chassis.dsl.internal.ADslDelegateClass
import com.hoffi.chassis.dsl.internal.DslCtxWrapper
import com.hoffi.chassis.shared.dsl.DslRef
import com.hoffi.chassis.shared.dsl.IDslRef
import com.hoffi.chassis.shared.helpers.getOrAdd
import com.hoffi.chassis.shared.shared.CopyBoundry
import com.hoffi.chassis.shared.shared.FillerData
import com.hoffi.chassis.shared.shared.FillerData.COPYTYPE
import com.hoffi.chassis.shared.shared.reffing.MODELREFENUM
import org.slf4j.LoggerFactory


// ======== API
interface IDslApiFillerBlock : IDslApiModelReffing {
    operator fun MODELREFENUM.unaryPlus(): FillerData
    infix fun MODELREFENUM.mutual(other: MODELREFENUM): FillerData
    infix fun MODELREFENUM.mutual(other: IDslRef): FillerData
    infix fun MODELREFENUM.mutual(other: String): FillerData
    infix fun IDslRef.mutual(other: MODELREFENUM): FillerData
    infix fun IDslRef.mutual(other: IDslRef): FillerData
    infix fun IDslRef.mutual(other: String): FillerData
    infix fun String.mutual(other: MODELREFENUM): FillerData
    infix fun String.mutual(other: IDslRef): FillerData
    infix fun String.mutual(other: String): FillerData
    infix fun MODELREFENUM.from(other: MODELREFENUM): FillerData
    infix fun MODELREFENUM.from(other: IDslRef): FillerData
    infix fun MODELREFENUM.from(other: String): FillerData
    infix fun IDslRef.from(other: MODELREFENUM): FillerData
    infix fun IDslRef.from(other: IDslRef): FillerData
    infix fun IDslRef.from(other: String): FillerData
    infix fun String.from(other: MODELREFENUM): FillerData
    infix fun String.from(other: IDslRef): FillerData
    infix fun String.from(other: String): FillerData

    fun FillerData.copyBoundry(copyType: COPYTYPE, modelref: MODELREFENUM): FillerData
    fun FillerData.copyBoundry(copyType: COPYTYPE, dslRef: IDslRef): FillerData
    fun FillerData.copyBoundry(copyType: COPYTYPE, other: String): FillerData
    fun FillerData.copyBoundry(copyType: COPYTYPE, modelref: MODELREFENUM, propName: String): FillerData
    fun FillerData.copyBoundry(copyType: COPYTYPE, dslRef: IDslRef, propName: String): FillerData
    fun FillerData.copyBoundry(copyType: COPYTYPE, other: String, propName: String): FillerData
    fun FillerData.copyBoundries(copyType: COPYTYPE, vararg modelref: MODELREFENUM): FillerData
    fun FillerData.copyBoundries(copyType: COPYTYPE, vararg dslRef: IDslRef): FillerData
    fun FillerData.copyBoundries(copyType: COPYTYPE, vararg other: String): FillerData

}

interface IDslApiFillerDelegate {
    fun filler(simpleName: String = C.DEFAULT, block: IDslApiFillerBlock.() -> Unit)
}

// ======== Impl
interface IDslImplFillerBlock : IDslApiFillerBlock

context(DslCtxWrapper)
class DslFillerDelegateImpl(simpleNameOfDelegator: String, delegatorRef: IDslRef)
    : ADslDelegateClass(simpleNameOfDelegator, delegatorRef),
    IDslApiFillerDelegate
{
    val log = LoggerFactory.getLogger(javaClass)
    override val selfDslRef = DslRef.filler(simpleNameOfDelegator, delegatorRef)

    var theFillerBlocks: MutableMap<String, DslFillerBlockImpl> = mutableMapOf()
    val theFillerDatas: MutableMap<String, MutableSet<FillerData>> = mutableMapOf()

    override fun filler(simpleName: String, block: IDslApiFillerBlock.() -> Unit) {
        log.info("fun {}(\"{}\") { ... } in PASS {}", object{}.javaClass.enclosingMethod.name, simpleName, dslCtx.currentPASS)
        when (dslCtx.currentPASS) {
            dslCtx.PASS_ERROR -> TODO()
            dslCtx.PASS_FINISH -> { /* TODO implement me! */ }
            dslCtx.PASS_5_REFERENCING -> {
                val dslImpl = theFillerBlocks.getOrPut(simpleName) { DslFillerBlockImpl(simpleName, selfDslRef) }
                dslImpl.apply(block)
            }
            else -> {}
        }
    }
}

context(DslCtxWrapper)
class DslFillerBlockImpl(val simpleName: String, override val selfDslRef: IDslRef)
    : ADslClass(),
        IDslImplFillerBlock,
        IDslApiModelReffing
{
    override fun toString() = "${this::class.simpleName}(${dslFillerDelegateImpl.theFillerBlocks[C.DEFAULT]})"
    val log = LoggerFactory.getLogger(javaClass)

    val dslFillerDelegateImpl: DslFillerDelegateImpl = dslCtx.ctxObj(selfDslRef)

    private fun fillerData(toDslRef: IDslRef, fromDslRef: IDslRef, theCopyBoundrys: MutableSet<CopyBoundry> = mutableSetOf()): FillerData {
        var newFillerData = FillerData(toDslRef, fromDslRef, theCopyBoundrys)
        val theFillerData: FillerData = dslFillerDelegateImpl.theFillerDatas.getOrPut(simpleName) { mutableSetOf() }.getOrAdd(newFillerData)
        return theFillerData
    }

    override fun MODELREFENUM.unaryPlus(): FillerData {
        val (selfGroupRef, selfElementRef, selfSubelRef) = DslImplModelReffing.groupElementAndSubelementLevelDslRef(dslFillerDelegateImpl)
        val simpleName = selfElementRef.simpleName
        val fillerData =  when (this) {
            MODELREFENUM.MODEL -> throw DslException("filler on '${selfDslRef}' unaryPlus not allowed to a 'MODEL'")
            MODELREFENUM.DTO ->   fillerData(DslRef.dto(C.DEFAULT, selfElementRef), DslRef.dto(C.DEFAULT, selfElementRef))
            MODELREFENUM.TABLE -> fillerData(DslRef.table(C.DEFAULT, selfElementRef), DslRef.table(C.DEFAULT, selfElementRef))
        }
        return fillerData
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

    override fun MODELREFENUM.mutual(other: MODELREFENUM): FillerData {
        val fillerData = this from other
        val mutualFillerData = fillerData(fillerData.fromDslRef, fillerData.toDslRef, fillerData.theCopyBoundrys) // just giving them the same set of CopyBoundrys
        return fillerData
    }

    override fun MODELREFENUM.mutual(other: IDslRef): FillerData {
        val fillerData = this from other
        val mutualFillerData = fillerData(fillerData.fromDslRef, fillerData.toDslRef, fillerData.theCopyBoundrys) // just giving them the same set of CopyBoundrys
        return fillerData
    }

    override fun MODELREFENUM.mutual(other: String): FillerData {
        val fillerData = this from other
        val mutualFillerData = fillerData(fillerData.fromDslRef, fillerData.toDslRef, fillerData.theCopyBoundrys) // just giving them the same set of CopyBoundrys
        return fillerData
    }

    override fun IDslRef.mutual(other: MODELREFENUM): FillerData {
        val fillerData = this from other
        val mutualFillerData = fillerData(fillerData.fromDslRef, fillerData.toDslRef, fillerData.theCopyBoundrys) // just giving them the same set of CopyBoundrys
        return fillerData
    }

    override fun IDslRef.mutual(other: IDslRef): FillerData {
        val fillerData = this from other
        val mutualFillerData = fillerData(fillerData.fromDslRef, fillerData.toDslRef, fillerData.theCopyBoundrys) // just giving them the same set of CopyBoundrys
        return fillerData
    }

    override fun IDslRef.mutual(other: String): FillerData {
        // other String must be a complete DslRefString (as we here refer to a modelSubelement definitely outside of this modelgroup
        TODO("Not yet implemented")
    }

    override fun String.mutual(other: MODELREFENUM): FillerData {
        // this String must be a complete DslRefString (as we here refer to a modelSubelement definitely outside of this modelgroup
        TODO("Not yet implemented")
    }

    override fun String.mutual(other: IDslRef): FillerData {
        // this String must be a complete DslRefString (as we here refer to a modelSubelement definitely outside of this modelgroup
        TODO("Not yet implemented")
    }

    override fun String.mutual(other: String): FillerData {
        // this String and other String must be a complete DslRefString (as we here refer to a modelSubelement definitely outside of this modelgroup
        TODO("Not yet implemented")
    }

    override fun MODELREFENUM.from(other: MODELREFENUM): FillerData {
        val (_, selfElementRef, _) = DslImplModelReffing.groupElementAndSubelementLevelDslRef(dslFillerDelegateImpl)

        val toRef =  when (this) {
            MODELREFENUM.MODEL -> throw DslException("$this: filling a MODEL is not allowed")
            MODELREFENUM.DTO ->   DslRef.dto(C.DEFAULT, selfElementRef)
            MODELREFENUM.TABLE -> DslRef.table(C.DEFAULT, selfElementRef)
        }
        val fromRef =  when (other) {
            MODELREFENUM.MODEL -> throw DslException("$this: filling a MODEL is not allowed")
            MODELREFENUM.DTO ->   DslRef.dto(C.DEFAULT, selfElementRef)
            MODELREFENUM.TABLE -> DslRef.table(C.DEFAULT, selfElementRef)
        }

        return fillerData(toRef, fromRef)
    }

    override fun MODELREFENUM.from(other: IDslRef): FillerData {
        if (other !is DslRef.ISubElementLevel) { throw DslException("$this: filling a MODEL is not allowed") }
        val (_, selfElementRef, _) = DslImplModelReffing.groupElementAndSubelementLevelDslRef(dslFillerDelegateImpl)

        val toRef =  when (this) {
            MODELREFENUM.MODEL -> throw DslException("$this: filling a MODEL is not allowed")
            MODELREFENUM.DTO ->   DslRef.dto(C.DEFAULT, selfElementRef)
            MODELREFENUM.TABLE -> DslRef.table(C.DEFAULT, selfElementRef)
        }

        return fillerData(toRef, other)
    }

    override fun MODELREFENUM.from(other: String): FillerData {
        if (this == MODELREFENUM.MODEL) { throw DslException("$this: filling a MODEL is not allowed") }
        val (_, _, selfSubelRef) = DslImplModelReffing.groupElementAndSubelementLevelDslRef(dslFillerDelegateImpl)
        if (selfSubelRef == null) throw DslException("$this filler not directly on a model/api element")

        val thisRef = this of selfSubelRef
        val otherRef = DslImplModelReffing.defaultSubElementWithName(other, dslFillerDelegateImpl)

        return fillerData(thisRef, otherRef)
    }

    override fun IDslRef.from(other: MODELREFENUM): FillerData {
        if (this !is DslRef.ISubElementLevel) throw DslException("$this not sub(!)element")
        val (_, elementRef, _) = DslImplModelReffing.groupElementAndSubelementLevelDslRef(dslFillerDelegateImpl)

        val fromRef =  when (other) {
            MODELREFENUM.MODEL -> throw DslException("$this: filling a MODEL is not allowed")
            MODELREFENUM.DTO ->   DslRef.dto(C.DEFAULT, elementRef)
            MODELREFENUM.TABLE -> DslRef.table(C.DEFAULT, elementRef)
        }

        return fillerData(this, fromRef)
    }

    override fun IDslRef.from(other: IDslRef): FillerData {
        if (this !is DslRef.ISubElementLevel || other !is DslRef.ISubElementLevel) throw DslException("$this or $other not sub(!)element")

        return fillerData(this, other)
    }

    override fun IDslRef.from(other: String): FillerData {
        // other String must be a complete DslRefString (as we here refer to a modelSubelement definitely outside of this modelgroup
        TODO("Not yet implemented")
    }

    override fun String.from(other: MODELREFENUM): FillerData {
        // this String must be a complete DslRefString (as we here refer to a modelSubelement definitely outside of this modelgroup
        TODO("Not yet implemented")
    }

    override fun String.from(other: IDslRef): FillerData {
        // this String must be a complete DslRefString (as we here refer to a modelSubelement definitely outside of this modelgroup
        TODO("Not yet implemented")
    }

    override fun String.from(other: String): FillerData {
        // this String must be a complete DslRefString (as we here refer to a modelSubelement definitely outside of this modelgroup
        TODO("Not yet implemented")
    }

    override fun FillerData.copyBoundry(copyType: COPYTYPE, modelref: MODELREFENUM): FillerData
    {
        TODO("Not yet implemented")
    }

    override fun FillerData.copyBoundry(copyType: COPYTYPE, dslRef: IDslRef): FillerData {
        TODO("Not yet implemented")
    }

    override fun FillerData.copyBoundry(copyType: COPYTYPE, other: String): FillerData {
        TODO("Not yet implemented")
    }

    override fun FillerData.copyBoundry(copyType: COPYTYPE, modelref: MODELREFENUM, propName: String): FillerData {
        TODO("Not yet implemented")
    }

    override fun FillerData.copyBoundry(copyType: COPYTYPE, dslRef: IDslRef, propName: String): FillerData {
        TODO("Not yet implemented")
    }

    override fun FillerData.copyBoundry(copyType: COPYTYPE, other: String, propName: String): FillerData {
        TODO("Not yet implemented")
    }

    override fun FillerData.copyBoundries(copyType: COPYTYPE, vararg modelref: MODELREFENUM): FillerData {
        TODO("Not yet implemented")
    }

    override fun FillerData.copyBoundries(copyType: COPYTYPE, vararg dslRef: IDslRef): FillerData {
        TODO("Not yet implemented")
    }

    override fun FillerData.copyBoundries(copyType: COPYTYPE, vararg other: String): FillerData {
        TODO("Not yet implemented")
    }

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

package com.hoffi.chassis.dsl.modelgroup

import com.hoffi.chassis.chassismodel.C
import com.hoffi.chassis.chassismodel.dsl.DslException
import com.hoffi.chassis.dsl.internal.ADslClass
import com.hoffi.chassis.dsl.internal.ADslDelegateClass
import com.hoffi.chassis.dsl.internal.ChassisDslMarker
import com.hoffi.chassis.dsl.internal.DslCtxWrapper
import com.hoffi.chassis.shared.dsl.DslRef
import com.hoffi.chassis.shared.dsl.IDslRef
import com.hoffi.chassis.shared.shared.FillerData
import com.hoffi.chassis.shared.shared.FillerData.COPYTYPE
import com.hoffi.chassis.shared.shared.reffing.MODELREFENUM
import org.slf4j.LoggerFactory
import java.util.*


// ======== API
interface IDslApiFillerBlock : IDslApiModelReffing {
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

    //fun FillerData.copyBoundry(named: String, copyType: COPYTYPE, vararg propRef: DslRef.prop): FillerData
    //fun FillerData.copyBoundry(named: String, copyType: COPYTYPE, vararg modelRef: DslRef.IModelOrModelSubelement): FillerData
    //fun FillerData.copyBoundry(named: String, copyType: COPYTYPE, vararg propNames: String): FillerData
    fun fillerName(fillerName: String, vararg dslFillerData: List<FillerData>, fillerNameBlock: IDslApiFillerName.() -> Unit)
    fun fillerName(fillerName: String, vararg modelrefenum: MODELREFENUM, fillerNameBlock: IDslApiFillerName.() -> Unit)

    //abstract fun fillerName(fillerName: String, dslFillerData: Array<List<FillerData>>, fillerNameBlock: IDslApiFillerName.() -> Unit)
}

interface IDslApiFillerDelegate {
    fun filler(simpleName: String = C.DEFAULT, block: IDslApiFillerBlock.() -> Unit)
}
interface IDslApiFillerName {
    fun copyBoundry(copyType: COPYTYPE, vararg propName: String)
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

    protected val theFillerDatas: MutableMap<String, MutableMap<UUID, FillerData>> = mutableMapOf()
    fun addFillerData(simpleName: String, dslFillerData: FillerData): FillerData {
        val fillerDatas = theFillerDatas.getOrPut(simpleName) { mutableMapOf() }
        fillerDatas[UUID.randomUUID()] = dslFillerData
        return dslFillerData
    }
    fun finishedFillerDatas(): MutableMap<String, MutableSet<FillerData>> {
        val resultMap: MutableMap<String, MutableSet<FillerData>> = mutableMapOf()
        for (fillersForSimpleNameEntry in theFillerDatas) {
            var fillerSet: MutableSet<FillerData> = resultMap.getOrPut(fillersForSimpleNameEntry.key) { mutableSetOf() }
            for (fillerData in fillersForSimpleNameEntry.value.map { it.value }) {
                //if ( ! fillerSet.add(fillerData) ) throw DslException("$selfDslRef filler '$fillerData' for ${fillerData.fillerName} there already was a filler from/to ${fillerData}")
                if ( ! fillerSet.add(fillerData) ) log.error("FIRST FILLER WON -> $selfDslRef filler '$fillerData' for ${fillerData.fillerName} there already was a filler from/to ${fillerData}")
            }
        }
        return resultMap
    }

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
@ChassisDslMarker
class DslFillerBlockImpl(val simpleName: String, override val selfDslRef: IDslRef)
    : ADslClass(),
        IDslImplFillerBlock,
        IDslApiModelReffing
{
    override fun toString() = "${this::class.simpleName}(${dslFillerDelegateImpl.theFillerBlocks[C.DEFAULT]})"
    val log = LoggerFactory.getLogger(javaClass)

    val dslFillerDelegateImpl: DslFillerDelegateImpl = dslCtx.ctxObj(selfDslRef)

    @ChassisDslMarker
    override fun fillerName(fillerName: String, vararg dslFillerData: List<FillerData>, fillerNameBlock: IDslApiFillerName.() -> Unit) {
        fillerNameBlock.invoke(DslImplFillerName(simpleName, fillerName, dslFillerData.flatMap { it }))
    }
    @ChassisDslMarker
    override fun fillerName(fillerName: String, vararg modelrefenum: MODELREFENUM, fillerNameBlock: IDslApiFillerName.() -> Unit) {
        fillerNameBlock.invoke(DslImplFillerName(simpleName, fillerName, modelrefenum.flatMap { +it }.toList()))
    }

    override fun MODELREFENUM.unaryPlus(): List<FillerData> {
        val (selfGroupRef, selfElementRef, selfSubelRef) = DslImplModelReffing.groupElementAndSubelementLevelDslRef(dslFillerDelegateImpl)
        val fillerData =  when (this) {
            MODELREFENUM.MODEL -> throw DslException("filler on '${selfDslRef}' unaryPlus not allowed to a 'MODEL'")
            MODELREFENUM.DTO ->   dslFillerDelegateImpl.addFillerData(simpleName, FillerData(C.DEFAULT, DslRef.dto(C.DEFAULT, selfElementRef), DslRef.dto(C.DEFAULT, selfElementRef)))
            MODELREFENUM.TABLE -> dslFillerDelegateImpl.addFillerData(simpleName, FillerData(C.DEFAULT, DslRef.table(C.DEFAULT, selfElementRef), DslRef.table(C.DEFAULT, selfElementRef)))
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
        val mutualFillerData = dslFillerDelegateImpl.addFillerData(simpleName, FillerData(C.DEFAULT, fillerData.sourceDslRef, fillerData.targetDslRef))
        return listOf(fillerData, mutualFillerData)
    }

    override fun MODELREFENUM.mutual(other: IDslRef): List<FillerData> {
        val fillerData = (this from other).first()
        val mutualFillerData = dslFillerDelegateImpl.addFillerData(simpleName, FillerData(C.DEFAULT, fillerData.sourceDslRef, fillerData.targetDslRef))
        return listOf(fillerData, mutualFillerData)
    }

    override fun MODELREFENUM.mutual(other: String): List<FillerData> {
        val fillerData = (this from other).first()
        val mutualFillerData = dslFillerDelegateImpl.addFillerData(simpleName, FillerData(C.DEFAULT, fillerData.sourceDslRef, fillerData.targetDslRef))
        return listOf(fillerData, mutualFillerData)
    }

    override fun IDslRef.mutual(other: MODELREFENUM): List<FillerData> {
        val fillerData = (this from other).first()
        val mutualFillerData = dslFillerDelegateImpl.addFillerData(simpleName, FillerData(C.DEFAULT, fillerData.sourceDslRef, fillerData.targetDslRef))
        return listOf(fillerData, mutualFillerData)
    }

    override fun IDslRef.mutual(other: IDslRef): List<FillerData> {
        val fillerData = (this from other).first()
        val mutualFillerData = dslFillerDelegateImpl.addFillerData(simpleName, FillerData(C.DEFAULT, fillerData.sourceDslRef, fillerData.targetDslRef))
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

        return listOf(dslFillerDelegateImpl.addFillerData(simpleName, FillerData(C.DEFAULT, toRef, fromRef)))
    }

    override fun MODELREFENUM.from(other: IDslRef): List<FillerData> {
        if (other !is DslRef.ISubElementLevel) { throw DslException("$this: filling a MODEL is not allowed") }
        val (_, selfElementRef, _) = DslImplModelReffing.groupElementAndSubelementLevelDslRef(dslFillerDelegateImpl)

        val toRef =  when (this) {
            MODELREFENUM.MODEL -> throw DslException("$this: filling a MODEL is not allowed")
            MODELREFENUM.DTO ->   DslRef.dto(C.DEFAULT, selfElementRef)
            MODELREFENUM.TABLE -> DslRef.table(C.DEFAULT, selfElementRef)
        }

        return listOf(dslFillerDelegateImpl.addFillerData(simpleName, FillerData(C.DEFAULT, toRef, other)))
    }

    override fun MODELREFENUM.from(other: String): List<FillerData> {
        if (this == MODELREFENUM.MODEL) { throw DslException("$this: filling a MODEL is not allowed") }
        val (_, _, selfSubelRef) = DslImplModelReffing.groupElementAndSubelementLevelDslRef(dslFillerDelegateImpl)
        if (selfSubelRef == null) throw DslException("$this filler not directly on a model/api element")

        val thisRef = this of selfSubelRef
        val otherRef = DslImplModelReffing.defaultSubElementWithName(other, dslFillerDelegateImpl)

        return listOf(dslFillerDelegateImpl.addFillerData(simpleName, FillerData(C.DEFAULT, thisRef, otherRef)))
    }

    override fun IDslRef.from(other: MODELREFENUM): List<FillerData> {
        if (this !is DslRef.ISubElementLevel) throw DslException("$this not sub(!)element")
        val (_, elementRef, _) = DslImplModelReffing.groupElementAndSubelementLevelDslRef(dslFillerDelegateImpl)

        val fromRef =  when (other) {
            MODELREFENUM.MODEL -> throw DslException("$this: filling a MODEL is not allowed")
            MODELREFENUM.DTO ->   DslRef.dto(C.DEFAULT, elementRef)
            MODELREFENUM.TABLE -> DslRef.table(C.DEFAULT, elementRef)
        }

        return listOf(dslFillerDelegateImpl.addFillerData(simpleName, FillerData(C.DEFAULT, this, fromRef)))
    }

    override fun IDslRef.from(other: IDslRef): List<FillerData> {
        if (this !is DslRef.ISubElementLevel || other !is DslRef.ISubElementLevel) throw DslException("$this or $other not sub(!)element")

        return listOf(dslFillerDelegateImpl.addFillerData(simpleName, FillerData(C.DEFAULT, this, other)))
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

class DslImplFillerName(val simpleName: String, val fillerName: String, val theDslFillerDatas: List<FillerData>) : IDslApiFillerName {
    override fun copyBoundry(copyType: COPYTYPE, vararg propName: String) {
        for (fillerData in theDslFillerDatas) {
            fillerData.fillerName = fillerName
            val copyBoundry = fillerData.theCopyBoundrys[copyType]!!
            copyBoundry.eitherPropNames.propNames.addAll(propName)
        }
    }

}

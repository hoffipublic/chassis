package com.hoffi.chassis.dsl.modelgroup.crud

import com.hoffi.chassis.chassismodel.C
import com.hoffi.chassis.chassismodel.dsl.DslException
import com.hoffi.chassis.dsl.internal.ADslDelegateClass
import com.hoffi.chassis.dsl.internal.ChassisDslMarker
import com.hoffi.chassis.dsl.internal.DslBlockOn
import com.hoffi.chassis.dsl.internal.DslCtxWrapper
import com.hoffi.chassis.dsl.modelgroup.DslImplModelReffing
import com.hoffi.chassis.dsl.modelgroup.DslTable
import com.hoffi.chassis.dsl.modelgroup.IDslApiModelReffing
import com.hoffi.chassis.dsl.modelgroup.OtherModelgroupSubelementWithSimpleNameDefault
import com.hoffi.chassis.dsl.modelgroup.copyboundry.DslImplCopyBoundryOn
import com.hoffi.chassis.dsl.modelgroup.copyboundry.IDslApiCopyBoundry
import com.hoffi.chassis.dsl.modelgroup.copyboundry.IDslApiPrefixedCopyBoundry
import com.hoffi.chassis.shared.dsl.DslRef
import com.hoffi.chassis.shared.dsl.IDslRef
import com.hoffi.chassis.shared.shared.AHasCopyBoundrysData
import com.hoffi.chassis.shared.shared.CrudData
import com.hoffi.chassis.shared.shared.reffing.MODELREFENUM
import com.hoffi.chassis.shared.whens.WhensDslRef
import org.slf4j.LoggerFactory

// ======== API

@ChassisDslMarker
interface IDslApiOuterCrudBlock : IDslApiCrudBlock {
    fun prefixed(businessName: String, prefixedCrudBlock: IDslApiPrefixedCrudScopeBlock.() -> Unit)
}

@ChassisDslMarker
interface IDslApiPrefixedCrudScopeBlock : IDslApiCrudBlock, IDslApiPrefixedCopyBoundry

@ChassisDslMarker
interface IDslApiCrudBlock
    :   IDslApiModelReffing
{
    enum class GREEDY { SHALLOW, DEEP }
    val STANDARD: String
        get() = "STANDARD"
    val CRUD: String
        get() = "CRUD"
    operator fun String.unaryPlus(): CrudData
    operator fun MODELREFENUM.unaryPlus(): List<CrudData>
    infix fun String.FOR(dslRef: IDslRef): List<CrudData>
    infix fun String.FOR(modelrefenum: MODELREFENUM): List<CrudData>
    infix fun CrudData.CRUD.FOR(modelrefenum: MODELREFENUM): List<CrudData>
    infix fun CrudData.CRUD.FOR(dslRef: IDslRef): List<CrudData>
}

@ChassisDslMarker
interface IDslApiCrudFun

@ChassisDslMarker
interface IDslApiCrudDelegate {
    @DslBlockOn(DslTable::class) // IDE clickable shortcuts to implementing @ChassisDslMarker classes
    fun crud(simpleName: String = C.DEFAULT, block: IDslApiOuterCrudBlock.() -> Unit)
}

// ======== Impl

/** outer scope */
context(DslCtxWrapper)
class DslCrudDelegateImpl(simpleNameOfDelegator: String, delegateRef: IDslRef)
    : ADslDelegateClass(simpleNameOfDelegator, delegateRef)
    , IDslApiCrudDelegate
{
    override fun toString() = "${this::class.simpleName}(${theCrudBlocks.size})"
    val log = LoggerFactory.getLogger(javaClass)
    override val selfDslRef = DslRef.crud(simpleNameOfDelegator, delegateRef)

    /** different gathered dsl data holder for different simpleName's inside the BlockImpl's */
    var theCrudBlocks: MutableMap<String, DslImplOuterCrudBlock> = mutableMapOf()

    private val theCrudDatas: MutableMap<String, MutableMap<String, MutableSet<CrudData>>> = mutableMapOf()
    fun getOrCreateCrudData(simpleName: String, businessName: String, targetDslRef: IDslRef, sourceDslRef: IDslRef, crud: CrudData.CRUD): CrudData {
        if (targetDslRef !is DslRef.table) throw DslException("CrudData targetDslRef always have to be DslRef.table! was $crud '$targetDslRef'")
        if (sourceDslRef is DslRef.table)  throw DslException("CrudData sourceDslRef not allowed to be DslRef.table! was $crud '$targetDslRef'")
        val crudData = CrudData(businessName, targetDslRef, sourceDslRef, crud)
        val allForSimpleName = theCrudDatas.getOrPut(simpleName) { mutableMapOf() }
        val allForBusinessName = allForSimpleName.getOrPut(businessName) { mutableSetOf() }
        val existingCrudData: CrudData? = allForBusinessName.firstOrNull{it == crudData}
        return if (existingCrudData != null) {
            return existingCrudData
        } else {
            allForBusinessName.add(crudData) ; crudData
        }
    }
    fun finishedCrudDatas(): MutableMap<String, MutableSet<CrudData>> {
        val resultMap: MutableMap<String, MutableSet<CrudData>> = mutableMapOf()
        for (crudsForSimpleNameEntry in theCrudDatas) {
            val setOfCrudData: MutableSet<CrudData> = mutableSetOf()
            resultMap[crudsForSimpleNameEntry.key] = setOfCrudData
            // TODO log.error("Check if dtoRef is Abstract or Interface")
            for (crudData in crudsForSimpleNameEntry.value.flatMap { it.value }) {
                // TODO check if we can remove the test if already exists in setOfCrudData
                if (!setOfCrudData.add(crudData)) log.error("FIRST CRUD WON -> $selfDslRef crud '$crudData' for ${crudData.businessName} there already was a crud from/to $crudData")
            }
        }
        return resultMap
    }

    /** DslBlock funcs always operate on IDslApi interfaces */
    override fun crud(simpleName: String, block: IDslApiOuterCrudBlock.() -> Unit) {
        log.info("fun {}(\"{}\") { ... } in PASS {}", object{}.javaClass.enclosingMethod.name, simpleName, dslCtx.currentPASS)
        when (dslCtx.currentPASS) {
            dslCtx.PASS_ERROR -> TODO()
            dslCtx.PASS_FINISH -> { /* TODO implement me! */ }
            dslCtx.PASS_1_BASEMODELS -> {
                val dslImpl = theCrudBlocks.getOrPut(simpleName) { DslImplOuterCrudBlock(simpleName, selfDslRef) }
                dslImpl.apply(block)
            }
            else -> {}
        }
    }
}

context(DslCtxWrapper)
@ChassisDslMarker
class DslImplOuterCrudBlock(val simpleName: String, val selfDslRef: IDslRef)
    : IDslApiOuterCrudBlock
{
    //override fun toString() = "${this::class.simpleName}(${dslCrudDelegateImpl.theCrudBlocks[C.DEFAULT]})"
    val log = LoggerFactory.getLogger(javaClass)

    val dslCrudDelegateImpl: DslCrudDelegateImpl = dslCtx.ctxObj(selfDslRef)

    var stdCrud = false

    private val defaultBlock = DslImplInnerCrudBlock(C.DEFAULT, this, simpleName, selfDslRef)
    private val theInnerCrudBlockImpls: MutableMap<String, DslImplInnerCrudBlock> = mutableMapOf()

    override fun prefixed(businessName: String, prefixedCrudBlock: IDslApiPrefixedCrudScopeBlock.() -> Unit) {
        val innerImpl = theInnerCrudBlockImpls.getOrPut(businessName) {
            DslImplInnerCrudBlock(businessName, this, simpleName, selfDslRef)
        }
        innerImpl.apply(prefixedCrudBlock)
    }


    override fun String.unaryPlus(): CrudData                                  = with(defaultBlock) { this@unaryPlus.unaryPlus() }
    override operator fun MODELREFENUM.unaryPlus(): List<CrudData>             = with(defaultBlock) { this@unaryPlus.unaryPlus() }
    override infix fun String.FOR(dslRef: IDslRef): List<CrudData>             = with(defaultBlock) { this@FOR.FOR(dslRef) }
    override fun String.FOR(modelrefenum: MODELREFENUM): List<CrudData>        = with(defaultBlock) { this@FOR.FOR(modelrefenum) }
    override fun CrudData.CRUD.FOR(modelrefenum: MODELREFENUM): List<CrudData> = with(defaultBlock) { this@FOR.FOR(modelrefenum) }
    override fun CrudData.CRUD.FOR(dslRef: IDslRef): List<CrudData>            = with(defaultBlock) { this@FOR.FOR(dslRef) }

    // ====================
    // === ModelReffing ===
    // ====================

    val modelReffing = DslImplModelReffing(dslCrudDelegateImpl)

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

//class DslImplCrudCopyBoundry(val dslCrudDelegateImpl: DslCrudDelegateImpl, val businessName: String, val theDslCrudDatas: List<CrudData>)
//    //: IDslApiCopyBoundry
//{
////    override fun copyBoundry(copyType: COPYTYPE, vararg propName: String) {
//    fun copyBoundry(copyType: COPYTYPE, vararg propName: String) {
//        for (crudData: CrudData in theDslCrudDatas) {
//            crudData.businessName = businessName
//            val copyBoundry = crudData.theCopyBoundrys[copyType]!!
//            copyBoundry.eitherPropNames.propNames.addAll(propName)
//        }
//    }
//}



context(DslCtxWrapper)
@ChassisDslMarker
class DslImplInnerCrudBlock(val businessName: String, val dslOuterCrudBlockImpl: DslImplOuterCrudBlock, val simpleName: String, val selfDslRef: IDslRef)
    : IDslApiPrefixedCrudScopeBlock {
    //override fun toString() = "${this::class.simpleName}(${dslCrudDelegateImpl.theCrudBlocks[C.DEFAULT]})"
    val log = LoggerFactory.getLogger(javaClass)

    val dslCrudDelegateImpl: DslCrudDelegateImpl = dslCtx.ctxObj(selfDslRef)

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

    override fun String.unaryPlus(): CrudData {
        if (this == STANDARD) dslOuterCrudBlockImpl.stdCrud = true else throw DslException("$selfDslRef only STANDARD is allowed for unaryPlus")
        TODO()
    }

    override operator fun MODELREFENUM.unaryPlus(): List<CrudData> {
        val (_, elementRef, _) = DslImplModelReffing.groupElementAndSubelementLevelDslRef(dslCrudDelegateImpl)
        val crudDatas =  when (this) {
            MODELREFENUM.MODEL -> throw DslException("crudData on '${selfDslRef}' unaryPlus not allowed to a 'MODEL'")
            MODELREFENUM.DTO -> {
                val tableRef = DslRef.table(C.DEFAULT, elementRef)
                val dtoRef = DslRef.dto(C.DEFAULT, elementRef)
                listOf(
                    dslCrudDelegateImpl.getOrCreateCrudData(simpleName, C.DEFAULT, tableRef, dtoRef, CrudData.CRUD.CREATE),
                    dslCrudDelegateImpl.getOrCreateCrudData(simpleName, C.DEFAULT, tableRef, dtoRef, CrudData.CRUD.READ),
                    dslCrudDelegateImpl.getOrCreateCrudData(simpleName, C.DEFAULT, tableRef, dtoRef, CrudData.CRUD.UPDATE),
                    dslCrudDelegateImpl.getOrCreateCrudData(simpleName, C.DEFAULT, tableRef, dtoRef, CrudData.CRUD.DELETE),
                )
            }
            MODELREFENUM.TABLE -> throw DslException("crudData on '${selfDslRef}' unaryPlus not allowed to a 'TABLE'")
        }
        return crudDatas
    }

    override infix fun String.FOR(dslRef: IDslRef): List<CrudData> {
        val (_, elementRef, _) = DslImplModelReffing.groupElementAndSubelementLevelDslRef(dslCrudDelegateImpl)
        val tableRef = DslRef.table(C.DEFAULT, elementRef)
        val dtoRef = dslRef as DslRef.IModelOrModelSubelement
        return listOf(
            dslCrudDelegateImpl.getOrCreateCrudData(simpleName, C.DEFAULT, tableRef, dtoRef, CrudData.CRUD.CREATE),
            dslCrudDelegateImpl.getOrCreateCrudData(simpleName, C.DEFAULT, tableRef, dtoRef, CrudData.CRUD.READ),
            dslCrudDelegateImpl.getOrCreateCrudData(simpleName, C.DEFAULT, tableRef, dtoRef, CrudData.CRUD.UPDATE),
            dslCrudDelegateImpl.getOrCreateCrudData(simpleName, C.DEFAULT, tableRef, dtoRef, CrudData.CRUD.DELETE),
        )
    }

    override fun String.FOR(modelrefenum: MODELREFENUM): List<CrudData> {
        // TODO not implemented yet
        when (this) {
            STANDARD -> { }
            CRUD -> {}
        }
        val listOfCrudData: MutableList<CrudData> = mutableListOf()
        for (c in CrudData.CRUD.entries) {
            listOfCrudData.addAll(c.FOR(modelrefenum))
        }
        return listOfCrudData
    }

    override fun CrudData.CRUD.FOR(modelrefenum: MODELREFENUM): List<CrudData> {
        val (selfGroupRef, selfElementRef, selfSubelRef) = DslImplModelReffing.groupElementAndSubelementLevelDslRef(dslCrudDelegateImpl)
        val crudData =  when (modelrefenum) {
            MODELREFENUM.MODEL -> throw DslException("crud on '${selfDslRef}' unaryPlus not allowed to a 'MODEL'")
            MODELREFENUM.DTO ->   dslCrudDelegateImpl.getOrCreateCrudData(simpleName, businessName, DslRef.table(C.DEFAULT, selfElementRef), DslRef.dto(C.DEFAULT, selfElementRef), this)
            MODELREFENUM.TABLE -> dslCrudDelegateImpl.getOrCreateCrudData(simpleName, businessName, DslRef.table(C.DEFAULT, selfElementRef), DslRef.dto(C.DEFAULT, selfElementRef), this)
        }
        return listOf(crudData)
    }

    override fun CrudData.CRUD.FOR(dslRef: IDslRef): List<CrudData> {
        val crudData = WhensDslRef.whenModelSubelement(dslRef,
            isDtoRef = { dslCrudDelegateImpl.getOrCreateCrudData(simpleName, businessName, DslRef.table(simpleName, dslRef.parentDslRef), dslRef, this) },
            isTableRef = { throw DslException("on ${dslCrudDelegateImpl.selfDslRef} $this FOR $dslRef not allowed for table { }") }
        ) {
            DslException("unknonwn error for ${dslCrudDelegateImpl.selfDslRef} $this FOR $dslRef not allowed for table { }")
        }
        return listOf(crudData)
    }

    // ====================
    // === ModelReffing ===
    // ====================

    val modelReffing = DslImplModelReffing(dslCrudDelegateImpl)

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

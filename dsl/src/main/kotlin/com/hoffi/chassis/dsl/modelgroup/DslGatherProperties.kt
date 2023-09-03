package com.hoffi.chassis.dsl.modelgroup

import com.hoffi.chassis.chassismodel.C
import com.hoffi.chassis.chassismodel.dsl.DslException
import com.hoffi.chassis.dsl.internal.ADslDelegateClass
import com.hoffi.chassis.dsl.internal.ChassisDslMarker
import com.hoffi.chassis.dsl.internal.DslCtxWrapper
import com.hoffi.chassis.dsl.internal.IDslParticipator
import com.hoffi.chassis.shared.dsl.DslRef
import com.hoffi.chassis.shared.dsl.DslRefString
import com.hoffi.chassis.shared.dsl.IDslRef
import com.hoffi.chassis.shared.shared.GatherPropertiesEnum
import com.hoffi.chassis.shared.shared.GatherPropertys
import com.hoffi.chassis.shared.shared.reffing.MODELREFENUM
import org.slf4j.LoggerFactory

// === Api interfaces define pure props/directFuns and "union/intersections used in DSL Lambdas and/or IDslApi delegation ===
@ChassisDslMarker
interface IDslApiGatherPropertiesProp
@ChassisDslMarker
interface IDslApiGatherPropertiesModelAndModelSubelementsCommon : IDslApiModelReffing {
    fun propertiesOfSuperclasses()
    fun propertiesOf(dslModelOrElementRefString: String,                   gatherPropertiesEnum: GatherPropertiesEnum = GatherPropertiesEnum.PROPERTIES_AND_SUPERCLASS_PROPERTIES)
    fun propertiesOf(dslModelOrElementRef: IDslRef, gatherPropertiesEnum: GatherPropertiesEnum = GatherPropertiesEnum.PROPERTIES_AND_SUPERCLASS_PROPERTIES)
}
@ChassisDslMarker
interface IDslApiGatherPropertiesElementsOnlyCommon : IDslApiGatherPropertiesProp {
    fun propertiesOf(modelElement: MODELREFENUM, gatherPropertiesEnum: GatherPropertiesEnum = GatherPropertiesEnum.NONE, simpleName: String = C.DEFAULT)
    fun propertiesOfSuperclassesOf(modelElement: MODELREFENUM, simpleName: String = C.DEFAULT)
}
@ChassisDslMarker
interface IDslApiGatherPropertiesBoth : IDslApiGatherPropertiesModelAndModelSubelementsCommon, IDslApiGatherPropertiesElementsOnlyCommon

// === Impl Interfaces (extend IDslApi's plus methods and props that should not be visible from the DSL ===
interface IDslImplGatherPropertiesProp : IDslApiGatherPropertiesProp {
    // non direct DSL props
    val theGatherPropertys: MutableSet<GatherPropertys>
}
interface IDslImplGatherPropertiesModelAndModelSubelementsCommon : IDslApiGatherPropertiesModelAndModelSubelementsCommon
interface IDslImplGatherPropertiesElementsOnlyCommon : IDslApiGatherPropertiesElementsOnlyCommon
interface IDslImplGatherPropertiesBoth : IDslImplGatherPropertiesProp, IDslApiGatherPropertiesBoth

/** DSL classes, that are contained (and delegated to)
 * by multiple @ChassisDsl IDslClass'es
 * - are no IDslClass themselves
 * - and do not have a ref to the parent IDslClass
 * as these are multiple different ones, so no help to have them anyway */
context(DslCtxWrapper)
@ChassisDslMarker(DslModel::class, DslDto::class, DslTable::class)
class DslGatherPropertiesDelegateImpl(
    simpleNameOfParentDslBlock: String,
    parentRef: IDslRef
)
    : ADslDelegateClass(simpleNameOfParentDslBlock, parentRef), IDslImplGatherPropertiesBoth, IDslParticipator
        //, IDslApiModelReffing by modelReffing  // by DslImplModelReffing(this)  // TODO remove workaround
{
    override fun toString() = "${super@DslGatherPropertiesDelegateImpl.toString()}->[${theGatherPropertys}]"
    val log = LoggerFactory.getLogger(javaClass)
    override val selfDslRef = DslRef.propertiesOf(simpleNameOfParentDslBlock, parentRef)

    override val theGatherPropertys = mutableSetOf<GatherPropertys>()

    override fun propertiesOfSuperclasses() {
        if (dslCtx.currentPASS != dslCtx.PASS_5_REFERENCING) return
        theGatherPropertys.add(GatherPropertys(delegateRef.parentDslRef as DslRef.IModelOrModelSubelement, GatherPropertiesEnum.PROPERTIES_AND_SUPERCLASS_PROPERTIES))
    }

    override fun propertiesOf(dslModelOrElementRefString: String, gatherPropertiesEnum: GatherPropertiesEnum) {
        if (dslCtx.currentPASS != dslCtx.PASS_5_REFERENCING) return

        val theRef = DslRefString.REFmodelOrModelSubelement(dslModelOrElementRefString)
        theGatherPropertys.add(GatherPropertys(theRef, gatherPropertiesEnum))
    }

    override fun propertiesOf(
        dslModelOrElementRef: IDslRef,
        gatherPropertiesEnum: GatherPropertiesEnum
    ) {
        if (dslCtx.currentPASS != dslCtx.PASS_5_REFERENCING) return
        if (dslModelOrElementRef !is DslRef.IModelOrModelSubelement) throw DslException("$this cannot reference a non-model/non-modelSubelement (dto, table, ...")
        theGatherPropertys.add(GatherPropertys(dslModelOrElementRef, gatherPropertiesEnum))
    }

    override fun propertiesOf(
        modelElement: MODELREFENUM,
        gatherPropertiesEnum: GatherPropertiesEnum,
        simpleName: String
    ) {
        if (dslCtx.currentPASS != dslCtx.PASS_5_REFERENCING) return
        // definitely a modelSubElement, as this function should only be callable in context of a DslRef.IModelSubelement
        val modelRef = delegateRef.parentDslRef as DslRef.model
        when (modelElement) {
            MODELREFENUM.MODEL -> theGatherPropertys.add(GatherPropertys(modelRef, gatherPropertiesEnum))
            MODELREFENUM.DTO -> theGatherPropertys.add(GatherPropertys(DslRef.dto(simpleName, modelRef), gatherPropertiesEnum))
            MODELREFENUM.TABLE -> theGatherPropertys.add(GatherPropertys(DslRef.table(simpleName, modelRef), gatherPropertiesEnum))
        }
    }

    override fun propertiesOfSuperclassesOf(modelElement: MODELREFENUM, simpleName: String) {
        propertiesOf(modelElement, GatherPropertiesEnum.SUPERCLASS_PROPERTIES_ONLY, simpleName)
    }

    // ====================
    // === ModelReffing ===
    // ====================

    val modelReffing = DslImplModelReffing(this)

    override fun MODELREFENUM.of(thisModelgroupSubElementRef: IDslRef): IDslRef {
        return modelReffing.fakeOf(this, thisModelgroupSubElementRef)
    }

    override fun MODELREFENUM.of(thisModelgroupsModelSimpleName: String): IDslRef {
        return modelReffing.fakeOf(this, thisModelgroupsModelSimpleName)
    }

    override fun MODELREFENUM.inModelgroup(otherModelgroupSimpleName: String): OtherModelgroupSubelementWithSimpleNameDefault {
        return modelReffing.fakeInModelgroup(this, otherModelgroupSimpleName)
    }

    override fun OtherModelgroupSubelementWithSimpleNameDefault.withModelName(modelName: String): IDslRef {
        return modelReffing.fakeWithModelName(this, modelName)
    }
}

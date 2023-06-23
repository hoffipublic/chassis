package com.hoffi.chassis.dsl.modelgroup

import com.hoffi.chassis.chassismodel.C
import com.hoffi.chassis.dsl.internal.ADslDelegateClass
import com.hoffi.chassis.dsl.internal.ChassisDslMarker
import com.hoffi.chassis.dsl.internal.DslCtxWrapper
import com.hoffi.chassis.dsl.internal.IDslParticipator
import com.hoffi.chassis.shared.dsl.DslDiscriminator
import com.hoffi.chassis.shared.dsl.DslRef
import com.hoffi.chassis.shared.dsl.DslRefString
import com.hoffi.chassis.shared.dsl.IDslRef
import com.hoffi.chassis.shared.shared.GatherPropertiesEnum
import com.hoffi.chassis.shared.shared.GatherPropertys
import org.slf4j.LoggerFactory

// === Api interfaces define pure props/directFuns and "union/intersections used in DSL Lambdas and/or IDslApi delegation ===
@ChassisDslMarker
interface IDslApiGatherPropertiesProp
@ChassisDslMarker
interface IDslApiGatherPropertiesModelAndModelSubelementsCommon {
    fun propertiesOfSuperclasses()
    fun propertiesOf(dslModelOrElementRefString: String,                   gatherPropertiesEnum: GatherPropertiesEnum = GatherPropertiesEnum.PROPERTIES)
    fun propertiesOf(dslModelOrElementRef: DslRef.IModelOrModelSubElement, gatherPropertiesEnum: GatherPropertiesEnum = GatherPropertiesEnum.PROPERTIES)
}
@ChassisDslMarker
interface IDslApiGatherPropertiesElementsOnlyCommon : IDslApiGatherPropertiesProp {
    fun propertiesOf(modelElement: DslRef.model.MODELELEMENT, gatherPropertiesEnum: GatherPropertiesEnum = GatherPropertiesEnum.NONE, simpleName: String = C.DEFAULTSTRING)
    fun propertiesOfSuperclassesOf(modelElement: DslRef.model.MODELELEMENT, simpleName: String = C.DEFAULTSTRING)
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
{
    override fun toString() = "${super@DslGatherPropertiesDelegateImpl.toString()}->[${theGatherPropertys}]"
    val log = LoggerFactory.getLogger(javaClass)
    override val selfDslRef = DslRef.propertiesOf(simpleNameOfParentDslBlock, parentRef)

    override val theGatherPropertys = mutableSetOf<GatherPropertys>()


    override fun propertiesOfSuperclasses() {
        if (dslCtx.currentPASS != dslCtx.PASS_4_REFERENCING) return
        theGatherPropertys.add(GatherPropertys(parentRef.parentRef as DslRef.IModelOrModelSubElement, GatherPropertiesEnum.PROPERTIES_AND_SUPERCLASS_PROPERTIES))
    }

    override fun propertiesOf(dslModelOrElementRefString: String, gatherPropertiesEnum: GatherPropertiesEnum) {
        if (dslCtx.currentPASS != dslCtx.PASS_4_REFERENCING) return

        TODO("Not yet implemented")
        val theRef = DslRefString.modelElementRef(dslModelOrElementRefString, DslDiscriminator.NULL)
        // string to ref
        theGatherPropertys.add(GatherPropertys(theRef, gatherPropertiesEnum))
    }

    override fun propertiesOf(
        dslModelOrElementRef: DslRef.IModelOrModelSubElement,
        gatherPropertiesEnum: GatherPropertiesEnum
    ) {
        if (dslCtx.currentPASS != dslCtx.PASS_4_REFERENCING) return
        theGatherPropertys.add(GatherPropertys(dslModelOrElementRef, gatherPropertiesEnum))
    }

    override fun propertiesOf(
        modelElement: DslRef.model.MODELELEMENT,
        gatherPropertiesEnum: GatherPropertiesEnum,
        simpleName: String
    ) {
        if (dslCtx.currentPASS != dslCtx.PASS_4_REFERENCING) return
        // definitely a modelSubElement, as this function should only be callable in context of a DslRef.IModelSubelement
        val modelRef = parentRef.parentRef as DslRef.model
        when (modelElement) {
            DslRef.model.MODELELEMENT.MODEL -> {
                theGatherPropertys.add(GatherPropertys(modelRef, gatherPropertiesEnum))
            }
            DslRef.model.MODELELEMENT.DTO -> {
                theGatherPropertys.add(GatherPropertys(DslRef.dto(simpleName, modelRef), gatherPropertiesEnum))
            }
            DslRef.model.MODELELEMENT.TABLE -> {
                theGatherPropertys.add(GatherPropertys(DslRef.table(simpleName, modelRef), gatherPropertiesEnum))
            }
        }
    }

    override fun propertiesOfSuperclassesOf(modelElement: DslRef.model.MODELELEMENT, simpleName: String) {
        propertiesOf(modelElement, GatherPropertiesEnum.SUPERCLASS_PROPERTIES_ONLY, simpleName)
    }
}

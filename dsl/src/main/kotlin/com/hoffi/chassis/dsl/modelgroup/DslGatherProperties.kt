package com.hoffi.chassis.dsl.modelgroup

import com.hoffi.chassis.chassismodel.C
import com.hoffi.chassis.dsl.internal.DslDelegators
import com.hoffi.chassis.shared.dsl.*

class GatherPropertys(
    var modelOrModelSubElement: DslRef.ICrosscuttingPropertiesOf = DslRef.model.NULL,
    var gatherPropertiesEnum: GatherPropertiesEnum = GatherPropertiesEnum.NONE
)

interface IDslGatherPropertiesProp {
    // non direct DSL props
    val igatherPropertys: GatherPropertys
}
interface IDslGatherPropertiesModelAndElementsCommon {
    fun propertiesOfSuperclasses()
    fun propertiesOf(dslModelOrElementRefString: String,                                     gatherPropertiesEnum: GatherPropertiesEnum = GatherPropertiesEnum.NONE)
    fun propertiesOf(dslModelOrElementRef: DslRef.ICrosscuttingPropertiesOf, gatherPropertiesEnum: GatherPropertiesEnum = GatherPropertiesEnum.NONE)
}
interface IDslGatherPropertiesElementsOnlyCommon : IDslGatherPropertiesProp {
    fun propertiesOf(modelElement: DslRef.model.MODELELEMENT, simpleName: String = C.DEFAULTSTRING, gatherPropertiesEnum: GatherPropertiesEnum = GatherPropertiesEnum.NONE)
    fun propertiesOfSuperclassesOf(modelElement: DslRef.model.MODELELEMENT, simpleName: String = C.DEFAULTSTRING)
}
interface IDslGatherPropertiesBoth : IDslGatherPropertiesModelAndElementsCommon, IDslGatherPropertiesElementsOnlyCommon
/** DSL classes, that are contained (and delegated to)
 * by multiple @ChassisDsl IDslClass'es
 * - are no IDslClass themselves
 * - and do not have a ref to the parent IDslClass
 * as these are multiple different ones, so no help to have them anyway */
@DslDelegators(DslModel::class, DslDto::class, DslTable::class)
class DslGatherPropertiesImpl(
    var modelOrModelSubElement: DslRef.ICrosscuttingPropertiesOf
)
    : IDslGatherPropertiesBoth, IDelegatee
{
    override val igatherPropertys: GatherPropertys = GatherPropertys()

    override fun propertiesOfSuperclasses() {
        igatherPropertys.modelOrModelSubElement = modelOrModelSubElement
        igatherPropertys.gatherPropertiesEnum = GatherPropertiesEnum.PROPERTIES_AND_SUPERCLASS_PROPERTIES
    }

    override fun propertiesOf(dslModelOrElementRefString: String, gatherPropertiesEnum: GatherPropertiesEnum) {
        TODO("Not yet implemented")
        DslRefString.modelElementRef(dslModelOrElementRefString, DslDiscriminator.NULL)
        // string to ref
        //igatherPropertys.modelOrModelSubElement = theRef
        igatherPropertys.gatherPropertiesEnum = gatherPropertiesEnum
    }

    override fun propertiesOf(
        dslModelOrElementRef: DslRef.ICrosscuttingPropertiesOf,
        gatherPropertiesEnum: GatherPropertiesEnum
    ) {
        igatherPropertys.modelOrModelSubElement = dslModelOrElementRef
        igatherPropertys.gatherPropertiesEnum = gatherPropertiesEnum
    }

    override fun propertiesOf(
        modelElement: DslRef.model.MODELELEMENT,
        simpleName: String,
        gatherPropertiesEnum: GatherPropertiesEnum
    ) {
        // definitely a modelSubElement, as this function should only be callable in context of a DslRef.IModelSubElement
        val modelRef = modelOrModelSubElement.parentRef as DslRef.model
        when (modelElement) {
            DslRef.model.MODELELEMENT.MODEL -> {
                igatherPropertys.modelOrModelSubElement = modelRef
                igatherPropertys.gatherPropertiesEnum = gatherPropertiesEnum
            }
            DslRef.model.MODELELEMENT.DTO -> {
                igatherPropertys.modelOrModelSubElement = DslRef.dto(simpleName, modelRef)
                igatherPropertys.gatherPropertiesEnum = gatherPropertiesEnum
            }
            DslRef.model.MODELELEMENT.TABLE -> {
                igatherPropertys.modelOrModelSubElement = DslRef.table(simpleName, modelRef)
                igatherPropertys.gatherPropertiesEnum = gatherPropertiesEnum
            }
        }
    }

    override fun propertiesOfSuperclassesOf(modelElement: DslRef.model.MODELELEMENT, simpleName: String) {
        propertiesOf(modelElement, simpleName, GatherPropertiesEnum.SUPERCLASS_PROPERTIES_ONLY)
    }
}

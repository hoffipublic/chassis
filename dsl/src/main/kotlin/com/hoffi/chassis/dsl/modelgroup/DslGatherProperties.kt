package com.hoffi.chassis.dsl.modelgroup

import com.hoffi.chassis.chassismodel.C
import com.hoffi.chassis.chassismodel.dsl.ChassisDslMarker
import com.hoffi.chassis.shared.dsl.DslRef
import com.hoffi.chassis.shared.dsl.DslRef.DslGroupRefEither.DslModelgroupRef
import com.hoffi.chassis.shared.dsl.DslRef.DslGroupRefEither.DslModelgroupRef.DslElementRefEither.DslModelRef.MODELELEMENT
import com.hoffi.chassis.shared.dsl.GatherPropertiesEnum

interface IDslGatherPropertiesProp {
    // non direct DSL props
    val igatherPropertys: GatherPropertys
}
interface IDslGatherPropertiesModelAndElementsCommon {
    fun propertiesOfSuperclasses()
    fun propertiesOf(dslModelOrElementRefString: String,                                     gatherPropertiesEnum: GatherPropertiesEnum = GatherPropertiesEnum.NONE)
    fun propertiesOf(dslModelOrElementRef: DslModelgroupRef.DslElementRefEither.DslModelRef, gatherPropertiesEnum: GatherPropertiesEnum = GatherPropertiesEnum.NONE)
}
interface IDslGatherPropertiesModelElementsOnlyCommon : IDslGatherPropertiesProp {
    fun propertiesOf(modelElement: MODELELEMENT, simpleName: String = C.DEFAULT, gatherPropertiesEnum: GatherPropertiesEnum = GatherPropertiesEnum.NONE)
    fun propertiesOfSuperclassesOf(modelElement: MODELELEMENT, simpleName: String = C.DEFAULT)
}
interface IDslGatherPropertiesBoth : IDslGatherPropertiesModelAndElementsCommon, IDslGatherPropertiesModelElementsOnlyCommon
@ChassisDslMarker
class DslGatherPropertiesModelAndElementsCommonImpl( // TODO rename as now the only impl class here
    var modelgroupOrElement: DslModelgroupRef
)
    : IDslGatherPropertiesBoth
{
    override val igatherPropertys: GatherPropertys = GatherPropertys()

    override fun propertiesOfSuperclasses() {
        igatherPropertys.modelgroupOrElement = modelgroupOrElement
        igatherPropertys.gatherPropertiesEnum = GatherPropertiesEnum.PROPERTIES_AND_SUPERCLASS_PROPERTIES
    }

    override fun propertiesOf(dslModelOrElementRefString: String, gatherPropertiesEnum: GatherPropertiesEnum) {
        TODO("Not yet implemented")
        // string to ref
        //igatherPropertys.modelgroupOrElement = theRef
        igatherPropertys.gatherPropertiesEnum = gatherPropertiesEnum
    }

    override fun propertiesOf(
        dslModelOrElementRef: DslModelgroupRef.DslElementRefEither.DslModelRef,
        gatherPropertiesEnum: GatherPropertiesEnum
    ) {
        igatherPropertys.modelgroupOrElement = dslModelOrElementRef
        igatherPropertys.gatherPropertiesEnum = gatherPropertiesEnum
    }

    override fun propertiesOf(
        modelElement: MODELELEMENT,
        simpleName: String,
        gatherPropertiesEnum: GatherPropertiesEnum
    ) {
        val modelRef = modelgroupOrElement.elementRef() as DslModelgroupRef.DslElementRefEither.DslModelRef
        when (modelElement) {
            MODELELEMENT.DTO -> {
                igatherPropertys.modelgroupOrElement = modelRef.dtoRef(simpleName)
                igatherPropertys.gatherPropertiesEnum = gatherPropertiesEnum
            }
            MODELELEMENT.TABLE -> {
                igatherPropertys.modelgroupOrElement = modelRef.tableRef(simpleName)
                igatherPropertys.gatherPropertiesEnum = gatherPropertiesEnum
            }
        }
    }

    override fun propertiesOfSuperclassesOf(
        modelElement: MODELELEMENT,
        simpleName: String
    ) {
        TODO("Not yet implemented")
    }
}

//@ChassisDslMarker
//open class DslGatherPropertiesModelElementsOnlyCommonImpl(
//    modelgroupOrElement: DslModelgroupRef
//)
//    : DslGatherPropertiesModelAndElementsCommonImpl(modelgroupOrElement),  IDslGatherPropertiesModelElementsOnlyCommon
//{
//
//}


class GatherPropertys(
    var modelgroupOrElement: DslRef.DslGroupRefEither = DslRef.DslGroupRefEither.NULL,
    var gatherPropertiesEnum: GatherPropertiesEnum = GatherPropertiesEnum.NONE
)

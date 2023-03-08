package com.hoffi.chassis.dsl.modelgroup

import com.hoffi.chassis.chassismodel.C
import com.hoffi.chassis.chassismodel.dsl.ChassisDslMarker
import com.hoffi.chassis.chassismodel.dsl.DslInstance
import com.hoffi.chassis.dsl.internal.DslBlockOn
import com.hoffi.chassis.dsl.internal.DslClassObjectOrInterface
import com.hoffi.chassis.dsl.whereto.INameAndWheretoPlusModelSubtypes
import com.hoffi.chassis.dsl.whereto.INameAndWheretoWithoutModelSubtypes
import com.hoffi.chassis.dsl.whereto.NameAndWheretoPlusModelSubtypesImpl
import com.hoffi.chassis.dsl.whereto.NameAndWheretoWithoutModelSubtypesImpl
import com.hoffi.chassis.shared.dsl.DslBlockName
import com.hoffi.chassis.shared.dsl.DslRef
import com.hoffi.chassis.shared.dsl.DslRef.DslGroupRefEither.DslModelgroupRef
import com.hoffi.chassis.shared.dsl.DslRef.DslGroupRefEither.DslModelgroupRef.DslElementRefEither.DslModelRef.MODELELEMENT
import com.hoffi.chassis.shared.dsl.IDslClass

@ChassisDslMarker
interface IDslModelAndElementsCommon
    // interfaces implemented by Model And Elements
    :   IDslGatherPropertiesModelAndElementsCommon,
        IDslGatherPropertiesProp
{
    // non direct DSL props
    override val igatherPropertys: GatherPropertys
    // DSL props
    var kind: DslClassObjectOrInterface
}
@ChassisDslMarker
interface IDslModelOnlyCommon
    : INameAndWheretoPlusModelSubtypes
@ChassisDslMarker
interface IDslModelElementsOnlyCommon
    :   INameAndWheretoWithoutModelSubtypes,
        IDslGatherPropertiesModelElementsOnlyCommon
{
    // non direct DSL props
    val modelElement: MODELELEMENT

    // DSL props
}
@ChassisDslMarker
interface IDslFillerModel
@ChassisDslMarker
interface IDslDtoModel :   IDslModelElementsOnlyCommon, IDslModelAndElementsCommon
@ChassisDslMarker
interface IDslTableModel : IDslModelElementsOnlyCommon, IDslModelAndElementsCommon
@ChassisDslMarker
interface IDslModel : IDslModelAndElementsCommon, IDslModelOnlyCommon, INameAndWheretoPlusModelSubtypes {
    @DslBlockOn<DslDtoModel>
    fun dto(simpleName: String = C.DEFAULT, dslBlock: IDslDtoModel.() -> Unit)
    @DslBlockOn<DslTableModel>
    fun table(simpleName: String = C.DEFAULT, dslBlock: IDslTableModel.() -> Unit)
}

/** abstract parent implementations of functionality that is the same in Model and any of its Elements(Dto/Table/...) */
abstract class ADslModelAndElementsCommonImpl(
    val modelOrDslElementRef: DslModelgroupRef.DslElementRefEither,
    final override val parent: IDslClass,
    val gatherProperties: IDslGatherPropertiesModelAndElementsCommon
) : IDslClass,
    IDslModelAndElementsCommon,
    IDslGatherPropertiesModelAndElementsCommon by gatherProperties
{
    // non direct DSL props
    override val selfDslRef: DslRef = modelOrDslElementRef
    override val parentDslRef: DslRef = parent.selfDslRef
    override val groupDslRef: DslRef.DslGroupRefEither = parent.groupDslRef

    // DSL props
    override var kind: DslClassObjectOrInterface = DslClassObjectOrInterface.CLASS
}
/** abstract parent implementations of functionality that is ONLY available in Elements(Dto/Table/...) but NOT in Model itself */
internal abstract class ADslModelElementsOnlyCommon(
    modelDslElementRef: DslModelgroupRef.DslElementRefEither,
    parent: IDslClass,
    val nameAndWheretoWithoutModelSubtypesImpl: NameAndWheretoWithoutModelSubtypesImpl,
    gatherProperties: DslGatherPropertiesModelAndElementsCommonImpl
)
    : ADslModelAndElementsCommonImpl(modelDslElementRef, parent, gatherProperties),
    IDslModelElementsOnlyCommon,
    INameAndWheretoWithoutModelSubtypes by nameAndWheretoWithoutModelSubtypesImpl,
    IDslGatherPropertiesModelElementsOnlyCommon by gatherProperties
{
}

@ChassisDslMarker
internal class DslDtoModel(
    modelDslDtoRef: DslModelgroupRef.DslElementRefEither.DslModelRef.DslSubElementRefEither.DslDtoRef,
    parent: IDslClass,
    nameAndWheretoWithoutModelSubtypesImpl: NameAndWheretoWithoutModelSubtypesImpl = NameAndWheretoWithoutModelSubtypesImpl(parent),
    gatherPropertiesImpl: DslGatherPropertiesModelAndElementsCommonImpl = DslGatherPropertiesModelAndElementsCommonImpl(modelDslDtoRef)
)
    : ADslModelElementsOnlyCommon(modelDslDtoRef, parent, nameAndWheretoWithoutModelSubtypesImpl, gatherPropertiesImpl), IDslDtoModel
{
    override val modelElement: MODELELEMENT = MODELELEMENT.DTO
}
@ChassisDslMarker
internal class DslTableModel(
    val modelDslTableRef: DslModelgroupRef.DslElementRefEither.DslModelRef.DslSubElementRefEither.DslTableRef,
    parent: IDslClass,
    nameAndWheretoWithoutModelSubtypesImpl: NameAndWheretoWithoutModelSubtypesImpl = NameAndWheretoWithoutModelSubtypesImpl(parent),
    gatherPropertiesImpl: DslGatherPropertiesModelAndElementsCommonImpl = DslGatherPropertiesModelAndElementsCommonImpl(modelDslTableRef)
)
    : ADslModelElementsOnlyCommon(modelDslTableRef, parent, nameAndWheretoWithoutModelSubtypesImpl, gatherPropertiesImpl), IDslTableModel
{
    override val modelElement: MODELELEMENT = MODELELEMENT.TABLE
}
@ChassisDslMarker
internal class DslModel(
    modelRef: DslModelgroupRef.DslElementRefEither.DslModelRef,
    parent: IDslClass,
    val nameAndWheretoPlusModelSubtypesImpl: NameAndWheretoPlusModelSubtypesImpl = NameAndWheretoPlusModelSubtypesImpl(parent),
    gatherPropertiesImpl: DslGatherPropertiesModelAndElementsCommonImpl = DslGatherPropertiesModelAndElementsCommonImpl(modelRef)
)
    : ADslModelAndElementsCommonImpl(modelRef, parent, gatherPropertiesImpl), IDslModel,
    INameAndWheretoPlusModelSubtypes by nameAndWheretoPlusModelSubtypesImpl,
    IDslGatherPropertiesModelElementsOnlyCommon by gatherPropertiesImpl
{
    @DslInstance
    internal val dslDtoObj = DslDtoModel(modelRef.dtoRef(DslBlockName.MODEL_DTO.name), this)
    @DslInstance
    internal val dslTableObj = DslTableModel(modelRef.tableRef(DslBlockName.MODEL_TABLE.name), this)

    init {
        // ensure at compile-time that all ModelSubtypes are handled in this class
        when (DslModelgroupRef.DslElementRefEither.DslModelRef.DslSubElementRefEither.NULL) {
            is DslModelgroupRef.DslElementRefEither.DslModelRef.DslSubElementRefEither.NoneRef -> {}
            is DslModelgroupRef.DslElementRefEither.DslModelRef.DslSubElementRefEither.DslDtoRef -> {}
            is DslModelgroupRef.DslElementRefEither.DslModelRef.DslSubElementRefEither.DslTableRef -> {}
        }
    }

    @DslBlockOn<DslDtoModel>
    override fun dto(simpleName: String, dslBlock: IDslDtoModel.() -> Unit) {
        dslDtoObj.apply(dslBlock)
    }

    @DslBlockOn<DslTableModel>
    override fun table(simpleName: String, dslBlock: IDslTableModel.() -> Unit) {
        dslTableObj.apply(dslBlock)
    }
}

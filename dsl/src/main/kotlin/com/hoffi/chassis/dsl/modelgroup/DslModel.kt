package com.hoffi.chassis.dsl.modelgroup

import com.hoffi.chassis.chassismodel.C
import com.hoffi.chassis.chassismodel.dsl.ChassisDsl
import com.hoffi.chassis.chassismodel.dsl.ChassisDslMarker
import com.hoffi.chassis.chassismodel.dsl.DslInstance
import com.hoffi.chassis.chassismodel.dsl.NonDsl
import com.hoffi.chassis.dsl.internal.DslBlockOn
import com.hoffi.chassis.dsl.internal.DslClassObjectOrInterface
import com.hoffi.chassis.dsl.whereto.INameAndWheretoPlusModelSubtypes
import com.hoffi.chassis.dsl.whereto.INameAndWheretoWithoutModelSubtypes
import com.hoffi.chassis.dsl.whereto.NameAndWheretoPlusModelSubtypesImpl
import com.hoffi.chassis.dsl.whereto.NameAndWheretoWithoutModelSubtypesImpl
import com.hoffi.chassis.shared.dsl.DslRef
import com.hoffi.chassis.shared.dsl.IDslClass
import com.hoffi.chassis.shared.dsl.IDslRef

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
interface IDslElementsOnlyCommon
    :   INameAndWheretoWithoutModelSubtypes,
        IDslGatherPropertiesElementsOnlyCommon
{
    // non direct DSL props
    val modelElement: DslRef.model.MODELELEMENT

    // DSL props
}
@ChassisDslMarker
interface IDslFillerModel
@ChassisDslMarker
interface IDslDto :   IDslElementsOnlyCommon, IDslModelAndElementsCommon
@ChassisDslMarker
interface IDslTable : IDslElementsOnlyCommon, IDslModelAndElementsCommon
@ChassisDslMarker
interface IDslModel : IDslModelAndElementsCommon, IDslModelOnlyCommon, INameAndWheretoPlusModelSubtypes {
    @DslBlockOn(DslDto::class)
    fun dto(simpleName: String = C.DEFAULT, dslBlock: IDslDto.() -> Unit)
    @DslBlockOn(DslTable::class)
    fun table(simpleName: String = C.DEFAULT, dslBlock: IDslTable.() -> Unit)
}

/** abstract parent implementations of functionality that is the same in Model and any of its Elements(Dto/Table/...) */
abstract class ADslModelAndElementsCommonImpl(
    val modelOrModelSubElementRef: DslRef.IModelOrModelSubElement,
    final override val parent: IDslClass,
    val gatherProperties: IDslGatherPropertiesModelAndElementsCommon
) : IDslClass,
    IDslModelAndElementsCommon,
    IDslGatherPropertiesModelAndElementsCommon by gatherProperties
{
    // non direct DSL props
    override val selfDslRef: DslRef.IModelOrModelSubElement = modelOrModelSubElementRef
    override val parentDslRef: IDslRef = parent.selfDslRef
    override val groupDslRef: DslRef.IGroupLevel = parent.groupDslRef
    override fun toString() = selfDslRef.toString()

    // DSL props
    override var kind: DslClassObjectOrInterface = DslClassObjectOrInterface.CLASS
}
/** abstract parent implementations of functionality that is ONLY available in Elements(Dto/Table/...) but NOT in Model itself */
abstract class ADslElementsOnlyCommon(
    modelDslSubElementRef: DslRef.IModelOrModelSubElement,
    parent: IDslClass,
    val nameAndWheretoWithoutModelSubtypesImpl: NameAndWheretoWithoutModelSubtypesImpl,
    gatherProperties: DslGatherPropertiesImpl
)
    : ADslModelAndElementsCommonImpl(modelDslSubElementRef, parent, gatherProperties),
    IDslElementsOnlyCommon,
    INameAndWheretoWithoutModelSubtypes by nameAndWheretoWithoutModelSubtypesImpl,
    IDslGatherPropertiesElementsOnlyCommon by gatherProperties
{
}

@ChassisDsl
class DslDto(
    simpleName: String,
    val dtoRef: DslRef.dto,
    parent: IDslClass,
    nameAndWheretoWithoutModelSubtypesImpl: NameAndWheretoWithoutModelSubtypesImpl = NameAndWheretoWithoutModelSubtypesImpl(DslRef.nameAndWhereto(simpleName, dtoRef)),
    gatherPropertiesImpl: DslGatherPropertiesImpl = DslGatherPropertiesImpl(dtoRef)
)
    : ADslElementsOnlyCommon(dtoRef, parent, nameAndWheretoWithoutModelSubtypesImpl, gatherPropertiesImpl), IDslDto
{
    @NonDsl
    override val modelElement = DslRef.model.MODELELEMENT.DTO
}
@ChassisDsl
class DslTable(
    simpleName: String,
    val tableRef: DslRef.table,
    parent: IDslClass,
    nameAndWheretoWithoutModelSubtypesImpl: NameAndWheretoWithoutModelSubtypesImpl = NameAndWheretoWithoutModelSubtypesImpl(DslRef.nameAndWhereto(simpleName, tableRef)),
    gatherPropertiesImpl: DslGatherPropertiesImpl = DslGatherPropertiesImpl(tableRef)
)
    : ADslElementsOnlyCommon(tableRef, parent, nameAndWheretoWithoutModelSubtypesImpl, gatherPropertiesImpl), IDslTable
{
    override val modelElement = DslRef.model.MODELELEMENT.TABLE
}
// TODO Model and ModelElements with simpleName !!!
@ChassisDsl
class DslModel(
    val simpleName: String,
    val modelRef: DslRef.model,
    parent: IDslClass,
    val nameAndWheretoPlusModelSubtypesImpl: NameAndWheretoPlusModelSubtypesImpl = NameAndWheretoPlusModelSubtypesImpl(DslRef.nameAndWhereto(simpleName, modelRef)),
    val gatherPropertiesImpl: DslGatherPropertiesImpl = DslGatherPropertiesImpl(modelRef)
)
    : ADslModelAndElementsCommonImpl(modelRef, parent, gatherPropertiesImpl), IDslModel,
    INameAndWheretoPlusModelSubtypes by nameAndWheretoPlusModelSubtypesImpl,
    IDslGatherPropertiesElementsOnlyCommon by gatherPropertiesImpl
{
    val dslDtos = mutableSetOf<DslDto>()
    val tableDtos = mutableSetOf<DslTable>()

    init {
        // ensure at compile-time that all ModelSubtypes are handled in this class
        when (DslRef.IModelSubElement::class.sealedSubclasses) {

        }
    }

    @DslBlockOn(DslDto::class)
    override fun dto(simpleName: String, dslBlock: IDslDto.() -> Unit) {
        @DslInstance
        val dslDtoObj = DslDto(simpleName, DslRef.dto(simpleName, modelRef), this)
        dslDtos.add(dslDtoObj)
        dslDtoObj.apply(dslBlock)
    }

    @DslBlockOn(DslTable::class)
    override fun table(simpleName: String, dslBlock: IDslTable.() -> Unit) {
        @DslInstance
        val dslTableObj = DslTable(simpleName, DslRef.table(simpleName, modelRef), this)
        tableDtos.add(dslTableObj)
        dslTableObj.apply(dslBlock)
    }

    companion object {
        val NULL: DslModel = DslModel(C.NULLSTRING, DslRef.model.NULL, IDslClass.Companion.NULL)
    }
}

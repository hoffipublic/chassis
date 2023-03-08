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
import com.hoffi.chassis.shared.values.gatherFromSuperclasses

@ChassisDslMarker
interface IDslModelAndElementsCommon : IDslClass {
    fun propertiesOfSuperclasses()
    fun propertiesOf(dslModelOrElementRef: String, gatherFromSuperclasses: gatherFromSuperclasses)

    var kind: DslClassObjectOrInterface
}
@ChassisDslMarker
interface IDslModelElementsCommon : IDslModelAndElementsCommon, INameAndWheretoWithoutModelSubtypes, IDslGatherProperties {
    val modelElement: MODELELEMENT
    fun propertiesOf(modelElement: MODELELEMENT, gatherFromSuperclasses: gatherFromSuperclasses)
    fun propertiesOfSuperclassesOf(modelElement: MODELELEMENT)
}
abstract class DslModelAndElementsCommonImpl(
    val modelOrDslElementRef: DslModelgroupRef.DslElementRefEither,
    final override val parent: IDslClass
) : IDslModelAndElementsCommon
{
    override val selfDslRef: DslRef = modelOrDslElementRef
    override val parentDslRef: DslRef = parent.selfDslRef
    override val groupDslRef: DslRef.DslGroupRefEither = parent.groupDslRef

    override var kind: DslClassObjectOrInterface = DslClassObjectOrInterface.CLASS

    override fun propertiesOfSuperclasses() {
        TODO("Not yet implemented")
    }

    override fun propertiesOf(dslModelOrElementRef: String, gatherFromSuperclasses: gatherFromSuperclasses) {
        TODO("Not yet implemented")
    }
}
@ChassisDslMarker
interface IDslDtoModel : IDslModelElementsCommon
@ChassisDslMarker
interface IDslTableModel : IDslModelElementsCommon
@ChassisDslMarker
interface IDslFillerModel

public interface IDslModel : IDslModelAndElementsCommon, INameAndWheretoPlusModelSubtypes, IDslGatherProperties {
    @DslBlockOn<DslDtoModel>
    fun dto(simpleName: String = C.DEFAULT, dslBlock: IDslDtoModel.() -> Unit)
    @DslBlockOn<DslTableModel>
    fun table(simpleName: String = C.DEFAULT, dslBlock: IDslTableModel.() -> Unit)
}


internal abstract class DslModelElementCommon(
    modelDslElementRef: DslModelgroupRef.DslElementRefEither,
    parent: IDslClass,
    val nameAndWheretoWithoutModelSubtypesImpl: NameAndWheretoWithoutModelSubtypesImpl,
    val gatherPropertiesImpl: DslGatherPropertiesImpl = DslGatherPropertiesImpl(modelDslElementRef)
)
    : DslModelAndElementsCommonImpl(modelDslElementRef, parent),
    IDslModelElementsCommon,
    INameAndWheretoWithoutModelSubtypes by nameAndWheretoWithoutModelSubtypesImpl,
    IDslGatherProperties by gatherPropertiesImpl
{
    override fun propertiesOf(modelElement: MODELELEMENT, gatherFromSuperclasses: gatherFromSuperclasses) {
        TODO("Not yet implemented")
    }

    override fun propertiesOfSuperclassesOf(modelElement: MODELELEMENT) {
        TODO("Not yet implemented")
    }
}

@ChassisDslMarker
internal class DslDtoModel(
    modelDslDtoRef: DslModelgroupRef.DslElementRefEither.DslModelRef.DslSubElementRefEither.DslDtoRef,
    parent: IDslClass,
    nameAndWheretoWithoutModelSubtypesImpl: NameAndWheretoWithoutModelSubtypesImpl = NameAndWheretoWithoutModelSubtypesImpl(parent),
    gatherPropertiesImpl: DslGatherPropertiesImpl = DslGatherPropertiesImpl(modelDslDtoRef)
)
    : IDslDtoModel, DslModelElementCommon(modelDslDtoRef, parent, nameAndWheretoWithoutModelSubtypesImpl, gatherPropertiesImpl)
{
    override val modelElement: MODELELEMENT = MODELELEMENT.DTO
}
@ChassisDslMarker
internal class DslTableModel(
    val modelDslTableRef: DslModelgroupRef.DslElementRefEither.DslModelRef.DslSubElementRefEither.DslTableRef,
    parent: IDslClass,
    nameAndWheretoWithoutModelSubtypesImpl: NameAndWheretoWithoutModelSubtypesImpl = NameAndWheretoWithoutModelSubtypesImpl(parent),
    gatherPropertiesImpl: DslGatherPropertiesImpl = DslGatherPropertiesImpl(modelDslTableRef)
)
    : IDslTableModel, DslModelElementCommon(modelDslTableRef, parent, nameAndWheretoWithoutModelSubtypesImpl, gatherPropertiesImpl)
{
    override val modelElement: MODELELEMENT = MODELELEMENT.TABLE
}
@ChassisDslMarker
internal class DslModel(
    modelRef: DslModelgroupRef.DslElementRefEither.DslModelRef,
    parent: IDslClass,
    val nameAndWheretoPlusModelSubtypesImpl: NameAndWheretoPlusModelSubtypesImpl = NameAndWheretoPlusModelSubtypesImpl(parent),
    val gatherPropertiesImpl: DslGatherPropertiesImpl = DslGatherPropertiesImpl(modelRef)
)
    : DslModelAndElementsCommonImpl(modelRef, parent), IDslModel,
    INameAndWheretoPlusModelSubtypes by nameAndWheretoPlusModelSubtypesImpl,
    IDslGatherProperties by gatherPropertiesImpl
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

package com.hoffi.chassis.dsl.modelgroup

import com.hoffi.chassis.chassismodel.C
import com.hoffi.chassis.dsl.DslClassModsDelegateImpl
import com.hoffi.chassis.dsl.IDslImplClassModsDelegate
import com.hoffi.chassis.dsl.internal.*
import com.hoffi.chassis.dsl.whereto.DslNameAndWheretoOnlyDelegateImpl
import com.hoffi.chassis.dsl.whereto.IDslApiNameAndWheretoOnly
import com.hoffi.chassis.shared.dsl.DslRef
import com.hoffi.chassis.shared.dsl.IDslRef
import com.hoffi.chassis.shared.parsedata.GenModel
import com.hoffi.chassis.shared.parsedata.nameandwhereto.CollectedNameAndWheretos
import com.hoffi.chassis.shared.parsedata.nameandwhereto.StrategyNameAndWhereto
import com.squareup.kotlinpoet.TypeSpec
import org.slf4j.LoggerFactory

@ChassisDslMarker
interface IDslApiDco
    :   IDslApiModelAndModelSubelementsCommon,
    IDslApiSubelementsOnlyCommon

context(DslCtxWrapper)
class DslDco(
    simpleName: String,
    dcoRef: DslRef.dco,
    classModifiersImpl: DslClassModifiersImpl             = DslClassModifiersImpl(),
    propsImpl: DslPropsDelegate                           = with (dslCtxWrapperFake) { dslCtx.ctxObjOrCreate(DslRef.properties(simpleName, dcoRef)) },
    nameAndWheretoWithoutModelSubelementsImpl: DslNameAndWheretoOnlyDelegateImpl = with (dslCtxWrapperFake) { dslCtx.ctxObjOrCreate(DslRef.nameAndWhereto(simpleName, dcoRef)) },
    gatherPropertiesImpl: DslGatherPropertiesDelegateImpl = with (dslCtxWrapperFake) { dslCtx.ctxObjOrCreate(DslRef.propertiesOf(simpleName, dcoRef)) },
    classModsImpl: DslClassModsDelegateImpl = with (dslCtxWrapperFake) { dslCtx.ctxObjOrCreate(DslRef.classMods(simpleName, dcoRef)) },
    extendsImpl: DslExtendsDelegateImpl                   = with (dslCtxWrapperFake) { dslCtx.ctxObjOrCreate(DslRef.extends(simpleName, dcoRef)) },
    //val propsImpl: DslPropsDelegate                           = dslCtx.ctxObjOrCreate(DslRef.properties(simpleNameOfParentDslBlock, dcoRef)),
    //val nameAndWheretoWithoutModelSubelementsImpl: DslNameAndWheretoOnlyDelegateImpl = dslCtx.ctxObjOrCreate(DslRef.nameAndWhereto(simpleNameOfParentDslBlock, dcoRef)),
    //val gatherPropertiesImpl: DslGatherPropertiesDelegateImpl = dslCtx.ctxObjOrCreate(DslRef.propertiesOf(simpleNameOfParentDslBlock, dcoRef)),
    //val classModsImpl: DslClassModsDelegateImpl               = dslCtx.ctxObjOrCreate(DslRef.classMods(simpleNameOfParentDslBlock, dcoRef)),
    //val extendsImpl: DslExtendsDelegateImpl                   = dslCtx.ctxObjOrCreate(DslRef.extends(simpleNameOfParentDslBlock, dcoRef)),
)
    : AProperModelSubelement(simpleName, dcoRef, classModifiersImpl, propsImpl, nameAndWheretoWithoutModelSubelementsImpl, gatherPropertiesImpl, classModsImpl, extendsImpl),
    IDslApiDco,
    IDslApiModelAndModelSubelementsCommon,
    IDslApiKindClassObjectOrInterface,
    IDslApiConstructorVisibility,

    IDslImplClassModifiers by classModifiersImpl,
    IDslApiPropFuns by propsImpl,
    IDslApiInitializer by propsImpl,
    IDslImplClassModsDelegate by classModsImpl,
    IDslImplExtendsDelegate by extendsImpl,

    IDslApiNameAndWheretoOnly by nameAndWheretoWithoutModelSubelementsImpl,
    IDslApiGatherPropertiesModelAndModelSubelementsCommon by gatherPropertiesImpl,
    IDslApiGatherPropertiesElementsOnlyCommon             by gatherPropertiesImpl
{
    val log = LoggerFactory.getLogger(javaClass)

    init {
        this@DslCtxWrapper.dslCtx.addToCtx(propsImpl)
        this@DslCtxWrapper.dslCtx.addToCtx(nameAndWheretoWithoutModelSubelementsImpl)
        this@DslCtxWrapper.dslCtx.addToCtx(gatherPropertiesImpl)
        this@DslCtxWrapper.dslCtx.addToCtx(classModsImpl)
        this@DslCtxWrapper.dslCtx.addToCtx(extendsImpl)
    }
    override var kind: DslClassObjectOrInterface = DslClassObjectOrInterface.UNDEFINED
    override var constructorVisibility: IDslApiConstructorVisibility.VISIBILITY = IDslApiConstructorVisibility.VISIBILITY.UNSET

    fun finish(dslCtx: DslCtx) {
        val collectedNameAndWheretos: CollectedNameAndWheretos = finishCollectedNameAndWheretos(nameAndWheretoWithoutModelSubelementsImpl)

        val modelClassName = StrategyNameAndWhereto.resolve(StrategyNameAndWhereto.STRATEGY.SPECIAL_WINS_ON_ABSOLUTE_CONCAT_ADDENDUMS, selfDslRef, collectedNameAndWheretos)
        val dcoModelFromDsl = GenModel.DcoModelFromDsl(selfDslRef as DslRef.dco, modelClassName)
        dslCtx.genCtx.putGenModelFromDsl(selfDslRef, dcoModelFromDsl)
        val dslModel: DslModel = dslCtx.ctxObj(parentDslRef)
        val dslGroup: DslModelgroup = dslCtx.ctxObj((parentDslRef.parentDslRef))

        when (kind) {
            DslClassObjectOrInterface.CLASS -> dcoModelFromDsl.kind = TypeSpec.Kind.CLASS
            DslClassObjectOrInterface.OBJECT -> dcoModelFromDsl.kind = TypeSpec.Kind.OBJECT
            DslClassObjectOrInterface.INTERFACE -> dcoModelFromDsl.kind = TypeSpec.Kind.INTERFACE
            DslClassObjectOrInterface.UNDEFINED -> {
                when (dslModel.kind) {
                    DslClassObjectOrInterface.CLASS -> dcoModelFromDsl.kind = TypeSpec.Kind.CLASS
                    DslClassObjectOrInterface.OBJECT -> dcoModelFromDsl.kind = TypeSpec.Kind.OBJECT
                    DslClassObjectOrInterface.INTERFACE -> dcoModelFromDsl.kind = TypeSpec.Kind.INTERFACE
                    DslClassObjectOrInterface.UNDEFINED -> dcoModelFromDsl.kind = TypeSpec.Kind.CLASS
                }
            }
        }
        dcoModelFromDsl.constructorVisibility =
            if (constructorVisibility == IDslApiConstructorVisibility.VISIBILITY.UNSET)
                if (dslModel.constructorVisibility == IDslApiConstructorVisibility.VISIBILITY.UNSET)
                    if (dslGroup.constructorVisibility == IDslApiConstructorVisibility.VISIBILITY.UNSET)
                        true
                    else
                        dslGroup.constructorVisibility == IDslApiConstructorVisibility.VISIBILITY.PUBLIC
                else
                    dslModel.constructorVisibility == IDslApiConstructorVisibility.VISIBILITY.PUBLIC
            else
                constructorVisibility == IDslApiConstructorVisibility.VISIBILITY.PUBLIC

        dcoModelFromDsl.additionalToStringMemberProps.addAll(propsImpl.additionalToStringMemberProps)
        dcoModelFromDsl.removeToStringMemberProps.addAll(propsImpl.removeToStringMemberProps)

        finishProperModelsModelClassData(dslModel, dcoModelFromDsl, classModifiersImpl, extendsImpl, gatherPropertiesImpl, propsImpl)
    }

    companion object {
        val NULL: DslDco = with (dslCtxWrapperFake)
        {
            DslDco(
                C.NULLSTRING,
                DslRef.dco(C.NULLSTRING, IDslRef.NULL),
                DslClassModifiersImpl(),
                DslPropsDelegate(C.NULLSTRING, parentRef = IDslRef.NULL),
                DslNameAndWheretoOnlyDelegateImpl(C.NULLSTRING, IDslRef.NULL),
                DslGatherPropertiesDelegateImpl(C.NULLSTRING, IDslRef.NULL),
                DslClassModsDelegateImpl(C.NULLSTRING, IDslRef.NULL),
                DslExtendsDelegateImpl(C.NULLSTRING, IDslRef.NULL),
            )
        }
    }
}


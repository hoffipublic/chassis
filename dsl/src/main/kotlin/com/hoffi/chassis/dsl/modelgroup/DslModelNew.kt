package com.hoffi.chassis.dsl.modelgroup

import com.hoffi.chassis.chassismodel.C
import com.hoffi.chassis.chassismodel.dsl.DslException
import com.hoffi.chassis.dsl.*
import com.hoffi.chassis.dsl.internal.*
import com.hoffi.chassis.dsl.whereto.*
import com.hoffi.chassis.shared.dsl.DslRef
import com.hoffi.chassis.shared.parsedata.EitherModel
import com.hoffi.chassis.shared.parsedata.StrategyGatherProperties
import com.hoffi.chassis.shared.parsedata.nameandwhereto.SharedGatheredNameAndWheretos
import com.hoffi.chassis.shared.parsedata.nameandwhereto.SharedNameAndWhereto
import com.hoffi.chassis.shared.parsedata.nameandwhereto.StrategyNameAndWhereto
import com.squareup.kotlinpoet.TypeSpec
import org.slf4j.LoggerFactory

@ChassisDslMarker
interface IDslApiClassObjectOrInterface {
    var kind: DslClassObjectOrInterface
}

context(DslCtxWrapper)
abstract class AModelSubelement(
    val simpleName: String,
    val modelSubelementRef: DslRef.IModelSubelement
)
    : ADslClass()
{
    override val selfDslRef = modelSubelementRef
}

@ChassisDslMarker
interface IDslApiModel
    :   IDslApiModelAndModelSubelementsCommon,
    IDslApiModelOnlyCommon,
    IDslApiExtendsDelegate,
    IDslApiPropFuns,
    IDslApiNameAndWheretoWithSubelements,
    IDslApiClassModsDelegate,
    IDslApiShowcaseDelegate
{
    @DslBlockOn(DslDto::class)
    fun dto(simpleName: String = C.DEFAULT, dslBlock: IDslApiDto.() -> Unit)
    @DslBlockOn(DslTable::class)
    fun table(simpleName: String = C.DEFAULT, dslBlock: IDslApiTable.() -> Unit)
}

context(DslCtxWrapper)
@ChassisDslMarker
class DslModel constructor(
    val simpleName: String,
    val modelRef: DslRef.model,
    val classModifiersImpl: DslClassModifiersImpl             = DslClassModifiersImpl(),
    val propsImpl: DslPropsDelegate                           = with (dslCtxWrapperFake) { dslCtx.ctxObjOrCreate(DslRef.properties(simpleName, modelRef)) },
    val nameAndWheretoWithSubelements: DslNameAndWheretoWithSubelementsDelegateImpl = with (dslCtxWrapperFake) { dslCtx.ctxObjOrCreate(DslRef.nameAndWhereto(simpleName, modelRef)) },
    val gatherPropertiesImpl: DslGatherPropertiesDelegateImpl = with (dslCtxWrapperFake) { dslCtx.ctxObjOrCreate(DslRef.propertiesOf(simpleName, modelRef)) },
    val classModsImpl: DslClassModsDelegateImpl               = with (dslCtxWrapperFake) { dslCtx.ctxObjOrCreate(DslRef.classMods(simpleName, modelRef)) },
    val extendsImpl: DslExtendsDelegateImpl                   = with (dslCtxWrapperFake) { dslCtx.ctxObjOrCreate(DslRef.extends(simpleName, modelRef)) },
    val showcaseImpl: DslShowcaseDelegateImpl                 = with (dslCtxWrapperFake) { dslCtx.ctxObjOrCreate(DslRef.showcase(simpleName, modelRef)) },
    //val propsImpl: DslPropsDelegate                           = this@DslCtxWrapper.dslCtx.ctxObjOrCreate(DslRef.properties(simpleName, modelRef)),
    //val nameAndWheretoWithSubelements: DslNameAndWheretoWithSubelementsDelegateImpl = this@DslCtxWrapper.dslCtx.ctxObjOrCreate(DslRef.nameAndWhereto(simpleName, modelRef)),
    //val gatherPropertiesImpl: DslGatherPropertiesDelegateImpl = this@DslCtxWrapper.dslCtx.ctxObjOrCreate(DslRef.propertiesOf(simpleName, modelRef)),
    //val classModsImpl: DslClassModsDelegateImpl               = this@DslCtxWrapper.dslCtx.ctxObjOrCreate(DslRef.classMods(simpleName, modelRef)),
    //val extendsImpl: DslExtendsDelegateImpl                   = this@DslCtxWrapper.dslCtx.ctxObjOrCreate(DslRef.extends(simpleName, modelRef)),
    //val showcaseImpl: DslShowcaseDelegateImpl                 = this@DslCtxWrapper.dslCtx.ctxObjOrCreate(DslRef.showcase(simpleName, modelRef))
) : ADslClass(),
    IDslApiModel,
    IDslApiClassObjectOrInterface,
    IDslApiClassModifiers by classModifiersImpl,
    IDslApiPropFuns by propsImpl,
    IDslApiNameAndWheretoWithSubelements by nameAndWheretoWithSubelements,
    IDslApiGatherPropertiesModelAndModelSubelementsCommon by gatherPropertiesImpl,
    IDslApiClassModsDelegate by classModsImpl,
    IDslApiExtendsDelegate by extendsImpl,
    IDslApiShowcaseDelegate by showcaseImpl
{
    val log = LoggerFactory.getLogger(javaClass)
    override val selfDslRef = modelRef

    init {
        val workaround = dslCtxWrapperFake
        this@DslCtxWrapper.dslCtx.addToCtx(propsImpl)
        this@DslCtxWrapper.dslCtx.addToCtx(nameAndWheretoWithSubelements)
        this@DslCtxWrapper.dslCtx.addToCtx(gatherPropertiesImpl)
        this@DslCtxWrapper.dslCtx.addToCtx(classModsImpl)
        this@DslCtxWrapper.dslCtx.addToCtx(extendsImpl)
        this@DslCtxWrapper.dslCtx.addToCtx(showcaseImpl)
    }

    val dslDtos = mutableMapOf<String, DslDto>()
    val dslTables = mutableMapOf<String, DslTable>()

    // IDslApiClassObjectOrInterface
    override var kind: DslClassObjectOrInterface = DslClassObjectOrInterface.CLASS

    @DslBlockOn(DslDto::class)
    override fun dto(simpleName: String, dslBlock: IDslApiDto.() -> Unit) {
        log.info("fun {}(\"{}\") { ... } in PASS {}", object{}.javaClass.enclosingMethod.name, simpleName, dslCtx.currentPASS)
        when (dslCtx.currentPASS) {
            dslCtx.PASS_1_BASEMODELS -> {
                val dto: DslDto = dslCtx.ctxObjCreateNonDelegate { DslDto(simpleName, DslRef.dto(simpleName, selfDslRef)) }
                dslDtos[simpleName] = dto
                dto.apply(dslBlock)
            }
            dslCtx.PASS_ERROR -> TODO()
            dslCtx.PASS_FINISH -> {
                val dto: DslDto = dslCtx.ctxObj(DslRef.dto(simpleName, selfDslRef))
                dto.apply(dslBlock) // first let all the subtree finish
                dto.finish(dslCtx)
            }
            else -> {
                val dto: DslDto = dslCtx.ctxObj(DslRef.dto(simpleName, selfDslRef))
                dto.apply(dslBlock)
            }
        }
    }

    @DslBlockOn(DslTable::class)
    override fun table(simpleName: String, dslBlock: IDslApiTable.() -> Unit) {
        log.info("fun {}(\"{}\") { ... } in PASS {}", object{}.javaClass.enclosingMethod.name, simpleName, dslCtx.currentPASS)
        when (dslCtx.currentPASS) {
            dslCtx.PASS_1_BASEMODELS -> {
                val table: DslTable = dslCtx.ctxObjCreateNonDelegate { DslTable(simpleName, DslRef.table(simpleName, selfDslRef)) }
                dslTables[simpleName] = table
                table.apply(dslBlock)
            }
            dslCtx.PASS_ERROR -> TODO()
            dslCtx.PASS_FINISH -> {
                val table: DslTable = dslCtx.ctxObj(DslRef.table(simpleName, selfDslRef))
                table.apply(dslBlock) // first let all the subtree finish
                table.finish(dslCtx)
            }
            else -> {
                val table: DslTable = dslCtx.ctxObj(DslRef.table(simpleName, selfDslRef))
                table.apply(dslBlock)
            }
        }
    }

    fun finish(dslCtx: DslCtx) {
        val gatheredNameAndWheretos: SharedGatheredNameAndWheretos = dslCtx.gatheredNameAndWheretos(modelRef)
        for (dslNameAndWheretoDelegateEntry: MutableMap.MutableEntry<String, DslNameAndWheretoOnSubElementsDelegateImpl> in nameAndWheretoWithSubelements.nameAndWheretos) {
            gatheredNameAndWheretos.createFor(SharedGatheredNameAndWheretos.THINGSWITHNAMEANDWHERETOS.model, SharedNameAndWhereto(
                dslNameAndWheretoDelegateEntry.value.simpleName,
                dslNameAndWheretoDelegateEntry.value.selfDslRef,
                dslNameAndWheretoDelegateEntry.value.strategyClassName, dslNameAndWheretoDelegateEntry.value.strategyTableName,
                dslNameAndWheretoDelegateEntry.value.baseDirPathAbsolute, dslNameAndWheretoDelegateEntry.value.baseDirAddendum,
                dslNameAndWheretoDelegateEntry.value.pathAbsolute , dslNameAndWheretoDelegateEntry.value.pathAddendum,
                dslNameAndWheretoDelegateEntry.value.classPrefixAbsolute, dslNameAndWheretoDelegateEntry.value.classPrefixAddendum,
                dslNameAndWheretoDelegateEntry.value.classPostfixAbsolute, dslNameAndWheretoDelegateEntry.value.classPostfixAddendum,
                dslNameAndWheretoDelegateEntry.value.basePackageAbsolute,dslNameAndWheretoDelegateEntry.value.basePackageAddendum,
                dslNameAndWheretoDelegateEntry.value.packageNameAbsolute,dslNameAndWheretoDelegateEntry.value.packageNameAddendum,
            ))
        }
        for (dslNameAndWheretoDelegateEntry in nameAndWheretoWithSubelements.dtoNameAndWheretos) {
            gatheredNameAndWheretos.createFromElementForSubelement(DslRef.dto(dslNameAndWheretoDelegateEntry.value.simpleName , selfDslRef), SharedNameAndWhereto(
                dslNameAndWheretoDelegateEntry.value.simpleName,
                dslNameAndWheretoDelegateEntry.value.selfDslRef,
                dslNameAndWheretoDelegateEntry.value.strategyClassName, dslNameAndWheretoDelegateEntry.value.strategyTableName,
                dslNameAndWheretoDelegateEntry.value.baseDirPathAbsolute, dslNameAndWheretoDelegateEntry.value.baseDirAddendum,
                dslNameAndWheretoDelegateEntry.value.pathAbsolute , dslNameAndWheretoDelegateEntry.value.pathAddendum,
                dslNameAndWheretoDelegateEntry.value.classPrefixAbsolute, dslNameAndWheretoDelegateEntry.value.classPrefixAddendum,
                dslNameAndWheretoDelegateEntry.value.classPostfixAbsolute, dslNameAndWheretoDelegateEntry.value.classPostfixAddendum,
                dslNameAndWheretoDelegateEntry.value.basePackageAbsolute,dslNameAndWheretoDelegateEntry.value.basePackageAddendum,
                dslNameAndWheretoDelegateEntry.value.packageNameAbsolute,dslNameAndWheretoDelegateEntry.value.packageNameAddendum,
            ))
        }
        for (dslNameAndWheretoDelegateEntry in nameAndWheretoWithSubelements.tableNameAndWheretos) {
            gatheredNameAndWheretos.createFromElementForSubelement(DslRef.table(dslNameAndWheretoDelegateEntry.value.simpleName , selfDslRef), SharedNameAndWhereto(
                dslNameAndWheretoDelegateEntry.value.simpleName,
                dslNameAndWheretoDelegateEntry.value.selfDslRef,
                dslNameAndWheretoDelegateEntry.value.strategyClassName, dslNameAndWheretoDelegateEntry.value.strategyTableName,
                dslNameAndWheretoDelegateEntry.value.baseDirPathAbsolute, dslNameAndWheretoDelegateEntry.value.baseDirAddendum,
                dslNameAndWheretoDelegateEntry.value.pathAbsolute , dslNameAndWheretoDelegateEntry.value.pathAddendum,
                dslNameAndWheretoDelegateEntry.value.classPrefixAbsolute, dslNameAndWheretoDelegateEntry.value.classPrefixAddendum,
                dslNameAndWheretoDelegateEntry.value.classPostfixAbsolute, dslNameAndWheretoDelegateEntry.value.classPostfixAddendum,
                dslNameAndWheretoDelegateEntry.value.basePackageAbsolute,dslNameAndWheretoDelegateEntry.value.basePackageAddendum,
                dslNameAndWheretoDelegateEntry.value.packageNameAbsolute,dslNameAndWheretoDelegateEntry.value.packageNameAddendum,
            ))
        }
    }
}

// === Api interfaces define pure props/directFuns and "union/intersections used in DSL Lambdas and/or IDslApi delegation ===
@ChassisDslMarker
interface IDslApiModelAndModelSubelementsCommon
// interfaces implemented by Model And Elements
    :   IDslApiGatherPropertiesModelAndModelSubelementsCommon,
        IDslApiClassModifiers,
        IDslApiGatherPropertiesProp,
        IDslApiPropFuns,
        IDslApiClassModsDelegate,
        IDslApiExtendsDelegate,
        IDslApiShowcaseDelegate
{
    var kind: DslClassObjectOrInterface
}
@ChassisDslMarker
interface IDslApiModelOnlyCommon // TODO remove trailing Common postfix
    :   IDslApiNameAndWheretoWithSubelements

@ChassisDslMarker
interface IDslApiSubelementsOnlyCommon
    :   IDslApiNameAndWheretoOnly,
        IDslApiGatherPropertiesElementsOnlyCommon
@ChassisDslMarker
interface IDslApiDto
    :   IDslApiModelAndModelSubelementsCommon,
        IDslApiSubelementsOnlyCommon
@ChassisDslMarker
interface IDslApiTable
    :   IDslApiModelAndModelSubelementsCommon,
        IDslApiSubelementsOnlyCommon

// === Impl Interfaces (extend IDslApi's plus methods and props that should not be visible from the DSL ===

context(DslCtxWrapper)
@ChassisDslMarker
class DslDto(
    simpleName: String,
    dtoRef: DslRef.dto,
    val classModifiersImpl: DslClassModifiersImpl             = DslClassModifiersImpl(),
    val propsImpl: DslPropsDelegate                           = with (dslCtxWrapperFake) { dslCtx.ctxObjOrCreate(DslRef.properties(simpleName, dtoRef)) },
    val nameAndWheretoWithoutModelSubelementsImpl: DslNameAndWheretoOnlyDelegateImpl = with (dslCtxWrapperFake) { dslCtx.ctxObjOrCreate(DslRef.nameAndWhereto(simpleName, dtoRef)) },
    val gatherPropertiesImpl: DslGatherPropertiesDelegateImpl = with (dslCtxWrapperFake) { dslCtx.ctxObjOrCreate(DslRef.propertiesOf(simpleName, dtoRef)) },
    val classModsImpl: DslClassModsDelegateImpl               = with (dslCtxWrapperFake) { dslCtx.ctxObjOrCreate(DslRef.classMods(simpleName, dtoRef)) },
    val extendsImpl: DslExtendsDelegateImpl                   = with (dslCtxWrapperFake) { dslCtx.ctxObjOrCreate(DslRef.extends(simpleName, dtoRef)) },
    val showcaseImpl: DslShowcaseDelegateImpl                 = with (dslCtxWrapperFake) { dslCtx.ctxObjOrCreate(DslRef.showcase(simpleName, dtoRef)) },
    //val propsImpl: DslPropsDelegate                           = dslCtx.ctxObjOrCreate(DslRef.properties(simpleNameOfParentDslBlock, dtoRef)),
    //val nameAndWheretoWithoutModelSubelementsImpl: DslNameAndWheretoOnlyDelegateImpl = dslCtx.ctxObjOrCreate(DslRef.nameAndWhereto(simpleNameOfParentDslBlock, dtoRef)),
    //val gatherPropertiesImpl: DslGatherPropertiesDelegateImpl = dslCtx.ctxObjOrCreate(DslRef.propertiesOf(simpleNameOfParentDslBlock, dtoRef)),
    //val classModsImpl: DslClassModsDelegateImpl               = dslCtx.ctxObjOrCreate(DslRef.classMods(simpleNameOfParentDslBlock, dtoRef)),
    //val extendsImpl: DslExtendsDelegateImpl                   = dslCtx.ctxObjOrCreate(DslRef.extends(simpleNameOfParentDslBlock, dtoRef)),
    //val showcaseImpl: DslShowcaseDelegateImpl                 = dslCtx.ctxObjOrCreate(DslRef.showcase(simpleNameOfParentDslBlock, dtoRef))
)
    : AModelSubelement(simpleName, dtoRef),
    IDslApiDto,
    IDslApiModelAndModelSubelementsCommon,
    IDslApiClassObjectOrInterface,

    IDslImplClassModifiers by classModifiersImpl,
    IDslApiPropFuns by propsImpl,
    IDslImplClassModsDelegate by classModsImpl,
    IDslImplExtendsDelegate by extendsImpl,
    IDslImplShowcaseDelegate by showcaseImpl,

    IDslApiNameAndWheretoOnly by nameAndWheretoWithoutModelSubelementsImpl,
    IDslApiGatherPropertiesModelAndModelSubelementsCommon by gatherPropertiesImpl,
    IDslApiGatherPropertiesElementsOnlyCommon             by gatherPropertiesImpl

{
    val log = LoggerFactory.getLogger(javaClass)

    //override val modelElement = DslRef.model.MODELELEMENT.DTO

    init {
        val workaround = dslCtxWrapperFake
        this@DslCtxWrapper.dslCtx.addToCtx(propsImpl)
        this@DslCtxWrapper.dslCtx.addToCtx(nameAndWheretoWithoutModelSubelementsImpl)
        this@DslCtxWrapper.dslCtx.addToCtx(gatherPropertiesImpl)
        this@DslCtxWrapper.dslCtx.addToCtx(classModsImpl)
        this@DslCtxWrapper.dslCtx.addToCtx(extendsImpl)
        this@DslCtxWrapper.dslCtx.addToCtx(showcaseImpl)
    }
    override var kind: DslClassObjectOrInterface = DslClassObjectOrInterface.CLASS


    fun finish(dslCtx: DslCtx) {
        val dtoModel = EitherModel.DtoModel(selfDslRef as DslRef.dto)
        dslCtx.genCtx.genModels[selfDslRef] = dtoModel
        when (kind) {
            DslClassObjectOrInterface.CLASS -> dtoModel.kind = TypeSpec.Kind.CLASS
            DslClassObjectOrInterface.OBJECT -> dtoModel.kind = TypeSpec.Kind.OBJECT
            DslClassObjectOrInterface.INTERFACE -> dtoModel.kind = TypeSpec.Kind.INTERFACE
            DslClassObjectOrInterface.UNDEFINED -> {
                when ((dslCtx.ctxObj<DslModel>(selfDslRef.parentRef)).kind) {
                    DslClassObjectOrInterface.CLASS -> dtoModel.kind = TypeSpec.Kind.CLASS
                    DslClassObjectOrInterface.OBJECT -> dtoModel.kind = TypeSpec.Kind.OBJECT
                    DslClassObjectOrInterface.INTERFACE -> dtoModel.kind = TypeSpec.Kind.INTERFACE
                    DslClassObjectOrInterface.UNDEFINED -> { throw DslException("ref: $selfDslRef has undefined kind, neither set in ModelSubelement, nor in parent model() { }")
                    }
                }
            }
        }
        val gatheredNameAndWheretos: SharedGatheredNameAndWheretos = dslCtx.gatheredNameAndWheretos(selfDslRef.parentRef as DslRef.IElementLevel)
        for (dslNameAndWhereto in nameAndWheretoWithoutModelSubelementsImpl.nameAndWheretos.values) {
            gatheredNameAndWheretos.createFor(selfDslRef, SharedNameAndWhereto(
                dslNameAndWhereto.simpleName,
                selfDslRef,
                dslNameAndWhereto.strategyClassName, dslNameAndWhereto.strategyTableName,
                dslNameAndWhereto.baseDirPathAbsolute, dslNameAndWhereto.baseDirAddendum,
                dslNameAndWhereto.pathAbsolute , dslNameAndWhereto.pathAddendum,
                dslNameAndWhereto.classPrefixAbsolute, dslNameAndWhereto.classPrefixAddendum,
                dslNameAndWhereto.classPostfixAbsolute, dslNameAndWhereto.classPostfixAddendum,
                dslNameAndWhereto.basePackageAbsolute, dslNameAndWhereto.basePackageAddendum,
                dslNameAndWhereto.packageNameAbsolute, dslNameAndWhereto.packageNameAddendum,
            ))
        }

        val modelClassName = StrategyNameAndWhereto.resolve(StrategyNameAndWhereto.STRATEGY.SPECIAL_WINS, selfDslRef, gatheredNameAndWheretos)
        val modelGatherProperties = StrategyGatherProperties.resolve(StrategyGatherProperties.STRATEGY.SPECIAL_WINS, this.modelSubelementRef)
//        // TODO XXX set "own" properties (and the ones of model { } into dtoModel
//        dtoModel.gatheredFromDslRefs.addAll(modelGatherProperties)
//
//        for (gatherFrom in modelGatherProperties) {
//            val otherDslModelOrModelSubelement: ADslClass = dslCtx.ctxObj(gatherFrom.modelOrModelSubelementRef)
//            when (otherDslModelOrModelSubelement) {
//                is DslModel -> {
//                    for (modelProp in otherDslModelOrModelSubelement.propsImpl.theProps.values) {
//                        val prop = Property(
//                            modelProp.name,
//                            modelProp.propRef,
//                            modelProp.mutable,
//                            modelProp.tags,
//                        )
//                        dtoModel.propertys[prop.name] = prop
//                    }
//                }
//            }
//        }
        // TODO XXX really gatherProperties and set them into dtoModel

        // TODO XXX Continue here
    }
}

context(DslCtxWrapper)
@ChassisDslMarker
class DslTable(
    simpleName: String,
    tableRef: DslRef.table,
    val classModifiersImpl: DslClassModifiersImpl             = DslClassModifiersImpl(),
    val propsImpl: DslPropsDelegate                           = with (dslCtxWrapperFake) { dslCtx.ctxObjOrCreate(DslRef.properties(simpleName, tableRef)) },
    val nameAndWheretoWithoutModelSubelementsImpl: DslNameAndWheretoOnlyDelegateImpl = with (dslCtxWrapperFake) { dslCtx.ctxObjOrCreate(DslRef.nameAndWhereto(simpleName, tableRef)) },
    val gatherPropertiesImpl: DslGatherPropertiesDelegateImpl = with (dslCtxWrapperFake) { dslCtx.ctxObjOrCreate(DslRef.propertiesOf(simpleName, tableRef)) },
    val classModsImpl: DslClassModsDelegateImpl               = with (dslCtxWrapperFake) { dslCtx.ctxObjOrCreate(DslRef.classMods(simpleName, tableRef)) },
    val extendsImpl: DslExtendsDelegateImpl                   = with (dslCtxWrapperFake) { dslCtx.ctxObjOrCreate(DslRef.extends(simpleName, tableRef)) },
    val showcaseImpl: DslShowcaseDelegateImpl                 = with (dslCtxWrapperFake) { dslCtx.ctxObjOrCreate(DslRef.showcase(simpleName, tableRef)) },
//    val propsImpl: DslPropsDelegate                           = dslCtx.ctxObjOrCreate(DslRef.properties(simpleNameOfParentDslBlock, tableRef)),
//    val nameAndWheretoWithoutModelSubelementsImpl: DslNameAndWheretoOnlyDelegateImpl = dslCtx.ctxObjOrCreate(DslRef.nameAndWhereto(simpleNameOfParentDslBlock, tableRef)),
//    val gatherPropertiesImpl: DslGatherPropertiesDelegateImpl = dslCtx.ctxObjOrCreate(DslRef.propertiesOf(simpleNameOfParentDslBlock, tableRef)),
//    val classModsImpl: DslClassModsDelegateImpl               = dslCtx.ctxObjOrCreate(DslRef.classMods(simpleNameOfParentDslBlock, tableRef)),
//    val extendsImpl: DslExtendsDelegateImpl                   = dslCtx.ctxObjOrCreate(DslRef.extends(simpleNameOfParentDslBlock, tableRef)),
//    val showcaseImpl: DslShowcaseDelegateImpl                 = dslCtx.ctxObjOrCreate(DslRef.showcase(simpleNameOfParentDslBlock, tableRef)),
)
    : AModelSubelement(simpleName, tableRef),
    IDslApiTable,
    IDslApiModelAndModelSubelementsCommon,
    IDslApiClassObjectOrInterface,

    IDslImplClassModifiers by classModifiersImpl,
    IDslApiPropFuns by propsImpl,
    IDslImplClassModsDelegate by classModsImpl,
    IDslImplExtendsDelegate by extendsImpl,
    IDslImplShowcaseDelegate by showcaseImpl,

    IDslApiNameAndWheretoOnly by nameAndWheretoWithoutModelSubelementsImpl,
    IDslApiGatherPropertiesModelAndModelSubelementsCommon by gatherPropertiesImpl,
    IDslApiGatherPropertiesElementsOnlyCommon             by gatherPropertiesImpl

{
    val log = LoggerFactory.getLogger(javaClass)

    //override val modelElement = DslRef.model.MODELELEMENT.TABLE

    init {
        val workaround = dslCtxWrapperFake
        this@DslCtxWrapper.dslCtx.addToCtx(propsImpl)
        this@DslCtxWrapper.dslCtx.addToCtx(nameAndWheretoWithoutModelSubelementsImpl)
        this@DslCtxWrapper.dslCtx.addToCtx(gatherPropertiesImpl)
        this@DslCtxWrapper.dslCtx.addToCtx(classModsImpl)
        this@DslCtxWrapper.dslCtx.addToCtx(extendsImpl)
        this@DslCtxWrapper.dslCtx.addToCtx(showcaseImpl)
    }

    override var kind: DslClassObjectOrInterface = DslClassObjectOrInterface.CLASS


    fun finish(dslCtx: DslCtx) {
        println("DslTable.finish() NOT IMPLEMENTED YET") // TODO("Not yet implemented")
    }
}

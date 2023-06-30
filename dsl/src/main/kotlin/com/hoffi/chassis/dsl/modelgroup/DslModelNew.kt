package com.hoffi.chassis.dsl.modelgroup

import com.hoffi.chassis.chassismodel.C
import com.hoffi.chassis.chassismodel.dsl.DslException
import com.hoffi.chassis.dsl.*
import com.hoffi.chassis.dsl.internal.*
import com.hoffi.chassis.dsl.whereto.*
import com.hoffi.chassis.shared.dsl.DslRef
import com.hoffi.chassis.shared.dsl.IDslRef
import com.hoffi.chassis.shared.parsedata.*
import com.hoffi.chassis.shared.parsedata.nameandwhereto.SharedGatheredNameAndWheretos
import com.hoffi.chassis.shared.parsedata.nameandwhereto.SharedNameAndWhereto
import com.hoffi.chassis.shared.parsedata.nameandwhereto.StrategyNameAndWhereto
import com.hoffi.chassis.shared.shared.Extends
import com.hoffi.chassis.shared.shared.GatherPropertys
import com.squareup.kotlinpoet.KModifier
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

    protected fun thePropertiesOf(
        subelementPropsImpl: DslPropsDelegate,
        elementPropsImpl: DslPropsDelegate
    ): MutableMap<String, Property> {
        val mapOfPropertys: MutableMap<String, Property> = mutableMapOf()
        // Subelement (Dto, Table, ...) properties
        for (dslProp in subelementPropsImpl.theProps.values) {
            val property: Property = DslPropToGenProp.createFrom(dslProp)
            mapOfPropertys[property.name] = property
        }
        // model (Element) properties
        for (dslProp in elementPropsImpl.theProps.values) {
            val property: Property = DslPropToGenProp.createFrom(dslProp)
            mapOfPropertys[property.name] = property
        }
        return mapOfPropertys
    }
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
globalDslCtx = dslCtx // TODO remove workaround
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
globalDslCtx = dslCtx // TODO remove workaround
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

    }
    fun prepareNameAndWheretos(dslCtx: DslCtx) {
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
            gatheredNameAndWheretos.createFromElementForSubelement(DslRef.model.MODELELEMENT.DTO, SharedNameAndWhereto(
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
            gatheredNameAndWheretos.createFromElementForSubelement(DslRef.model.MODELELEMENT.TABLE, SharedNameAndWhereto(
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

    companion object {
        val NULL: DslModel = with (dslCtxWrapperFake)
        {
            DslModel(
                C.NULLSTRING,
                DslRef.model(C.NULLSTRING, IDslRef.NULL),
                DslClassModifiersImpl(),
                DslPropsDelegate(C.NULLSTRING, parentRef = IDslRef.NULL),
                DslNameAndWheretoWithSubelementsDelegateImpl(C.NULLSTRING, IDslRef.NULL),
                DslGatherPropertiesDelegateImpl(C.NULLSTRING, IDslRef.NULL),
                DslClassModsDelegateImpl(C.NULLSTRING, IDslRef.NULL),
                DslExtendsDelegateImpl(C.NULLSTRING, IDslRef.NULL),
                DslShowcaseDelegateImpl(C.NULLSTRING, IDslRef.NULL)
            )
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

    init {
        this@DslCtxWrapper.dslCtx.addToCtx(propsImpl)
        this@DslCtxWrapper.dslCtx.addToCtx(nameAndWheretoWithoutModelSubelementsImpl)
        this@DslCtxWrapper.dslCtx.addToCtx(gatherPropertiesImpl)
        this@DslCtxWrapper.dslCtx.addToCtx(classModsImpl)
        this@DslCtxWrapper.dslCtx.addToCtx(extendsImpl)
        this@DslCtxWrapper.dslCtx.addToCtx(showcaseImpl)
    }
    override var kind: DslClassObjectOrInterface = DslClassObjectOrInterface.UNDEFINED


    fun finish(dslCtx: DslCtx) {
        val sharedGatheredNameAndWheretos: SharedGatheredNameAndWheretos = dslCtx.gatheredNameAndWheretos(selfDslRef.parentRef as DslRef.IElementLevel)
        for (dslNameAndWhereto in nameAndWheretoWithoutModelSubelementsImpl.nameAndWheretos.values) {
            sharedGatheredNameAndWheretos.createForSubelement(selfDslRef, SharedNameAndWhereto(
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

        val modelClassName = StrategyNameAndWhereto.resolve(StrategyNameAndWhereto.STRATEGY.SPECIAL_WINS_ON_ABSOLUTE_CONCAT_ADDENDUMS, selfDslRef, sharedGatheredNameAndWheretos)
        val dtoModel = GenModel.DtoModel(selfDslRef as DslRef.dto, modelClassName)
        dslCtx.genCtx.allGenModels[selfDslRef] = dtoModel
        val dslModel: DslModel = dslCtx.ctxObj(selfDslRef.parentRef)

        when (kind) {
            DslClassObjectOrInterface.CLASS -> dtoModel.kind = TypeSpec.Kind.CLASS
            DslClassObjectOrInterface.OBJECT -> dtoModel.kind = TypeSpec.Kind.OBJECT
            DslClassObjectOrInterface.INTERFACE -> dtoModel.kind = TypeSpec.Kind.INTERFACE
            DslClassObjectOrInterface.UNDEFINED -> {
                when (dslModel.kind) {
                    DslClassObjectOrInterface.CLASS -> dtoModel.kind = TypeSpec.Kind.CLASS
                    DslClassObjectOrInterface.OBJECT -> dtoModel.kind = TypeSpec.Kind.OBJECT
                    DslClassObjectOrInterface.INTERFACE -> dtoModel.kind = TypeSpec.Kind.INTERFACE
                    DslClassObjectOrInterface.UNDEFINED -> { throw DslException("ref: $selfDslRef has undefined kind, neither set in ModelSubelement, nor in parent model() { }")
                    }
                }
            }
        }

        val sharedGatheredClassModifiers = dslCtx.gatheredClassModifiers(dslModel.selfDslRef)
        if (sharedGatheredClassModifiers.allFromSubelements[selfDslRef]?.containsKey(selfDslRef.simpleName) ?: false) throw DslException("There is already a set of ClassModifiers in dslCtx for '${selfDslRef}")
        val setOfGatheredClassModifiers : MutableSet<KModifier> = mutableSetOf()
        sharedGatheredClassModifiers.allFromSubelements.getOrPut(selfDslRef) { mutableMapOf<String, MutableSet<KModifier>>() }.put(selfDslRef.simpleName, setOfGatheredClassModifiers)
        setOfGatheredClassModifiers.addAll(classModifiersImpl.theClassModifiers)
        val modelGatherClassModifiers: Set<KModifier> = StrategyGatherClassModifiers.resolve(StrategyGatherClassModifiers.STRATEGY.UNION, selfDslRef, sharedGatheredClassModifiers)
        dtoModel.classModifiers.addAll(modelGatherClassModifiers)

        val sharedGatheredExtends = dslCtx.gatheredExtends(dslModel.selfDslRef)
        if (sharedGatheredExtends.allFromSubelements[selfDslRef]?.containsKey(simpleName) ?: false) throw DslException("There is already a map.entry of Extends for simpleName '${simpleName}' in dslCtx for '${selfDslRef}'")
        //val setOfGatheredExtends: MutableSet<Extends> = mutableSetOf()
        sharedGatheredExtends.allFromSubelements.getOrPut(selfDslRef) { mutableMapOf() }.putAll(extendsImpl.theExtendBlocks.values.map { it.simpleName to it.extends })
        val modelGatherExtends: MutableMap<String, Extends> = StrategyGatherExtends.resolve(StrategyGatherExtends.STRATEGY.UNION, selfDslRef, sharedGatheredExtends)
        dtoModel.extends.putAll(modelGatherExtends)

        val sharedGatheredGatherPropertys: SharedGatheredGatherPropertys = dslCtx.gatheredGatherPropertys(selfDslRef.parentRef as DslRef.IElementLevel)
        if (sharedGatheredGatherPropertys.allFromSubelements[selfDslRef]?.containsKey(simpleName) ?: false) throw DslException("There is already a set of GatherPropertys in dslCtx for '${selfDslRef}")
        val setOfGatheredPropertysOfThis: MutableSet<GatherPropertys> = mutableSetOf()
        sharedGatheredGatherPropertys.allFromSubelements.getOrPut(selfDslRef) { mutableMapOf() }.put(selfDslRef.simpleName, setOfGatheredPropertysOfThis)
        setOfGatheredPropertysOfThis.addAll(gatherPropertiesImpl.theGatherPropertys)
        val modelGatherProperties: Set<GatherPropertys> = StrategyGatherProperties.resolve(StrategyGatherProperties.STRATEGY.UNION, selfDslRef, sharedGatheredGatherPropertys)
        dtoModel.gatheredFromDslRefs.addAll(modelGatherProperties)

        // the gathered properties will be fetched into the model                      in Modelgroup's PASS_GENMODELSCREATED fun gatherInheritedPropertys()
        // the ModelClassName of GenModel's will be set in Modelgroup's PASS_GENMODELSCREATED fun setModelClassNameOfReffedModelProperties()
        val mapOfPropertys = thePropertiesOf(this.propsImpl, dslModel.propsImpl)
        dtoModel.propertys.putAll(mapOfPropertys)

        // TODO XXX Continue here
    }

    companion object {
        val NULL: DslDto = with (dslCtxWrapperFake)
        {
            DslDto(
                C.NULLSTRING,
                DslRef.dto(C.NULLSTRING, IDslRef.NULL),
                DslClassModifiersImpl(),
                DslPropsDelegate(C.NULLSTRING, parentRef = IDslRef.NULL),
                DslNameAndWheretoOnlyDelegateImpl(C.NULLSTRING, IDslRef.NULL),
                DslGatherPropertiesDelegateImpl(C.NULLSTRING, IDslRef.NULL),
                DslClassModsDelegateImpl(C.NULLSTRING, IDslRef.NULL),
                DslExtendsDelegateImpl(C.NULLSTRING, IDslRef.NULL),
                DslShowcaseDelegateImpl(C.NULLSTRING, IDslRef.NULL)
            )
        }
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

    init {
        this@DslCtxWrapper.dslCtx.addToCtx(propsImpl)
        this@DslCtxWrapper.dslCtx.addToCtx(nameAndWheretoWithoutModelSubelementsImpl)
        this@DslCtxWrapper.dslCtx.addToCtx(gatherPropertiesImpl)
        this@DslCtxWrapper.dslCtx.addToCtx(classModsImpl)
        this@DslCtxWrapper.dslCtx.addToCtx(extendsImpl)
        this@DslCtxWrapper.dslCtx.addToCtx(showcaseImpl)
    }

    override var kind: DslClassObjectOrInterface = DslClassObjectOrInterface.OBJECT


    fun finish(dslCtx: DslCtx) {
        val sharedGatheredNameAndWheretos: SharedGatheredNameAndWheretos = dslCtx.gatheredNameAndWheretos(selfDslRef.parentRef as DslRef.IElementLevel)
        for (dslNameAndWhereto in nameAndWheretoWithoutModelSubelementsImpl.nameAndWheretos.values) {
            sharedGatheredNameAndWheretos.createForSubelement(selfDslRef, SharedNameAndWhereto(
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

        val modelClassName = StrategyNameAndWhereto.resolve(StrategyNameAndWhereto.STRATEGY.SPECIAL_WINS_ON_ABSOLUTE_CONCAT_ADDENDUMS, selfDslRef, sharedGatheredNameAndWheretos)
        val tableModel = GenModel.TableModel(selfDslRef as DslRef.table, modelClassName)
        dslCtx.genCtx.allGenModels[selfDslRef] = tableModel
        val dslModel: DslModel = dslCtx.ctxObj(selfDslRef.parentRef)

        when (kind) {
            DslClassObjectOrInterface.CLASS -> tableModel.kind = TypeSpec.Kind.CLASS
            DslClassObjectOrInterface.OBJECT -> tableModel.kind = TypeSpec.Kind.OBJECT
            DslClassObjectOrInterface.INTERFACE -> tableModel.kind = TypeSpec.Kind.INTERFACE
            DslClassObjectOrInterface.UNDEFINED -> {
                when (dslModel.kind) {
                    DslClassObjectOrInterface.CLASS -> tableModel.kind = TypeSpec.Kind.CLASS
                    DslClassObjectOrInterface.OBJECT -> tableModel.kind = TypeSpec.Kind.OBJECT
                    DslClassObjectOrInterface.INTERFACE -> tableModel.kind = TypeSpec.Kind.INTERFACE
                    DslClassObjectOrInterface.UNDEFINED -> { throw DslException("ref: $selfDslRef has undefined kind, neither set in ModelSubelement, nor in parent model() { }")
                    }
                }
            }
        }

        val sharedGatheredClassModifiers = dslCtx.gatheredClassModifiers(dslModel.selfDslRef)
        if (sharedGatheredClassModifiers.allFromSubelements[selfDslRef]?.containsKey(selfDslRef.simpleName) ?: false) throw DslException("There is already a set of ClassModifiers in dslCtx for '${selfDslRef}")
        val setOfGatheredClassModifiers : MutableSet<KModifier> = mutableSetOf()
        sharedGatheredClassModifiers.allFromSubelements.getOrPut(selfDslRef) { mutableMapOf() }.put(selfDslRef.simpleName, setOfGatheredClassModifiers)
        setOfGatheredClassModifiers.addAll(classModifiersImpl.theClassModifiers)
        val modelGatherClassModifiers: Set<KModifier> = StrategyGatherClassModifiers.resolve(StrategyGatherClassModifiers.STRATEGY.UNION, selfDslRef, sharedGatheredClassModifiers)
        tableModel.classModifiers.addAll(modelGatherClassModifiers)

        val sharedGatheredExtends = dslCtx.gatheredExtends(dslModel.selfDslRef)
        if (sharedGatheredExtends.allFromSubelements[selfDslRef]?.containsKey(simpleName) ?: false) throw DslException("There is already a map.entry of Extends for simpleName '${simpleName}' in dslCtx for '${selfDslRef}'")
        //val setOfGatheredExtends: MutableSet<Extends> = mutableSetOf()
        sharedGatheredExtends.allFromSubelements.getOrPut(selfDslRef) { mutableMapOf() }.putAll(extendsImpl.theExtendBlocks.values.map { it.simpleName to it.extends })
        val modelGatherExtends: MutableMap<String, Extends> = StrategyGatherExtends.resolve(StrategyGatherExtends.STRATEGY.UNION, selfDslRef, sharedGatheredExtends)
        tableModel.extends.putAll(modelGatherExtends)

        val sharedGatheredGatherPropertys: SharedGatheredGatherPropertys = dslCtx.gatheredGatherPropertys(selfDslRef.parentRef as DslRef.IElementLevel)
        if (sharedGatheredGatherPropertys.allFromSubelements[selfDslRef]?.containsKey(simpleName) ?: false) throw DslException("There is already a set of GatherPropertys in dslCtx for '${selfDslRef}")
        val setOfGatheredPropertysOfThis: MutableSet<GatherPropertys> = mutableSetOf()
        sharedGatheredGatherPropertys.allFromSubelements.getOrPut(selfDslRef) { mutableMapOf() }.put(selfDslRef.simpleName, setOfGatheredPropertysOfThis)
        setOfGatheredPropertysOfThis.addAll(gatherPropertiesImpl.theGatherPropertys)
        val modelGatherProperties: Set<GatherPropertys> = StrategyGatherProperties.resolve(StrategyGatherProperties.STRATEGY.UNION, selfDslRef, sharedGatheredGatherPropertys)
        tableModel.gatheredFromDslRefs.addAll(modelGatherProperties)

        // the gathered properties will be fetched into the model in Modelgroup's PASS_GENMODELSCREATED fun gatherInheritedPropertys()
        val mapOfPropertys = thePropertiesOf(this.propsImpl, dslModel.propsImpl)
        tableModel.propertys.putAll(mapOfPropertys)

       // TODO XXX Continue here
    }

    companion object {
        val NULL: DslTable = with (dslCtxWrapperFake) {
            DslTable(
                C.NULLSTRING,
                DslRef.table(C.NULLSTRING, IDslRef.NULL),
                DslClassModifiersImpl(),
                DslPropsDelegate(C.NULLSTRING, parentRef = IDslRef.NULL),
                DslNameAndWheretoOnlyDelegateImpl(C.NULLSTRING, IDslRef.NULL),
                DslGatherPropertiesDelegateImpl(C.NULLSTRING, IDslRef.NULL),
                DslClassModsDelegateImpl(C.NULLSTRING, IDslRef.NULL),
                DslExtendsDelegateImpl(C.NULLSTRING, IDslRef.NULL),
                DslShowcaseDelegateImpl(C.NULLSTRING, IDslRef.NULL)
            )
        }
    }
}

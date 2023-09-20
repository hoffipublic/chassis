package com.hoffi.chassis.dsl.modelgroup

import com.hoffi.chassis.chassismodel.C
import com.hoffi.chassis.chassismodel.dsl.DslException
import com.hoffi.chassis.dsl.*
import com.hoffi.chassis.dsl.internal.*
import com.hoffi.chassis.dsl.modelgroup.crud.DslCrudDelegateImpl
import com.hoffi.chassis.dsl.modelgroup.crud.IDslApiCrudDelegate
import com.hoffi.chassis.dsl.whereto.*
import com.hoffi.chassis.shared.EitherTypOrModelOrPoetType
import com.hoffi.chassis.shared.dsl.DslRef
import com.hoffi.chassis.shared.dsl.IDslRef
import com.hoffi.chassis.shared.parsedata.*
import com.hoffi.chassis.shared.parsedata.nameandwhereto.CollectedNameAndWheretos
import com.hoffi.chassis.shared.parsedata.nameandwhereto.SharedNameAndWhereto
import com.hoffi.chassis.shared.parsedata.nameandwhereto.StrategyNameAndWhereto
import com.hoffi.chassis.shared.shared.*
import com.hoffi.chassis.shared.shared.reffing.MODELREFENUM
import com.squareup.kotlinpoet.KModifier
import com.squareup.kotlinpoet.TypeSpec
import org.slf4j.LoggerFactory

@ChassisDslMarker
interface IDslApiKindClassObjectOrInterface {
    var kind: DslClassObjectOrInterface
}
@ChassisDslMarker
interface IDslApiConstructorVisibility {
    enum class VISIBILITY { PUBLIC, PROTECTED, UNSET}
    var constructorVisibility: VISIBILITY
}

context(DslCtxWrapper)
abstract class AProperModelSubelement(
    val simpleName: String,
    val modelSubelementRef: DslRef.IModelSubelement,
    val classModifiersImpl: DslClassModifiersImpl,
    val propsImpl: DslPropsDelegate,
    val nameAndWheretoWithoutModelSubelementsImpl: DslNameAndWheretoOnlyDelegateImpl,
    val gatherPropertiesImpl: DslGatherPropertiesDelegateImpl,
    val classModsImpl: DslClassModsDelegateImpl,
    val extendsImpl: DslExtendsDelegateImpl,
    val showcaseImpl: DslShowcaseDelegateImpl? = null
)
    : ADslClass()
{
    override val selfDslRef = modelSubelementRef

    var tableFor: MODELREFENUM? = null //for a real persistent(!) model-subelement it has to be some nonpersistent(!) proper subelement

    fun directPropertiesOf(subelementPropsImpl: DslPropsDelegate, elementPropsImpl: DslPropsDelegate): Map<String, Property> {
        return directDslPropertiesOf(subelementPropsImpl, elementPropsImpl).mapValues { it.value.toProperty(modelSubelementRef) }
    }
    fun directDslPropertiesOf(subelementPropsImpl: DslPropsDelegate, elementPropsImpl: DslPropsDelegate): MutableMap<String, DslModelProp> {
        val mapOfDslProps: MutableMap<String, DslModelProp> = mutableMapOf()
        // model (Element) properties
        mapOfDslProps.putAll( elementPropsImpl.theProps)
        // Subelement (Dto, Table, ...) properties
        mapOfDslProps.putAll(subelementPropsImpl.theProps)
        return mapOfDslProps
    }

    protected fun finishCollectedNameAndWheretos(nameAndWheretoWithoutModelSubelementsImpl: DslNameAndWheretoOnlyDelegateImpl): CollectedNameAndWheretos {
        val collectedNameAndWheretos: CollectedNameAndWheretos =
            dslCtx.cloneOfCollectedBasemodelNameAndWheretos(parentDslRef as DslRef.IElementLevel)
        for (dslNameAndWhereto in nameAndWheretoWithoutModelSubelementsImpl.nameAndWheretos.values) {
            collectedNameAndWheretos.createForSubelement(
                selfDslRef, SharedNameAndWhereto(
                    dslNameAndWhereto.simpleName,
                    selfDslRef,
                    dslNameAndWhereto.strategyClassName, dslNameAndWhereto.strategyTableName,
                    dslNameAndWhereto.baseDirPathAbsolute, dslNameAndWhereto.baseDirAddendum,
                    dslNameAndWhereto.pathAbsolute, dslNameAndWhereto.pathAddendum,
                    dslNameAndWhereto.classPrefixAbsolute, dslNameAndWhereto.classPrefixAddendum,
                    dslNameAndWhereto.classPostfixAbsolute, dslNameAndWhereto.classPostfixAddendum,
                    dslNameAndWhereto.basePackageAbsolute, dslNameAndWhereto.basePackageAddendum,
                    dslNameAndWhereto.packageNameAbsolute, dslNameAndWhereto.packageNameAddendum,
                )
            )
        }
        return collectedNameAndWheretos
    }

    protected fun finishProperModelsModelClassData(
        dslModel: DslModel,
        modelClassDataFromDsl: ModelClassDataFromDsl,
        classModifiersImpl: DslClassModifiersImpl,
        extendsImpl: DslExtendsDelegateImpl,
        gatherPropertiesImpl: DslGatherPropertiesDelegateImpl,
        propsImpl: DslPropsDelegate
    ) {
        val collectedClassModifiers: CollectedClassModifiers = dslCtx.cloneOfCollectedBasemodelClassModifiers(dslModel.selfDslRef)
        if (collectedClassModifiers.allFromSubelements[selfDslRef]?.containsKey(selfDslRef.simpleName) ?: false) throw DslException("There is already a set of ClassModifiers in dslCtx for '${selfDslRef}")
        val setOfGatheredClassModifiers: MutableSet<KModifier> = mutableSetOf()
        collectedClassModifiers.allFromSubelements.getOrPut(selfDslRef) { mutableMapOf() }.put(selfDslRef.simpleName, setOfGatheredClassModifiers)
        setOfGatheredClassModifiers.addAll(classModifiersImpl.theClassModifiers)
        val modelGatherClassModifiers: Set<KModifier> = StrategyGatherClassModifiers.resolve(StrategyGatherClassModifiers.STRATEGY.UNION, selfDslRef, collectedClassModifiers)
        modelClassDataFromDsl.classModifiers.addAll(modelGatherClassModifiers)

        val collectedExtends: CollectedExtends = dslCtx.cloneOfCollectedBasemodelExtends(dslModel.selfDslRef)
        if (collectedExtends.allFromSubelements[selfDslRef]?.containsKey(simpleName) ?: false) throw DslException("There is already a map.entry of Extends for simpleName '${simpleName}' in dslCtx for '${selfDslRef}'")
        //val setOfGatheredExtends: MutableSet<Extends> = mutableSetOf()
        collectedExtends.allFromSubelements.getOrPut(selfDslRef) { mutableMapOf() }.putAll(extendsImpl.theExtendBlocks.values.map { it.simpleName to it.extends })
        val modelGatherExtends: MutableMap<String, Extends> = StrategyGatherExtends.resolve(StrategyGatherExtends.STRATEGY.DEFAULT, selfDslRef, collectedExtends)
        modelClassDataFromDsl.extends.putAll(modelGatherExtends)

        val collectedGatherPropertys: CollectedGatherPropertys = dslCtx.cloneOfCollectedBasemodelGatherPropertys(parentDslRef as DslRef.IElementLevel)
        if (collectedGatherPropertys.allFromSubelements[selfDslRef]?.containsKey(simpleName) ?: false) throw DslException("There is already a set of GatherPropertys in dslCtx for '${selfDslRef}")
        val setOfGatheredPropertysOfThis: MutableSet<GatherPropertys> = mutableSetOf()
        collectedGatherPropertys.allFromSubelements.getOrPut(selfDslRef) { mutableMapOf() }.put(selfDslRef.simpleName, setOfGatheredPropertysOfThis)
        setOfGatheredPropertysOfThis.addAll(gatherPropertiesImpl.theGatherPropertys)
        val modelGatherProperties: Set<GatherPropertys> = StrategyGatherProperties.resolve(StrategyGatherProperties.STRATEGY.UNION, selfDslRef, collectedGatherPropertys)
        modelClassDataFromDsl.gatheredPropsDslModelRefs.addAll(modelGatherProperties)

        // the gathered properties will be fetched into the model                      in Modelgroup's PASS_GENMODELSCREATED fun gatherInheritedPropertys()
        // the ModelClassName of GenModel's will be set in Modelgroup's PASS_GENMODELSCREATED fun setModelClassNameOfReffedModelProperties()
        val mapOfPropertys = directPropertiesOf(propsImpl, dslModel.propsImpl)
        modelClassDataFromDsl.directProps.putAll(mapOfPropertys)
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
    IDslApiFillerDelegate,
    IDslApiShowcaseDelegate
{
    @DslBlockOn(DslDto::class)
    fun dto(simpleName: String = C.DEFAULT, dslBlock: IDslApiDto.() -> Unit)
    @DslBlockOn(DslTable::class)
    fun tableFor(tableFor: MODELREFENUM, simpleName: String = C.DEFAULT, dslBlock: IDslApiTable.() -> Unit)
    @DslBlockOn(DslDco::class)
    fun dco(simpleName: String = C.DEFAULT, dslBlock: IDslApiDco.() -> Unit)
}

context(DslCtxWrapper)
class DslModel(
    val simpleName: String,
    val modelRef: DslRef.model,
    val classModifiersImpl: DslClassModifiersImpl             = DslClassModifiersImpl(),
    val propsImpl: DslPropsDelegate                           = with (dslCtxWrapperFake) { dslCtx.ctxObjOrCreate(DslRef.properties(simpleName, modelRef)) },
    val nameAndWheretoWithSubelements: DslNameAndWheretoWithSubelementsDelegateImpl = with (dslCtxWrapperFake) { dslCtx.ctxObjOrCreate(DslRef.nameAndWhereto(simpleName, modelRef)) },
    val gatherPropertiesImpl: DslGatherPropertiesDelegateImpl = with (dslCtxWrapperFake) { dslCtx.ctxObjOrCreate(DslRef.propertiesOf(simpleName, modelRef)) },
    val classModsImpl: DslClassModsDelegateImpl               = with (dslCtxWrapperFake) { dslCtx.ctxObjOrCreate(DslRef.classMods(simpleName, modelRef)) },
    val extendsImpl: DslExtendsDelegateImpl                   = with (dslCtxWrapperFake) { dslCtx.ctxObjOrCreate(DslRef.extends(simpleName, modelRef)) },
    val fillerImpl: DslFillerDelegateImpl                     = with (dslCtxWrapperFake) { dslCtx.ctxObjOrCreate(DslRef.filler(simpleName, modelRef)) },
    val showcaseImpl: DslShowcaseDelegateImpl                 = with (dslCtxWrapperFake) { dslCtx.ctxObjOrCreate(DslRef.showcase(simpleName, modelRef)) },
    //val propsImpl: DslPropsDelegate                           = this@DslCtxWrapper.dslCtx.ctxObjOrCreate(DslRef.properties(simpleName, modelRef)),
    //val nameAndWheretoWithSubelements: DslNameAndWheretoWithSubelementsDelegateImpl = this@DslCtxWrapper.dslCtx.ctxObjOrCreate(DslRef.nameAndWhereto(simpleName, modelRef)),
    //val gatherPropertiesImpl: DslGatherPropertiesDelegateImpl = this@DslCtxWrapper.dslCtx.ctxObjOrCreate(DslRef.propertiesOf(simpleName, modelRef)),
    //val classModsImpl: DslClassModsDelegateImpl               = this@DslCtxWrapper.dslCtx.ctxObjOrCreate(DslRef.classMods(simpleName, modelRef)),
    //val extendsImpl: DslExtendsDelegateImpl                   = this@DslCtxWrapper.dslCtx.ctxObjOrCreate(DslRef.extends(simpleName, modelRef)),
    //val showcaseImpl: DslShowcaseDelegateImpl                 = this@DslCtxWrapper.dslCtx.ctxObjOrCreate(DslRef.showcase(simpleName, modelRef))
) : ADslClass(),
    IDslApiModel,
    IDslApiKindClassObjectOrInterface,
    IDslApiConstructorVisibility,
    IDslApiClassModifiers by classModifiersImpl,
    IDslApiPropFuns by propsImpl,
    IDslApiNameAndWheretoWithSubelements by nameAndWheretoWithSubelements,
    IDslApiGatherPropertiesModelAndModelSubelementsCommon by gatherPropertiesImpl,
    IDslApiClassModsDelegate by classModsImpl,
    IDslApiExtendsDelegate by extendsImpl,
    IDslApiFillerDelegate by fillerImpl,
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

    val dslDtos: MutableMap<String, DslDto> = mutableMapOf()     // finished in DslModelgroup dslCtx.PASS_FINISHGENMODELS
    val dslTables: MutableMap<String, DslTable> = mutableMapOf() // finished in DslModelgroup dslCtx.PASS_FINISHGENMODELS
    val dslDcos: MutableMap<String, DslDco> = mutableMapOf()     // finished in DslModelgroup dslCtx.PASS_FINISHGENMODELS

    // IDslApiKindClassObjectOrInterface
    override var kind: DslClassObjectOrInterface = DslClassObjectOrInterface.UNDEFINED
    override var constructorVisibility: IDslApiConstructorVisibility.VISIBILITY = IDslApiConstructorVisibility.VISIBILITY.UNSET

    @DslBlockOn(DslDto::class)
    override fun dto(simpleName: String, dslBlock: IDslApiDto.() -> Unit) {
globalDslCtx = dslCtx // TODO remove workaround
        log.info("fun {}(\"{}\") { ... } in PASS {}", object{}.javaClass.enclosingMethod.name, simpleName, dslCtx.currentPASS)
        when (dslCtx.currentPASS) {
            dslCtx.PASS_1_BASEMODELS -> {
                val dslDto: DslDto = dslCtx.ctxObjCreateNonDelegate { DslDto(simpleName, DslRef.dto(simpleName, selfDslRef)) }
                dslDtos[simpleName] = dslDto
                dslDto.apply(dslBlock)
            }
            dslCtx.PASS_ERROR -> TODO()
            dslCtx.PASS_FINISH -> {
                val dslDto: DslDto = dslCtx.ctxObj(DslRef.dto(simpleName, selfDslRef))
                dslDto.apply(dslBlock) // first let all the subtree finish
                dslDto.finish(dslCtx)
            }
            else -> {
                val dslDto: DslDto = dslCtx.ctxObj(DslRef.dto(simpleName, selfDslRef))
                dslDto.apply(dslBlock)
            }
        }
    }

    @DslBlockOn(DslTable::class)
    override fun tableFor(tableFor: MODELREFENUM, simpleName: String, dslBlock: IDslApiTable.() -> Unit) {
globalDslCtx = dslCtx // TODO remove workaround
        log.info("fun {}(\"{}\") { ... } in PASS {}", object{}.javaClass.enclosingMethod.name, simpleName, dslCtx.currentPASS)
        when (dslCtx.currentPASS) {
            dslCtx.PASS_1_BASEMODELS -> {
                val dslTable: DslTable = dslCtx.ctxObjCreateNonDelegate { DslTable(simpleName, DslRef.table(simpleName, selfDslRef)) }
                when (tableFor) {
                    MODELREFENUM.MODEL -> throw DslException("tableFor was '$tableFor', but only nonpersistent MODELREFENUM are allow for tableFor(...) { }")
                    MODELREFENUM.TABLE -> throw DslException("tableFor was '$tableFor', but only nonpersistent MODELREFENUM are allow for tableFor(...) { }")
                    MODELREFENUM.DTO, MODELREFENUM.DCO -> {
                        dslTable.tableFor = tableFor
                        dslTable.kind = DslClassObjectOrInterface.OBJECT
                        dslTable.constructorVisibility = IDslApiConstructorVisibility.VISIBILITY.PUBLIC
                        dslTable.extendsImpl.extends { replaceSuperclass = true }
                    }
                }
                dslTables[simpleName] = dslTable
                dslTable.apply(dslBlock)
            }
            dslCtx.PASS_5_REFERENCING -> {
                val dslTable: DslTable = dslCtx.ctxObj(DslRef.table(simpleName, selfDslRef))
                dslTable.propertiesOf(tableFor, GatherPropertiesEnum.PROPERTIES_AND_SUPERCLASS_PROPERTIES, simpleName)
                dslTable.apply(dslBlock)
            }
            dslCtx.PASS_ERROR -> TODO()
            dslCtx.PASS_FINISH -> {
                val dslTable: DslTable = dslCtx.ctxObj(DslRef.table(simpleName, selfDslRef))
                dslTable.apply(dslBlock) // first let all the subtree finish
                dslTable.finish(dslCtx)
            }
            else -> {
                val dslTable: DslTable = dslCtx.ctxObj(DslRef.table(simpleName, selfDslRef))
                dslTable.apply(dslBlock)
            }
        }
    }

    @DslBlockOn(DslDco::class)
    override fun dco(simpleName: String, dslBlock: IDslApiDco.() -> Unit) {
globalDslCtx = dslCtx // TODO remove workaround
        log.info("fun {}(\"{}\") { ... } in PASS {}", object{}.javaClass.enclosingMethod.name, simpleName, dslCtx.currentPASS)
        when (dslCtx.currentPASS) {
            dslCtx.PASS_1_BASEMODELS -> {
                val dslDco: DslDco = dslCtx.ctxObjCreateNonDelegate { DslDco(simpleName, DslRef.dco(simpleName, selfDslRef)) }
                dslDcos[simpleName] = dslDco
                dslDco.apply(dslBlock)
            }
            dslCtx.PASS_ERROR -> TODO()
            dslCtx.PASS_FINISH -> {
                val dslDco: DslDco = dslCtx.ctxObj(DslRef.dco(simpleName, selfDslRef))
                dslDco.apply(dslBlock) // first let all the subtree finish
                dslDco.finish(dslCtx)
            }
            else -> {
                val dslDco: DslDco = dslCtx.ctxObj(DslRef.dco(simpleName, selfDslRef))
                dslDco.apply(dslBlock)
            }
        }
    }

    fun finish(dslCtx: DslCtx) {
        // consistency check if filler referenced models really exist and do not point into "nirvana"
        val finishedFillerDatas: MutableMap<String, MutableSet<FillerData>> = fillerImpl.finishedFillerDatas()
        for (entry in finishedFillerDatas) {
            for (fillerData: FillerData in entry.value) {
                try {
                    dslCtx.ctxObj<ADslClass>(fillerData.targetDslRef)
                } catch (e: Exception) {
                    throw DslException("filler targetDslRef: '${fillerData.targetDslRef}' does not exist in DslCtx")
                }
            }
            for (fillerData: FillerData in entry.value) {
                try {
                    dslCtx.ctxObj<ADslClass>(fillerData.sourceDslRef)
                } catch (e: Exception) {
                    throw DslException("filler sourceDslRef: '${fillerData.sourceDslRef}' does not exist in DslCtx")
                }
            }
        }
        for ((simpleName, setOfFillerDatas) in finishedFillerDatas) {
            dslCtx.genCtx.fillerDatas.getOrPut(simpleName) { mutableMapOf() }[selfDslRef] = setOfFillerDatas
        }
    }
    fun prepareNameAndWheretos(collectedNameAndWheretosOfModel: CollectedNameAndWheretos) {
        for (dslNameAndWheretoDelegateEntry: MutableMap.MutableEntry<String, DslNameAndWheretoOnSubElementsDelegateImpl> in nameAndWheretoWithSubelements.nameAndWheretos) {
            collectedNameAndWheretosOfModel.createFor(CollectedNameAndWheretos.THINGSWITHNAMEANDWHERETOS.model, SharedNameAndWhereto(
                dslNameAndWheretoDelegateEntry.value.simpleName,
                dslNameAndWheretoDelegateEntry.value.selfDslRef,
                dslNameAndWheretoDelegateEntry.value.strategyClassName,    dslNameAndWheretoDelegateEntry.value.strategyTableName,
                dslNameAndWheretoDelegateEntry.value.baseDirPathAbsolute,  dslNameAndWheretoDelegateEntry.value.baseDirAddendum,
                dslNameAndWheretoDelegateEntry.value.pathAbsolute ,        dslNameAndWheretoDelegateEntry.value.pathAddendum,
                dslNameAndWheretoDelegateEntry.value.classPrefixAbsolute,  dslNameAndWheretoDelegateEntry.value.classPrefixAddendum,
                dslNameAndWheretoDelegateEntry.value.classPostfixAbsolute, dslNameAndWheretoDelegateEntry.value.classPostfixAddendum,
                dslNameAndWheretoDelegateEntry.value.basePackageAbsolute,  dslNameAndWheretoDelegateEntry.value.basePackageAddendum,
                dslNameAndWheretoDelegateEntry.value.packageNameAbsolute,  dslNameAndWheretoDelegateEntry.value.packageNameAddendum,
            ))
        }
        when (MODELREFENUM.sentinel) { MODELREFENUM.MODEL, MODELREFENUM.DTO, MODELREFENUM.TABLE, MODELREFENUM.DCO -> {} } // sentinel to check if new MODELREFENUM was added
        for (dslNameAndWheretoDelegateEntry in nameAndWheretoWithSubelements.dtoNameAndWheretos) {
            collectedNameAndWheretosOfModel.createFromElementForSubelement(MODELREFENUM.DTO, SharedNameAndWhereto(
                dslNameAndWheretoDelegateEntry.value.simpleName,
                dslNameAndWheretoDelegateEntry.value.selfDslRef,
                dslNameAndWheretoDelegateEntry.value.strategyClassName,    dslNameAndWheretoDelegateEntry.value.strategyTableName,
                dslNameAndWheretoDelegateEntry.value.baseDirPathAbsolute,  dslNameAndWheretoDelegateEntry.value.baseDirAddendum,
                dslNameAndWheretoDelegateEntry.value.pathAbsolute,         dslNameAndWheretoDelegateEntry.value.pathAddendum,
                dslNameAndWheretoDelegateEntry.value.classPrefixAbsolute,  dslNameAndWheretoDelegateEntry.value.classPrefixAddendum,
                dslNameAndWheretoDelegateEntry.value.classPostfixAbsolute, dslNameAndWheretoDelegateEntry.value.classPostfixAddendum,
                dslNameAndWheretoDelegateEntry.value.basePackageAbsolute,  dslNameAndWheretoDelegateEntry.value.basePackageAddendum,
                dslNameAndWheretoDelegateEntry.value.packageNameAbsolute,  dslNameAndWheretoDelegateEntry.value.packageNameAddendum,
            ))
        }
        for (dslNameAndWheretoDelegateEntry in nameAndWheretoWithSubelements.dcoNameAndWheretos) {
            collectedNameAndWheretosOfModel.createFromElementForSubelement(MODELREFENUM.DCO, SharedNameAndWhereto(
                dslNameAndWheretoDelegateEntry.value.simpleName,
                dslNameAndWheretoDelegateEntry.value.selfDslRef,
                dslNameAndWheretoDelegateEntry.value.strategyClassName,    dslNameAndWheretoDelegateEntry.value.strategyTableName,
                dslNameAndWheretoDelegateEntry.value.baseDirPathAbsolute,  dslNameAndWheretoDelegateEntry.value.baseDirAddendum,
                dslNameAndWheretoDelegateEntry.value.pathAbsolute,         dslNameAndWheretoDelegateEntry.value.pathAddendum,
                dslNameAndWheretoDelegateEntry.value.classPrefixAbsolute,  dslNameAndWheretoDelegateEntry.value.classPrefixAddendum,
                dslNameAndWheretoDelegateEntry.value.classPostfixAbsolute, dslNameAndWheretoDelegateEntry.value.classPostfixAddendum,
                dslNameAndWheretoDelegateEntry.value.basePackageAbsolute,  dslNameAndWheretoDelegateEntry.value.basePackageAddendum,
                dslNameAndWheretoDelegateEntry.value.packageNameAbsolute,  dslNameAndWheretoDelegateEntry.value.packageNameAddendum,
            ))
        }
        for (dslNameAndWheretoDelegateEntry in nameAndWheretoWithSubelements.tableNameAndWheretos) {
            collectedNameAndWheretosOfModel.createFromElementForSubelement(MODELREFENUM.TABLE, SharedNameAndWhereto(
                dslNameAndWheretoDelegateEntry.value.simpleName,
                dslNameAndWheretoDelegateEntry.value.selfDslRef,
                dslNameAndWheretoDelegateEntry.value.strategyClassName,     dslNameAndWheretoDelegateEntry.value.strategyTableName,
                dslNameAndWheretoDelegateEntry.value.baseDirPathAbsolute,   dslNameAndWheretoDelegateEntry.value.baseDirAddendum,
                dslNameAndWheretoDelegateEntry.value.pathAbsolute,          dslNameAndWheretoDelegateEntry.value.pathAddendum,
                dslNameAndWheretoDelegateEntry.value.classPrefixAbsolute,   dslNameAndWheretoDelegateEntry.value.classPrefixAddendum,
                dslNameAndWheretoDelegateEntry.value.classPostfixAbsolute,  dslNameAndWheretoDelegateEntry.value.classPostfixAddendum,
                dslNameAndWheretoDelegateEntry.value.basePackageAbsolute,   dslNameAndWheretoDelegateEntry.value.basePackageAddendum,
                dslNameAndWheretoDelegateEntry.value.packageNameAbsolute,   dslNameAndWheretoDelegateEntry.value.packageNameAddendum,
            ))
        }
    }

    companion object {
        val NULL: DslModel = with (dslCtxWrapperFake) {
            DslModel(
                C.NULLSTRING,
                DslRef.model(C.NULLSTRING, IDslRef.NULL),
                DslClassModifiersImpl(),
                DslPropsDelegate(C.NULLSTRING, parentRef = IDslRef.NULL),
                DslNameAndWheretoWithSubelementsDelegateImpl(C.NULLSTRING, IDslRef.NULL),
                DslGatherPropertiesDelegateImpl(C.NULLSTRING, IDslRef.NULL),
                DslClassModsDelegateImpl(C.NULLSTRING, IDslRef.NULL),
                DslExtendsDelegateImpl(C.NULLSTRING, IDslRef.NULL),
                DslFillerDelegateImpl(C.NULLSTRING, IDslRef.NULL),
                DslShowcaseDelegateImpl(C.NULLSTRING, IDslRef.NULL)
            )
        }
    }
}

// === Api interfaces define pure props/directFuns and "union/intersections used in DSL Lambdas and/or IDslApi delegation ===
@ChassisDslMarker
interface IDslApiModelAndModelSubelementsCommon
// interfaces implemented by Model And Elements
    :   IDslApiKindClassObjectOrInterface,
        IDslApiConstructorVisibility,
        IDslApiGatherPropertiesModelAndModelSubelementsCommon,
        IDslApiClassModifiers,
        IDslApiGatherPropertiesProp,
        IDslApiPropFuns,
        IDslApiClassModsDelegate,
        IDslApiExtendsDelegate
@ChassisDslMarker
interface IDslApiModelOnlyCommon // TODO remove trailing Common postfix
    :   IDslApiNameAndWheretoWithSubelements

@ChassisDslMarker
interface IDslApiSubelementsOnlyCommon
    :   IDslApiNameAndWheretoOnly,
        IDslApiGatherPropertiesElementsOnlyCommon,
        IDslApiInitializer
@ChassisDslMarker
interface IDslApiDto
    :   IDslApiModelAndModelSubelementsCommon,
        IDslApiSubelementsOnlyCommon,
        IDslApiShowcaseDelegate
@ChassisDslMarker
interface IDslApiTable
    :   IDslApiModelAndModelSubelementsCommon,
        IDslApiSubelementsOnlyCommon,
        IDslApiCrudDelegate

// === Impl Interfaces (extend IDslApi's plus methods and props that should not be visible from the DSL ===

context(DslCtxWrapper)
class DslDto(
    simpleName: String,
    dtoRef: DslRef.dto,
    classModifiersImpl: DslClassModifiersImpl             = DslClassModifiersImpl(),
    propsImpl: DslPropsDelegate                           = with (dslCtxWrapperFake) { dslCtx.ctxObjOrCreate(DslRef.properties(simpleName, dtoRef)) },
    nameAndWheretoWithoutModelSubelementsImpl: DslNameAndWheretoOnlyDelegateImpl = with (dslCtxWrapperFake) { dslCtx.ctxObjOrCreate(DslRef.nameAndWhereto(simpleName, dtoRef)) },
    gatherPropertiesImpl: DslGatherPropertiesDelegateImpl = with (dslCtxWrapperFake) { dslCtx.ctxObjOrCreate(DslRef.propertiesOf(simpleName, dtoRef)) },
    classModsImpl: DslClassModsDelegateImpl               = with (dslCtxWrapperFake) { dslCtx.ctxObjOrCreate(DslRef.classMods(simpleName, dtoRef)) },
    extendsImpl: DslExtendsDelegateImpl                   = with (dslCtxWrapperFake) { dslCtx.ctxObjOrCreate(DslRef.extends(simpleName, dtoRef)) },
    showcaseImpl: DslShowcaseDelegateImpl                 = with (dslCtxWrapperFake) { dslCtx.ctxObjOrCreate(DslRef.showcase(simpleName, dtoRef)) },
    //val propsImpl: DslPropsDelegate                           = dslCtx.ctxObjOrCreate(DslRef.properties(simpleNameOfParentDslBlock, dtoRef)),
    //val nameAndWheretoWithoutModelSubelementsImpl: DslNameAndWheretoOnlyDelegateImpl = dslCtx.ctxObjOrCreate(DslRef.nameAndWhereto(simpleNameOfParentDslBlock, dtoRef)),
    //val gatherPropertiesImpl: DslGatherPropertiesDelegateImpl = dslCtx.ctxObjOrCreate(DslRef.propertiesOf(simpleNameOfParentDslBlock, dtoRef)),
    //val classModsImpl: DslClassModsDelegateImpl               = dslCtx.ctxObjOrCreate(DslRef.classMods(simpleNameOfParentDslBlock, dtoRef)),
    //val extendsImpl: DslExtendsDelegateImpl                   = dslCtx.ctxObjOrCreate(DslRef.extends(simpleNameOfParentDslBlock, dtoRef)),
    //val showcaseImpl: DslShowcaseDelegateImpl                 = dslCtx.ctxObjOrCreate(DslRef.showcase(simpleNameOfParentDslBlock, dtoRef))
)
    : AProperModelSubelement(simpleName, dtoRef, classModifiersImpl, propsImpl, nameAndWheretoWithoutModelSubelementsImpl, gatherPropertiesImpl, classModsImpl, extendsImpl, showcaseImpl),
    IDslApiDto,
    IDslApiModelAndModelSubelementsCommon,
    IDslApiKindClassObjectOrInterface,
    IDslApiConstructorVisibility,

    IDslImplClassModifiers by classModifiersImpl,
    IDslApiPropFuns by propsImpl,
    IDslApiInitializer by propsImpl,
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
    override var constructorVisibility: IDslApiConstructorVisibility.VISIBILITY = IDslApiConstructorVisibility.VISIBILITY.UNSET

    fun finish(dslCtx: DslCtx) {
        val collectedNameAndWheretos: CollectedNameAndWheretos = finishCollectedNameAndWheretos(nameAndWheretoWithoutModelSubelementsImpl)

        val modelClassName = StrategyNameAndWhereto.resolve(StrategyNameAndWhereto.STRATEGY.SPECIAL_WINS_ON_ABSOLUTE_CONCAT_ADDENDUMS, selfDslRef, collectedNameAndWheretos)
        val dtoModelFromDsl = GenModel.DtoModelFromDsl(selfDslRef as DslRef.dto, modelClassName)
        dslCtx.genCtx.putGenModelFromDsl(selfDslRef, dtoModelFromDsl)
        val dslModel: DslModel = dslCtx.ctxObj(parentDslRef)
        val dslGroup: DslModelgroup = dslCtx.ctxObj((parentDslRef.parentDslRef))

        when (kind) {
            DslClassObjectOrInterface.CLASS -> dtoModelFromDsl.kind = TypeSpec.Kind.CLASS
            DslClassObjectOrInterface.OBJECT -> dtoModelFromDsl.kind = TypeSpec.Kind.OBJECT
            DslClassObjectOrInterface.INTERFACE -> dtoModelFromDsl.kind = TypeSpec.Kind.INTERFACE
            DslClassObjectOrInterface.UNDEFINED -> {
                when (dslModel.kind) {
                    DslClassObjectOrInterface.CLASS -> dtoModelFromDsl.kind = TypeSpec.Kind.CLASS
                    DslClassObjectOrInterface.OBJECT -> dtoModelFromDsl.kind = TypeSpec.Kind.OBJECT
                    DslClassObjectOrInterface.INTERFACE -> dtoModelFromDsl.kind = TypeSpec.Kind.INTERFACE
                    DslClassObjectOrInterface.UNDEFINED -> dtoModelFromDsl.kind = TypeSpec.Kind.CLASS
                }
            }
        }
        dtoModelFromDsl.constructorVisibility =
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

        dtoModelFromDsl.additionalToStringMemberProps.addAll(propsImpl.additionalToStringMemberProps)
        dtoModelFromDsl.removeToStringMemberProps.addAll(propsImpl.removeToStringMemberProps)

        finishProperModelsModelClassData(dslModel, dtoModelFromDsl, classModifiersImpl, extendsImpl, gatherPropertiesImpl, propsImpl)
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
class DslTable(
    simpleName: String,
    tableRef: DslRef.table,
    classModifiersImpl: DslClassModifiersImpl             = DslClassModifiersImpl(),
    propsImpl: DslPropsDelegate                           = with (dslCtxWrapperFake) { dslCtx.ctxObjOrCreate(DslRef.properties(simpleName, tableRef)) },
    nameAndWheretoWithoutModelSubelementsImpl: DslNameAndWheretoOnlyDelegateImpl = with (dslCtxWrapperFake) { dslCtx.ctxObjOrCreate(DslRef.nameAndWhereto(simpleName, tableRef)) },
    gatherPropertiesImpl: DslGatherPropertiesDelegateImpl = with (dslCtxWrapperFake) { dslCtx.ctxObjOrCreate(DslRef.propertiesOf(simpleName, tableRef)) },
    classModsImpl: DslClassModsDelegateImpl               = with (dslCtxWrapperFake) { dslCtx.ctxObjOrCreate(DslRef.classMods(simpleName, tableRef)) },
    extendsImpl: DslExtendsDelegateImpl                   = with (dslCtxWrapperFake) { dslCtx.ctxObjOrCreate(DslRef.extends(simpleName, tableRef)) },
    val crudImpl: DslCrudDelegateImpl                     = with (dslCtxWrapperFake) { dslCtx.ctxObjOrCreate(DslRef.crud(simpleName, tableRef)) },
//    val propsImpl: DslPropsDelegate                           = dslCtx.ctxObjOrCreate(DslRef.properties(simpleNameOfParentDslBlock, tableRef)),
//    val nameAndWheretoWithoutModelSubelementsImpl: DslNameAndWheretoOnlyDelegateImpl = dslCtx.ctxObjOrCreate(DslRef.nameAndWhereto(simpleNameOfParentDslBlock, tableRef)),
//    val gatherPropertiesImpl: DslGatherPropertiesDelegateImpl = dslCtx.ctxObjOrCreate(DslRef.propertiesOf(simpleNameOfParentDslBlock, tableRef)),
//    val classModsImpl: DslClassModsDelegateImpl               = dslCtx.ctxObjOrCreate(DslRef.classMods(simpleNameOfParentDslBlock, tableRef)),
//    val extendsImpl: DslExtendsDelegateImpl                   = dslCtx.ctxObjOrCreate(DslRef.extends(simpleNameOfParentDslBlock, tableRef)),
)
    : AProperModelSubelement(simpleName, tableRef, classModifiersImpl, propsImpl, nameAndWheretoWithoutModelSubelementsImpl, gatherPropertiesImpl, classModsImpl, extendsImpl),
    IDslApiTable,
    IDslApiModelAndModelSubelementsCommon,
    IDslApiKindClassObjectOrInterface,
    IDslApiConstructorVisibility,

    IDslImplClassModifiers by classModifiersImpl,
    IDslApiPropFuns by propsImpl,
    IDslApiInitializer by propsImpl,
    IDslImplClassModsDelegate by classModsImpl,
    IDslImplExtendsDelegate by extendsImpl,
    IDslApiCrudDelegate by crudImpl,

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

    override var kind: DslClassObjectOrInterface = DslClassObjectOrInterface.OBJECT
    override var constructorVisibility: IDslApiConstructorVisibility.VISIBILITY = IDslApiConstructorVisibility.VISIBILITY.UNSET

    fun finish(dslCtx: DslCtx) {
        val collectedNameAndWheretos: CollectedNameAndWheretos = finishCollectedNameAndWheretos(nameAndWheretoWithoutModelSubelementsImpl)

        val modelClassName = StrategyNameAndWhereto.resolve(StrategyNameAndWhereto.STRATEGY.SPECIAL_WINS_ON_ABSOLUTE_CONCAT_ADDENDUMS, selfDslRef, collectedNameAndWheretos)
        val tableModelFromDsl = GenModel.TableModelFromDsl(selfDslRef as DslRef.table, modelClassName)
        dslCtx.genCtx.putGenModelFromDsl(selfDslRef, tableModelFromDsl)
        val dslModel: DslModel = dslCtx.ctxObj(parentDslRef)
        //val dslGroup: DslModelgroup = dslCtx.ctxObj(parentDslRef.parentDslRef)

        tableModelFromDsl.tableFor = tableFor
        // these have been pre-defined in override fun tableFor(tableFor: MODELREFENUM,
        tableModelFromDsl.kind = TypeSpec.Kind.OBJECT
        tableModelFromDsl.constructorVisibility = true

        finishProperModelsModelClassData(dslModel, tableModelFromDsl, classModifiersImpl, extendsImpl, gatherPropertiesImpl, propsImpl)
        // table decides itself what its superclass is, so extends class for table will be removed later on
        // after(!) gathered properties of the superclass the persistent proper subelement refers to have been fetched
        // but if the extends strategy resolves to a superclass/superinterfaces from a base model, it will resolve/expand the ref to our own MODELREFENUM subelement (DslRef.table in this case)
        // we have to correct this to the wanted tableFor proper subelement ref
        // in modelgroupFunc dslCtx.PASS_FINISHGENMODELS
        for (extendsEntry in tableModelFromDsl.extends) {
            when (val eitherModel = extendsEntry.value.typeClassOrDslRef) {
                is EitherTypOrModelOrPoetType.EitherModel -> {
                    when (tableFor) {
                        MODELREFENUM.MODEL -> throw DslException("should have been expanded by strategy resolution of expands")
                        MODELREFENUM.TABLE -> throw DslException("after expanded should not point to a(nother) persistent proper subelement")
                        MODELREFENUM.DTO -> {
                            eitherModel.modelSubElementRefExpanded = DslRef.dto(eitherModel.modelSubElementRefExpanded?.simpleName ?: throw DslException("doesn't have a modelSubElementRefExpanded after strategy resolution of extended's { }"), eitherModel.modelSubElementRefExpanded?.parentDslRef ?: throw DslException("doesn't have a modelSubElementRefExpanded after strategy resolution of extended's { }"))
                        }
                        MODELREFENUM.DCO -> {
                            eitherModel.modelSubElementRefExpanded = DslRef.dco(eitherModel.modelSubElementRefExpanded?.simpleName ?: throw DslException("doesn't have a modelSubElementRefExpanded after strategy resolution of extended's { }"), eitherModel.modelSubElementRefExpanded?.parentDslRef ?: throw DslException("doesn't have a modelSubElementRefExpanded after strategy resolution of extended's { }"))
                        }
                        null -> throw DslException("any persistent(!) proper subelement has to have a tableFor")
                    }
                }
                is EitherTypOrModelOrPoetType.EitherPoetType, is EitherTypOrModelOrPoetType.EitherTyp, is EitherTypOrModelOrPoetType.NOTHING -> {}
            }
            for (ifc in extendsEntry.value.superInterfaces) {
                when (ifc) {
                    is EitherTypOrModelOrPoetType.EitherModel -> {
                        when (tableFor) {
                            MODELREFENUM.MODEL -> throw DslException("should have been expanded by strategy resolution of expands")
                            MODELREFENUM.TABLE -> throw DslException("after expanded should not point to a(nother) persistent proper subelement")
                            MODELREFENUM.DTO -> {
                                ifc.modelSubElementRefExpanded = DslRef.dto(ifc.modelSubElementRefExpanded?.simpleName ?: throw DslException("doesn't have a modelSubElementRefExpanded after strategy resolution of extended's { }"), ifc.modelSubElementRefExpanded?.parentDslRef ?: throw DslException("doesn't have a modelSubElementRefExpanded after strategy resolution of extended's { }"))
                            }
                            MODELREFENUM.DCO -> {
                                ifc.modelSubElementRefExpanded = DslRef.dco(ifc.modelSubElementRefExpanded?.simpleName ?: throw DslException("doesn't have a modelSubElementRefExpanded after strategy resolution of extended's { }"), ifc.modelSubElementRefExpanded?.parentDslRef ?: throw DslException("doesn't have a modelSubElementRefExpanded after strategy resolution of extended's { }"))
                            }
                            null -> throw DslException("any persistent(!) proper subelement has to have a tableFor")
                        }
                    }
                    is EitherTypOrModelOrPoetType.EitherPoetType, is EitherTypOrModelOrPoetType.EitherTyp, is EitherTypOrModelOrPoetType.NOTHING -> {}
                }
            }
            // add super interfaces of tableFor
            when (tableFor) {
                MODELREFENUM.MODEL -> throw DslException("tableFor MODEL not allowed")
                MODELREFENUM.TABLE -> throw DslException("tableFor TABLE not allowed")
                MODELREFENUM.DTO -> {
                    val tableForGenModel: DslDto = dslCtx.ctxObj(DslRef.dto(selfDslRef.simpleName, selfDslRef.parentDslRef))
                    val tableForExtends: Extends? = tableForGenModel.extendsImpl.theExtendBlocks[simpleName]?.extends
                    tableModelFromDsl.extends[simpleName]!!.superInterfaces.addAll(tableForExtends?.superInterfaces ?: emptyList())
                }
                MODELREFENUM.DCO -> {
                    val tableForGenModel: DslDco = dslCtx.ctxObj(DslRef.dco(selfDslRef.simpleName, selfDslRef.parentDslRef))
                    val tableForExtends: Extends? = tableForGenModel.extendsImpl.theExtendBlocks[simpleName]?.extends
                    tableModelFromDsl.extends[simpleName]!!.superInterfaces.addAll(tableForExtends?.superInterfaces ?: emptyList())
                }
                null -> { throw DslException("tableFor should never  be null here")}
            }
        }

        finishCrudDatas()
    }

    private fun finishCrudDatas() {
        // consistency check if filler referenced models really exist and do not point into "nirvana"
        val finishedCrudDatas: MutableMap<String, MutableSet<CrudData>> = crudImpl.finishedCrudDatas()
        for (entry in finishedCrudDatas) {
            for (crudData: CrudData in entry.value) {
                try {
                    dslCtx.ctxObj<ADslClass>(crudData.targetDslRef)
                } catch (e: Exception) {
                    throw DslException("crud targetDslRef: '${crudData.targetDslRef}' does not exist in DslCtx")
                }
            }
            for (crudData: CrudData in entry.value) {
                try {
                    dslCtx.ctxObj<ADslClass>(crudData.sourceDslRef)
                } catch (e: Exception) {
                    throw DslException("crud sourceDslRef: '${crudData.sourceDslRef}' does not exist in DslCtx")
                }
            }
        }
        for ((simpleName, setOfCrudDatas) in finishedCrudDatas) {
            dslCtx.genCtx.crudDatas.getOrPut(simpleName) { mutableMapOf() }[selfDslRef as DslRef.table] = setOfCrudDatas
        }
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
                DslCrudDelegateImpl(C.NULLSTRING, IDslRef.NULL),
            )
        }
    }
}

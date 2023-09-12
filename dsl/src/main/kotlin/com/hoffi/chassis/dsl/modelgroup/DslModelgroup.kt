package com.hoffi.chassis.dsl.modelgroup

import arrow.core.Either
import com.hoffi.chassis.chassismodel.C
import com.hoffi.chassis.chassismodel.dsl.DslException
import com.hoffi.chassis.chassismodel.padForHeader
import com.hoffi.chassis.dsl.internal.*
import com.hoffi.chassis.dsl.modelgroup.allmodels.AllModels
import com.hoffi.chassis.dsl.modelgroup.allmodels.IApiAllModels
import com.hoffi.chassis.dsl.whereto.DslNameAndWheretoWithSubelementsDelegateImpl
import com.hoffi.chassis.dsl.whereto.IDslApiNameAndWheretoWithSubelements
import com.hoffi.chassis.shared.EitherTypOrModelOrPoetType
import com.hoffi.chassis.shared.dsl.DslRef
import com.hoffi.chassis.shared.helpers.onThrow
import com.hoffi.chassis.shared.helpers.tryCatch
import com.hoffi.chassis.shared.parsedata.CollectedExtends
import com.hoffi.chassis.shared.parsedata.GenModel
import com.hoffi.chassis.shared.parsedata.ModelClassDataFromDsl
import com.hoffi.chassis.shared.parsedata.Property
import com.hoffi.chassis.shared.parsedata.nameandwhereto.CollectedNameAndWheretos
import com.hoffi.chassis.shared.parsedata.nameandwhereto.SharedNameAndWhereto
import com.hoffi.chassis.shared.shared.GatherPropertiesEnum
import com.hoffi.chassis.shared.shared.GatherPropertys
import com.hoffi.chassis.shared.shared.reffing.MODELREFENUM
import com.hoffi.chassis.shared.whens.WhensDslRef
import org.slf4j.LoggerFactory

context(DslCtxWrapper)
@ChassisDslMarker
class DslModelgroup(
    val simpleName: String,
    val modelgroupRef: DslRef.modelgroup,
    //override val parent: TopLevelDslFunction,
    val classModifiersImpl: DslClassModifiersImpl                           = DslClassModifiersImpl(),
    //val nameAndWheretoWithSubelements: DslNameAndWheretoWithSubelementsDelegateImpl = dslCtx.ctxObjOrCreate(DslRef.nameAndWhereto(simpleName, modelgroupRef)),
    //val gatherPropertiesImpl: DslGatherPropertiesDelegateImpl                       = dslCtx.ctxObjOrCreate(DslRef.propertiesOf(simpleName, modelgroupRef)),
    val nameAndWheretoWithSubelements: DslNameAndWheretoWithSubelementsDelegateImpl = with (dslCtxWrapperFake) { dslCtx.ctxObjOrCreate(DslRef.nameAndWhereto(simpleName, modelgroupRef)) },
    val gatherPropertiesImpl: DslGatherPropertiesDelegateImpl                       = with (dslCtxWrapperFake) { dslCtx.ctxObjOrCreate(DslRef.propertiesOf(simpleName, modelgroupRef)) },
)
    : ADslClass(),
    IDslApiConstructorVisibility,
    IDslApiNameAndWheretoWithSubelements by nameAndWheretoWithSubelements,
    IDslApiClassModifiers by classModifiersImpl,
    IDslApiGatherPropertiesModelAndModelSubelementsCommon by gatherPropertiesImpl
{
    val log = LoggerFactory.getLogger(javaClass)
    init {
        this@DslCtxWrapper.dslCtx.addToCtx(nameAndWheretoWithSubelements)
        this@DslCtxWrapper.dslCtx.addToCtx(gatherPropertiesImpl)
    }
    override val selfDslRef: DslRef.modelgroup = modelgroupRef
    override fun toString() = selfDslRef.toString()

    internal val allModelsBlockImpls = mutableSetOf<AllModels>()

    override var constructorVisibility: IDslApiConstructorVisibility.VISIBILITY = IDslApiConstructorVisibility.VISIBILITY.UNSET

    context(DslCtxWrapper)
    @DslBlockOn(AllModels::class)
    fun allModels(simpleName: String = C.DEFAULT, allModelsBlock: IApiAllModels.() -> Unit) {
globalDslCtx = dslCtx // TODO remove workaround
        log.info("fun {}(\"{}\") { ... } in PASS {}", object{}.javaClass.enclosingMethod.name, simpleName, dslCtx.currentPASS)
        when (dslCtx.currentPASS) {
            dslCtx.PASS_0_CONFIGURE -> {}
            dslCtx.PASS_1_BASEMODELS -> {
                val allModelsImpl: AllModels = dslCtx.ctxObjCreateNonDelegate { AllModels(simpleName, DslRef.allModels(C.DEFAULT, modelgroupRef)) }
                allModelsBlockImpls.add(allModelsImpl)
                allModelsImpl.apply(allModelsBlock)
            }
            dslCtx.PASS_ERROR -> TODO()
            dslCtx.PASS_FINISH -> { /* TODO implement me! */ }
            else -> {
                val allModelsRef = DslRef.allModels(C.DEFAULT, modelgroupRef)
                allModelsBlockImpls.first{it.selfDslRef == allModelsRef}.apply(allModelsBlock)
            }
        }
    }

    var dslModels = mutableSetOf<DslModel>()

    context(DslCtxWrapper)
    @DslBlockOn(DslModel::class)
    fun model(simpleName: String, dslModelBlock: IDslApiModel.() -> Unit) {
globalDslCtx = dslCtx // TODO remove workaround
        //        log.info("fun {}(\"{}\") { ... } in PASS {}", object{}.javaClass.enclosingMethod.name, simpleName, dslCtx.currentPASS)
        when (dslCtx.currentPASS) {
            dslCtx.PASS_0_CONFIGURE -> {}
            dslCtx.PASS_1_BASEMODELS -> {
                val dslModel = dslCtx.createModel(simpleName, selfDslRef)
                dslModels.add(dslModel)
                dslModel.apply(dslModelBlock)
            }
            dslCtx.PASS_ERROR -> TODO()
            dslCtx.PASS_FINISH -> {
                val modelRef = DslRef.model(simpleName, modelgroupRef)
                println("now finishing modelgroup '${this@DslModelgroup.simpleName}'s model '${dslCtx.ctxObj<DslModel>(modelRef).simpleName}'".
                    padForHeader(120, 3, spacesAround = 5))
                val dslModel = dslCtx.getModel(modelRef).apply(dslModelBlock) // first let all the subtree finish
                dslModel.finish(dslCtx) // this happens AFTER all its Subelements have finish()ed
            }
            else -> {
                val modelRef = DslRef.model(simpleName, modelgroupRef)
                dslCtx.getModel(modelRef).apply(dslModelBlock)
            }
        }
    }

    fun finish() {

    }
    fun putBasemodelNameAndWheretosInDslCtx(dslCtx: DslCtx) {
        val fakeCollectedNameAndWheretosAboveModel = CollectedNameAndWheretos.FAKE(object {}.javaClass.enclosingMethod.name)
        val nameAndWheretoWithSubelementsDevRun: DslNameAndWheretoWithSubelementsDelegateImpl = dslCtx.ctxObj(DslRef.nameAndWhereto(C.DSLRUNREFSIMPLENAME, dslCtx.dslRun.runRef))
        for (dslNameAndWheretoDelegateEntry in nameAndWheretoWithSubelementsDevRun.nameAndWheretos) {
            fakeCollectedNameAndWheretosAboveModel.createFor(CollectedNameAndWheretos.THINGSWITHNAMEANDWHERETOS.DslRunConfigure, SharedNameAndWhereto(
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
        when (MODELREFENUM.sentinel) { MODELREFENUM.MODEL, MODELREFENUM.DTO, MODELREFENUM.TABLE, MODELREFENUM.DCO -> {} } // sentinel to check if new MODELREFENUM was added
        for (dslNameAndWheretoDelegateEntry in nameAndWheretoWithSubelementsDevRun.dtoNameAndWheretos) {
            fakeCollectedNameAndWheretosAboveModel.createFromDslRunForSubelement(
                MODELREFENUM.DTO, SharedNameAndWhereto(
                    dslNameAndWheretoDelegateEntry.value.simpleName,
                    dslNameAndWheretoDelegateEntry.value.selfDslRef,
                    dslNameAndWheretoDelegateEntry.value.strategyClassName, dslNameAndWheretoDelegateEntry.value.strategyTableName,
                    dslNameAndWheretoDelegateEntry.value.baseDirPathAbsolute, dslNameAndWheretoDelegateEntry.value.baseDirAddendum,
                    dslNameAndWheretoDelegateEntry.value.pathAbsolute, dslNameAndWheretoDelegateEntry.value.pathAddendum,
                    dslNameAndWheretoDelegateEntry.value.classPrefixAbsolute, dslNameAndWheretoDelegateEntry.value.classPrefixAddendum,
                    dslNameAndWheretoDelegateEntry.value.classPostfixAbsolute, dslNameAndWheretoDelegateEntry.value.classPostfixAddendum,
                    dslNameAndWheretoDelegateEntry.value.basePackageAbsolute, dslNameAndWheretoDelegateEntry.value.basePackageAddendum,
                    dslNameAndWheretoDelegateEntry.value.packageNameAbsolute, dslNameAndWheretoDelegateEntry.value.packageNameAddendum,
                )
            )
        }
        for (dslNameAndWheretoDelegateEntry in nameAndWheretoWithSubelementsDevRun.dcoNameAndWheretos) {
            fakeCollectedNameAndWheretosAboveModel.createFromDslRunForSubelement(
                MODELREFENUM.DCO, SharedNameAndWhereto(
                    dslNameAndWheretoDelegateEntry.value.simpleName,
                    dslNameAndWheretoDelegateEntry.value.selfDslRef,
                    dslNameAndWheretoDelegateEntry.value.strategyClassName, dslNameAndWheretoDelegateEntry.value.strategyTableName,
                    dslNameAndWheretoDelegateEntry.value.baseDirPathAbsolute, dslNameAndWheretoDelegateEntry.value.baseDirAddendum,
                    dslNameAndWheretoDelegateEntry.value.pathAbsolute, dslNameAndWheretoDelegateEntry.value.pathAddendum,
                    dslNameAndWheretoDelegateEntry.value.classPrefixAbsolute, dslNameAndWheretoDelegateEntry.value.classPrefixAddendum,
                    dslNameAndWheretoDelegateEntry.value.classPostfixAbsolute, dslNameAndWheretoDelegateEntry.value.classPostfixAddendum,
                    dslNameAndWheretoDelegateEntry.value.basePackageAbsolute, dslNameAndWheretoDelegateEntry.value.basePackageAddendum,
                    dslNameAndWheretoDelegateEntry.value.packageNameAbsolute, dslNameAndWheretoDelegateEntry.value.packageNameAddendum,
                )
            )
        }
        for (dslNameAndWheretoDelegateEntry in nameAndWheretoWithSubelementsDevRun.tableNameAndWheretos) {
            fakeCollectedNameAndWheretosAboveModel.createFromDslRunForSubelement(
                MODELREFENUM.TABLE, SharedNameAndWhereto(
                    dslNameAndWheretoDelegateEntry.value.simpleName,
                    dslNameAndWheretoDelegateEntry.value.selfDslRef,
                    dslNameAndWheretoDelegateEntry.value.strategyClassName, dslNameAndWheretoDelegateEntry.value.strategyTableName,
                    dslNameAndWheretoDelegateEntry.value.baseDirPathAbsolute, dslNameAndWheretoDelegateEntry.value.baseDirAddendum,
                    dslNameAndWheretoDelegateEntry.value.pathAbsolute, dslNameAndWheretoDelegateEntry.value.pathAddendum,
                    dslNameAndWheretoDelegateEntry.value.classPrefixAbsolute, dslNameAndWheretoDelegateEntry.value.classPrefixAddendum,
                    dslNameAndWheretoDelegateEntry.value.classPostfixAbsolute, dslNameAndWheretoDelegateEntry.value.classPostfixAddendum,
                    dslNameAndWheretoDelegateEntry.value.basePackageAbsolute, dslNameAndWheretoDelegateEntry.value.basePackageAddendum,
                    dslNameAndWheretoDelegateEntry.value.packageNameAbsolute, dslNameAndWheretoDelegateEntry.value.packageNameAddendum,
                )
            )
        }
        for (dslNameAndWheretoDelegateEntry in nameAndWheretoWithSubelements.nameAndWheretos) {
            fakeCollectedNameAndWheretosAboveModel.createFor(CollectedNameAndWheretos.THINGSWITHNAMEANDWHERETOS.Modelgroup, SharedNameAndWhereto(
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
        when (MODELREFENUM.sentinel) { MODELREFENUM.MODEL, MODELREFENUM.DTO, MODELREFENUM.TABLE, MODELREFENUM.DCO -> {} } // sentinel to check if new MODELREFENUM was added
        for (dslNameAndWheretoDelegateEntry in nameAndWheretoWithSubelements.dtoNameAndWheretos) {
            fakeCollectedNameAndWheretosAboveModel.createFromGroupForSubelement(
                MODELREFENUM.DTO, SharedNameAndWhereto(
                    dslNameAndWheretoDelegateEntry.value.simpleName,
                    dslNameAndWheretoDelegateEntry.value.selfDslRef,
                    dslNameAndWheretoDelegateEntry.value.strategyClassName, dslNameAndWheretoDelegateEntry.value.strategyTableName,
                    dslNameAndWheretoDelegateEntry.value.baseDirPathAbsolute, dslNameAndWheretoDelegateEntry.value.baseDirAddendum,
                    dslNameAndWheretoDelegateEntry.value.pathAbsolute, dslNameAndWheretoDelegateEntry.value.pathAddendum,
                    dslNameAndWheretoDelegateEntry.value.classPrefixAbsolute, dslNameAndWheretoDelegateEntry.value.classPrefixAddendum,
                    dslNameAndWheretoDelegateEntry.value.classPostfixAbsolute, dslNameAndWheretoDelegateEntry.value.classPostfixAddendum,
                    dslNameAndWheretoDelegateEntry.value.basePackageAbsolute, dslNameAndWheretoDelegateEntry.value.basePackageAddendum,
                    dslNameAndWheretoDelegateEntry.value.packageNameAbsolute, dslNameAndWheretoDelegateEntry.value.packageNameAddendum,
                )
            )
        }
        for (dslNameAndWheretoDelegateEntry in nameAndWheretoWithSubelements.dcoNameAndWheretos) {
            fakeCollectedNameAndWheretosAboveModel.createFromGroupForSubelement(
                MODELREFENUM.DCO, SharedNameAndWhereto(
                    dslNameAndWheretoDelegateEntry.value.simpleName,
                    dslNameAndWheretoDelegateEntry.value.selfDslRef,
                    dslNameAndWheretoDelegateEntry.value.strategyClassName, dslNameAndWheretoDelegateEntry.value.strategyTableName,
                    dslNameAndWheretoDelegateEntry.value.baseDirPathAbsolute, dslNameAndWheretoDelegateEntry.value.baseDirAddendum,
                    dslNameAndWheretoDelegateEntry.value.pathAbsolute, dslNameAndWheretoDelegateEntry.value.pathAddendum,
                    dslNameAndWheretoDelegateEntry.value.classPrefixAbsolute, dslNameAndWheretoDelegateEntry.value.classPrefixAddendum,
                    dslNameAndWheretoDelegateEntry.value.classPostfixAbsolute, dslNameAndWheretoDelegateEntry.value.classPostfixAddendum,
                    dslNameAndWheretoDelegateEntry.value.basePackageAbsolute, dslNameAndWheretoDelegateEntry.value.basePackageAddendum,
                    dslNameAndWheretoDelegateEntry.value.packageNameAbsolute, dslNameAndWheretoDelegateEntry.value.packageNameAddendum,
                )
            )
        }
        for (dslNameAndWheretoDelegateEntry in nameAndWheretoWithSubelements.tableNameAndWheretos) {
            fakeCollectedNameAndWheretosAboveModel.createFromGroupForSubelement(
                MODELREFENUM.TABLE, SharedNameAndWhereto(
                    dslNameAndWheretoDelegateEntry.value.simpleName,
                    dslNameAndWheretoDelegateEntry.value.selfDslRef,
                    dslNameAndWheretoDelegateEntry.value.strategyClassName, dslNameAndWheretoDelegateEntry.value.strategyTableName,
                    dslNameAndWheretoDelegateEntry.value.baseDirPathAbsolute, dslNameAndWheretoDelegateEntry.value.baseDirAddendum,
                    dslNameAndWheretoDelegateEntry.value.pathAbsolute, dslNameAndWheretoDelegateEntry.value.pathAddendum,
                    dslNameAndWheretoDelegateEntry.value.classPrefixAbsolute, dslNameAndWheretoDelegateEntry.value.classPrefixAddendum,
                    dslNameAndWheretoDelegateEntry.value.classPostfixAbsolute, dslNameAndWheretoDelegateEntry.value.classPostfixAddendum,
                    dslNameAndWheretoDelegateEntry.value.basePackageAbsolute, dslNameAndWheretoDelegateEntry.value.basePackageAddendum,
                    dslNameAndWheretoDelegateEntry.value.packageNameAbsolute, dslNameAndWheretoDelegateEntry.value.packageNameAddendum,
                )
            )
        }
        for (dslModel in dslModels) {
            val collectedNameAndWheretos: CollectedNameAndWheretos = dslCtx.createCollectedBasemodelNameAndWheretos(dslModel.selfDslRef)
            collectedNameAndWheretos.allFromDslRunConfigure.putAll(fakeCollectedNameAndWheretosAboveModel.allFromDslRunConfigure)
            collectedNameAndWheretos.allFromDslRunConfigureForSubelement.putAll(fakeCollectedNameAndWheretosAboveModel.allFromDslRunConfigureForSubelement)
            collectedNameAndWheretos.allFromGroup.putAll(fakeCollectedNameAndWheretosAboveModel.allFromGroup)
            collectedNameAndWheretos.allFromGroupForSubelement.putAll(fakeCollectedNameAndWheretosAboveModel.allFromGroupForSubelement)
            dslModel.prepareNameAndWheretos(collectedNameAndWheretos)
        }
    }

    fun putBasemodelClassModifiersInDslCtx(dslCtx: DslCtx) {
        for (dslModel in dslModels) {
            val gatheredClassModifiers = dslCtx.createCollectedBasemodelClassModifiers(dslModel.selfDslRef)
            gatheredClassModifiers.allFromGroup.addAll(classModifiersImpl.theClassModifiers)
            gatheredClassModifiers.allFromElement.addAll(dslModel.classModifiersImpl.theClassModifiers)
        }
    }

    fun putBasemodelExtendsInDslCtx(dslCtx: DslCtx) {
        for (dslModel in dslModels) {
            val gatheredExtends: CollectedExtends = dslCtx.createCollectedBasemodelExtends(dslModel.selfDslRef)
            gatheredExtends.allFromElement.putAll(dslModel.extendsImpl.theExtendBlocks.map { entry -> entry.key to entry.value.extends })
        }
    }

    /** gather dslRefs to gather from */
    fun putBasemodelGatherPropertysInDslCtx(dslCtx: DslCtx) {
        for (dslModel in dslModels) {
            val gatheredGatherPropertys = dslCtx.createCollectedBasemodelGatherPropertys(dslModel.selfDslRef)
            gatheredGatherPropertys.allFromGroup.addAll(gatherPropertiesImpl.theGatherPropertys)
            gatheredGatherPropertys.allFromElement.addAll(dslModel.gatherPropertiesImpl.theGatherPropertys)
        }
    }

    fun setModelClassNameOfReffedModelPropertiesAndExtendsModel(dslCtx: DslCtx) {
        for (dslModel in dslModels) {
            when (MODELREFENUM.sentinel) { MODELREFENUM.MODEL, MODELREFENUM.DTO, MODELREFENUM.TABLE, MODELREFENUM.DCO -> {} } // sentinel to check if new MODELREFENUM was added
            for (dslSubel: AProperModelSubelement in (dslModel.dslDtos.values + dslModel.dslTables.values + dslModel.dslDcos.values)) {
                val genModel: ModelClassDataFromDsl = dslCtx.genCtx.genModelFromDsl(dslSubel.selfDslRef)
                val listOfNonKClassNonTypButModelProps = genModel.directProps.values.filter { it.eitherTypModelOrClass is EitherTypOrModelOrPoetType.EitherModel }
                for (modelProp in listOfNonKClassNonTypButModelProps) {
                    val reffedModel: GenModel = subelementGenModel(modelProp.eitherTypModelOrClass as EitherTypOrModelOrPoetType.EitherModel, dslSubel, dslCtx, "modelProp '$modelProp'")
                    val reffedModelClassName = reffedModel.modelClassName
                    modelProp.eitherTypModelOrClass.modelClassName = reffedModelClassName
                }
                //val listOfExtendsModelClass = genModel.extends.values.filter { it.typeClassOrDslRef is EitherTypOrModelOrPoetType.EitherModel }
                //for (extendsEitherModel in listOfExtendsModelClass) {
                //    //val reffedSubElRef = extendsEitherModel.modelSubElementRef
                //    val reffedModel = subelementGenModel(extendsEitherModel.typeClassOrDslRef as EitherTypOrModelOrPoetType.EitherModel, dslSubel, dslCtx, "extends Class: '${extendsEitherModel.typeClassOrDslRef}")
                //    val reffedModelClassName = reffedModel.modelClassName
                //    extendsEitherModel.typeClassOrDslRef.modelClassName = reffedModelClassName
                //}
                for (extends in genModel.extends.values) {
                    var extendsTypClassOrDslRef = extends.typeClassOrDslRef
                    when (extendsTypClassOrDslRef) {
                        is EitherTypOrModelOrPoetType.EitherModel -> {
                            //when (MODELREFENUM.sentinel) { MODELREFENUM.MODEL, MODELREFENUM.DTO, MODELREFENUM.TABLE, MODELREFENUM.DCO -> {} } // sentinel to check if new MODELREFENUM was added
                            //if (dslSubel.selfDslRef !is DslRef.table) { // persistent proper subelements (e.g. tables) get their own superclass in codegen
                                val reffedModel = subelementGenModel(extendsTypClassOrDslRef, dslSubel, dslCtx, "extends Class: '${extendsTypClassOrDslRef}")
                                val reffedModelClassName = reffedModel.modelClassName
                                extendsTypClassOrDslRef.modelClassName = reffedModelClassName
                            //}
                        }
                        else -> {}
                    }
                    for (extendsInterface in extends.superInterfaces) {
                        when (extendsInterface) {
                            is EitherTypOrModelOrPoetType.EitherModel -> {
                                val reffedModel = subelementGenModel(extendsInterface, dslSubel, dslCtx, "extends Interface: '${extendsTypClassOrDslRef}")
                                val reffedModelClassName = reffedModel.modelClassName
                                extendsInterface.modelClassName = reffedModelClassName
                            }
                            else -> {}
                        }
                    }
                }
            }
        }
    }

    private fun subelementGenModel(
        eitherModel: EitherTypOrModelOrPoetType.EitherModel,
        dslSubel: AProperModelSubelement,
        dslCtx: DslCtx,
        stringForExceptionCase: String
    ): GenModel {
        val reffedGenModel = try {
            WhensDslRef.whenModelOrModelSubelement(eitherModel.modelSubElementRef,
                isModelRef = {
                    WhensDslRef.whenModelSubelement(dslSubel.selfDslRef,
                        isDtoRef = { dslCtx.genCtx.genModelFromDsl(DslRef.dto(C.DEFAULT, eitherModel.modelSubElementRef)) },
                        isDcoRef = { dslCtx.genCtx.genModelFromDsl(DslRef.dco(C.DEFAULT, eitherModel.modelSubElementRef)) },
                        isTableRef = { dslCtx.genCtx.genModelFromDsl(DslRef.table(C.DEFAULT, eitherModel.modelSubElementRef)) }
                    ) {
                        DslException("neither known ModelSubelement")
                    }
                },
                isModelSubelementRef = { dslCtx.genCtx.genModelFromDsl(eitherModel.modelSubElementRef) }
            ) { DslException("neither Model or Model Subelement") }
        } catch (e: Exception) {
            throw DslException("for '$dslSubel' '$stringForExceptionCase' ${e.message}")
        }
        return reffedGenModel
    }

    /** actually gather the reffed propertys */
    fun gatherReferencedPropertys(dslCtx: DslCtx) {
        for (dslModel in dslModels) {
            when (MODELREFENUM.sentinel) { MODELREFENUM.MODEL, MODELREFENUM.DTO, MODELREFENUM.TABLE, MODELREFENUM.DCO -> {} } // sentinel to check if new MODELREFENUM was added
            for (modelSubelement: AProperModelSubelement in (dslModel.dslDtos.values + dslModel.dslTables.values + dslModel.dslDcos.values)) {
                val genModel: ModelClassDataFromDsl = dslCtx.genCtx.genModelFromDsl(modelSubelement.selfDslRef)
                val refsToGatherPropsFrom: MutableList<GatherPropertys> = mutableListOf<GatherPropertys>().also { it.addAll(genModel.gatheredPropsDslModelRefs) }
                while (refsToGatherPropsFrom.isNotEmpty()) {
                    val reffedGatherPropertys: GatherPropertys = refsToGatherPropsFrom.removeFirst()
                    val reffedDslClass: ADslClass = Either.tryCatch { dslCtx.ctxObj<ADslClass>(reffedGatherPropertys.modelSubelementRef) }.onThrow { throw DslException("$modelSubelement selfDslRef:'$selfDslRef' tries to gather propertiesOf(...) non-existing ${reffedGatherPropertys.modelSubelementRef} | original: '${reffedGatherPropertys.modelOrModelSubelementRefOriginal}' | expanded: '${reffedGatherPropertys.modelSubelementRefExpanded}'") }
                    when (reffedDslClass) {
                        is DslModel -> {
                            throw DslException("refs to models should have been resolved while earlier PASSes")
                        }
                        is DslDto, is DslTable -> {
                            val reffedGenModel: ModelClassDataFromDsl = dslCtx.genCtx.genModelFromDsl(reffedDslClass.selfDslRef as DslRef.IModelSubelement)

                            when (reffedGatherPropertys.gatherPropertiesEnum) {
                                GatherPropertiesEnum.NONE -> { }
                                GatherPropertiesEnum.PROPERTIES_ONLY_DIRECT_ONES, GatherPropertiesEnum.PROPERTIES_AND_SUPERCLASS_PROPERTIES -> {
                                    val directProps = reffedGenModel.directProps
                                    genModel.gatheredProps.putAll(directProps)
                                }
                                GatherPropertiesEnum.SUPERCLASS_PROPERTIES_ONLY -> { }
                            }
                            when (reffedGatherPropertys.gatherPropertiesEnum) {
                                GatherPropertiesEnum.NONE -> { }
                                GatherPropertiesEnum.PROPERTIES_ONLY_DIRECT_ONES -> { }
                                GatherPropertiesEnum.PROPERTIES_AND_SUPERCLASS_PROPERTIES, GatherPropertiesEnum.SUPERCLASS_PROPERTIES_ONLY -> {
                                    val supersuperGatherPropertys = reffedGenModel.extends.values.filter {
                                        it.typeClassOrDslRef is EitherTypOrModelOrPoetType.EitherModel
                                    }.map {
                                        gatherSuperclassPropertiesFor(it.typeClassOrDslRef as EitherTypOrModelOrPoetType.EitherModel, reffedGatherPropertys.gatherPropertiesEnum, reffedDslClass)
                                    }
                                    refsToGatherPropsFrom.addAll(supersuperGatherPropertys)
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    fun gatherSuperclassPropertys(dslCtx: DslCtx) {
        for (dslModel in dslModels) {
            when (MODELREFENUM.sentinel) { MODELREFENUM.MODEL, MODELREFENUM.DTO, MODELREFENUM.TABLE, MODELREFENUM.DCO -> {} } // sentinel to check if new MODELREFENUM was added
            for (modelSubelement: AProperModelSubelement in (dslModel.dslDtos.values + dslModel.dslTables.values + dslModel.dslDcos.values)) {
                val superclassesProps: MutableMap<String, Property> = mutableMapOf()
                val genModel: ModelClassDataFromDsl = dslCtx.genCtx.genModelFromDsl(modelSubelement.selfDslRef)
                var extendsEither: EitherTypOrModelOrPoetType? = genModel.extends[C.DEFAULT]?.typeClassOrDslRef
                var extendsModel: EitherTypOrModelOrPoetType.EitherModel? = null
                if (extendsEither != null && extendsEither is EitherTypOrModelOrPoetType.EitherModel) {
                    extendsModel = extendsEither
                }
                while (extendsModel != null) {
                    val theGenModel: GenModel = dslCtx.genCtx.genModelFromDsl(extendsModel.modelSubElementRef)
                    superclassesProps.putAll(theGenModel.allProps)
                    extendsEither = theGenModel.extends[C.DEFAULT]?.typeClassOrDslRef
                    extendsModel = if (extendsEither != null && extendsEither is EitherTypOrModelOrPoetType.EitherModel) extendsEither else null
                }
                genModel.superclassProps.putAll(superclassesProps)
                //genModel.initByRefProps()
            }
        }
    }

    private fun gatherSuperclassPropertiesFor(reffedModelExtendsEitherModel: EitherTypOrModelOrPoetType.EitherModel, gatherPropertiesEnum: GatherPropertiesEnum, reffedDslClass: ADslClass): GatherPropertys {
        // TODO if we gather from an other DslModel which is an DslModel, do we only fetch the properties defined on the model itself? or of the same subelement type as WE are (eitherDslRef's)
        return when (reffedDslClass) {
            is DslModel -> {
                when (reffedModelExtendsEitherModel.modelSubElementRef) {
                    is DslRef.dto -> GatherPropertys(DslRef.dto(reffedModelExtendsEitherModel.modelSubElementRef.simpleName, reffedDslClass.selfDslRef), gatherPropertiesEnum)
                    is DslRef.table -> GatherPropertys(DslRef.table(reffedModelExtendsEitherModel.modelSubElementRef.simpleName, reffedDslClass.selfDslRef), gatherPropertiesEnum)
                    else -> { throw DslException("unknown modelSubElementRef")}
                }
            }
            is DslDto ->   GatherPropertys(reffedModelExtendsEitherModel.modelSubElementRef, gatherPropertiesEnum)
            is DslTable ->   GatherPropertys(reffedModelExtendsEitherModel.modelSubElementRef, gatherPropertiesEnum)
            else -> throw DslException("unknown")
        }
    }
}


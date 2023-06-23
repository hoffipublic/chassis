package com.hoffi.chassis.dsl.modelgroup

import com.hoffi.chassis.chassismodel.C
import com.hoffi.chassis.chassismodel.padForHeader
import com.hoffi.chassis.dsl.internal.*
import com.hoffi.chassis.dsl.modelgroup.allmodels.AllModels
import com.hoffi.chassis.dsl.modelgroup.allmodels.IApiAllModels
import com.hoffi.chassis.dsl.whereto.DslNameAndWheretoWithSubelementsDelegateImpl
import com.hoffi.chassis.dsl.whereto.IDslApiNameAndWheretoWithSubelements
import com.hoffi.chassis.shared.dsl.DslRef
import com.hoffi.chassis.shared.parsedata.nameandwhereto.SharedGatheredNameAndWheretos
import com.hoffi.chassis.shared.parsedata.nameandwhereto.SharedNameAndWhereto
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
    IDslApiNameAndWheretoWithSubelements by nameAndWheretoWithSubelements,
    IDslApiClassModifiers by classModifiersImpl,
    IDslApiGatherPropertiesModelAndModelSubelementsCommon by gatherPropertiesImpl
{
    val log = LoggerFactory.getLogger(javaClass)
    init {
        val workaround = dslCtxWrapperFake
        this@DslCtxWrapper.dslCtx.addToCtx(nameAndWheretoWithSubelements)
        this@DslCtxWrapper.dslCtx.addToCtx(gatherPropertiesImpl)
    }
    override val selfDslRef: DslRef.modelgroup = modelgroupRef
    override fun toString() = selfDslRef.toString()

    @DslInstance
    internal val allModelsBlockImpls = mutableSetOf<AllModels>()

    context(DslCtxWrapper)
    @DslBlockOn(AllModels::class)
    fun allModels(simpleName: String = C.DEFAULT, allModelsBlock: IApiAllModels.() -> Unit) {
        log.info("fun {}(\"{}\") { ... } in PASS {}", object{}.javaClass.enclosingMethod.name, simpleName, dslCtx.currentPASS)
        when (dslCtx.currentPASS) {
            dslCtx.PASS_0_CONFIGURE -> {}
            dslCtx.PASS_1_BASEMODELS -> {
                val allModelsImpl: AllModels = dslCtx.ctxObjCreateNonDelegate { AllModels(simpleName, DslRef.allModels(C.DEFAULTSTRING, modelgroupRef)) }
                allModelsBlockImpls.add(allModelsImpl)
                allModelsImpl.apply(allModelsBlock)
            }
            dslCtx.PASS_ERROR -> TODO()
            dslCtx.PASS_FINISH -> { /* TODO implement me! */ }
            else -> {
                val allModelsRef = DslRef.allModels(C.DEFAULTSTRING, modelgroupRef)
                allModelsBlockImpls.first{it.selfDslRef == allModelsRef}.apply(allModelsBlock)
            }
        }
    }

    @DslInstance
    var dslModels = mutableSetOf<DslModel>()

    context(DslCtxWrapper)
    @DslBlockOn(DslModel::class)
    fun model(simpleName: String, dslModelBlock: IDslApiModel.() -> Unit) {
        log.info("fun {}(\"{}\") { ... } in PASS {}", object{}.javaClass.enclosingMethod.name, simpleName, dslCtx.currentPASS)
        when (dslCtx.currentPASS) {
            dslCtx.PASS_0_CONFIGURE -> {}
            dslCtx.PASS_1_BASEMODELS -> {
                val modelRef = DslRef.model(simpleName, modelgroupRef)
                @DslInstance val dslModel = dslCtx.createModel(simpleName, selfDslRef)
                dslModels.add(dslModel)
                dslModel.apply(dslModelBlock)
            }
            dslCtx.PASS_ERROR -> TODO()
            dslCtx.PASS_FINISH -> {
                val modelRef = DslRef.model(simpleName, modelgroupRef)
                println("now finishing modelgroup '${this@DslModelgroup.simpleName}'s model '${dslCtx.ctxObj<DslModel>(modelRef).simpleName}'".
                    padForHeader(120, 3, spacesAround = 5))
                val dslModel = dslCtx.getModel(modelRef).apply(dslModelBlock) // first let all the subtree finish
                dslModel.finish(dslCtx)
            }
            else -> {
                val modelRef = DslRef.model(simpleName, modelgroupRef)
                dslCtx.getModel(modelRef).apply(dslModelBlock)
            }
        }
    }

    fun finish(dslCtx: DslCtx) {

    }
    fun finishNameAndWheretos(dslCtx: DslCtx) {
        val gatheredNameAndWheretosFakeOfDslRun = SharedGatheredNameAndWheretos(DslRef.model("Fake", DslRef.NULL), "Fake")
        val nameAndWheretoWithSubelementsDevRun: DslNameAndWheretoWithSubelementsDelegateImpl = dslCtx.ctxObj(DslRef.nameAndWhereto("<DslRun>", dslCtx.dslRun.runRef))
        for (dslNameAndWheretoDelegateEntry in nameAndWheretoWithSubelementsDevRun.nameAndWheretos) {
            gatheredNameAndWheretosFakeOfDslRun.createFor(SharedGatheredNameAndWheretos.THINGSWITHNAMEANDWHERETOS.DslRunConfigure, SharedNameAndWhereto(
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
        for (dslNameAndWheretoDelegateEntry in nameAndWheretoWithSubelementsDevRun.dtoNameAndWheretos) {
            gatheredNameAndWheretosFakeOfDslRun.createFromDslRunForSubelement(DslRef.dto(dslNameAndWheretoDelegateEntry.value.simpleName , selfDslRef), SharedNameAndWhereto(
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
        for (dslNameAndWheretoDelegateEntry in nameAndWheretoWithSubelementsDevRun.tableNameAndWheretos) {
            gatheredNameAndWheretosFakeOfDslRun.createFromDslRunForSubelement(DslRef.table(dslNameAndWheretoDelegateEntry.value.simpleName , selfDslRef), SharedNameAndWhereto(
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

        for (dslNameAndWheretoDelegateEntry in nameAndWheretoWithSubelements.nameAndWheretos) {
            gatheredNameAndWheretosFakeOfDslRun.createFor(SharedGatheredNameAndWheretos.THINGSWITHNAMEANDWHERETOS.Modelgroup, SharedNameAndWhereto(
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
            gatheredNameAndWheretosFakeOfDslRun.createFromGroupForSubelement(DslRef.dto(dslNameAndWheretoDelegateEntry.value.simpleName , selfDslRef), SharedNameAndWhereto(
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
            gatheredNameAndWheretosFakeOfDslRun.createFromGroupForSubelement(DslRef.table(dslNameAndWheretoDelegateEntry.value.simpleName , selfDslRef), SharedNameAndWhereto(
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

        for (dslModel in dslModels) {
            val gatheredNameAndWheretos = dslCtx.gatheredNameAndWheretos(dslModel.selfDslRef)
            gatheredNameAndWheretos.allFromDslRunConfigure.putAll(gatheredNameAndWheretosFakeOfDslRun.allFromDslRunConfigure)
            gatheredNameAndWheretos.allFromDslRunConfigureForSubelement.putAll(gatheredNameAndWheretosFakeOfDslRun.allFromDslRunConfigureForSubelement)
            gatheredNameAndWheretos.allFromGroup.putAll(gatheredNameAndWheretosFakeOfDslRun.allFromGroup)
            gatheredNameAndWheretos.allFromGroupForSubelement.putAll(gatheredNameAndWheretosFakeOfDslRun.allFromGroupForSubelement)
        }
    }
}


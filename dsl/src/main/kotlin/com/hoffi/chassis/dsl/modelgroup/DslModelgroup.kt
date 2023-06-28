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
import com.hoffi.chassis.shared.EitherTypeOrDslRef
import com.hoffi.chassis.shared.dsl.DslRef
import com.hoffi.chassis.shared.helpers.onThrow
import com.hoffi.chassis.shared.helpers.tryCatch
import com.hoffi.chassis.shared.parsedata.ModelClassData
import com.hoffi.chassis.shared.parsedata.SharedGatheredExtends
import com.hoffi.chassis.shared.parsedata.nameandwhereto.SharedGatheredNameAndWheretos
import com.hoffi.chassis.shared.parsedata.nameandwhereto.SharedNameAndWhereto
import com.hoffi.chassis.shared.shared.GatherPropertiesEnum
import com.hoffi.chassis.shared.shared.GatherPropertys
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

    @DslInstance
    var dslModels = mutableSetOf<DslModel>()

    context(DslCtxWrapper)
    @DslBlockOn(DslModel::class)
    fun model(simpleName: String, dslModelBlock: IDslApiModel.() -> Unit) {
globalDslCtx = dslCtx // TODO remove workaround
        //        log.info("fun {}(\"{}\") { ... } in PASS {}", object{}.javaClass.enclosingMethod.name, simpleName, dslCtx.currentPASS)
        when (dslCtx.currentPASS) {
            dslCtx.PASS_0_CONFIGURE -> {}
            dslCtx.PASS_1_BASEMODELS -> {
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
                dslModel.finish(dslCtx) // this happens AFTER all its Subelements have finish()ed
            }
            else -> {
                val modelRef = DslRef.model(simpleName, modelgroupRef)
                dslCtx.getModel(modelRef).apply(dslModelBlock)
            }
        }
    }

    fun finish(dslCtx: DslCtx) {

    }
    fun prepareNameAndWheretos(dslCtx: DslCtx) {
        val gatheredNameAndWheretosFakeOfDslRun = SharedGatheredNameAndWheretos(DslRef.model("Fake", DslRef.NULL), "Fake")
        val nameAndWheretoWithSubelementsDevRun: DslNameAndWheretoWithSubelementsDelegateImpl = dslCtx.ctxObj(DslRef.nameAndWhereto(C.DSLRUNREFSIMPLENAME, dslCtx.dslRun.runRef))
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
            gatheredNameAndWheretosFakeOfDslRun.createFromDslRunForSubelement(DslRef.model.MODELELEMENT.DTO, SharedNameAndWhereto(
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
            gatheredNameAndWheretosFakeOfDslRun.createFromDslRunForSubelement(DslRef.model.MODELELEMENT.TABLE, SharedNameAndWhereto(
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
            gatheredNameAndWheretosFakeOfDslRun.createFromGroupForSubelement(DslRef.model.MODELELEMENT.DTO, SharedNameAndWhereto(
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
            gatheredNameAndWheretosFakeOfDslRun.createFromGroupForSubelement(DslRef.model.MODELELEMENT.TABLE, SharedNameAndWhereto(
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
            val gatheredNameAndWheretos: SharedGatheredNameAndWheretos = dslCtx.gatheredNameAndWheretos(dslModel.selfDslRef)
            gatheredNameAndWheretos.allFromDslRunConfigure.putAll(gatheredNameAndWheretosFakeOfDslRun.allFromDslRunConfigure)
            gatheredNameAndWheretos.allFromDslRunConfigureForSubelement.putAll(gatheredNameAndWheretosFakeOfDslRun.allFromDslRunConfigureForSubelement)
            gatheredNameAndWheretos.allFromGroup.putAll(gatheredNameAndWheretosFakeOfDslRun.allFromGroup)
            gatheredNameAndWheretos.allFromGroupForSubelement.putAll(gatheredNameAndWheretosFakeOfDslRun.allFromGroupForSubelement)
            dslModel.prepareNameAndWheretos(dslCtx)
        }
    }

    fun prepareClassModifiers(dslCtx: DslCtx) {
        for (dslModel in dslModels) {
            val gatheredClassModifiers = dslCtx.gatheredClassModifiers(dslModel.selfDslRef)
            gatheredClassModifiers.allFromGroup.addAll(classModifiersImpl.theClassModifiers)
            gatheredClassModifiers.allFromElement.addAll(dslModel.classModifiersImpl.theClassModifiers)
        }
    }

    fun prepareExtends(dslCtx: DslCtx) {
        for (dslModel in dslModels) {
            val gatheredExtends: SharedGatheredExtends = dslCtx.gatheredExtends(dslModel.selfDslRef)
            gatheredExtends.allFromElement.putAll(dslModel.extendsImpl.theExtendBlocks.map { entry -> entry.key to entry.value.extends })
        }
    }

    /** gather dslRefs to gather from */
    fun prepareGatherPropertys(dslCtx: DslCtx) {
        for (dslModel in dslModels) {
            val gatheredGatherPropertys = dslCtx.gatheredGatherPropertys(dslModel.selfDslRef)
            gatheredGatherPropertys.allFromGroup.addAll(gatherPropertiesImpl.theGatherPropertys)
            gatheredGatherPropertys.allFromElement.addAll(dslModel.gatherPropertiesImpl.theGatherPropertys)
        }
    }

    fun setModelClassNameOfReffedModelProperties(dslCtx: DslCtx) {
        for (dslModel in dslModels) {
            for (dslSubel: AModelSubelement in (dslModel.dslDtos.values + dslModel.dslTables.values)) {
                val genModel: ModelClassData = dslCtx.genCtx.genModels[dslSubel.selfDslRef]!!
                val listOfNonKClassNonTypButModelRefGenProp = genModel.propertys.values.filter { it.eitherTypModelOrClass is EitherTypOrModelOrPoetType.EitherModel }
                for (genProp in listOfNonKClassNonTypButModelRefGenProp) {
                    val reffedSubElRef = (genProp.eitherTypModelOrClass as EitherTypOrModelOrPoetType.EitherModel).modelSubElement
                    //val reffedModelClassName = if (reffedSubElRef.parentRef is DslRef.IElementLevel) {
                    //    when (dslSubel) {
                    //        is DslDto ->   { dslCtx.genCtx.genModels[DslRef.dto(dslSubel.simpleName, reffedSubElRef)]!!.modelClassName }
                    //        is DslTable -> { dslCtx.genCtx.genModels[DslRef.table(dslSubel.simpleName, reffedSubElRef)]!!.modelClassName }
                    //        else -> throw DslException("cannot determine for which modelSubelement from '$reffedSubElRef'")
                    //    }
                    //} else {
                    //    dslCtx.genCtx.genModels[reffedSubElRef]!!.modelClassName
                    //}
                    val reffedModelClassName = dslCtx.genCtx.genModels[reffedSubElRef]!!.modelClassName
                    (genProp.eitherTypModelOrClass as EitherTypOrModelOrPoetType.EitherModel).modelClassName = reffedModelClassName
                }
            }
        }
    }

    /** actually gather the reffed propertys */
    fun gatherInheritedPropertys(dslCtx: DslCtx) {
        for (dslModel in dslModels) {
            for (dslDto in dslModel.dslDtos.values) {
                val genModel: ModelClassData = dslCtx.genCtx.genModels[dslDto.selfDslRef]!!
                val refsToGatherPropsFrom: MutableList<GatherPropertys> = mutableListOf<GatherPropertys>().also { it.addAll(genModel.gatheredFromDslRefs) }
                while (refsToGatherPropsFrom.isNotEmpty()) {
                    val reffedGatherPropertys: GatherPropertys = refsToGatherPropsFrom.removeFirst()
                    val otherDslModelOrModelSubelement: ADslClass = Either.tryCatch { dslCtx.ctxObj<ADslClass>(reffedGatherPropertys.modelOrModelSubelementRef) }.onThrow { throw DslException("$selfDslRef tries to gather propertiesOf(...) non-existing ${reffedGatherPropertys.modelOrModelSubelementRef}") }
                    when (otherDslModelOrModelSubelement) {
                        is DslModel -> {
                            TODO()
                        }
                        is DslDto, is DslTable -> {
                            val reffedModel: ModelClassData = dslCtx.genCtx.genModels[otherDslModelOrModelSubelement.selfDslRef]!!

                            when (reffedGatherPropertys.gatherPropertiesEnum) {
                                GatherPropertiesEnum.NONE -> { }
                                GatherPropertiesEnum.PROPERTIES, GatherPropertiesEnum.PROPERTIES_AND_SUPERCLASS_PROPERTIES -> {
                                    val itsProps = reffedModel.propertys
                                    genModel.gatheredPropertys.putAll(itsProps)
                                }
                                GatherPropertiesEnum.SUPERCLASS_PROPERTIES_ONLY -> { }
                            }
                            when (reffedGatherPropertys.gatherPropertiesEnum) {
                                GatherPropertiesEnum.NONE -> { }
                                GatherPropertiesEnum.PROPERTIES -> { }
                                GatherPropertiesEnum.PROPERTIES_AND_SUPERCLASS_PROPERTIES, GatherPropertiesEnum.SUPERCLASS_PROPERTIES_ONLY -> {
                                    refsToGatherPropsFrom.addAll(reffedModel.extends.values.filter {
                                        it.typeClassOrDslRef is EitherTypeOrDslRef.EitherDslRef
                                    }.map {
                                        gatherSuperclassPropertiesFor(it.typeClassOrDslRef as EitherTypeOrDslRef.EitherDslRef, reffedGatherPropertys.gatherPropertiesEnum, otherDslModelOrModelSubelement)
                                    })
                                }
                            }
                        }
                    }
                }
            }
            for (dslTable in dslModel.dslTables.values) {
                val genModel: ModelClassData = dslCtx.genCtx.genModels[dslTable.selfDslRef]!!
                val refsToGatherPropsFrom: MutableList<GatherPropertys> = mutableListOf<GatherPropertys>().also { it.addAll(genModel.gatheredFromDslRefs) }
                while (refsToGatherPropsFrom.isNotEmpty()) {
                    val reffedGatherPropertys: GatherPropertys = refsToGatherPropsFrom.removeFirst()
                    val otherDslModelOrModelSubelement: ADslClass = Either.tryCatch { dslCtx.ctxObj<ADslClass>(reffedGatherPropertys.modelOrModelSubelementRef) }.onThrow { throw DslException("$selfDslRef tries to gather propertiesOf(...) non-existing ${reffedGatherPropertys.modelOrModelSubelementRef}") }
                    when (otherDslModelOrModelSubelement) {
                        is DslModel -> {
                            TODO()
                        }
                        is DslDto, is DslTable -> {
                            val reffedModel: ModelClassData = dslCtx.genCtx.genModels[otherDslModelOrModelSubelement.selfDslRef]!!

                            when (reffedGatherPropertys.gatherPropertiesEnum) {
                                GatherPropertiesEnum.NONE -> { }
                                GatherPropertiesEnum.PROPERTIES, GatherPropertiesEnum.PROPERTIES_AND_SUPERCLASS_PROPERTIES -> {
                                    val itsProps = reffedModel.propertys
                                    genModel.gatheredPropertys.putAll(itsProps)
                                }
                                GatherPropertiesEnum.SUPERCLASS_PROPERTIES_ONLY -> { }
                            }
                            when (reffedGatherPropertys.gatherPropertiesEnum) {
                                GatherPropertiesEnum.NONE -> { }
                                GatherPropertiesEnum.PROPERTIES -> { }
                                GatherPropertiesEnum.PROPERTIES_AND_SUPERCLASS_PROPERTIES, GatherPropertiesEnum.SUPERCLASS_PROPERTIES_ONLY -> {
                                    refsToGatherPropsFrom.addAll(reffedModel.gatheredFromDslRefs)
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    private fun gatherSuperclassPropertiesFor(eitherDslRef: EitherTypeOrDslRef.EitherDslRef, gatherPropertiesEnum: GatherPropertiesEnum, otherDslModelOrModelSubelement: ADslClass): GatherPropertys {
        return when (otherDslModelOrModelSubelement) {
            is DslModel -> GatherPropertys(eitherDslRef.dslRef, gatherPropertiesEnum)
            is DslDto ->   GatherPropertys(DslRef.dto(C.DEFAULT, eitherDslRef.dslRef), gatherPropertiesEnum)
            is DslTable ->   GatherPropertys(DslRef.table(C.DEFAULT, eitherDslRef.dslRef), gatherPropertiesEnum)
            else -> throw DslException("unknown")
        }
    }
}


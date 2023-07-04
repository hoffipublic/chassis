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
import com.hoffi.chassis.shared.parsedata.ModelClassData
import com.hoffi.chassis.shared.parsedata.SharedGatheredExtends
import com.hoffi.chassis.shared.parsedata.nameandwhereto.SharedGatheredNameAndWheretos
import com.hoffi.chassis.shared.parsedata.nameandwhereto.SharedNameAndWhereto
import com.hoffi.chassis.shared.shared.GatherPropertiesEnum
import com.hoffi.chassis.shared.shared.GatherPropertys
import com.hoffi.chassis.shared.shared.reffing.MODELREFENUM
import com.squareup.kotlinpoet.TypeSpec
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
            gatheredNameAndWheretosFakeOfDslRun.createFromDslRunForSubelement(MODELREFENUM.DTO, SharedNameAndWhereto(
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
            gatheredNameAndWheretosFakeOfDslRun.createFromDslRunForSubelement(MODELREFENUM.TABLE, SharedNameAndWhereto(
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
            gatheredNameAndWheretosFakeOfDslRun.createFromGroupForSubelement(MODELREFENUM.DTO, SharedNameAndWhereto(
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
            gatheredNameAndWheretosFakeOfDslRun.createFromGroupForSubelement(MODELREFENUM.TABLE, SharedNameAndWhereto(
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

    fun setModelClassNameOfReffedModelPropertiesAndExtendsModel(dslCtx: DslCtx) {
        for (dslModel in dslModels) {
            for (dslSubel: AModelSubelement in (dslModel.dslDtos.values + dslModel.dslTables.values)) {
                val genModel: ModelClassData = dslCtx.genCtx.genModel(dslSubel.selfDslRef)
                val listOfNonKClassNonTypButModelRefGenProp = genModel.propertys.values.filter { it.eitherTypModelOrClass is EitherTypOrModelOrPoetType.EitherModel }.map { it.eitherTypModelOrClass as EitherTypOrModelOrPoetType.EitherModel }
                for (genPropEitherModel in listOfNonKClassNonTypButModelRefGenProp) {
                    val reffedSubElRef = genPropEitherModel.modelSubElementRef
                    val reffedModel = dslCtx.genCtx.genModel(reffedSubElRef)
                    val reffedModelClassName = reffedModel.modelClassName
                    genPropEitherModel.modelClassName = reffedModelClassName
                    genPropEitherModel.isInterface = reffedModel.kind == TypeSpec.Kind.INTERFACE
                }
                val listOfExtendsModelClass = genModel.extends.values.filter { it.typeClassOrDslRef is EitherTypOrModelOrPoetType.EitherModel }.map { it.typeClassOrDslRef as EitherTypOrModelOrPoetType.EitherModel }
                for (extendsEitherModel in listOfExtendsModelClass) {
                    val reffedSubElRef = extendsEitherModel.modelSubElementRef
                    val reffedModel = dslCtx.genCtx.genModel(reffedSubElRef)
                    val reffedModelClassName = reffedModel.modelClassName
                    extendsEitherModel.modelClassName = reffedModelClassName
                    extendsEitherModel.isInterface = reffedModel.kind == TypeSpec.Kind.INTERFACE
                }
                for (extends in genModel.extends.values) {
                    var extendsTypClassOrDslRef = extends.typeClassOrDslRef
                    when (extendsTypClassOrDslRef) {
                        is EitherTypOrModelOrPoetType.EitherModel -> {
                            val reffedSubElRef = extendsTypClassOrDslRef.modelSubElementRef
                            val reffedModel = dslCtx.genCtx.genModel(reffedSubElRef)
                            val reffedModelClassName = reffedModel.modelClassName
                            extendsTypClassOrDslRef.modelClassName = reffedModelClassName
                            extendsTypClassOrDslRef.isInterface = reffedModel.kind == TypeSpec.Kind.INTERFACE
                        }
                        else -> {}
                    }
                    for (extendsInterface in extends.superInterfaces) {
                        when (extendsInterface) {
                            is EitherTypOrModelOrPoetType.EitherModel -> {
                                val reffedSubElRef = extendsInterface.modelSubElementRef
                                val reffedModel = dslCtx.genCtx.genModel(reffedSubElRef)
                                val reffedModelClassName = reffedModel.modelClassName
                                extendsInterface.modelClassName = reffedModelClassName
                                extendsInterface.isInterface = reffedModel.kind == TypeSpec.Kind.INTERFACE
                            }
                            else -> {}
                        }
                    }
                }
            }
        }
    }

    /** actually gather the reffed propertys */
    fun gatherReferencedPropertys(dslCtx: DslCtx) {
        for (dslModel in dslModels) {
            for (dslDto in dslModel.dslDtos.values) {
                val genModel: ModelClassData = dslCtx.genCtx.genModel(dslDto.selfDslRef)
                val refsToGatherPropsFrom: MutableList<GatherPropertys> = mutableListOf<GatherPropertys>().also { it.addAll(genModel.gatheredFromDslRefs) }
                while (refsToGatherPropsFrom.isNotEmpty()) {
                    val reffedGatherPropertys: GatherPropertys = refsToGatherPropsFrom.removeFirst()
                    val reffedDslClass: ADslClass = Either.tryCatch { dslCtx.ctxObj<ADslClass>(reffedGatherPropertys.modelOrModelSubelementRef) }.onThrow { throw DslException("$dslDto selfDslRef:'$selfDslRef' tries to gather propertiesOf(...) non-existing ${reffedGatherPropertys.modelOrModelSubelementRef}") }
                    when (reffedDslClass) {
                        is DslModel -> {
                            TODO()
                        }
                        is DslDto, is DslTable -> {
                            val reffedGenModel: ModelClassData = dslCtx.genCtx.genModel(reffedDslClass.selfDslRef as DslRef.IModelSubelement)

                            when (reffedGatherPropertys.gatherPropertiesEnum) {
                                GatherPropertiesEnum.NONE -> { }
                                GatherPropertiesEnum.PROPERTIES, GatherPropertiesEnum.PROPERTIES_AND_SUPERCLASS_PROPERTIES -> {
                                    val itsProps = reffedGenModel.propertys
                                    genModel.gatheredPropertys.putAll(itsProps)
                                }
                                GatherPropertiesEnum.SUPERCLASS_PROPERTIES_ONLY -> { }
                            }
                            when (reffedGatherPropertys.gatherPropertiesEnum) {
                                GatherPropertiesEnum.NONE -> { }
                                GatherPropertiesEnum.PROPERTIES -> { }
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
            for (dslTable in dslModel.dslTables.values) {
                val genModel: ModelClassData = dslCtx.genCtx.genModel(dslTable.selfDslRef)
                val refsToGatherPropsFrom: MutableList<GatherPropertys> = mutableListOf<GatherPropertys>().also { it.addAll(genModel.gatheredFromDslRefs) }
                while (refsToGatherPropsFrom.isNotEmpty()) {
                    val reffedGatherPropertys: GatherPropertys = refsToGatherPropsFrom.removeFirst()
                    val otherDslModelOrModelSubelement: ADslClass = Either.tryCatch { dslCtx.ctxObj<ADslClass>(reffedGatherPropertys.modelOrModelSubelementRef) }.onThrow { throw DslException("$selfDslRef tries to gather propertiesOf(...) non-existing ${reffedGatherPropertys.modelOrModelSubelementRef}") }
                    when (otherDslModelOrModelSubelement) {
                        is DslModel -> {
                            TODO()
                        }
                        is DslDto, is DslTable -> {
                            val reffedModel: ModelClassData = dslCtx.genCtx.genModel(otherDslModelOrModelSubelement.selfDslRef as DslRef.IModelSubelement)

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


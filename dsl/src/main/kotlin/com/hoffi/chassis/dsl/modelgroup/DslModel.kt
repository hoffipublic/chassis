//package com.hoffi.chassis.dsl.modelgroup
//
//import com.hoffi.chassis.chassismodel.C
//import com.hoffi.chassis.chassismodel.dsl.DslException
//import com.hoffi.chassis.dsl.*
//import com.hoffi.chassis.dsl.internal.*
//import com.hoffi.chassis.dsl.strategies.DslResolutionStrategies
//import com.hoffi.chassis.dsl.whereto.DslNameAndWheretoOnlyDelegateImpl
//import com.hoffi.chassis.dsl.whereto.DslNameAndWheretoWithSubelementsDelegateImpl
//import com.hoffi.chassis.dsl.whereto.IDslApiNameAndWheretoOnly
//import com.hoffi.chassis.dsl.whereto.IDslApiNameAndWheretoWithSubelements
//import com.hoffi.chassis.shared.dsl.DslRef
//import com.hoffi.chassis.shared.parsedata.EitherModel
//import com.hoffi.chassis.shared.parsedata.Property
//import com.squareup.kotlinpoet.TypeSpec
//import org.slf4j.LoggerFactory
//
//// === Api interfaces define pure props/directFuns and "union/intersections used in DSL Lambdas and/or IDslApi delegation ===
//interface IDslApiModelAndModelSubelementsCommon
//    // interfaces implemented by Model And Elements
//    :   IDslApiGatherPropertiesModelAndModelSubelementsCommon,
//        IDslApiClassModifiers,
//        IDslApiGatherPropertiesProp,
//        IDslApiPropFuns,
//        IDslApiClassModsDelegate,
//        IDslApiExtendsDelegate,
//        IDslApiShowcaseDelegate
//{
//    var kind: DslClassObjectOrInterface
//}
//interface IDslApiModelOnlyCommon
//    : IDslApiNameAndWheretoWithSubelements
//interface IDslApiSubelementsOnlyCommon
//    :   IDslApiNameAndWheretoOnly,
//        IDslApiGatherPropertiesElementsOnlyCommon
//interface IDslApiFillerModel
//interface IDslApiDto :   IDslApiSubelementsOnlyCommon, IDslApiModelAndModelSubelementsCommon
//interface IDslApiTable : IDslApiSubelementsOnlyCommon, IDslApiModelAndModelSubelementsCommon
//interface IDslApiModel : IDslApiModelAndModelSubelementsCommon, IDslApiModelOnlyCommon, IDslApiExtendsDelegate, IDslApiPropFuns, IDslApiNameAndWheretoWithSubelements, IDslApiClassModsDelegate, IDslApiShowcaseDelegate {
//    context(DslCtxWrapper)
//    @DslBlockOn(DslDto::class)
//    fun dto(simpleName: String = C.DEFAULT, dslBlock: IDslApiDto.() -> Unit)
//    context(DslCtxWrapper)
//    @DslBlockOn(DslTable::class)
//    fun table(simpleName: String = C.DEFAULT, dslBlock: IDslApiTable.() -> Unit)
//}
//
//// === Impl Interfaces (extend IDslApi's plus methods and props that should not be visible from the DSL ===
//interface IDslImplModelAndModelSubElementsCommon : IDslApiModelAndModelSubelementsCommon {
//    val modelElement: DslRef.model.MODELELEMENT
//}
//interface IDslImplModelOnlyCommon : IDslApiModelOnlyCommon
//interface IDslImplSubElementsOnlyCommon : IDslApiSubelementsOnlyCommon
//interface IDslImplFillerModel : IDslApiFillerModel
//interface IDslImplDto : IDslApiDto, IDslImplSubElementsOnlyCommon, IDslImplModelAndModelSubElementsCommon
//interface IDslImplTable : IDslApiTable, IDslImplSubElementsOnlyCommon, IDslImplModelAndModelSubElementsCommon
//interface IDslImplModel : IDslApiModel, IDslImplModelOnlyCommon, IDslImplModelAndModelSubElementsCommon
//
//
///** abstract parent implementations of functionality that is the same in Model and any of its Elements(Dto/Table/...) */
//context(DslCtxWrapper)
//@ChassisDslMarker
//abstract class ADslModelAndElementsCommonImpl(
//    simpleName: String,
//    parentRef: DslRef.IModelOrModelSubElement,
//    val classModifiersImpl: DslClassModifiersImpl,
//    val propsImpl: DslPropsDelegate,
//    val classModsImpl: DslClassModsDelegateImpl,
//    val extendsImpl: DslExtendsDelegateImpl,
//    val showcaseImpl: DslShowcaseDelegateImpl
//) : ADslClass(),
//    IDslImplModelAndModelSubElementsCommon,
//    IDslImplClassModifiers by classModifiersImpl,
//    IDslApiPropFuns by propsImpl,
//    IDslImplClassModsDelegate by classModsImpl,
//    IDslImplExtendsDelegate by extendsImpl,
//    IDslImplShowcaseDelegate by showcaseImpl
//{
//    val log = LoggerFactory.getLogger(javaClass)
//    // non direct DSL props
//    //override val selfDslRef: DslRef.IModelOrModelSubElement = modelOrModelSubElementRef
//    override val selfDslRef = DslRef.model(simpleName, parentRef)
//
//    // DSL props
//    override var kind: DslClassObjectOrInterface = DslClassObjectOrInterface.CLASS
//}
///** abstract parent implementations of functionality that is ONLY available in Elements(Dto/Table/...) but NOT in Model itself */
//context(DslCtxWrapper)
//@ChassisDslMarker
//abstract class ADslSubElementsOnly(
//    simpleName: String,
//    parentRef: DslRef.IModelOrModelSubElement,
//    classModifiersImpl: DslClassModifiersImpl,
//    propsImpl: DslPropsDelegate,
//    val nameAndWheretos: DslNameAndWheretoOnlyDelegateImpl,
//    val gatherPropertiesImpl: DslGatherPropertiesDelegateImpl,
//    classModsImpl: DslClassModsDelegateImpl,
//    extendsImpl: DslExtendsDelegateImpl,
//    showcaseImpl: DslShowcaseDelegateImpl
//)
//    : ADslModelAndElementsCommonImpl(
//    simpleName,
//    parentRef,
//    classModifiersImpl,
//    propsImpl,
//    classModsImpl,
//    extendsImpl,
//    showcaseImpl
//),
//    IDslImplSubElementsOnlyCommon,
//    IDslApiNameAndWheretoOnly by nameAndWheretos,
//    IDslApiGatherPropertiesBoth by gatherPropertiesImpl
//{
//}
///** abstract parent implementations of functionality that is ONLY available in Model (parent of Dto/Table/...) */
//context(DslCtxWrapper)
//@ChassisDslMarker
//abstract class ADslModelOnly(
//    simpleName: String,
//    parentRef: DslRef.IModelOrModelSubElement,
//    classModifiersImpl: DslClassModifiersImpl,
//    propsImpl: DslPropsDelegate,
//    val nameAndWheretos: DslNameAndWheretoWithSubelementsDelegateImpl,
//    val gatherProperties: DslGatherPropertiesDelegateImpl,
//    classModsImpl: DslClassModsDelegateImpl,
//    extendsImpl: DslExtendsDelegateImpl,
//    showcaseImpl: DslShowcaseDelegateImpl
//)
//    : ADslModelAndElementsCommonImpl(simpleName, parentRef, classModifiersImpl, propsImpl, classModsImpl, extendsImpl, showcaseImpl),
//    IDslImplModelOnlyCommon,
//    IDslApiNameAndWheretoWithSubelements by nameAndWheretos,
//    IDslApiGatherPropertiesModelAndModelSubelementsCommon by gatherProperties
//{
//}
//
//// TODO rename Delegate Impls to be postfixed xxxDelegateImpl for DslClassModifiersImpl and DslPropsDelegate
//
//context(DslCtxWrapper)
//@ChassisDslMarker
//class DslDto(
//    simpleName: String,
//    modelRef: DslRef.model,
//    classModifiersImpl: DslClassModifiersImpl                            = DslClassModifiersImpl(),
//    propsImpl: DslPropsDelegate                                              = DslPropsDelegate(dtoRef, this@DslCtxWrapper),
//    nameAndWheretoWithoutModelSubElementsImpl: DslNameAndWheretoOnlyDelegateImpl = this@DslCtxWrapper.dslCtx.ctxObjOrCreate(DslRef.nameAndWhereto(simpleName, modelRef)),
//    gatherPropertiesImpl: DslGatherPropertiesDelegateImpl                = this@DslCtxWrapper.dslCtx.ctxObjOrCreate(DslRef.propertiesOf(simpleName, modelRef)),
//    classModsImpl: DslClassModsDelegateImpl                              = this@DslCtxWrapper.dslCtx.ctxObjOrCreate(DslRef.classMods(simpleName, modelRef)),
//    extendsImpl: DslExtendsDelegateImpl                                  = this@DslCtxWrapper.dslCtx.ctxObjOrCreate(DslRef.extends(simpleName, modelRef)),
//    showcaseImpl: DslShowcaseDelegateImpl                                = this@DslCtxWrapper.dslCtx.ctxObjOrCreate(DslRef.showcase(simpleName, modelRef))
//)
//    : ADslSubElementsOnly(simpleName, modelRef, classModifiersImpl, propsImpl, nameAndWheretoWithoutModelSubElementsImpl, gatherPropertiesImpl, classModsImpl, extendsImpl, showcaseImpl)
//    , IDslImplDto
//{
//    init {
//        // set all delegate's IDslClass.parent to this
//        //nameAndWheretoWithoutModelSubElementsImpl.explicitParent = this
//        //gatherPropertiesImpl.explicitParent = this
//        //classModsImpl.explicitParent = this
//        //extendsImpl.explicitParent = this
//        //showcaseImpl.explicitParent = this
//    }
//    override val modelElement = DslRef.model.MODELELEMENT.DTO
//
//    fun finish(dslCtx: DslCtx) {
//        val dtoModel = EitherModel.DtoModel(dtoRef)
//        dslCtx.genCtx.genModels[dtoRef] = dtoModel
//        when (kind) {
//            DslClassObjectOrInterface.CLASS -> dtoModel.kind = TypeSpec.Kind.CLASS
//            DslClassObjectOrInterface.OBJECT -> dtoModel.kind = TypeSpec.Kind.OBJECT
//            DslClassObjectOrInterface.INTERFACE -> dtoModel.kind = TypeSpec.Kind.INTERFACE
//            DslClassObjectOrInterface.UNDEFINED -> {
//                when ((parent as DslModel).kind) {
//                    DslClassObjectOrInterface.CLASS -> dtoModel.kind = TypeSpec.Kind.CLASS
//                    DslClassObjectOrInterface.OBJECT -> dtoModel.kind = TypeSpec.Kind.OBJECT
//                    DslClassObjectOrInterface.INTERFACE -> dtoModel.kind = TypeSpec.Kind.INTERFACE
//                    DslClassObjectOrInterface.UNDEFINED -> { throw DslException("ref: $dtoRef has undefined kind, neither set in ModelSubElement, nor in parent model() { }")}
//                }
//            }
//        }
//        val modelClassName = DslResolutionStrategies.resolveNameAndWheretoStrategy(this, C.DEFAULT)
//        dtoModel.modelClassName.setToDataOf(modelClassName)
//        // TODO XXX set "own" properties (and the ones of model { } into dtoModel
//        val modelGatherProperties = DslResolutionStrategies.resolveGatherPropertiesStrategy(this, C.DEFAULT)
//        dtoModel.gatheredFromDslRefs.addAll(modelGatherProperties)
//
//        for (gatherFrom in modelGatherProperties) {
//            val otherDslModelOrModelSubElement = dslCtx.getDslClass(gatherFrom.modelOrModelSubElementRef)
//            when (otherDslModelOrModelSubElement) {
//                is DslModel -> {
//                    for (modelProp in otherDslModelOrModelSubElement.propsImpl.theProps.values) {
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
//        // TODO XXX really gatherProperties and set them into dtoModel
//
//        // TODO XXX Continue here
//    }
//}
//context(DslCtxWrapper)
//@ChassisDslMarker
//class DslTable(
//    simpleName: String,
//    val tableRef: DslRef.table,
//    parent: IDslClass,
//    classModifiersImpl: DslClassModifiersImpl                            = DslClassModifiersImpl(),
//    propsImpl: DslPropsDelegate                                              = DslPropsDelegate(tableRef, this@DslCtxWrapper),
//    nameAndWheretoWithoutModelSubElementsImpl: DslNameAndWheretoOnlyDelegateImpl = this@DslCtxWrapper.dslCtx.ctxObjOrCreate(DslRef.nameAndWhereto(simpleName, tableRef)),
//    gatherPropertiesImpl: DslGatherPropertiesDelegateImpl                = this@DslCtxWrapper.dslCtx.ctxObjOrCreate(DslRef.propertiesOf(simpleName, tableRef)),
//    classModsImpl: DslClassModsDelegateImpl                              = this@DslCtxWrapper.dslCtx.ctxObjOrCreate(DslRef.classMods(simpleName, tableRef)),
//    extendsImpl: DslExtendsDelegateImpl                                  = this@DslCtxWrapper.dslCtx.ctxObjOrCreate(DslRef.extends(simpleName, tableRef)),
//    showcaseImpl: DslShowcaseDelegateImpl                                = this@DslCtxWrapper.dslCtx.ctxObjOrCreate(DslRef.showcase(simpleName, tableRef))
//)
//    : ADslSubElementsOnly(this@DslCtxWrapper, simpleName, tableRef, parent, classModifiersImpl, propsImpl, nameAndWheretoWithoutModelSubElementsImpl, gatherPropertiesImpl, classModsImpl, extendsImpl, showcaseImpl), IDslImplTable
//{
//    init {
//        //nameAndWheretoWithoutModelSubElementsImpl.explicitParent = this
//        //gatherPropertiesImpl.explicitParent = this
//        //classModsImpl.explicitParent = this
//        //extendsImpl.explicitParent = this
//        //showcaseImpl.explicitParent = this
//    }
//    override val modelElement = DslRef.model.MODELELEMENT.TABLE
//
//    fun finish() {
//        TODO("Not yet implemented")
//    }
//}
//context(DslCtxWrapper)
//@ChassisDslMarker
//class DslModel constructor(
//    simpleName: String,
//    modelgroupRef: DslRef.modelgroup,
//    classModifiersImpl: DslClassModifiersImpl                           = DslClassModifiersImpl(),
//    propsImpl: DslPropsDelegate                                         = this@DslCtxWrapper.dslCtx.ctxObjOrCreate(DslRef.properties(simpleName, modelgroupRef)),
//    nameAndWheretoWithSubElements: DslNameAndWheretoWithSubelementsDelegateImpl = this@DslCtxWrapper.dslCtx.ctxObjOrCreate(DslRef.nameAndWhereto(simpleName, modelgroupRef)),
//    gatherPropertiesImpl: DslGatherPropertiesDelegateImpl               = this@DslCtxWrapper.dslCtx.ctxObjOrCreate(DslRef.propertiesOf(simpleName, modelgroupRef)),
//    classModsImpl: DslClassModsDelegateImpl                             = this@DslCtxWrapper.dslCtx.ctxObjOrCreate(DslRef.classMods(simpleName, modelgroupRef)),
//    extendsImpl: DslExtendsDelegateImpl                                 = this@DslCtxWrapper.dslCtx.ctxObjOrCreate(DslRef.extends(simpleName, modelgroupRef)),
//    showcaseImpl: DslShowcaseDelegateImpl                               = this@DslCtxWrapper.dslCtx.ctxObjOrCreate(DslRef.showcase(simpleName, modelgroupRef))
//)
//    : ADslModelOnly(simpleName, modelgroupRef, classModifiersImpl, propsImpl, nameAndWheretoWithSubElements, gatherPropertiesImpl, classModsImpl, extendsImpl, showcaseImpl), IDslImplModel
//{
//    init {
//        // set all delegate's IDslClass.parent to this
//        //nameAndWheretoWithSubElements.explicitParent = this
//        //gatherPropertiesImpl.explicitParent = this
//        //classModsImpl.explicitParent = this
//        //extendsImpl.explicitParent = this
//        //showcaseImpl.explicitParent = this
//    }
//    override val modelElement = DslRef.model.MODELELEMENT.MODEL
//    val dslDtos = mutableMapOf<String, DslDto>()
//    val dslTables = mutableMapOf<String, DslTable>()
//
//    context(DslCtxWrapper)
//    @DslBlockOn(DslDto::class)
//    override fun dto(simpleName: String, dslBlock: IDslApiDto.() -> Unit) {
//        log.info("fun {}(\"{}\") { ... } in PASS {}", object{}.javaClass.enclosingMethod.name, simpleName, dslCtx.currentPASS)
//        when (dslCtx.currentPASS) {
//            dslCtx.PASS_1_BASEMODELS -> {
//                val dtoRef = DslRef.dto(simpleName, modelRef)
//                val dslImpl = dslCtx.put { dslDtos.getOrPut(simpleName) { DslDto(simpleName, dtoRef, this@DslModel) } }
//                dslImpl.apply(dslBlock)
//            }
//            dslCtx.PASS_ERROR -> TODO()
//            dslCtx.PASS_FINISH -> {
//                val dtoRef = DslRef.dto(simpleName, modelRef)
//                val dslImpl = dslCtx.getDslClass(dtoRef) as DslDto
//                dslImpl.apply(dslBlock) // first let all the subtree finish
//                dslImpl.finish(dslCtx)
//            }
//            else -> {
//                val dtoRef = DslRef.dto(simpleName, modelRef)
//                val dslImpl = dslCtx.getDslClass(dtoRef) as DslDto
//                dslImpl.apply(dslBlock)
//            }
//        }
//    }
//
//    context(DslCtxWrapper)
//    @DslBlockOn(DslTable::class)
//    override fun table(simpleName: String, dslBlock: IDslApiTable.() -> Unit) {
//        log.info("fun {}(\"{}\") { ... } in PASS {}", object{}.javaClass.enclosingMethod.name, simpleName, dslCtx.currentPASS)
//        when (dslCtx.currentPASS) {
//            dslCtx.PASS_1_BASEMODELS -> {
//                val tableRef = DslRef.table(simpleName, modelRef)
//                val dslImpl = dslCtx.put { dslTables.getOrPut(simpleName) { DslTable(simpleName, tableRef, this@DslModel) } }
//                dslImpl.apply(dslBlock)
//            }
//            dslCtx.PASS_ERROR -> TODO()
//            dslCtx.PASS_FINISH -> {
//                val tableRef = DslRef.table(simpleName, modelRef)
//                val dslImpl = dslCtx.getDslClass(tableRef) as DslTable
//                dslImpl.apply(dslBlock) // first let all the subtree finish
//                dslImpl.finish()
//            }
//            else -> {
//                val tableRef = DslRef.table(simpleName, modelRef)
//                val dslImpl = dslCtx.getDslClass(tableRef) as DslTable
//                dslImpl.apply(dslBlock)
//            }
//        }
//    }
//}

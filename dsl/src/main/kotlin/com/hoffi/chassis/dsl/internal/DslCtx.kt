package com.hoffi.chassis.dsl.internal

import com.hoffi.chassis.chassismodel.C
import com.hoffi.chassis.chassismodel.dsl.DslCtxException
import com.hoffi.chassis.chassismodel.dsl.DslException
import com.hoffi.chassis.dsl.modelgroup.*
import com.hoffi.chassis.dsl.whens.WhensModelgroup
import com.hoffi.chassis.shared.codegen.GenCtx
import com.hoffi.chassis.shared.dsl.DslDiscriminator
import com.hoffi.chassis.shared.dsl.DslRef
import com.hoffi.chassis.shared.dsl.IDslRef
import com.hoffi.chassis.shared.parsedata.CollectedClassModifiers
import com.hoffi.chassis.shared.parsedata.CollectedExtends
import com.hoffi.chassis.shared.parsedata.CollectedGatherPropertys
import com.hoffi.chassis.shared.parsedata.nameandwhereto.CollectedNameAndWheretos
import com.hoffi.chassis.shared.whens.WhensDslRef
import org.slf4j.LoggerFactory
import kotlin.reflect.full.createType
import kotlin.reflect.full.isSubtypeOf
import kotlin.reflect.full.primaryConstructor

lateinit var globalDslCtx: DslCtx // TODO remove workaround
val dslCtxWrapperFake: DslCtxWrapper // TODO remove workaround
    get() = DslCtxWrapper(globalDslCtx, DslDiscriminator("fake"))
/** using context(DslCtxWrapper) a) to use qualified dslCtx.xxx context functions in context and b) to add context stuff like a DslDiscriminator */
class DslCtxWrapper(val dslCtx: DslCtx, var dslDiscriminator: DslDiscriminator) {
    override fun toString() = "DslCtxWrapper(dslCtx=$dslCtx, dslDiscriminator=$dslDiscriminator)"
    fun withDslDiscriminator(dslDisc: String) = this.also { this.dslDiscriminator = DslDiscriminator(dslDisc) }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is DslCtxWrapper) return false
        return dslCtx == other.dslCtx
    }
    override fun hashCode() = dslCtx.hashCode()
}

class DslCtx private constructor(){
    override fun toString() = "${super.hashCode()} DslCtx of (${if(dslRun.running) "" else "not "}running) in currentPass='${currentPASS}' of DslRun '${dslRun.runIdentifierEgEnvAndTime}'"
    var topLevelDslFunctionName: String = C.NULLSTRING
        set(value) { log.info("||--> of topLeveDslFunctionName {}()", value) ; field = value }
    lateinit var dslRun: DslRun
    val log = LoggerFactory.getLogger(javaClass)

    val genCtx by lazy { GenCtx._internal_create() } // filled to be passed on by @ChassisDslMarker classes finish() methods
    var currentPASS: DSLPASS = DSLPASS.NULL
    // we need Instances of DSLPASS to be able to do when(...) on them
    val PASS_ERROR            = DSLPASS.PASS_ERROR(this)
    val PASS_FINISHGENMODELS  = DSLPASS.PASS_FINISHGENMODELS(this)
    val PASS_FINISH           = DSLPASS.PASS_FINISH(PASS_FINISHGENMODELS, this)
    val PASS_5_REFERENCING    = DSLPASS.PASS_5_REFERENCING(PASS_FINISH, this)
    val PASS_4_PREPREFFING    = DSLPASS.PASS_4_PREPREFFING(PASS_5_REFERENCING, this)
    val PASS_3_ALLMODELS      = DSLPASS.PASS_3_ALLMODELS(PASS_4_PREPREFFING, this)
    val PASS_2_TABLEMODELS    = DSLPASS.PASS_2_TABLEMODELS(PASS_3_ALLMODELS, this)
    val PASS_1_BASEMODELS     = DSLPASS.PASS_1_BASEMODELS(PASS_2_TABLEMODELS, this)
    val firstPass = PASS_1_BASEMODELS
    val PASS_0_CONFIGURE = DSLPASS.PASS_0_CONFIGURE(firstPass, this)
    fun start(): DSLPASS     {
        if (currentPASS != DSLPASS.NULL && currentPASS != PASS_0_CONFIGURE) { throw DslCtxException("DslRun(\"${dslRun.runIdentifierEgEnvAndTime}\")'s DslCtx already started and in PASS '${currentPASS}'. exiting.") }
        currentPASS = firstPass
        return currentPASS.start()
    }
    fun nextPass(): DSLPASS? {
        val nextPass: DSLPASS? = currentPASS.nextPass()
        return if (nextPass != null) {
            currentPASS.finish()
            nextPass.start()
            if (nextPass !is DSLPASS.PASS_FINISH) {
                currentPASS = nextPass; nextPass
            } else {
                if (errors.isEmpty()) {
                    currentPASS = nextPass; nextPass
                } else {
                    nextPass.finish() // was started above -> immediately finish
                    PASS_ERROR.start()
                    currentPASS = PASS_ERROR ; PASS_ERROR
                }
            }
        } else {
            currentPASS.finish()
            null
        }
    }

    val errors = mutableMapOf<String, String>()

    val allCtxObjs: MutableMap<IDslRef, ADslClass> = mutableMapOf()

    /** get named class from Ctx or create and put it into ctx</br>
     *  for this to work you have to assign the result to a typed variable:
     *  val x = ctxObj("targetName") // <-- cannot infer reified type
     *  val x : Target = ctxObj("targetName") // correct */
    context(DslCtxWrapper) // for calling constructor of ADslClass 1st parameter
    inline fun <reified T : ADslClass> ctxObjOrCreate(dslRef: IDslRef): T {
        var theObj: T? = allCtxObjs[dslRef] as T?
        if (theObj == null) {
            theObj = ctxObjCreate(dslRef)
        } else {
            println("operating on already existing ${T::class.simpleName}(name = ${theObj.selfDslRef.simpleName})")
        }
        return theObj
    }
    context(DslCtxWrapper)
    inline fun <reified T : ADslClass> ctxObjCreate(dslRef: IDslRef): T {
        if (allCtxObjs.containsKey(dslRef)) { throw DslCtxException("DslClass '$dslRef' already exists in DslCtx") }
        if (dslRef == IDslRef.NULL) { throw DslCtxException("trying to create DslCtx Object for IDslRef.NULL") }
        // create an instance via reflection (every ADslClass has to have a constructor(simpleName, parentDslRef)
        // TODO construct via 2 param constructor (or give all ADslClass'es some CREATE (static) method??)
        val constr = T::class.primaryConstructor!!
        if (constr.parameters.size < 2 ||
            constr.parameters[0].type.classifier != String::class  || constr.parameters[0].type.isMarkedNullable ||
            ! constr.parameters[1].type.isSubtypeOf(IDslRef::class.createType()) || constr.parameters[1].type.isMarkedNullable
        ) throw DslCtxException("${T::class.simpleName} does not have a constructor(String, IDslRef")
        log.debug("{}.constr({}) in DslCtx.ctxObjCreate(dslRef = '{}')", T::class.simpleName, dslRef::class.simpleName, dslRef)
        // TODO use callBy to enable further constructor Parameters with default values (but doesn't work with context'ual constr yet
        //val constrParamMap: MutableMap<KParameter, Any?> = mutableMapOf(
        //    constr.parameters[0] to "dynamicString"
        //)
        //val theObj: T = constr.callBy(constrParamMap)
        // for ADslDelegateClass'es call ctxObjCreate() with the delegate DslRef
        val theObj: T = constr.call(this@DslCtxWrapper, dslRef.simpleName, dslRef.parentDslRef)
        log.debug("ok.")
        //theObj = T::class.getDeclaredConstructor().newInstance()
        allCtxObjs[dslRef] = theObj
        return theObj
    }
    inline fun <reified T : ADslClass> ctxObj(key: IDslRef): T {
        var theObj: ADslClass? = allCtxObjs[key]
        return theObj as T? ?: throw DslCtxException("no DslClass found in DslCtx for '$key'")
    }
    inline fun <reified T : ADslClass> addToCtx(aDslClass: T): T {
        allCtxObjs[aDslClass.selfDslRef] = aDslClass
        return aDslClass
    }
    inline fun <reified T : ADslClass> ctxObjCreateNonDelegate(aDslClassCreateBlock: () -> T): T {
        val theNonDslDelegateADslClass = aDslClassCreateBlock()
        allCtxObjs[theNonDslDelegateADslClass.selfDslRef] = theNonDslDelegateADslClass
        return theNonDslDelegateADslClass
    }

    //    val allDslClassInstances = mutableMapOf<IDslRef, IDslClass>()
//    fun getDslClass(dslRef: IDslRef): IDslClass = allDslClassInstances[dslRef]!!
//    fun <T: IDslClass> put(createIDslClassBlock: () -> T): T {
//        val theIDslClass = createIDslClassBlock()
//        allDslClassInstances[theIDslClass.selfDslRef] = theIDslClass
//        return theIDslClass
//    }
    // some specialized functions for "toplevel" DslClasses
    private val modelgroups = mutableMapOf<DslRef.modelgroup, DslModelgroup>()
    private val models = mutableMapOf<DslRef.model, DslModel>()
    fun modelgroups() = modelgroups.values
    fun countModelsOfModelgroup(modelgroupRef: DslRef.modelgroup) = models.values.count { it.parentDslRef == modelgroupRef }

    context(DslCtxWrapper)
    fun createModelgroup(simpleName: String): DslModelgroup {
        val modelgroupRef = DslRef.modelgroup(simpleName, dslDiscriminator)
        val dslModelgroup = DslModelgroup(simpleName, modelgroupRef)
        if (allCtxObjs.putIfAbsent(modelgroupRef, dslModelgroup) != null) { throw DslException("Duplicate Definition of '${dslModelgroup.selfDslRef}'") }
        modelgroups[modelgroupRef] = dslModelgroup
        return dslModelgroup
    }
    context(DslCtxWrapper)
    fun createModel(simpleName: String, parentRef: DslRef.modelgroup) = DslModel(simpleName, DslRef.model(simpleName, parentRef)).let { dslModel ->
        if (allCtxObjs.putIfAbsent(dslModel.selfDslRef, dslModel) != null) { throw DslException("Duplicate Definition of '${dslModel.selfDslRef}") }
        models[dslModel.selfDslRef] = dslModel
        /* return */ dslModel
    }
    //operator fun set(modelRef: ModelRef, dslModel: DslModel) { if(!models.containsKey(modelRef)) { models[modelRef] = dslModel } else { throw DslException("Duplicate Definition of $modelRef") }  }

    //fun getModelgroup(dslRefString: String) = getModelgroup(DslRef.ModelgroupRef.from(dslRefString))
    fun getModelgroup(modelgroupRef: DslRef.modelgroup) = modelgroups[modelgroupRef] ?: throw DslCtxException("no ModelgroupRef found for '${modelgroupRef}'")
    fun getModelgroupBySimpleName(modelgroupSimpleName: String): DslModelgroup = modelgroups.values.firstOrNull { it.simpleName == modelgroupSimpleName } ?: throw DslCtxException("no modelgroup(\"$modelgroupSimpleName\") found.")
    //fun getModelElement(dslRefString:String) = getModelElement(DslRef.ModelRef.from(dslRefString))
    fun getModel(modelRef: DslRef.model) = models[modelRef] ?: throw DslCtxException("no ModelRef found for '${modelRef}'")

    fun getAllModelgroups() = modelgroups.values
    fun getAllModels() = models.values

    private val basemodelNameAndWheretos: MutableMap<DslRef.IElementLevel, CollectedNameAndWheretos> = mutableMapOf()
    fun createCollectedBasemodelNameAndWheretos(dslRef: DslRef.IElementLevel): CollectedNameAndWheretos {
        val item = basemodelNameAndWheretos[dslRef]
        return if (item == null) CollectedNameAndWheretos(dslRef, dslRun.runIdentifierEgEnvAndTime).also { basemodelNameAndWheretos[dslRef] = it }
            else throw DslCtxException("${CollectedNameAndWheretos::class.simpleName}('$dslRef') already exists in DslCtx('${dslRun.runIdentifierEgEnvAndTime}')")
    }
    fun cloneOfCollectedBasemodelNameAndWheretos(dslRef: DslRef.IElementLevel) = getCollectedBasemodelNameAndWheretos(dslRef).copyForNewSubelement()
    fun getCollectedBasemodelNameAndWheretos(dslRef: DslRef.IElementLevel): CollectedNameAndWheretos =
        basemodelNameAndWheretos[dslRef] ?: throw DslCtxException("no ${CollectedNameAndWheretos::class.simpleName}('$dslRef') in DslCtx('${dslRun.runIdentifierEgEnvAndTime}')")

    private val basemodelGatheredGatherPropertys: MutableMap<DslRef.IElementLevel, CollectedGatherPropertys> = mutableMapOf()
    fun createCollectedBasemodelGatherPropertys(dslRef: DslRef.IElementLevel): CollectedGatherPropertys {
        val item = basemodelGatheredGatherPropertys[dslRef]
        return if (item == null) CollectedGatherPropertys(dslRef, dslRun.runIdentifierEgEnvAndTime).also { basemodelGatheredGatherPropertys[dslRef] = it }
            else throw DslCtxException("${CollectedGatherPropertys::class.simpleName}('$dslRef') already exists in DslCtx('${dslRun.runIdentifierEgEnvAndTime}')")
    }
    fun cloneOfCollectedBasemodelGatherPropertys(dslRef: DslRef.IElementLevel) = getCollectedBasemodelGatherPropertys(dslRef).copyForNewSubelement()
    fun getCollectedBasemodelGatherPropertys(dslRef: DslRef.IElementLevel): CollectedGatherPropertys =
        basemodelGatheredGatherPropertys[dslRef] ?: throw DslCtxException("no ${CollectedGatherPropertys::class.simpleName}('$dslRef') in DslCtx('${dslRun.runIdentifierEgEnvAndTime}')")
    //fun gatheredGatherPropertys(dslRef: DslRef.IElementLevel): CollectedGatherPropertys =
    //    basemodelGatheredGatherPropertys[dslRef] ?: CollectedGatherPropertys(dslRef, dslRun.runIdentifierEgEnvAndTime).also { basemodelGatheredGatherPropertys[dslRef] = it }

    private val basemodelClassModifiers: MutableMap<DslRef.IElementLevel, CollectedClassModifiers> = mutableMapOf()
    fun createCollectedBasemodelClassModifiers(dslRef: DslRef.IElementLevel): CollectedClassModifiers {
        val item = basemodelClassModifiers[dslRef]
        return if (item == null) CollectedClassModifiers(dslRef, dslRun.runIdentifierEgEnvAndTime).also { basemodelClassModifiers[dslRef] = it }
            else throw DslCtxException("${CollectedClassModifiers::class.simpleName}('$dslRef') already exists in DslCtx('${dslRun.runIdentifierEgEnvAndTime}')")
    }
    fun cloneOfCollectedBasemodelClassModifiers(dslRef: DslRef.IElementLevel) = getCollectedBasemodelClassModifiers(dslRef).copyForNewSubelement()
    fun getCollectedBasemodelClassModifiers(dslRef: DslRef.IElementLevel): CollectedClassModifiers =
        basemodelClassModifiers[dslRef] ?: throw DslCtxException("no ${CollectedClassModifiers::class.simpleName}('$dslRef') in DslCtx('${dslRun.runIdentifierEgEnvAndTime}')")
    //fun gatheredClassModifiers(dslRef: DslRef.IElementLevel): CollectedClassModifiers =
    //    basemodelClassModifiers[dslRef] ?: CollectedClassModifiers(dslRef, dslRun.runIdentifierEgEnvAndTime).also { basemodelClassModifiers[dslRef] = it }

    private val basemodelExtends: MutableMap<DslRef.IElementLevel, CollectedExtends> = mutableMapOf()
    fun createCollectedBasemodelExtends(dslRef: DslRef.IElementLevel): CollectedExtends {
        val item = basemodelExtends[dslRef]
        return if (item == null) CollectedExtends(dslRef, dslRun.runIdentifierEgEnvAndTime).also { basemodelExtends[dslRef] = it }
            else throw DslCtxException("${CollectedExtends::class.simpleName}('$dslRef') already exists in DslCtx('${dslRun.runIdentifierEgEnvAndTime}')")
    }
    fun cloneOfCollectedBasemodelExtends(dslRef: DslRef.IElementLevel) = getCollectedBasemodelExtends(dslRef).copyForNewSubelement()
    fun getCollectedBasemodelExtends(dslRef: DslRef.IElementLevel): CollectedExtends =
        basemodelExtends[dslRef] ?: throw DslCtxException("no ${CollectedExtends::class.simpleName}('$dslRef') in DslCtx('${dslRun.runIdentifierEgEnvAndTime}')")
    //fun gatheredExtends(dslRef: DslRef.IElementLevel): CollectedExtends =
    //    basemodelExtends[dslRef] ?: CollectedExtends(dslRef, dslRun.runIdentifierEgEnvAndTime).also { basemodelExtends[dslRef] = it }

    fun isInterface(dslRef: IDslRef, callerDslClass: ADslClass): Boolean {
        val reffedDslClass = ctxObj<ADslClass>(dslRef) as IDslApiKindClassObjectOrInterface
        val callerExtendsParent: IDslApiKindClassObjectOrInterface = ctxObj(callerDslClass.parentDslRef)
        return WhensModelgroup.whenModelOrModelSubelement(callerExtendsParent as ADslClass,
            isDslModel = {
                WhensDslRef.whenModelOrModelSubelement(dslRef,
                    isModelRef = {
                        // I am a DslModel and I reference a Model
                        // HERE we want to know the kind (interface) of a model AND we do so FROM a model, but we don't know if any subelement might overrule this later on in finish()
                        log.warn("isInterface() reference directly from a model (not a dto/tableFor/...) and referencing also a model (not a dto/tableFor/...) ! caller: '{}' is reffing: '{}'", callerDslClass.selfDslRef, dslRef)
                        if (reffedDslClass.kind == DslClassObjectOrInterface.UNDEFINED) {
                            log.warn("isInterface() AND reffed MODEL has Kind.UNDEFINED")
                        }
                        reffedDslClass.kind == DslClassObjectOrInterface.INTERFACE
                        //throw DslException("isInterface() reference directly from a model (not a dto/tableFor/...) and referencing also a model (not a dto/tableFor/...) ! caller: '${callerDslClass.selfDslRef}' was reffing: '$dslRef'")

                    },
                    isModelSubelementRef = {
                        // I am a DslModel and I reference a ModelSubelement (Dto, Table, ...)
                        if (reffedDslClass.kind != DslClassObjectOrInterface.UNDEFINED) {
                            reffedDslClass.kind == DslClassObjectOrInterface.INTERFACE
                        } else {
                            val reffedDslClassParent: DslModel = ctxObj(dslRef.parentDslRef)
                            reffedDslClassParent.kind == DslClassObjectOrInterface.INTERFACE
                        }
                    }
                ){
                    DslException("model reffing for neither model nor modelSubelement")
                }
            },
            isModelSubelement = {
                WhensDslRef.whenModelOrModelSubelement(dslRef,
                    isModelRef = {
                        // I am a ModelSubelement and I reference a Model
                        val subelementDslClass: IDslApiKindClassObjectOrInterface = WhensDslRef.whenModelSubelement(callerExtendsParent.selfDslRef,
                            isDtoRef = { ctxObj<DslDto>(DslRef.dto(C.DEFAULT, dslRef)) },
                            isDcoRef = { ctxObj<DslDco>(DslRef.dco(C.DEFAULT, dslRef)) },
                            isTableRef = { ctxObj<DslTable>(DslRef.table(C.DEFAULT, dslRef)) }
                        ) {
                            DslException("neither of defined Model Subelements")
                        }
                        if (subelementDslClass.kind != DslClassObjectOrInterface.UNDEFINED) {
                            subelementDslClass.kind == DslClassObjectOrInterface.INTERFACE
                        } else {
                            reffedDslClass.kind == DslClassObjectOrInterface.INTERFACE
                        }
                    },
                    isModelSubelementRef = {
                        // I am a ModelSubelement and I reference a ModelSubelement (Dto, Table, ...)
                        if (reffedDslClass.kind != DslClassObjectOrInterface.UNDEFINED) {
                            reffedDslClass.kind == DslClassObjectOrInterface.INTERFACE
                        } else {
                            val reffedDslClassParent: DslModel = ctxObj(dslRef.parentDslRef)
                            reffedDslClassParent.kind == DslClassObjectOrInterface.INTERFACE
                        }
                    }
                ) {
                    DslException("isInterface() on DslRef which is not a modelelement or modelsubelement not implemented yet!")
                }
            }
        ) {
            DslException("isInterface() from caller which is not a modelelement or modelsubelement not implemented yet!")
        }
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is DslCtx) return false
        return dslRun == other.dslRun
    }
    override fun hashCode(): Int {
        return dslRun.hashCode()
    }
    companion object {
        val NULL = _create(DslRun(C.NULLSTRING))
        fun _create(dslRun: DslRun) = DslCtx().also {it.dslRun = dslRun }
    }
}

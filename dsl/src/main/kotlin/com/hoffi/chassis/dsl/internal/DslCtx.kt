package com.hoffi.chassis.dsl.internal

import com.hoffi.chassis.chassismodel.C
import com.hoffi.chassis.chassismodel.dsl.DslCtxException
import com.hoffi.chassis.chassismodel.dsl.DslException
import com.hoffi.chassis.dsl.modelgroup.DslModel
import com.hoffi.chassis.dsl.modelgroup.DslModelgroup
import com.hoffi.chassis.shared.codegen.CodeGenRun
import com.hoffi.chassis.shared.codegen.GenCtx
import com.hoffi.chassis.shared.dsl.DslDiscriminator
import com.hoffi.chassis.shared.dsl.DslRef
import com.hoffi.chassis.shared.dsl.IDslRef
import org.slf4j.LoggerFactory
import kotlin.reflect.full.createType
import kotlin.reflect.full.isSubtypeOf

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
    override fun toString() = "DslCtx of (${if(dslRun.running) "" else "not "}running) DslRun '${dslRun.runIdentifierEgEnvAndTime}'"
    lateinit var dslRun: DslRun
    val log = LoggerFactory.getLogger(javaClass)

    val genCtx by lazy { GenCtx(CodeGenRun(dslRun.runIdentifierEgEnvAndTime)) } // filled to be passed on by @ChassisDslMarker classes finish() methods
    var currentPASS: DSLPASS = DSLPASS.NULL
    // we need Instances of DSLPASS to be able to do when(...) on them
    val PASS_ERROR =         DSLPASS.PASS_ERROR(this)
    val PASS_FINISH =        DSLPASS.PASS_FINISH(this)
    val PASS_4_REFERENCING = DSLPASS.PASS_4_REFERENCING(PASS_FINISH, this)
    val PASS_3_ALLMODELS =   DSLPASS.PASS_3_ALLMODELS(PASS_4_REFERENCING, this)
    val PASS_2_TABLEMODELS = DSLPASS.PASS_2_TABLEMODELS(PASS_3_ALLMODELS, this)
    val PASS_1_BASEMODELS  = DSLPASS.PASS_1_BASEMODELS(PASS_2_TABLEMODELS, this)
    val firstPass = PASS_1_BASEMODELS
    val PASS_0_CONFIGURE  = DSLPASS.PASS_0_CONFIGURE(firstPass, this)
    fun start(): DSLPASS     {
        if (currentPASS != DSLPASS.NULL) { throw DslCtxException("DslRun(\"${dslRun.runIdentifierEgEnvAndTime}\")'s DslCtx already started and in PASS '${currentPASS}'. exiting.") }
        currentPASS = firstPass
        return currentPASS.start()
    }
    fun nextPass(): DSLPASS? {
        val nextPass: DSLPASS? = currentPASS.nextPass()
        return if (nextPass != null) { currentPASS = nextPass ; nextPass } else null
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
        val constr = T::class.constructors.find {
            it.parameters.size == 2 &&
                    it.parameters[0].type.classifier == String::class  && ! it.parameters[0].type.isMarkedNullable
                    it.parameters[1].type.isSubtypeOf(IDslRef::class.createType()) && ! it.parameters[0].type.isMarkedNullable
        } ?: throw DslCtxException("${T::class.simpleName} does not have a constructor(String, IDslRef")
        log.debug("{}.constr({}) in DslCtx.ctxObjCreate('DslRef.{}')", T::class.simpleName, dslRef::class.simpleName, dslRef)
        val theObj: T = constr.call(this@DslCtxWrapper, dslRef.simpleName, dslRef.parentRef)
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
    fun countModelsOfModelgroup(modelgroupRef: DslRef.modelgroup) = models.values.count { it.selfDslRef.parentRef == modelgroupRef }

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

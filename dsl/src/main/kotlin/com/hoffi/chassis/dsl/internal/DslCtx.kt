package com.hoffi.chassis.dsl.internal

import com.hoffi.chassis.chassismodel.dsl.DslCtxException
import com.hoffi.chassis.dsl.modelgroup.DslModel
import com.hoffi.chassis.dsl.modelgroup.DslModelgroup
import com.hoffi.chassis.shared.dsl.DslRef.DslGroupRefEither.DslModelgroupRef
import com.hoffi.chassis.shared.dsl.DslRef.DslGroupRefEither.DslModelgroupRef.DslElementRefEither.DslModelRef

internal class DslCtx(val dslRun: DslRun){
    var pass: DSLPASS = DSLPASS.NULL

    fun start(): DSLPASS = DSLPASS.initialPass(dslRun).also { this.pass = it }

    private val modelgroups = mutableMapOf<DslModelgroupRef, DslModelgroup>()
    private val models = mutableMapOf<DslModelRef, DslModel>()
    fun countModelsOfModelgroup(modelgroupRef: DslModelgroupRef) = models.values.count { it.parentDslRef == modelgroupRef }

    fun createModelgroup(modelgroupRef: DslModelgroupRef) = DslModelgroup(modelgroupRef, TopLevelDslFunction()).let { modelgroups[modelgroupRef] = it ; it }
    fun createModel(modelRef: DslModelRef, parent: DslModelgroup) = DslModel(modelRef, parent).let { models[modelRef] = it ; it }
    //operator fun set(modelRef: ModelRef, dslModel: DslModel) { if(!models.containsKey(modelRef)) { models[modelRef] = dslModel } else { throw DslException("Duplicate Definition of $modelRef") }  }

    //fun getModelgroup(dslRefString: String) = getModelgroup(DslRef.ModelgroupRef.from(dslRefString))
    fun getModelgroup(modelgroupRef: DslModelgroupRef) = modelgroups[modelgroupRef] ?: throw DslCtxException("no ModelgroupRef found for '${modelgroupRef}'")
    //fun getModelElement(dslRefString:String) = getModelElement(DslRef.ModelRef.from(dslRefString))
    fun getModel(modelRef: DslModelRef) = models[modelRef] ?: throw DslCtxException("no ModelRef found for '${modelRef}'")

    fun getAllModelgroups() = modelgroups.values
    fun getAllModels() = models.values
}

package com.hoffi.chassis.dsl.internal

import com.hoffi.chassis.chassismodel.dsl.DslCtxException
import com.hoffi.chassis.dsl.modelgroup.DslModel
import com.hoffi.chassis.dsl.modelgroup.DslModelgroup
import com.hoffi.chassis.shared.dsl.DslRef

internal class DslCtx(val dslRun: DslRun){
    var pass: DSLPASS = DSLPASS.NULL

    fun start(): DSLPASS = DSLPASS.initialPass(dslRun).also { this.pass = it }

    private val modelgroups = mutableMapOf<DslRef.modelgroup, DslModelgroup>()
    private val models = mutableMapOf<DslRef.model, DslModel>()
    fun countModelsOfModelgroup(modelgroupRef: DslRef.modelgroup) = models.values.count { it.parentDslRef == modelgroupRef }

    fun createModelgroup(modelgroupRef: DslRef.modelgroup) = DslModelgroup(modelgroupRef, TopLevelDslFunction()).let { modelgroups[modelgroupRef] = it ; it }
    fun createModel(simpleName: String, modelRef: DslRef.model, parent: DslModelgroup) = DslModel(simpleName, modelRef, parent).let { models[modelRef] = it ; it }
    //operator fun set(modelRef: ModelRef, dslModel: DslModel) { if(!models.containsKey(modelRef)) { models[modelRef] = dslModel } else { throw DslException("Duplicate Definition of $modelRef") }  }

    //fun getModelgroup(dslRefString: String) = getModelgroup(DslRef.ModelgroupRef.from(dslRefString))
    fun getModelgroup(modelgroupRef: DslRef.modelgroup) = modelgroups[modelgroupRef] ?: throw DslCtxException("no ModelgroupRef found for '${modelgroupRef}'")
    //fun getModelElement(dslRefString:String) = getModelElement(DslRef.ModelRef.from(dslRefString))
    fun getModel(modelRef: DslRef.model) = models[modelRef] ?: throw DslCtxException("no ModelRef found for '${modelRef}'")

    fun getAllModelgroups() = modelgroups.values
    fun getAllModels() = models.values
}

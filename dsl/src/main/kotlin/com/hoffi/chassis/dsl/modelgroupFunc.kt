package com.hoffi.chassis.dsl

import com.hoffi.chassis.dsl.internal.DslCtxWrapper
import com.hoffi.chassis.dsl.internal.DslTopLevel
import com.hoffi.chassis.dsl.internal.globalDslCtx
import com.hoffi.chassis.dsl.modelgroup.DslModelgroup
import com.hoffi.chassis.shared.dsl.DslRef
import org.slf4j.LoggerFactory

context(DslCtxWrapper)
@DslTopLevel
fun modelgroup(simpleName: String, modelgroupBlock: DslModelgroup.() -> Unit) {
    val log = LoggerFactory.getLogger("modelgroupFun")

    log.info("fun {}(\"{}\") { ... } in PASS {}", object{}.javaClass.enclosingMethod.name, simpleName, dslCtx.currentPASS)

    globalDslCtx = dslCtx  // TODO remove workaround
    when (dslCtx.currentPASS) {
        dslCtx.PASS_1_BASEMODELS -> {
            val dslModelgroup = dslCtx.createModelgroup(simpleName)
            dslModelgroup.apply(modelgroupBlock)
        }
        dslCtx.PASS_ERROR -> TODO()
        dslCtx.PASS_FINISH -> {
            /* no special modelgroup finishing at the moment, so the same as else -> { } */
            val modelgroupRef = DslRef.modelgroup(simpleName, dslDiscriminator)
            val dslModelgroup = dslCtx.getModelgroup(modelgroupRef)
            // "prepare" information that might get "inherited" from run|modelgroup|model
            // (like nameAndWhereto, extends, classModifiers or props defined on model for all subelements)
            // and put them into ctx (for cloning in finish() of subelements)
            dslModelgroup.putBasemodelNameAndWheretosInDslCtx(dslCtx)
            dslModelgroup.putBasemodelClassModifiersInDslCtx(dslCtx)
            dslModelgroup.putBasemodelExtendsInDslCtx(dslCtx)
            dslModelgroup.putBasemodelGatherPropertysInDslCtx(dslCtx)
            dslModelgroup.apply(modelgroupBlock)
            dslModelgroup.finish()
        }
        dslCtx.PASS_FINISHGENMODELS -> {
            // we do not decend the DSL tree in this Pass !!!
            val modelgroupRef = DslRef.modelgroup(simpleName, dslDiscriminator)
            val dslModelgroup = dslCtx.getModelgroup(modelgroupRef)
            // after(!) all elements and subelements finish() was completed, do some housekeeping
            // so that codegen doesn't have to do this (all the time)
            dslModelgroup.setModelClassNameOfReffedModelPropertiesAndExtendsModel(dslCtx)
            dslModelgroup.gatherReferencedPropertys(dslCtx)
            dslModelgroup.gatherSuperclassPropertys(dslCtx)
        }
        else -> {
            val modelgroupRef = DslRef.modelgroup(simpleName, dslDiscriminator)
            dslCtx.getModelgroup(modelgroupRef).apply(modelgroupBlock)
        }
    }
}

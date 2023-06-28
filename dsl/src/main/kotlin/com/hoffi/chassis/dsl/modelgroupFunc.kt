package com.hoffi.chassis.dsl

import com.hoffi.chassis.dsl.internal.DslCtxWrapper
import com.hoffi.chassis.dsl.internal.DslInstance
import com.hoffi.chassis.dsl.internal.DslTopLevel
import com.hoffi.chassis.dsl.internal.globalDslCtx
import com.hoffi.chassis.dsl.modelgroup.DslModelgroup
import com.hoffi.chassis.shared.dsl.DslRef
import org.slf4j.LoggerFactory

context(DslCtxWrapper)
@DslTopLevel
fun modelgroup(simpleName: String, modelgroupBlock: DslModelgroup.() -> Unit) {
    val log = LoggerFactory.getLogger("modelgroup")

    log.info("fun {}(\"{}\") { ... } in PASS {}", object{}.javaClass.enclosingMethod.name, simpleName, dslCtx.currentPASS)

    globalDslCtx = dslCtx  // TODO remove workaround
    when (dslCtx.currentPASS) {
        dslCtx.PASS_1_BASEMODELS -> {
            @DslInstance val dslModelgroup = dslCtx.createModelgroup(simpleName)
            dslModelgroup.apply(modelgroupBlock)
        }
        dslCtx.PASS_ERROR -> TODO()
        dslCtx.PASS_FINISH -> {
            /* no special modelgroup finishing at the moment, so the same as else -> { } */
            val modelgroupRef = DslRef.modelgroup(simpleName, dslDiscriminator)
            val dslModelgroup = dslCtx.getModelgroup(modelgroupRef)
            dslModelgroup.prepareNameAndWheretos(dslCtx)
            dslModelgroup.prepareClassModifiers(dslCtx)
            dslModelgroup.prepareExtends(dslCtx)
            dslModelgroup.prepareGatherPropertys(dslCtx)
            dslModelgroup.apply(modelgroupBlock)
            dslModelgroup.finish(dslCtx)
        }
        dslCtx.PASS_INHERITANCE -> {
            val modelgroupRef = DslRef.modelgroup(simpleName, dslDiscriminator)
            val dslModelgroup = dslCtx.getModelgroup(modelgroupRef)
            dslModelgroup.gatherInheritedPropertys(dslCtx)
        }
        else -> {
            val modelgroupRef = DslRef.modelgroup(simpleName, dslDiscriminator)
            dslCtx.getModelgroup(modelgroupRef).apply(modelgroupBlock)
        }
    }
}

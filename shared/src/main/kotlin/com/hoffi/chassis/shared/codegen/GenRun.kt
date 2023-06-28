package com.hoffi.chassis.shared.codegen

import com.hoffi.chassis.chassismodel.C
import com.hoffi.chassis.chassismodel.dsl.IRun

class GenRun(val genCtx: GenCtx, var runIdentifier: String) : IRun {
    var running = false
    val genRun = this

    init {
        genCtx.genRun = this
    }

    context(GenCtxWrapper)
    fun start(runBlock: GenRun.() -> Unit = {}): GenRun {
        this@GenRun.apply(runBlock)
        return this@GenRun
    }

    companion object {
        val NULL = GenRun(GenCtx._internal_create(), C.NULLSTRING)
    }
}

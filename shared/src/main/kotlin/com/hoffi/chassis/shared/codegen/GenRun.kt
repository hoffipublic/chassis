package com.hoffi.chassis.shared.codegen

import com.hoffi.chassis.chassismodel.C
import com.hoffi.chassis.chassismodel.dsl.IRun

class GenRun(val genCtx: GenCtx, var runIdentifier: String) : IRun {
    var running = false
    val genRun = this

    fun start(runBlock: GenRun.() -> Unit = {}): GenRun {
        this.apply(runBlock)
        return this
    }
    companion object {
        val NULL = GenRun(GenCtx._internal_create(), C.NULLSTRING)
    }
}

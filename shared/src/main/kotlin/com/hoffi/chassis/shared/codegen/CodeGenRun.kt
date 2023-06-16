package com.hoffi.chassis.shared.codegen

import com.hoffi.chassis.chassismodel.dsl.IRun

data class CodeGenRun(var runIdentifierEgEnvAndTime: String) : IRun {
    var running = false
    val genCtx = GenCtx(this)

    fun start(runBlock: CodeGenRun.() -> Unit = {}): CodeGenRun {
        this.apply(runBlock)
        return this
    }
    companion object {
        val NULL = CodeGenRun("NULL")
    }
}

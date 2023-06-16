package com.hoffi.chassis

import com.hoffi.chassis.chassismodel.dsl.IRun

data class ChassisRun(var runIdentifierEgEnvAndTime: String) : IRun {
    var running = false

    fun start(runBlock: ChassisRun.() -> Unit = {}): ChassisRun {
        this.apply(runBlock)
        return this
    }
    companion object {
        val NULL = ChassisRun("NULL")
    }
}

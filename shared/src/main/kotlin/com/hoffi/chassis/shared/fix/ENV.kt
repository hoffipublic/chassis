package com.hoffi.chassis.shared.fix

import java.net.InetAddress
import java.time.Instant
import java.time.ZoneId

object ENV {
    enum class ENVS { LOCALDEV, LOCAL, LOCALTEST, INT, INTTEST, PROD }
    val CURRENT_ENV = ENVS.LOCALDEV
    val currentEnv: String
        get() = CURRENT_ENV.toString()
    val generationLocalDateTime: String
        get() = if (CURRENT_ENV != ENVS.PROD) "DEVTIME" else Instant.ofEpochMilli(System.currentTimeMillis()).atZone(
            ZoneId.systemDefault()).toLocalDateTime().toString()
    //        get() = if (CURRENT_ENV != ENVS.PROD) "DEBUG" else Instant.ofEpochMilli(System.currentTimeMillis()).atZone(ZoneId.systemDefault()).toLocalDateTime().toString()
    val hostname: String
        get() = InetAddress.getLocalHost().canonicalHostName

    enum class EXEMODE { DEBUG, LOCAL, INT, PROD }
    val CURRENT_EXEMODE = EXEMODE.PROD
    fun DEBUG(function: () -> Unit) {
        if (CURRENT_EXEMODE == EXEMODE.DEBUG) {
            function()
        }
    }
}

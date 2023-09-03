package com.hoffi.chassis.shared.test

import io.kotest.common.ExperimentalKotest
import io.kotest.core.config.AbstractProjectConfig
import io.kotest.core.config.LogLevel
import io.kotest.core.extensions.Extension
import io.kotest.core.test.TestCase
import io.kotest.engine.test.logging.LogEntry
import io.kotest.engine.test.logging.LogExtension

class KotestProjectConfig : AbstractProjectConfig() {
    override val globalAssertSoftly = true
    override val logLevel = LogLevel.Info
    @OptIn(ExperimentalKotest::class)
    override fun extensions(): List<Extension> = listOf(
        object : LogExtension {
            override suspend fun handleLogs(testCase: TestCase, logs: List<LogEntry>) {
                logs.forEach { println(it.level.name + " - " + it.message) }
            }
        }
    )

    override suspend fun beforeProject() {
        println("kotests: (beforeProject() of ${this::class.simpleName})")
    }

    override suspend fun afterProject() {
        println("kotests finished. (afterProject() of ${this::class.simpleName})")
    }
}

package com.hoffi.chassis.shared.test

import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.koin.KoinExtension
import io.kotest.koin.KoinLifecycleMode
import org.koin.test.KoinTest

@Suppress("UNCHECKED_CAST")
abstract class KoinBddSpec(val koinModules: List<org.koin.core.module.Module>, behaviorSpec: KoinBddSpec.() -> Unit): KoinTest, BehaviorSpec(behaviorSpec as BehaviorSpec.() -> Unit) {
    constructor(vararg koinModules: org.koin.core.module.Module, behaviorSpec: KoinBddSpec.() -> Unit) : this(koinModules.asList(), behaviorSpec)
    override fun extensions() = koinModules.map { KoinExtension(module = it, mockProvider = null, mode = KoinLifecycleMode.Root) }
}

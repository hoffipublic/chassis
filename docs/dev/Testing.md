---
layout: page
title: Chassis Regression Testing
subtitle: implementing new features and refactoring
menubar: data_menu_chassis
toc: true
show_sidebar: false
hero_image: ../assets/Chassis.png
---
# Regression Testing

Chassis chose [Kotest](https://kotest.io/){:target="_blank"} as TestFramework, but ordinary JUnit5 Tests also work.

(Chassis is not really using DI (Dependency Injection) by now, but evaluated it using [Koin](https://insert-koin.io/){:target="_blank"}.<br/>
Despite the Code atm not using DI extensively (as passing a Context with kotlin `context(CtxWrapper)` proved kind of enough by now)<br/>
Chassis is prepared to use Koin DI in the future.

Especially the Testsuite is prepared to use Kotest with Koin in the [Behaviour Driven Style](https://kotest.io/docs/5.6/framework/testing-styles.html#behavior-spec){:target="_blank"}.

## Shared gradle `TestFixure`s

For BDD Kotests to be more convenient the chassis `/shared/build.gradle.kts` provides [TestFixtures](https://docs.gradle.org/current/userguide/java_testing.html#sec:java_test_fixtures){:target="_blank"}
in `shared/src/testFixtures/kotlin/com/hoffi/chassis/shared/test`.

Namely:

```kotlin
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
```

And the `KotestProjectConfig` [see Kotest Project Level Config](https://kotest.io/docs/framework/project-config.html){:target="_blank"}

```kotlin
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
```

Gradle subProjects use these by declaring a special `dependencies { ... }` dependency:

```kotlin
dependencies {
    testFixturesImplementation(libs.bundles.testJunitKotestKoin)
}
```

`buildLogic/libs.versions.toml`:

```toml
[libraries]
kointest = { module = "io.insert-koin:koin-test", version.ref = "kointest" }
kotest-assertions-core = { module = "io.kotest:kotest-assertions-core", version.ref = "kotest" }
kotest-extensions-koin = { module = "io.kotest.extensions:kotest-extensions-koin", version.ref = "kotest-extensions-koin" }
kotest-framework-dataset = { module = "io.kotest:kotest-framework-datatest", version.ref = "kotest" }
kotest-framework-engine = { module = "io.kotest:kotest-framework-engine", version.ref = "kotest" }
kotest-runner-junit5 = { module = "io.kotest:kotest-runner-junit5", version.ref = "kotest" }

[bundles]
testJunitKotestKoin = [
    "kointest",
    "kotest-assertions-core",
    "kotest-extensions-koin",
    "kotest-framework-dataset",
    "kotest-framework-engine",
]
```

*Unfortunately I did not (yet) find a way to use* `src/test/resources/kotest.properties` from the shared project to the other subprojects)<br/>
***ergo the `resources/kotest.properties` for testing is unix softlinked in all subprojects!!!***

### Kotest with Koin DI (usaging of shared TestFixture's KoinBddSpec)

The `abstract class KoinBddSpec` above enables you to write `Kotest Koin BDD Specs` with minimal boilerplate code on using Koin DI modules:

```kotlin
import io.kotest.common.ExperimentalKotest
import io.kotest.engine.test.logging.info
import io.kotest.matchers.string.shouldEndWith
import io.kotest.matchers.string.shouldNotEndWith
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import org.koin.core.module.dsl.bind
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.module
import org.koin.test.inject

// Kotest Koin BDD Test
class SmokeKoinBddSpec : KoinBddSpec(dummyModule, behaviorSpec = {
    Given("an injected Dummy") {
        val dummy: Dummy by inject()
        When("calling f() on injected Dummy") {
            info { "${SmokeKoinBddSpec::class.simpleName}: When(1)"}
            val result = dummy.f()
            Then("result should be 1") {
                result shouldEndWith "with DummyDep(dp='depDummy', depDummy(1))"
            }
            Then("result should not be 2") {
                result shouldNotEndWith  "with DummyDep(dp='depDummy', depDummy(2))"
            }
        }
        When("calling f() again on injected Dummy") {
            info { "${SmokeKoinBddSpec::class.simpleName}: When(2)"}
            val result = dummy.f()
            Then("result should be 2") {
                result shouldEndWith "with DummyDep(dp='depDummy', depDummy(2))"
            }
        }
    }
})

// ===============================================================
// Fake stuff to demonstrate Kotest with Koin DI in BDD Spec Style
// ===============================================================

/** Koin dummy DI module */
val dummyModule = module {
    factory { params -> DoIt(get(), params.get()) }
    singleOf(::Dummy) { bind<IDummy>() }
    singleOf(::DummyDep)
}

interface IDummy {
    val p: String
    fun f(): String
}
class Dummy (val dummyDep: DummyDep): IDummy {
    //actual val dummyDep = dummyDep
    override val p: String = "JVM"
    override fun f(): String {
        return "$p with $dummyDep"
    }
}
class DummyDep {
    override fun toString() = "DummyDep(dp='$dp', ${f()})"
    val dp: String = "depDummy"
    fun f() = "$dp(${count++})"

    companion object {
        var count = 1L
    }
}

data class Par(val par: String)

class DoIt(val dummy: Dummy, val par: Par) : KoinComponent {
    private val otherDummy: Dummy by inject()
    fun doIt() {
        println("par='${par}' ${dummy.f()}")
        println("par='${par}' ${otherDummy.f()}")
    }
}
```

## Caveat Kotest does not find Kotests

You have to install the intellij [Kotest plugin](https://plugins.jetbrains.com/plugin/14080-kotest){:target="_blank"}

for gradle to find Kotests you have to explicitly `useJunitPlatform()` in your `build.gradle.kts` tests configuration,<br/>
otherwise it will only find `JUnit` Tests. (`> No tests found`)

```kotlin
kotlin {
    jvmToolchain(BuildLogicGlobal.jdkVersion)
    tasks.withType<Test>().configureEach {
        // since gradle 8.x JunitPlatform is the default and must not be configured explicitly anymore
        useJUnitPlatform() // but if missing this line, kotlin kotests won't be found and run TODO
        failFast = false
    }
}
```

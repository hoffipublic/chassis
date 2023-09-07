package com.hoffi.chassis.codegen.smoke

//import org.koin.core.component.inject
import com.hoffi.chassis.shared.test.KoinBddSpec
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

val dummyModule = module {
    factory { params -> DoIt(get(), params.get()) }
    singleOf(::Dummy) { bind<IDummy>() }
    singleOf(::DummyDep)
}

data class Par(val par: String)

class DoIt(val dummy: Dummy, val par: Par) : KoinComponent {
    private val otherDummy: Dummy by inject()
    fun doIt() {
        println("par='${par}' ${dummy.f()}")
        println("par='${par}' ${otherDummy.f()}")
    }
}

@OptIn(ExperimentalKotest::class)
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

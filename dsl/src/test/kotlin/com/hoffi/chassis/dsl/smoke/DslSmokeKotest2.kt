package com.hoffi.chassis.dsl.smoke

import io.kotest.core.spec.style.ShouldSpec
import io.kotest.matchers.shouldBe

class DslSmokeKotest2 : ShouldSpec() {
    init {
        should("return the length of the string") { //.config(invocations = 10, threads = 2) {
            "sammy".length shouldBe  5
            "".length shouldBe 0
        }
    }
}

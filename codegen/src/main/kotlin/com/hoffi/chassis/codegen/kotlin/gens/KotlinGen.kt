package com.hoffi.chassis.codegen.kotlin.gens

import com.hoffi.chassis.codegen.kotlin.GenCtxWrapper
import com.hoffi.chassis.shared.parsedata.ModelClassDataFromDsl

sealed interface KotlinGen {
    val modelClassDataFromDsl: ModelClassDataFromDsl
    context(GenCtxWrapper)
    class KotlinGenDto(modelClassDataFromDsl: ModelClassDataFromDsl) : KotlinGen,
        KotlinGenClassNonPersistant(modelClassDataFromDsl)
    context(GenCtxWrapper)
    class KotlinGenDco(modelClassDataFromDsl: ModelClassDataFromDsl) : KotlinGen,
        KotlinGenClassNonPersistant(modelClassDataFromDsl)
    context(GenCtxWrapper)
    class KotlinGenTable(modelClassDataFromDsl: ModelClassDataFromDsl) : KotlinGen,
        KotlinGenExposedTable(modelClassDataFromDsl)
}

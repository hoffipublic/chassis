package com.hoffi.chassis.shared.codegen

import com.hoffi.chassis.shared.dsl.DslRef
import com.hoffi.chassis.shared.parsedata.EitherModel

class GenCtx private constructor() {
    lateinit var genRun: GenRun
    val genModels = mutableMapOf<DslRef.IModelSubelement, EitherModel>()

    companion object {
        val NULL = GenCtx()
        fun _create(genRun: GenRun): GenCtx = GenCtx().also { it.genRun = genRun }
        fun _internal_create(): GenCtx = GenCtx()
    }
}

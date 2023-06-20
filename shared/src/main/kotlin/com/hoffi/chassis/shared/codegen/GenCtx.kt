package com.hoffi.chassis.shared.codegen

import com.hoffi.chassis.shared.dsl.DslRef
import com.hoffi.chassis.shared.parsedata.EitherModel

class GenCtx(val dslRun: CodeGenRun) {
    val genModels = mutableMapOf<DslRef.IModelSubelement, EitherModel>()
}

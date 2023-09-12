package com.hoffi.chassis.codegen.kotlin.gens.sql

import com.hoffi.chassis.codegen.kotlin.GenCtxWrapper
import com.hoffi.chassis.shared.parsedata.GenModel

context(GenCtxWrapper)
class ExposedBaseSqlStatements constructor(val genModelFromDsl: GenModel.TableModelFromDsl) {
    fun build() {
        TODO("Not yet implemented")
        for(fk in kotlinGenCtx.allFKs()) {
            println("HERE NOT IMPLEMENTED")
        }

    }
}

package com.hoffi.chassis.codegen.kotlin

import com.hoffi.chassis.chassismodel.C
import com.hoffi.chassis.chassismodel.dsl.GenException
import com.hoffi.chassis.shared.codegen.GenCtx
import com.hoffi.chassis.shared.dsl.DslRef
import com.hoffi.chassis.shared.parsedata.GenModel
import com.hoffi.chassis.shared.parsedata.ModelClassDataFromDsl
import com.hoffi.chassis.shared.shared.reffing.MODELREFENUM
import com.squareup.kotlinpoet.TypeName

object GenDslRefHelpers {
    fun tableClassName(tableGenModelFromDsl: GenModel, genCtx: GenCtx) : TypeName {
        if (tableGenModelFromDsl.modelSubElRef.parentDslRef !is DslRef.IElementLevel) throw GenException("no IElementLevel given")
        val swappedGenModel = genCtx.genModelFromDsl(DslRef.table(C.DEFAULT, tableGenModelFromDsl.modelSubElRef.parentDslRef))
        return swappedGenModel.poetType
    }
    context(GenCtxWrapper)
    fun nonpersistentClassName(modelClassDataFromDsl: ModelClassDataFromDsl) : TypeName {
        if (modelClassDataFromDsl.modelSubElRef.parentDslRef !is DslRef.IElementLevel) throw GenException("no IElementLevel given")
        val swappedGenModel = when (modelClassDataFromDsl.tableFor) {
            MODELREFENUM.DTO -> genCtx.genModelFromDsl(DslRef.dto(C.DEFAULT, modelClassDataFromDsl.modelSubElRef.parentDslRef))
            MODELREFENUM.DCO -> genCtx.genModelFromDsl(DslRef.dco(C.DEFAULT, modelClassDataFromDsl.modelSubElRef.parentDslRef))
            MODELREFENUM.TABLE -> throw GenException("forTable should not be called with MODELREFENUM.TABLE")
            MODELREFENUM.MODEL -> throw GenException("forTable should not be called with MODELREFENUM.MODEL")
            null -> throw GenException("has a 'null' forTable MODELREFENUM")
        }
        return swappedGenModel.poetType
    }


}

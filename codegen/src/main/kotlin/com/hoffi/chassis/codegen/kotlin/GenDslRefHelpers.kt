package com.hoffi.chassis.codegen.kotlin

import com.hoffi.chassis.chassismodel.C
import com.hoffi.chassis.chassismodel.dsl.GenException
import com.hoffi.chassis.shared.codegen.GenCtx
import com.hoffi.chassis.shared.dsl.DslRef
import com.hoffi.chassis.shared.parsedata.GenModel
import com.hoffi.chassis.shared.parsedata.ModelClassData
import com.squareup.kotlinpoet.TypeName

object GenDslRefHelpers {
    fun tableClassName(tableGenModel: GenModel, genCtx: GenCtx) : TypeName {
        if (tableGenModel.modelSubElRef.parentDslRef !is DslRef.IElementLevel) throw GenException("no IElementLEvel given")
        val swappedGenModel = genCtx.genModel(DslRef.table(C.DEFAULT, tableGenModel.modelSubElRef.parentDslRef))
        return swappedGenModel.poetType
    }
    fun dtoClassName(dtoGenModel: ModelClassData, genCtx: GenCtx) : TypeName {
        if (dtoGenModel.modelSubElRef.parentDslRef !is DslRef.IElementLevel) throw GenException("no IElementLEvel given")
        val swappedGenModel = genCtx.genModel(DslRef.dto(C.DEFAULT, dtoGenModel.modelSubElRef.parentDslRef))
        return swappedGenModel.poetType
    }


}

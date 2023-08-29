package com.hoffi.chassis.codegen.kotlin

import com.hoffi.chassis.chassismodel.C
import com.hoffi.chassis.chassismodel.dsl.GenException
import com.hoffi.chassis.shared.dsl.DslRef
import com.hoffi.chassis.shared.dsl.IDslRef
import com.hoffi.chassis.shared.shared.CrudData
import com.hoffi.chassis.shared.shared.reffing.MODELREFENUM
import com.squareup.kotlinpoet.ClassName

object GenClassNames {
    context(GenCtxWrapper)
    fun fillerFor(subelDslRef: IDslRef, modelrefenum: MODELREFENUM): ClassName {
        val swappedDslRef = when (modelrefenum) {
            MODELREFENUM.MODEL -> throw GenException("MODELs do not have fillers themselves")
            MODELREFENUM.DTO -> DslRef.dto(C.DEFAULT, subelDslRef.parentDslRef)
            MODELREFENUM.TABLE -> DslRef.table(C.DEFAULT, subelDslRef.parentDslRef)
        }
        val swappedGenModel = genCtx.genModel(swappedDslRef)
        return ClassName(swappedGenModel.poetType.packageName + ".filler", "Filler" + swappedGenModel.poetType.simpleName)
    }
    context(GenCtxWrapper)
    fun crudFor(subelDslRef: IDslRef, crud: CrudData.CRUD): ClassName {
        // TODO remove sentinel?
        if (subelDslRef !is DslRef.ISubElementLevel) throw GenException("targetDslRef for propCrud($subelDslRef) always should be a (model) subelement (DTO, TABLE, ...)")
        val swappedDslRef = DslRef.table(C.DEFAULT, subelDslRef.parentDslRef)
        val swappedGenModel = genCtx.genModel(swappedDslRef)
        return ClassName(swappedGenModel.poetType.packageName + ".sql", swappedGenModel.modelClassName.crudBasePoetTypeForAllCruds.simpleName + crud.toString())
    }

}

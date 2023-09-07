package com.hoffi.chassis.codegen.kotlin

import com.hoffi.chassis.chassismodel.C
import com.hoffi.chassis.chassismodel.Cap
import com.hoffi.chassis.chassismodel.RuntimeDefaults
import com.hoffi.chassis.chassismodel.decap
import com.hoffi.chassis.chassismodel.typ.COLLECTIONTYP
import com.hoffi.chassis.shared.parsedata.nameandwhereto.ModelClassName
import com.hoffi.chassis.shared.shared.AHasCopyBoundrysData
import com.hoffi.chassis.shared.shared.FK

object GenNaming {
    context(GenCtxWrapper)
    fun fkPropVarNameUUID(fk: FK): String =
        when (fk.COLLECTIONTYP) {
            is COLLECTIONTYP.NONE -> fk.toProp.name(postfix = RuntimeDefaults.UUID_PROPNAME)
            is COLLECTIONTYP.LIST, is COLLECTIONTYP.SET, is COLLECTIONTYP.COLLECTION, is COLLECTIONTYP.ITERABLE -> {
                val toTableKotlinClassTable = kotlinGenCtx.kotlinGenClass(fk.toTableRef)
                "${toTableKotlinClassTable.modelClassDataFromDsl.asVarNameWithoutPostfix}${fk.toProp.name(postfix = RuntimeDefaults.UUID_PROPNAME).Cap()}"
            }
        }
    context(GenCtxWrapper)
    fun fkPropColumnNameUUID(fk: FK): String =
        when (fk.COLLECTIONTYP) {
            is COLLECTIONTYP.NONE -> fk.toProp.name(postfix = RuntimeDefaults.UUID_PROPNAME)
            is COLLECTIONTYP.LIST, is COLLECTIONTYP.SET, is COLLECTIONTYP.COLLECTION, is COLLECTIONTYP.ITERABLE -> {
                val toTableKotlinClassTable = kotlinGenCtx.kotlinGenClass(fk.toTableRef)
                toTableKotlinClassTable.modelClassDataFromDsl.modelClassName.tableNameStrategy.nameOf(toTableKotlinClassTable.modelClassDataFromDsl.tableName, postfix = fk.toProp.columnName())
            }
        }
    context(GenCtxWrapper)
    fun fkPropVarName(fk: FK): String =
        when (fk.COLLECTIONTYP) {
            is COLLECTIONTYP.NONE -> fk.toProp.name(postfix = RuntimeDefaults.UUID_PROPNAME)
            is COLLECTIONTYP.LIST, is COLLECTIONTYP.SET, is COLLECTIONTYP.COLLECTION, is COLLECTIONTYP.ITERABLE -> {
                val toTableKotlinClassTable = kotlinGenCtx.kotlinGenClass(fk.toTableRef)
                "${fk.toProp.name()}${toTableKotlinClassTable.modelClassDataFromDsl.asVarNameWithoutPostfix.Cap()}"
            }
        }
    context(GenCtxWrapper)
    fun fkPropColumnName(fk: FK): String =
        when (fk.COLLECTIONTYP) {
            is COLLECTIONTYP.NONE -> fk.toProp.name(postfix = RuntimeDefaults.UUID_PROPNAME)
            is COLLECTIONTYP.LIST, is COLLECTIONTYP.SET, is COLLECTIONTYP.COLLECTION, is COLLECTIONTYP.ITERABLE -> {
                val toTableKotlinClassTable = kotlinGenCtx.kotlinGenClass(fk.toTableRef)
                toTableKotlinClassTable.modelClassDataFromDsl.modelClassName.tableNameStrategy.nameOf(toTableKotlinClassTable.modelClassDataFromDsl.tableName, postfix = fk.toProp.columnName())
            }
        }
    fun createFromTableFunName(aHasCopyBoundrysData: AHasCopyBoundrysData, modelClassName: ModelClassName) =
        if (aHasCopyBoundrysData.businessName == C.DEFAULT)
            modelClassName.asVarName
        else
            aHasCopyBoundrysData.businessName.decap() + modelClassName.asVarName.Cap()
}

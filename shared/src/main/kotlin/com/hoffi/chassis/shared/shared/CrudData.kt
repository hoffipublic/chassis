package com.hoffi.chassis.shared.shared

import com.hoffi.chassis.shared.dsl.DslRef
import com.hoffi.chassis.shared.dsl.IDslRef

class CrudData(businessName: String, targetDslRef: IDslRef, sourceDslRef: IDslRef, val crud: CRUD)
    : AHasCopyBoundrysData(businessName, targetDslRef, sourceDslRef)
{
    override fun toString() = "${this::class.simpleName}('$businessName', target: '${targetDslRef.refList.takeLast(2).joinToString(
        DslRef.ATOMSEP)}', source: '${sourceDslRef.refList.takeLast(2).joinToString(DslRef.ATOMSEP)}', " +
            theCopyBoundrys.values.joinToString("") { it.toString() }.ifBlank { "NONE" } + ")"

    enum class CRUD { CREATE, READ, UPDATE, DELETE }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is CrudData) return false
        if (businessName != other.businessName) return false
        if (targetDslRef != other.targetDslRef) return false
        if (sourceDslRef != other.sourceDslRef) return false
        if (crud != other.crud) return false
        return true
    }
    override fun hashCode(): Int {
        var result = businessName.hashCode()
        result = 31 * result + targetDslRef.hashCode()
        result = 31 * result + sourceDslRef.hashCode()
        result = 31 * result + crud.hashCode()
        return result
    }
}

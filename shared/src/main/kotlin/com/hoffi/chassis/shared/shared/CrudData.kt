package com.hoffi.chassis.shared.shared

import com.hoffi.chassis.shared.dsl.IDslRef

open class CrudData(businessName: String, targetDslRef: IDslRef, sourceDslRef: IDslRef, val crud: CRUD)
    : AHasCopyBoundrysData(businessName, targetDslRef, sourceDslRef)
{
    override fun toString() = "Crud('$businessName', ${String.format("%-6s", crud)}, '${targetDslRef.toString(2)}' <-- '${sourceDslRef.toString(2)}', " +
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

class SynthCrudData private constructor(businessName: String, targetDslRef: IDslRef, sourceDslRef: IDslRef, crud: CRUD, val via: String)
    : CrudData(businessName, targetDslRef, sourceDslRef, crud) {
    override fun toString() = "Synth${super.toString()}->\"$via\""
    companion object {
        fun create(targetDslRef: IDslRef, sourceDslRef: IDslRef, originalCrud: CrudData, via: String): SynthCrudData {
            val synthCrudData = SynthCrudData(originalCrud.businessName, targetDslRef, sourceDslRef, originalCrud.crud, via)
            synthCrudData.theCopyBoundrys.putAll(originalCrud.theCopyBoundrys)
            return synthCrudData
        }
    }
}

package com.hoffi.chassis.shared.shared

import com.hoffi.chassis.shared.dsl.DslRef
import com.hoffi.chassis.shared.dsl.IDslRef

open class FillerData(businessName: String, targetDslRef: IDslRef, sourceDslRef: IDslRef)
    : AHasCopyBoundrysData(businessName, targetDslRef, sourceDslRef) {
    override fun toString() = "${this::class.simpleName}('$businessName', target: '${targetDslRef.refList.takeLast(2).joinToString(DslRef.ATOMSEP)}', source: '${sourceDslRef.refList.takeLast(2).joinToString(DslRef.ATOMSEP)}', " +
            theCopyBoundrys.values.joinToString("") { it.toString() }.ifBlank { "NONE" } + ")"

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is FillerData) return false
        if (businessName != other.businessName) return false
        if (targetDslRef != other.targetDslRef) return false
        return sourceDslRef == other.sourceDslRef
    }
    override fun hashCode(): Int {
        var result = businessName.hashCode()
        result = 31 * result + targetDslRef.hashCode()
        result = 31 * result + sourceDslRef.hashCode()
        return result
    }
}
class SynthFillerData(businessName: String, targetDslRef: IDslRef, sourceDslRef: IDslRef, val via: String)
    : FillerData(businessName, targetDslRef, sourceDslRef) {
    override fun toString() = "${super.toString()}->\"$via\""
}

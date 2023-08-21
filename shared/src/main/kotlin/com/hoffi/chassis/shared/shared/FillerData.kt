package com.hoffi.chassis.shared.shared

import com.hoffi.chassis.shared.dsl.IDslRef

open class FillerData(businessName: String, targetDslRef: IDslRef, sourceDslRef: IDslRef)
    : AHasCopyBoundrysData(businessName, targetDslRef, sourceDslRef) {
    override fun toString() = "Filler('$businessName', '${targetDslRef.toString(2)}' <-- '${sourceDslRef.toString(2)}', " +
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
class SynthFillerData private constructor(businessName: String, targetDslRef: IDslRef, sourceDslRef: IDslRef, val via: String)
    : FillerData(businessName, targetDslRef, sourceDslRef) {
    override fun toString() = "Synth${super.toString()}->\"$via\""
    companion object {
        fun create(targetDslRef: IDslRef, sourceDslRef: IDslRef, originalFiller: FillerData, via: String): SynthFillerData {
            val synthFillerData = SynthFillerData(originalFiller.businessName, targetDslRef, sourceDslRef, via)
            synthFillerData.theCopyBoundrys.putAll(originalFiller.theCopyBoundrys)
            return synthFillerData
        }
        fun create(targetDslRef: IDslRef, sourceDslRef: IDslRef, originalCrud: CrudData, via: String): SynthFillerData {
            val synthFillerData = SynthFillerData(originalCrud.businessName, targetDslRef, sourceDslRef, via)
            synthFillerData.theCopyBoundrys.putAll(originalCrud.theCopyBoundrys)
            return synthFillerData
        }
    }
}

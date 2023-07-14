package com.hoffi.chassis.shared.shared

import com.hoffi.chassis.shared.dsl.DslRef
import com.hoffi.chassis.shared.dsl.IDslRef

//enum class SYNTHETIK { REAL, SYNTHETIK, SEMI}

open class FillerData constructor(val targetDslRef: IDslRef, val sourceDslRef: IDslRef, val theCopyBoundrys: MutableSet<CopyBoundry> = mutableSetOf(), funcNamePostfix: String = "") {
    //var isSynthetik: SYNTHETIK = SYNTHETIK.REAL
    override fun toString() = "${this::class.simpleName}(target: '${targetDslRef.refList.takeLast(2).joinToString(DslRef.ATOMSEP)}', source: '${sourceDslRef.refList.takeLast(2).joinToString(DslRef.ATOMSEP)}')"
    enum class COPYTYPE {IGNORE, INSTANCE, NEW, DEEP, DEEPNEW }
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is FillerData) return false
        if (targetDslRef != other.targetDslRef) return false
        if (sourceDslRef != other.sourceDslRef) return false
        return true
    }
    override fun hashCode(): Int {
        var result = targetDslRef.hashCode()
        result = 31 * result + sourceDslRef.hashCode()
        return result
    }
}
class SynthFillerData(targetDslRef: IDslRef, sourceDslRef: IDslRef, theCopyBoundrys: MutableSet<CopyBoundry> = mutableSetOf(), funcNamePostfix: String = "", val via: String)
    : FillerData(targetDslRef, sourceDslRef, theCopyBoundrys) {
    override fun toString() = "${super.toString()}->\"$via\""
}

data class CopyBoundry(val dslRef: IDslRef, val copyType: FillerData.COPYTYPE) {
    companion object {
        val NONE = CopyBoundry(IDslRef.NULL, FillerData.COPYTYPE.DEEP)
    }
}

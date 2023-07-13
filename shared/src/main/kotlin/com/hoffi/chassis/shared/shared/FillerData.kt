package com.hoffi.chassis.shared.shared

import com.hoffi.chassis.shared.dsl.DslRef
import com.hoffi.chassis.shared.dsl.IDslRef

class FillerData(val toDslRef: IDslRef, val fromDslRef: IDslRef, val theCopyBoundrys: MutableSet<CopyBoundry> = mutableSetOf()) {
    override fun toString() = "${this::class.simpleName}(to: '${toDslRef.refList.takeLast(2).joinToString(DslRef.ATOMSEP)}', from: '${fromDslRef.refList.takeLast(2).joinToString(DslRef.ATOMSEP)}')"
    enum class COPYTYPE {IGNORE, INSTANCE, NEW, DEEP, DEEPNEW }
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is FillerData) return false
        if (toDslRef != other.toDslRef) return false
        if (fromDslRef != other.fromDslRef) return false
        return true
    }
    override fun hashCode(): Int {
        var result = toDslRef.hashCode()
        result = 31 * result + fromDslRef.hashCode()
        return result
    }
}

data class CopyBoundry(val dslRef: IDslRef, val copyType: FillerData.COPYTYPE) {
    companion object {
        val NONE = CopyBoundry(IDslRef.NULL, FillerData.COPYTYPE.DEEP)
    }
}

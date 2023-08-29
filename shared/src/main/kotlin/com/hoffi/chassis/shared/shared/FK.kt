package com.hoffi.chassis.shared.shared

import com.hoffi.chassis.chassismodel.typ.COLLECTIONTYP
import com.hoffi.chassis.shared.dsl.DslRef
import com.hoffi.chassis.shared.parsedata.Property

class FK(
    val fromTableRef: DslRef.IModelSubelement,
    val toTableRef: DslRef.IModelSubelement,
    var toProp: Property,
    val COLLECTIONTYP: COLLECTIONTYP,
) : Comparable<FK> {
    override fun toString() = "FK($COLLECTIONTYP prop='${toProp.name()}', '${toTableRef.toString(2)}' <-- '${fromTableRef.toString(2)}'})"
    override fun compareTo(other: FK): Int = toProp.dslPropName.compareTo(other.toProp.dslPropName)

    //val toPropElementRef = DslRef.groupElementAndSubelementLevelDslRef(toProp.propRef).second
    val toPropSubelementRef = toProp.propRef.parentDslRef
    val toPropElementRef = toProp.propRef.parentDslRef.parentDslRef

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is FK) return false
        if (fromTableRef != other.fromTableRef) return false
        if (toTableRef != other.toTableRef) return false
        if (toProp != other.toProp) return false
        if (COLLECTIONTYP != other.COLLECTIONTYP) return false
        return true
    }
    override fun hashCode(): Int {
        var result = fromTableRef.hashCode()
        result = 31 * result + toTableRef.hashCode()
        result = 31 * result + toProp.hashCode()
        result = 31 * result + COLLECTIONTYP.hashCode()
        return result
    }
}

package com.hoffi.chassis.shared.shared

import com.hoffi.chassis.shared.dsl.DslRef

enum class GatherPropertiesEnum {
    NONE,
    PROPERTIES_ONLY_DIRECT_ONES,
    PROPERTIES_AND_SUPERCLASS_PROPERTIES,
    SUPERCLASS_PROPERTIES_ONLY
}

class GatherPropertys(
    val modelOrModelSubelementRefOriginal: DslRef.IModelOrModelSubelement,
    val gatherPropertiesEnum: GatherPropertiesEnum = GatherPropertiesEnum.PROPERTIES_AND_SUPERCLASS_PROPERTIES
) {
    override fun toString() = "$gatherPropertiesEnum of $modelSubelementRef"

    val modelSubelementRef: DslRef.IModelSubelement
        get() = modelSubelementRefExpanded ?: modelOrModelSubelementRefOriginal as DslRef.IModelSubelement
    var modelSubelementRefExpanded: DslRef.IModelSubelement? = null

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is GatherPropertys) return false
        if (gatherPropertiesEnum != other.gatherPropertiesEnum) return false
        return modelSubelementRef == other.modelSubelementRef
    }
    override fun hashCode() = 31 * modelOrModelSubelementRefOriginal.hashCode() + gatherPropertiesEnum.hashCode()
}

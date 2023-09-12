package com.hoffi.chassis.shared.shared

import com.hoffi.chassis.shared.dsl.DslRef

enum class GatherPropertiesEnum {
    NONE,
    PROPERTIES_ONLY_DIRECT_ONES,
    PROPERTIES_AND_SUPERCLASS_PROPERTIES,
    SUPERCLASS_PROPERTIES_ONLY
}

data class GatherPropertys(
    val modelOrModelSubelementRefOriginal: DslRef.IModelOrModelSubelement,
    val gatherPropertiesEnum: GatherPropertiesEnum = GatherPropertiesEnum.PROPERTIES_AND_SUPERCLASS_PROPERTIES,
    //val actualMODELREFENUM: MODELREFENUM
) {
    override fun toString() = "$gatherPropertiesEnum of $modelSubelementRef"
    //override fun toString() = "$actualMODELREFENUM gathers $gatherPropertiesEnum of $modelSubelementRef"

    val modelSubelementRef: DslRef.IModelSubelement
        get() = modelSubelementRefExpanded ?: modelOrModelSubelementRefOriginal as DslRef.IModelSubelement
    var modelSubelementRefExpanded: DslRef.IModelSubelement? = null

    fun copyDeep() = this.copy().also { it.modelSubelementRefExpanded = this.modelSubelementRefExpanded }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is GatherPropertys) return false
        if (modelOrModelSubelementRefOriginal != other.modelOrModelSubelementRefOriginal) return false
        if (gatherPropertiesEnum != other.gatherPropertiesEnum) return false
        //if (actualMODELREFENUM != other.actualMODELREFENUM) return false
        return true
    }

    override fun hashCode(): Int {
        var result = modelOrModelSubelementRefOriginal.hashCode()
        result = 31 * result + gatherPropertiesEnum.hashCode()
        //result = 31 * result + actualMODELREFENUM.hashCode()
        return result
    }
}

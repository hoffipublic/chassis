package com.hoffi.chassis.shared.shared

import com.hoffi.chassis.shared.dsl.IDslRef

enum class GatherPropertiesEnum {
    NONE,
    PROPERTIES,
    PROPERTIES_AND_SUPERCLASS_PROPERTIES,
    SUPERCLASS_PROPERTIES_ONLY
}

data class GatherPropertys(
    val modelOrModelSubelementRef: IDslRef,
    val gatherPropertiesEnum: GatherPropertiesEnum = GatherPropertiesEnum.PROPERTIES
) { override fun toString() = "$gatherPropertiesEnum of $modelOrModelSubelementRef" }

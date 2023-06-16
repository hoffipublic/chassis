package com.hoffi.chassis.shared.shared

import com.hoffi.chassis.shared.dsl.DslRef

enum class GatherPropertiesEnum {
    NONE,
    PROPERTIES,
    PROPERTIES_AND_SUPERCLASS_PROPERTIES,
    SUPERCLASS_PROPERTIES_ONLY
}

data class GatherPropertys(
    var modelOrModelSubElementRef: DslRef.IModelOrModelSubElement,
    var gatherPropertiesEnum: GatherPropertiesEnum = GatherPropertiesEnum.PROPERTIES
) { override fun toString() = "$gatherPropertiesEnum of $modelOrModelSubElementRef" }

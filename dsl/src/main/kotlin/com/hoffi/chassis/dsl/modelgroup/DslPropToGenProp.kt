package com.hoffi.chassis.dsl.modelgroup

import com.hoffi.chassis.shared.parsedata.Property

object DslPropToGenProp {
    fun createFrom(dslProp: DslModelProp): Property {
        val p = Property(
            dslProp.name,
            dslProp.propRef,
            dslProp.mutable,
            dslProp.tags
        )
        return p
    }
}

package com.hoffi.chassis.dsl.modelgroup

import com.hoffi.chassis.shared.parsedata.Property

object DslPropToGenProp {
    fun createFrom(dslProp: DslModelProp): Property {
        val p = Property(
            dslProp.name,
            dslProp.propRef,
            dslProp.eitherTypModelOrClass,
            dslProp.mutable,
            dslProp.modifiers,
            dslProp.tags,
            dslProp.length,
            dslProp.collectionType
        )
        return p
    }
}

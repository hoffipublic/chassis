package com.hoffi.chassis.shared.parsedata

import com.hoffi.chassis.shared.Mutable
import com.hoffi.chassis.shared.dsl.DslRef
import com.hoffi.chassis.shared.shared.Tags

class Property(
    val name: String,
    val propRef: DslRef.prop,
    val mutable: Mutable = Mutable(false),
    val tags: Tags = Tags.NONE,
) {
    override fun toString() = "$name${if(mutable.bool) " mutable" else ""} $tags"
}

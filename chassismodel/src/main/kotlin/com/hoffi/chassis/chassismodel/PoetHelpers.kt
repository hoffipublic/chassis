package com.hoffi.chassis.chassismodel

import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.TypeName

object PoetHelpers {
    fun TypeName.nullable() = this.copy(nullable = true)
    fun ClassName.nullable() = this.copy(nullable = true, annotations = this.annotations, tags = this.tags)
}

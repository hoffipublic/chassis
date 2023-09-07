package com.hoffi.chassis.chassismodel

import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.CodeBlock
import com.squareup.kotlinpoet.TypeName

object PoetHelpers {
    // TODO remove when available in future versions of KotlinPoet
    public fun CodeBlock.Builder.addComment(format: String, vararg args: Any): CodeBlock.Builder = apply {
        this.add("//·${format.replace(' ', '·')}\n", *args)
    }
    fun TypeName.nullable() = this.copy(nullable = true)
    fun ClassName.nullable() = this.copy(nullable = true, annotations = this.annotations, tags = this.tags)
}

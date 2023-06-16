package com.hoffi.chassis.shared.parsedata

import com.squareup.kotlinpoet.*

data class TypeWrapper(val typeName: TypeName, val gentypePrefix: String, val gentypePostfix: String) {
    val packageName: String
        get() = className.packageName
    val simpleName: String
        get() = className.simpleName

    val className: ClassName
        get() = when (typeName) {
            is ClassName -> typeName
            Dynamic -> TODO()
            is LambdaTypeName -> TODO()
            is ParameterizedTypeName -> typeName.rawType
            is TypeVariableName -> TODO()
            is WildcardTypeName -> TODO()
        }
}

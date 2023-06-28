package com.hoffi.chassis.shared.parsedata

import com.squareup.kotlinpoet.*
import kotlin.reflect.KClass

data class TypeWrapper(val typeName: TypeName, var isInterface: Boolean) {
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
    companion object {
        fun KClass<*>.typeWrapper() = TypeWrapper(this.asClassName(), this.java.isInterface)
    }
}

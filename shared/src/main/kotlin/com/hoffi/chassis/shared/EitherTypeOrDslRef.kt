package com.hoffi.chassis.shared

//sealed class EitherTypeOrDslRef {
//    abstract var isInterface: Boolean
//
//    data class EitherKClass(val typeWrapper: TypeWrapper): EitherTypeOrDslRef() {
//        override fun toString() = "${this::class.simpleName} $typeWrapper"
//        override var isInterface: Boolean
//            get() = typeWrapper.isInterface
//            set(value) { typeWrapper.isInterface = value }
//
//        fun x() { when (typeWrapper.typeName) {
//            is ClassName -> TODO()
//            Dynamic -> TODO()
//            is LambdaTypeName -> TODO()
//            is ParameterizedTypeName -> TODO()
//            is TypeVariableName -> TODO()
//            is WildcardTypeName -> TODO()
//        } }
//    }
//    data class EitherDslRef(val dslRef: IDslRef, override var isInterface: Boolean): EitherTypeOrDslRef() {
//        override fun toString() = "${this::class.simpleName} $dslRef"
//    }
//    class ExtendsNothing: EitherTypeOrDslRef() {
//        override fun toString() = "${this::class.simpleName}"
//        override var isInterface = false
//    }
//
//    companion object {
//        val NOTHING = ExtendsNothing()
//        fun KClass<*>.createEitherKClass() = EitherKClass(TypeWrapper(this.asClassName(), this.java.isInterface))
//    }
//}

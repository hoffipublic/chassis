package com.hoffi.chassis.chassismodel.dsl

open class DslException(message: String, cause: Throwable? = null) : Exception(message, cause)
open class DslCtxException(message: String, cause: Throwable? = null) : DslException(message, cause)

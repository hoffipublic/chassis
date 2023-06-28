package com.hoffi.chassis.chassismodel.dsl

open class DslException(message: String, cause: Throwable? = null) : Exception(message, cause)
open class DslCtxException(message: String, cause: Throwable? = null) : DslException(message, cause)
open class DslRefException(message: String, cause: Throwable? = null) : DslException(message, cause)

open class GenException(message: String, cause: Throwable? = null) : Exception(message, cause)
open class GenCtxException(message: String, cause: Throwable? = null) : GenException(message, cause)

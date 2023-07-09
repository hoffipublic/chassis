package com.hoffi.chassis.shared.whens

import com.hoffi.chassis.chassismodel.dsl.GenException

class WhensCtx {
    val whenCtx: MutableMap<String, Any> = mutableMapOf()
    inline operator fun <reified T : Any> set(key: String, value: T): T = value.also { whenCtx[key] = value }
    inline operator fun <reified T : Any> get(key: String): T = ctxObj(key)
    inline fun <reified T : Any> ctxObj(key: String): T {
        var theObj: Any? = whenCtx[key]
        return theObj as T? ?: throw GenException("no whenCtx Object '$key' found in WhenCtx")
    }
}

package com.hoffi.chassis.dsl.internal

import com.hoffi.chassis.shared.dsl.IDslRef

context(DslCtxWrapper)
class TopLevelDslFunction() : ADslClass() {
    //override val parent: IDslClass = this
    override val selfDslRef: IDslRef = IDslRef.NULL
}

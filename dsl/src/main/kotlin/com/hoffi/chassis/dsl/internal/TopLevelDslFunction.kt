package com.hoffi.chassis.dsl.internal

import com.hoffi.chassis.shared.dsl.DslRef
import com.hoffi.chassis.shared.dsl.IDslClass
import com.hoffi.chassis.shared.dsl.IDslRef

class TopLevelDslFunction() : IDslClass {
    override val parent: IDslClass = this
    override val selfDslRef: DslRef = DslRef.NULL
    override val parentDslRef: IDslRef = DslRef.NULL
    override val groupDslRef: DslRef.IGroupLevel = DslRef.IGroupLevel.NULL
    override fun toString() = selfDslRef.toString()
}

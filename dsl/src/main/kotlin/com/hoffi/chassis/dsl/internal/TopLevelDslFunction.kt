package com.hoffi.chassis.dsl.internal

import com.hoffi.chassis.shared.dsl.DslRef
import com.hoffi.chassis.shared.dsl.DslRef.DslGroupRefEither.DslModelgroupRef
import com.hoffi.chassis.shared.dsl.IDslClass

class TopLevelDslFunction() : IDslClass {
    override val parent: IDslClass = this
    override val selfDslRef: DslRef = DslRef.NULL
    override val parentDslRef: DslRef = DslRef.NULL
    override val groupDslRef: DslRef.DslGroupRefEither = DslModelgroupRef.NULL
}

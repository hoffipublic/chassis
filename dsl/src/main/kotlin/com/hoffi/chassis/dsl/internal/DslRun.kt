package com.hoffi.chassis.dsl.internal

import com.hoffi.chassis.chassismodel.dsl.DslInstance
import com.hoffi.chassis.dsl.whereto.INameAndWheretoPlusModelSubtypes
import com.hoffi.chassis.dsl.whereto.NameAndWheretoPlusModelSubtypesImpl
import com.hoffi.chassis.shared.dsl.DslRef
import com.hoffi.chassis.shared.dsl.IDslClass

class DslRun(var runIdentifierEgEnvAndTime: String) : IDslClass {
    var running = false
    val dslRun = this
    internal val dslCtx: DslCtx = DslCtx(this)

    val runTopLevelFunction = TopLevelDslFunction()

    override val parent: IDslClass = runTopLevelFunction
    override val selfDslRef: DslRef = DslRef.dslRunRef("runDiscriminator", runIdentifierEgEnvAndTime)
    override val parentDslRef: DslRef = parent.selfDslRef
    override val groupDslRef: DslRef.DslGroupRefEither = DslRef.DslGroupRefEither.NULL

    fun start(dslRunBlock: DslRun.() -> Unit = {}): DslRun {
        this.apply(dslRunBlock)
        return this
    }
    companion object {
        val NULL = DslRun("NULL")
    }

    @DslInstance
    internal val wheretoImpl = NameAndWheretoPlusModelSubtypesImpl(this)
    @DslBlockOn<NameAndWheretoPlusModelSubtypesImpl>
    fun configure(whereToBlock: INameAndWheretoPlusModelSubtypes.() -> Unit) {
        wheretoImpl.apply(whereToBlock)
    }
}

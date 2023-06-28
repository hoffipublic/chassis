package com.hoffi.chassis.dsl.internal

import com.hoffi.chassis.chassismodel.C
import com.hoffi.chassis.chassismodel.dsl.DslCtxException
import com.hoffi.chassis.dsl.whereto.DslNameAndWheretoWithSubelementsDelegateImpl
import com.hoffi.chassis.dsl.whereto.IDslApiNameAndWheretoWithSubelements
import com.hoffi.chassis.shared.dsl.DslDiscriminator
import com.hoffi.chassis.shared.dsl.DslRef
import kotlin.time.DurationUnit
import kotlin.time.toDuration

/** a DslRun starts parsing multiple DSLs into the same DslCtx */
class DslRun(var runIdentifierEgEnvAndTime: String) : IDslClass {
    override fun toString() = "DslRun(runIdentifierEgEnvAndTime='$runIdentifierEgEnvAndTime')"
    var running = false
    val dslRun = this
    val dslCtx: DslCtx = DslCtx._create(this)
        .also {  globalDslCtx = it }
    val dslCtxWrapper by lazy { DslCtxWrapper(dslCtx, DslDiscriminator("run: $runIdentifierEgEnvAndTime")) }

    val runRef = DslRef.DslRun(runIdentifierEgEnvAndTime)
    override val selfDslRef: DslRef = runRef

    fun start(dslRunDiscriminator: String, dslRunBlock: DslCtxWrapper.() -> Unit): DslRun {
        dslCtxWrapper.dslDiscriminator = DslDiscriminator(dslRunDiscriminator)
        return start(dslRunBlock)
    }
    fun start(dslRunBlock: DslCtxWrapper.() -> Unit): DslRun {
        if (running) throw DslCtxException("DslRun is already running! $this ")
        dslCtx.start()
        running = true
        while (true) {
            dslCtxWrapper.apply(dslRunBlock)

//            val nextPass: DSLPASS? = dslCtx.currentPASS.nextPass()
//            if (nextPass != null) {
//                if (nextPass !is DSLPASS.PASS_FINISH) {
//                    dslCtx.currentPASS = nextPass
//                } else {
//                    if (dslCtx.errors.isEmpty()) {
//                        dslCtx.currentPASS = nextPass
//                    } else {
//                        dslCtx.currentPASS = dslCtx.PASS_ERROR
//                    }
//                }
//            } else {
//                dslCtx.currentPASS.finish() // execEnd Time of last PASS (predecessors have been finished by call to nextPass()
//                break
//            }
            dslCtx.nextPass() ?: break
        }
        println("\ninfo summary on ${this::class.simpleName}(${this.runIdentifierEgEnvAndTime}):")
        val fs=20
        for (pass in listOf(dslCtx.PASS_0_CONFIGURE, dslCtx.PASS_1_BASEMODELS, dslCtx.PASS_2_TABLEMODELS, dslCtx.PASS_3_ALLMODELS, dslCtx.PASS_4_REFERENCING, dslCtx.PASS_FINISH, dslCtx.PASS_INHERITANCE, dslCtx.PASS_ERROR) ) {
            if (pass.execStart != 0L) {
                if (pass.execEnd == -1L) {
                    println(String.format("%-${fs}s execEnd was never set", pass::class.simpleName))
                } else {
                    println(String.format("%-${fs}s took %9s", pass::class.simpleName, (pass.execEnd - pass.execStart).toDuration(DurationUnit.MILLISECONDS)))
                }
            }
        }
        println()
        return this
    }

    @DslInstance
    internal val wheretoImpl: DslNameAndWheretoWithSubelementsDelegateImpl = with(dslCtxWrapper) {
        dslCtx.ctxObjOrCreate(DslRef.nameAndWhereto(C.DSLRUNREFSIMPLENAME, runRef))
    }

    @DslBlockOn(DslNameAndWheretoWithSubelementsDelegateImpl::class)
    fun configure(whereToBlock: IDslApiNameAndWheretoWithSubelements.() -> Unit) {
        dslCtx.currentPASS = dslCtx.PASS_0_CONFIGURE // special PASS_0 !
        wheretoImpl.apply(whereToBlock)
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is DslRun) return false
        if (!super.equals(other)) return false
        return runIdentifierEgEnvAndTime == other.runIdentifierEgEnvAndTime
    }
    override fun hashCode(): Int {
        var result = super.hashCode()
        result = 31 * result + runIdentifierEgEnvAndTime.hashCode()
        return result
    }
    companion object {
        val NULL = DslRun(C.NULLSTRING)
    }
}

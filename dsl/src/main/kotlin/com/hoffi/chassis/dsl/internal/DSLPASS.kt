package com.hoffi.chassis.dsl.internal

import com.hoffi.chassis.chassismodel.C
import org.slf4j.LoggerFactory

sealed class DSLPASS(val index: Int, protected val nextDSLPASS: DSLPASS?, protected val dslCtx: DslCtx) {
    override fun toString() = this::class.simpleName!!
    private val log = LoggerFactory.getLogger(javaClass)
    var execStart: Long = 0L
    var execEnd: Long = -1L
    fun start(): DSLPASS { execStart = System.currentTimeMillis() ; log.info("started {}", this::class.simpleName) ; return this }
    fun nextPass(): DSLPASS? { execEnd = System.currentTimeMillis() ; return nextDSLPASS?.start() }
    fun finish() { execEnd = System.currentTimeMillis() ; log.info("chassis DSL Parsing finished completely.") }

    companion object {
        val NULL: DSLPASS = PASS_ERROR(DslCtx._create(DslRun(C.NULLSTRING)))
    }
    internal fun doIf(pass: DSLPASS, vararg passes: DSLPASS, block: () -> Unit) {
        if (dslCtx.currentPASS == pass || dslCtx.currentPASS in passes) block()
    }

    //    internal fun <T: DSLPASS> after(dslCtx: DslCtx, pass: KClass<T>, block: () -> Unit) {
//        if (dslCtx.pass.index > pass.index) {}
//    }
    class PASS_0_CONFIGURE  (nextPASS: DSLPASS, dslCtx: DslCtx) : DSLPASS(1, nextPASS, dslCtx) /* normal models like DTOs */
    class PASS_1_BASEMODELS (nextPASS: DSLPASS, dslCtx: DslCtx) : DSLPASS(1, nextPASS, dslCtx) /* normal models like DTOs */
    class PASS_2_TABLEMODELS(nextPASS: DSLPASS, dslCtx: DslCtx) : DSLPASS(2, nextPASS, dslCtx) // TABLE models (probably gathering propertys and stuff from above models)
    class PASS_3_ALLMODELS  (nextPASS: DSLPASS, dslCtx: DslCtx) : DSLPASS(3, nextPASS, dslCtx) // allModels { } at the end of DSLPASS.TWO_TABLEMODELS
    class PASS_4_REFERENCING(nextPASS: DSLPASS, dslCtx: DslCtx) : DSLPASS(4, nextPASS, dslCtx) // things that potentially reference other Models, like extends { } clauses
    class PASS_FINISH       (                   dslCtx: DslCtx) : DSLPASS(5, nextDSLPASS = null, dslCtx) // optional cleanup for specific models
    class PASS_ERROR        (                   dslCtx: DslCtx) : DSLPASS(99, nextDSLPASS = null, dslCtx)

    //region equals and hashCode ...
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is DSLPASS) return false

        if (index != other.index) return false
        if (nextDSLPASS != other.nextDSLPASS) return false
        if (dslCtx != other.dslCtx) return false

        return true
    }

    override fun hashCode(): Int {
        var result = index
        result = 31 * result + (nextDSLPASS?.hashCode() ?: 0)
        result = 31 * result + dslCtx.hashCode()
        return result
    }
    //endregion
}

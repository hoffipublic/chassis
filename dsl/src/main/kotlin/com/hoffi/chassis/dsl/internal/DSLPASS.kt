package com.hoffi.chassis.dsl.internal

sealed class DSLPASS(val dslRun: DslRun, private var nextDSLPASS: DSLPASS) {
    var execStart: Long = System.currentTimeMillis()

    companion object {
        val NULL = PASS_ERROR(DslRun("NULL"))
        fun initialPass(dslRun: DslRun): DSLPASS = PASS_1_BASEMODELS(dslRun)
    }
    class PASS_1_BASEMODELS(dslRun: DslRun)  : DSLPASS(dslRun, PASS_2_TABLEMODELS(dslRun)) // normal models like DTOs
    class PASS_2_TABLEMODELS(dslRun: DslRun) : DSLPASS(dslRun, PASS_3_ALLMODELS(dslRun)) // TABLE models (probably gathering propertys and stuff from above models)
    class PASS_3_ALLMODELS(dslRun: DslRun)   : DSLPASS(dslRun, PASS_4_REFERENCING(dslRun)) // allModels { } at the end of DSLPASS.TWO_TABLEMODELS
    class PASS_4_REFERENCING(dslRun: DslRun) : DSLPASS(dslRun, PASS_FINISH(dslRun)) // things that potentially reference other Models, like extends { } clauses
    class PASS_FINISH(dslRun: DslRun)        : DSLPASS(dslRun, PASS_ERROR(dslRun)) // optional cleanup for specific models
    class PASS_ERROR(dslRun: DslRun)         : DSLPASS(dslRun, NULL)

    fun nextPass(): DSLPASS = dslRun.dslCtx.pass.nextDSLPASS.also { it.execStart = System.currentTimeMillis() }
}

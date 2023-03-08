package com.hoffi.chassis.shared.dsl

import com.hoffi.chassis.chassismodel.C

enum class DslBlockName(name: String) {
    //region dslBlock function names
    MODELGROUP("modelgroup"),
    APIGROUP("apigroup"),
    WHERETO("whereto"),
    FILLER("filler"),
    MODEL("model"),
    MODEL_DTO("dto"),
    MODEL_TABLE("table"),
    API("api"),
    ALLMODELS("allModels")
    //endregion
}
data class DslRefAtom(val functionName: String, val simpleName: String = C.DEFAULT) {
    override fun toString() = "$functionName:$simpleName"
    companion object {
        fun from(refString: String) = refString.split(":").let { if(it.size == 2) DslRefAtom(it[0], it[1]) else DslRefAtom(it[0]) }
    }
}
object DslRefString {
    //region DslRefString { ...
    /** padding the given ref to construct an absolute ref */
    fun from(selfDslRef: DslRef, refString: String): DslRef {
        // TODO fill prefixing, so that the level of parent is reached
        //      if nothing is to be filled up, then it is an absolute ref
        //        hmm, no, if first part starts with a groupref, then it is an absolute ref
        //        otherwise a ref within the same group
        val partsList = refString.split(";").map { DslRefAtom.from(it) }
        if (refString.contains(";")) {
            // relative from parent (if not TopLevelDslFunction
        } else {
            // a parent Dslref (if not TopLevelDslFunction
        }
        return DslRef.NULL
    }
    /** ref from an absolute-ref-string */
    fun ref(refString: String): DslRef {
        val partsList = refString.split(";").map { DslRefAtom.from(it) }
        return DslRef.NULL
    }
    fun modelRef(refString: String): DslRef.DslGroupRefEither.DslModelgroupRef.DslElementRefEither.DslModelRef {
        val partsList = refString.split(";").map { DslRefAtom.from(it) }
        return DslRef.DslGroupRefEither.DslModelgroupRef.DslElementRefEither.DslModelRef.NULL
    }
    fun modelElementRef(refString: String): DslRef.DslGroupRefEither.DslModelgroupRef.DslElementRefEither.DslModelRef.DslSubElementRefEither {
        val partsList = refString.split(";").map { DslRefAtom.from(it) }
        return DslRef.DslGroupRefEither.DslModelgroupRef.DslElementRefEither.DslModelRef.NULL
    }
    context(IDslClass)
    fun fromSelf(selfDslRef: DslRef, refString: String): DslRef { // TODO implement me
        val atomList = refString.split(";").map { DslRefAtom.from(it) }
        val discriminator = atomList[0].functionName
        val group = DslRef.DslGroupRefEither.NULL
        if(atomList.size > 1) {
            when (atomList[1].functionName) {
                DslBlockName.MODELGROUP.name -> DslRef.modelgroupRef(discriminator, atomList[1].simpleName)
                DslBlockName.APIGROUP.name   -> DslRef.apigroupRef(discriminator, atomList[1].simpleName)
            }
        }
        return DslRef.NULL
}
    //endregion
}
context(IDslClass, DslDiscriminator)
fun String.ref() = DslRefString.fromSelf(selfDslRef, this)

sealed class DslRef(val discriminator: String, val dslTopLevelFunctionName: String, val simpleName: String) {
    override fun toString(): String = "$discriminator;${refList.joinToString(";")}"

    open val level = 0
    val refList = mutableListOf<DslRefAtom>()

    companion object {
        val NULL = DslGroupRefEither.NoneRef()
        fun dslRunRef(disc: String, simpleName: String)  = DslRunRef(disc, simpleName)
        fun modelgroupRef(disc: String, simpleName: String) = DslGroupRefEither.DslModelgroupRef(disc, simpleName).also { it.refList.add(DslRefAtom(it.dslGroupFunctionName, simpleName)) }
        fun apigroupRef(disc: String, simpleName: String) = DslGroupRefEither.DslApigroupRef  (disc, simpleName).also { it.refList.add(DslRefAtom(it.dslGroupFunctionName, simpleName)) }
    }

    // dsl blocks which can be on multiple different dsl blocks
    fun wheretoRef(simpleName: String) = DslWheretoRef(discriminator, simpleName)
        .also { refList.addAll(this.refList) ; refList.add(DslRefAtom(DslBlockName.WHERETO.name, simpleName)) }
    class DslWheretoRef(disc: String, simpleName: String) : DslRef(disc, DslBlockName.WHERETO.name, simpleName)

    // special class to be able to configure DslRun in dsl-style
    class DslRunRef(disc: String, simpleName: String) : DslRef(disc, "<specialDslRun>", simpleName)
    /**
     * chassis dsl class hierarchy
     */
    sealed class DslGroupRefEither(disc: String, val dslGroupFunctionName: String, simpleName: String) : DslRef(disc, dslGroupFunctionName, simpleName) {
        override val level = 1
        companion object { val NULL = NoneRef() }
        class NoneRef : DslGroupRefEither(C.NULLSTRING, C.NULLSTRING, C.NULLSTRING)
        open class DslModelgroupRef(disc: String, simpleName: String) : DslGroupRefEither(disc, "modelgroup", simpleName) {
            //override val level = 1
            companion object { val NULL = DslElementRefEither.DslModelRef(C.NULLSTRING, C.NULLSTRING) }
            fun modelRef(    simpleName: String) = DslElementRefEither.DslModelRef(    discriminator, simpleName)        .also { it.refList.addAll(this.refList) ; it.refList.add(DslRefAtom(it.dslElementFunctionName, simpleName)) }
            fun fillerRef(   simpleName: String) = DslElementRefEither.DslFillerRef(   discriminator, simpleName)        .also { it.refList.addAll(this.refList) ; it.refList.add(DslRefAtom(it.dslElementFunctionName, simpleName)) }
            fun apiRef(      simpleName: String) = DslElementRefEither.DslApiRef(      discriminator, simpleName)        .also { it.refList.addAll(this.refList) ; it.refList.add(DslRefAtom(it.dslElementFunctionName, simpleName)) }
            //fun wheretoRef(  simpleName: String) = DslElementRefEither.DslWheretoRef(  discriminator, simpleName)        .also { it.refList.addAll(this.refList) ; it.refList.add(DslRefAtom(it.dslElementFunctionName, simpleName)) }
            fun allModelsRef(simpleName: String) = DslElementRefEither.DslAllModelsRef(discriminator, simpleName)        .also { it.refList.addAll(this.refList) ; it.refList.add(DslRefAtom(it.dslElementFunctionName, simpleName)) }

            sealed class DslElementRefEither(disc: String, val dslElementFunctionName: String, simpleName: String) : DslModelgroupRef(disc, simpleName) {
                override val level = 2
                open class DslModelRef(disc: String, simpleName: String) : DslElementRefEither(disc, DslBlockName.MODEL.name, simpleName) {
                    companion object { val NULL = DslSubElementRefEither.NoneRef() }
                    enum class MODELELEMENT { DTO, TABLE }
                    fun dtoRef(simpleName: String = C.DEFAULT)   = DslSubElementRefEither.DslDtoRef  (discriminator, simpleName).also { it.refList.addAll(this.refList) ; it.refList.add(DslRefAtom(it.dslSubElementFunctionName, simpleName)) }
                    fun tableRef(simpleName: String) = DslSubElementRefEither.DslTableRef(discriminator, simpleName).also { it.refList.addAll(this.refList) ; it.refList.add(DslRefAtom(it.dslSubElementFunctionName, simpleName)) }

                    sealed class DslSubElementRefEither(disc: String, val dslSubElementFunctionName: String, simpleName: String) : DslModelRef(disc, simpleName) {
                        override val level = 3
                        companion object { val NULL: DslSubElementRefEither = NoneRef() }
                        class NoneRef internal constructor() : DslSubElementRefEither("NONE", "NONE", "NONE")
                        open class DslDtoRef(disc: String, simpleName: String) : DslSubElementRefEither(disc, "dto", simpleName) {
                            //fun someSubSubType(simpleName: String) = ...
                        }
                        open class DslTableRef(disc: String, simpleName: String): DslSubElementRefEither(disc, "table", simpleName)
                    }
                }
                open class DslFillerRef(disc: String, simpleName: String) : DslElementRefEither(disc, DslBlockName.FILLER.name, simpleName)
                open class DslApiRef(disc: String, simpleName: String) : DslElementRefEither(disc, DslBlockName.API.name, simpleName)
                //open class DslWheretoRef(disc: String, simpleName: String) : DslElementRefEither(disc, DslBlockName.WHERETO.name, simpleName)
                open class DslAllModelsRef(disc: String, simpleName: String) : DslElementRefEither(disc, DslBlockName.ALLMODELS.name, simpleName)
            }
        }
        open class DslApigroupRef(disc: String, simpleName: String) : DslGroupRefEither(disc, DslBlockName.APIGROUP.name, simpleName) {
            // override val level = still 1
            companion object { val NULL = DslApigroupRef.DslElementRefEither.DslApiRef(C.NULLSTRING, C.NULLSTRING) }
            fun apiRef(    simpleName: String) = DslApigroupRef.DslElementRefEither.DslApiRef(    discriminator, simpleName)        .also { it.refList.addAll(this.refList) ; it.refList.add(DslRefAtom(it.dslElementFunctionName, simpleName)) }

            sealed class DslElementRefEither(disc: String, val dslElementFunctionName: String, simpleName: String) : DslApigroupRef(disc, simpleName) {
                override val level = 2
                open class DslApiRef(disc: String, simpleName: String) : DslElementRefEither(disc, DslBlockName.API.name, simpleName)
            }
        }
    }
}

fun main(@Suppress("UNUSED_PARAMETER") args: Array<String> = arrayOf()) {
    val groupRef = DslRef.modelgroupRef("disc", "grName")
    val modelRef = groupRef.modelRef("moName")
    val dtoRef = modelRef.dtoRef("dtoName")
    println(dtoRef.dslTopLevelFunctionName)
    println(dtoRef.dslGroupFunctionName)
    println(dtoRef.dslElementFunctionName)
    println(dtoRef.dslSubElementFunctionName)
    println("\"${dtoRef.simpleName}\"")
    println(groupRef.refList)
    println(modelRef.refList)
    println(dtoRef.refList)
    println("$dtoRef")
}

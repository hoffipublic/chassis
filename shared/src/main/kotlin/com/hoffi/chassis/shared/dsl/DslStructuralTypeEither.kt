package com.hoffi.chassis.shared.dsl

sealed class DslStructuralTypeEither(val dslTopLevelFunctionName: String, val simpleName: String) {

    sealed class DslGroupTypeEither(val dslGroupFunctionName: String, simpleName: String) : DslStructuralTypeEither(dslGroupFunctionName, simpleName) {
        open class DslModelgroupType(simpleName: String) : DslGroupTypeEither("modelgroup", simpleName) {
            sealed class DslElementTypeEither(val dslElementFunctionName: String, simpleName: String) : DslModelgroupType(simpleName) {
                open class DslModelType(simpleName: String) : DslElementTypeEither("model", simpleName) {
                    sealed class DslSubElementTypeEither(val dslSubElementFunctionName: String, simpleName: String) : DslModelType(simpleName) {
                        companion object {val NULL: DslSubElementTypeEither = NoneType() }
                        class NoneType internal constructor() : DslSubElementTypeEither("NONE", "NONE")
                        class DtoType(simpleName: String) : DslSubElementTypeEither("dto", simpleName)
                        class TableType(simpleName: String): DslSubElementTypeEither("table", simpleName)
                    }
                }
                open class DslFillerType(simpleName: String) : DslElementTypeEither("filler", simpleName)
                open class DslApiType(simpleName: String) : DslElementTypeEither("api", simpleName)
                open class DslWheretoType(simpleName: String) : DslElementTypeEither("whereto", simpleName)
            }
        }
        open class DslApigroupType(simpleName: String) : DslGroupTypeEither("apigroup", simpleName) {
            sealed class DslElementTypeEither(val dslElementFunctionName: String, simpleName: String) : DslApigroupType(simpleName) {
                open class DslApiType(simpleName: String) : DslElementTypeEither("api", simpleName)
            }
        }
    }

}

fun main(@Suppress("UNUSED_PARAMETER") args: Array<String>) {
    val dtoType = DslStructuralTypeEither.DslGroupTypeEither.DslModelgroupType.DslElementTypeEither.DslModelType.DslSubElementTypeEither.DtoType("someName")
    println(dtoType.dslTopLevelFunctionName)
    println(dtoType.dslGroupFunctionName)
    println(dtoType.dslElementFunctionName)
    println(dtoType.dslSubElementFunctionName)
}

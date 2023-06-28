package com.hoffi.chassis.shared.parsedata

import com.hoffi.chassis.chassismodel.C
import com.hoffi.chassis.shared.COLLECTIONTYPE
import com.hoffi.chassis.shared.EitherTypOrModelOrPoetType
import com.hoffi.chassis.shared.Mutable
import com.hoffi.chassis.shared.dsl.DslRef
import com.hoffi.chassis.shared.shared.Tag
import com.hoffi.chassis.shared.shared.Tags
import com.hoffi.chassis.shared.strategies.*

class Property constructor(
    val name: String,
    val propRef: DslRef.prop,
    var eitherTypModelOrClass: EitherTypOrModelOrPoetType,
    val mutable: Mutable = Mutable(false),
    val tags: Tags = Tags.NONE,
    var length: Int = C.DEFAULT_INT,
    val collectionType: COLLECTIONTYPE = COLLECTIONTYPE.NONE
) {
    override fun toString() = "${this::class.simpleName}[${if (tags.contains(Tag.CONSTRUCTOR)) "C " else "  "}${if (mutable.bool) "var " else "fix "}${if (collectionType != COLLECTIONTYPE.NONE) "$collectionType " else ""}${name}/${eitherTypModelOrClass}})${if (tags.isNotEmpty()) ", tags:$tags" else ""}] OF $propRef"
    var classNameStrategy = ClassNameStrategy.get(IClassNameStrategy.STRATEGY.DEFAULT)
    var tableNameStrategy = TableNameStrategy.get(ITableNameStrategy.STRATEGY.DEFAULT)
    var varNameStrategy = VarNameStrategy.get(IVarNameStrategy.STRATEGY.DEFAULT)

    fun name(prefix: String = "", postfix: String = "") = varNameStrategy.nameLowerFirst(name, prefix, postfix)

}

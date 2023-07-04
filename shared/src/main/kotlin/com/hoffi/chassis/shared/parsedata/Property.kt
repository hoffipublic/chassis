package com.hoffi.chassis.shared.parsedata

import com.hoffi.chassis.chassismodel.C
import com.hoffi.chassis.shared.COLLECTIONTYP
import com.hoffi.chassis.shared.EitherTypOrModelOrPoetType
import com.hoffi.chassis.shared.Mutable
import com.hoffi.chassis.shared.dsl.DslRef
import com.hoffi.chassis.shared.helpers.PoetHelpers.nullable
import com.hoffi.chassis.shared.helpers.Validate.failIfIdentifierInvalid
import com.hoffi.chassis.shared.shared.Initializer
import com.hoffi.chassis.shared.shared.Tag
import com.hoffi.chassis.shared.shared.Tags
import com.hoffi.chassis.shared.strategies.*
import com.squareup.kotlinpoet.KModifier
import com.squareup.kotlinpoet.TypeName

class Property constructor(
    val name: String,
    val propRef: DslRef.prop,
    var eitherTypModelOrClass: EitherTypOrModelOrPoetType,
    val mutable: Mutable = Mutable(false),
    val modifiers: MutableSet<KModifier> = mutableSetOf(),
    val tags: Tags = Tags.NONE,
    var length: Int = C.DEFAULT_INT,
    val collectionType: COLLECTIONTYP = COLLECTIONTYP.NONE
) {
    override fun toString() = "${this::class.simpleName}[${if (tags.contains(Tag.CONSTRUCTOR)) "C " else "  "}${if (mutable.bool) "var " else "fix "}${if (collectionType != COLLECTIONTYP.NONE) "$collectionType " else ""}${name}/${eitherTypModelOrClass}})${if (tags.isNotEmpty()) ", tags:$tags" else ""}] OF $propRef"
    var classNameStrategy = ClassNameStrategy.get(IClassNameStrategy.STRATEGY.DEFAULT)
    var tableNameStrategy = TableNameStrategy.get(ITableNameStrategy.STRATEGY.DEFAULT)
    var varNameStrategy = VarNameStrategy.get(IVarNameStrategy.STRATEGY.DEFAULT)

    fun name(prefix: String = "", postfix: String = "") = varNameStrategy.nameLowerFirst(name, prefix, postfix)

    // convenience methods

    val isNullable: Boolean
        get() = Tag.NULLABLE in tags
    val isNullableGenerictype: Boolean
        get() = Tag.NULLABLE_GENERICTYPE in tags
    val isInterface: Boolean
        get() = eitherTypModelOrClass.isInterface
    val poetType: TypeName
        get() = when (collectionType) {
            is COLLECTIONTYP.NONE ->
                if (isNullable) { eitherTypModelOrClass.modelClassName.poetType.nullable() } else { eitherTypModelOrClass.modelClassName.poetType }
            is COLLECTIONTYP.COLLECTION, is COLLECTIONTYP.ITERABLE, is COLLECTIONTYP.LIST, is COLLECTIONTYP.SET ->
                if (isNullableGenerictype) { eitherTypModelOrClass.modelClassName.poetType.nullable() } else { eitherTypModelOrClass.modelClassName.poetType }
        }
    val modelOrTypeNameString: String
        get() = eitherTypModelOrClass.modelClassName.modelOrTypeNameString
    val initializer: Initializer
        get() = eitherTypModelOrClass.initializer

    // housekeeping methods

    fun validate(any: Any) {
        name.failIfIdentifierInvalid(any)
        eitherTypModelOrClass.validate("$any->$this")
    }

    // TODO better decide on name, AND(!) eitherTypModelOrClass AND(!) if prop isNullable
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is Property) return false
        return name == other.name
    }
    override fun hashCode() = name.hashCode()
}

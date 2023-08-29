package com.hoffi.chassis.shared.parsedata

import com.hoffi.chassis.chassismodel.C
import com.hoffi.chassis.chassismodel.Initializer
import com.hoffi.chassis.chassismodel.PoetHelpers.nullable
import com.hoffi.chassis.chassismodel.RuntimeDefaults
import com.hoffi.chassis.chassismodel.typ.COLLECTIONTYP
import com.hoffi.chassis.chassismodel.typ.Mutable
import com.hoffi.chassis.shared.EitherTypOrModelOrPoetType
import com.hoffi.chassis.shared.dsl.DslRef
import com.hoffi.chassis.shared.helpers.Validate.failIfIdentifierInvalid
import com.hoffi.chassis.shared.shared.Tag
import com.hoffi.chassis.shared.shared.Tags
import com.hoffi.chassis.shared.strategies.IVarNameStrategy
import com.hoffi.chassis.shared.strategies.VarNameStrategy
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.KModifier
import com.squareup.kotlinpoet.TypeName

class Property(
    val dslPropName: String,
    val propRef: DslRef.prop,
    val containedInSubelementRef: DslRef.IModelSubelement,
    var eitherTypModelOrClass: EitherTypOrModelOrPoetType,
    val mutable: Mutable = Mutable(false),
    val modifiers: MutableSet<KModifier> = mutableSetOf(),
    val tags: Tags = Tags.NONE,
    var length: Int = C.DEFAULT_INT,
    val collectionType: COLLECTIONTYP = COLLECTIONTYP.NONE
) {
    override fun toString() = "${"%-38s".format("${this::class.simpleName} ${if (tags.contains(Tag.CONSTRUCTOR)) "C " else "  "}${if (mutable.bool) "var " else "val "}${if (collectionType != COLLECTIONTYP.NONE) "$collectionType " else ""}$dslPropName")} : ${eitherTypModelOrClass}${if (tags.isNotEmpty()) ", tags:[$tags" else ""}] of $propRef"
    var varNameStrategy = VarNameStrategy.get(IVarNameStrategy.STRATEGY.DEFAULT)
    var columnNameStrategy = VarNameStrategy.get(IVarNameStrategy.STRATEGY.LOWERSNAKECASE)

    fun name(prefix: String = "", postfix: String = "") = varNameStrategy.nameLowerFirstOf(dslPropName, prefix, postfix)
    fun columnName(prefix: String = "", postfix: String = "") = when (eitherTypModelOrClass) {
        is EitherTypOrModelOrPoetType.EitherModel -> {
            columnNameStrategy.nameLowerFirstOf(dslPropName, prefix, postfix + columnNameStrategy.nameUpperFirstOf(RuntimeDefaults.UUID_PROPNAME))
        }
        else -> {
            columnNameStrategy.nameLowerFirstOf(dslPropName)
        }
    }

    // convenience methods

    val isNullable: Boolean
        get() = Tag.NULLABLE in tags
    val isNullableGenerictype: Boolean
        get() = Tag.NULLABLE_GENERICTYPE in tags
    val isInterface: Boolean
        get() = eitherTypModelOrClass.isInterface
    val poetType: ClassName
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
        dslPropName.failIfIdentifierInvalid(any)
        eitherTypModelOrClass.validate("$any->$this")
    }

    /** used e.g. to determine props to copy over in Fillers */
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is Property) return false
        if (dslPropName != other.dslPropName) return false
        if (eitherTypModelOrClass != other.eitherTypModelOrClass) return false
        return true
    }
    override fun hashCode(): Int {
        var result = dslPropName.hashCode()
        result = 31 * result + eitherTypModelOrClass.hashCode()
        return result
    }
}

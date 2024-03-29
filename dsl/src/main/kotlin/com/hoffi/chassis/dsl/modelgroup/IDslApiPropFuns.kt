package com.hoffi.chassis.dsl.modelgroup

import com.hoffi.chassis.chassismodel.Initializer
import com.hoffi.chassis.chassismodel.ReplaceAppendOrModify
import com.hoffi.chassis.chassismodel.typ.COLLECTIONTYP
import com.hoffi.chassis.chassismodel.typ.Mutable
import com.hoffi.chassis.chassismodel.typ.TYP
import com.hoffi.chassis.chassismodel.typ.immutable
import com.hoffi.chassis.dsl.internal.ChassisDslMarker
import com.hoffi.chassis.shared.dsl.IDslRef
import com.hoffi.chassis.shared.shared.Tag
import com.hoffi.chassis.shared.shared.Tags
import com.hoffi.chassis.shared.shared.reffing.MODELREFENUM
import com.squareup.kotlinpoet.KModifier
import com.squareup.kotlinpoet.TypeName
import com.squareup.kotlinpoet.asTypeName
import kotlin.reflect.KClass

@ChassisDslMarker
interface IDslApiInitializer {
    /** only operate on 'addendum' properties, never on 'original' properties if you want to "alter" the original props, use ReplaceAppendOrModify.REPLACE*/
    fun initializer(name: String, replaceAppendOrModify: ReplaceAppendOrModify, format: String, vararg args: Any, modifyInitializerBlock: Initializer.() -> Unit = {}) }

@ChassisDslMarker
    interface IDslApiPropFuns {
    fun addToStringMembers(vararg propName: String)
    fun removeToStringMembers(vararg propName: String)
    // ===================================================================================================================================
    // ====================   "primitive" TYP properties   ===============================================================================
    // ===================================================================================================================================
    fun property(name: String, typ: TYP = TYP.STRING, mutable: Mutable = immutable, vararg tags: Tag) = property(name, typ, mutable, COLLECTIONTYP.NONE, Initializer.EMPTY, mutableSetOf(), TYP.DEFAULT_INT, Tags.of(*tags))
    fun property(name: String, typ: TYP = TYP.STRING, mutable: Mutable = immutable, tags: Tags) = property(name, typ, mutable, COLLECTIONTYP.NONE, Initializer.EMPTY, mutableSetOf(), TYP.DEFAULT_INT, tags)
    fun property(name: String, typ: TYP = TYP.STRING, mutable: Mutable = immutable, length: Int = TYP.DEFAULT_INT, vararg tags: Tag) = property(name, typ, mutable, COLLECTIONTYP.NONE, Initializer.EMPTY, mutableSetOf(), length, Tags.of(*tags))
    fun property(name: String, typ: TYP = TYP.STRING, mutable: Mutable = immutable, length: Int = TYP.DEFAULT_INT, tags: Tags) = property(name, typ, mutable, COLLECTIONTYP.NONE, Initializer.EMPTY, mutableSetOf(), length, tags)
    fun property(name: String, typ: TYP = TYP.STRING, collectionType: COLLECTIONTYP = COLLECTIONTYP.NONE, vararg tags: Tag) = property(name, typ, immutable, collectionType, Initializer.EMPTY, mutableSetOf(), TYP.DEFAULT_INT, Tags.of(*tags))
    fun property(name: String, typ: TYP = TYP.STRING, collectionType: COLLECTIONTYP = COLLECTIONTYP.NONE, tags: Tags = Tags.NONE) = property(name, typ, immutable, collectionType, Initializer.EMPTY, mutableSetOf(), TYP.DEFAULT_INT, tags)
    fun property(name: String, typ: TYP = TYP.STRING, mutable: Mutable = immutable, collectionType: COLLECTIONTYP, vararg tags: Tag) = property(name, typ, mutable, collectionType, Initializer.EMPTY, mutableSetOf(), TYP.DEFAULT_INT, Tags.of(*tags))
    fun property(name: String, typ: TYP = TYP.STRING, mutable: Mutable = immutable, collectionType: COLLECTIONTYP, tags: Tags) = property(name, typ, mutable, collectionType, Initializer.EMPTY, mutableSetOf(), TYP.DEFAULT_INT, tags)
    fun property(name: String, typ: TYP = TYP.STRING, mutable: Mutable = immutable, collectionType: COLLECTIONTYP = COLLECTIONTYP.NONE, initializer: Initializer = Initializer.EMPTY, modifiers: MutableSet<KModifier> = mutableSetOf(), length: Int = TYP.DEFAULT_INT, vararg tags: Tag) = property(name, typ, mutable, collectionType, initializer, modifiers, length, Tags.of(*tags))
    fun property(name: String, typ: TYP = TYP.STRING, mutable: Mutable = immutable, collectionType: COLLECTIONTYP = COLLECTIONTYP.NONE, initializer: Initializer = Initializer.EMPTY, modifiers: MutableSet<KModifier> = mutableSetOf(), length: Int = TYP.DEFAULT_INT, tags: Tags = Tags.NONE)

    // ===================================================================================================================================
    // ====================   "CLASS" propertys   ====================================================================================
    // ===================================================================================================================================
    fun property(name: String, kclass: KClass<*>, mutable: Mutable, initializer: Initializer, vararg tags: Tag) = property(name, kclass.asTypeName(), kclass.java.isInterface, mutable, COLLECTIONTYP.NONE, initializer, mutableSetOf(), TYP.DEFAULT_INT, Tags.of(*tags))
    fun property(name: String, kclass: KClass<*>, mutable: Mutable = immutable, vararg tags: Tag) = property(name, kclass.asTypeName(), kclass.java.isInterface, mutable, COLLECTIONTYP.NONE, Initializer.EMPTY, mutableSetOf(), TYP.DEFAULT_INT, Tags.of(*tags))
    fun property(name: String, kclass: KClass<*>, mutable: Mutable = immutable, tags: Tags) = property(name, kclass.asTypeName(), kclass.java.isInterface, mutable, COLLECTIONTYP.NONE, Initializer.EMPTY, mutableSetOf(), TYP.DEFAULT_INT, tags)
    fun property(name: String, kclass: KClass<*>, mutable: Mutable = immutable, length: Int = TYP.DEFAULT_INT, vararg tags: Tag) = property(name, kclass.asTypeName(), kclass.java.isInterface, mutable, COLLECTIONTYP.NONE, Initializer.EMPTY, mutableSetOf(), length, Tags.of(*tags))
    fun property(name: String, kclass: KClass<*>, mutable: Mutable = immutable, length: Int = TYP.DEFAULT_INT, tags: Tags) = property(name, kclass.asTypeName(), kclass.java.isInterface, mutable, COLLECTIONTYP.NONE, Initializer.EMPTY, mutableSetOf(), length, tags)
    fun property(name: String, kclass: KClass<*>, collectionType: COLLECTIONTYP = COLLECTIONTYP.NONE, vararg tags: Tag) = property(name, kclass.asTypeName(), kclass.java.isInterface, immutable, collectionType, Initializer.EMPTY, mutableSetOf(), TYP.DEFAULT_INT, Tags.of(*tags))
    fun property(name: String, kclass: KClass<*>, collectionType: COLLECTIONTYP = COLLECTIONTYP.NONE, tags: Tags = Tags.NONE) = property(name, kclass.asTypeName(), kclass.java.isInterface, immutable, collectionType, Initializer.EMPTY, mutableSetOf(), TYP.DEFAULT_INT, tags)
    fun property(name: String, kclass: KClass<*>, mutable: Mutable = immutable, initializer: Initializer = Initializer.EMPTY, length: Int = TYP.DEFAULT_INT, vararg tags: Tag) = property(name, kclass.asTypeName(), kclass.java.isInterface, mutable, COLLECTIONTYP.NONE, initializer, mutableSetOf(), length, Tags.of(*tags))
    fun property(name: String, kclass: KClass<*>, mutable: Mutable = immutable, initializer: Initializer = Initializer.EMPTY, length: Int = TYP.DEFAULT_INT, tags: Tags) = property(name, kclass.asTypeName(), kclass.java.isInterface, mutable, COLLECTIONTYP.NONE, initializer, mutableSetOf(), length, tags)
    fun property(name: String, kclass: KClass<*>, mutable: Mutable = immutable, collectionType: COLLECTIONTYP = COLLECTIONTYP.NONE, vararg tags: Tag) = property(name, kclass.asTypeName(), kclass.java.isInterface, mutable, collectionType, Initializer.EMPTY, mutableSetOf(), TYP.DEFAULT_INT, Tags.of(*tags))
    fun property(name: String, kclass: KClass<*>, mutable: Mutable = immutable, collectionType: COLLECTIONTYP = COLLECTIONTYP.NONE, tags: Tags) = property(name, kclass.asTypeName(), kclass.java.isInterface, mutable, collectionType, Initializer.EMPTY, mutableSetOf(), TYP.DEFAULT_INT, tags)

    fun property(name: String, poetType: TypeName, isInterface: Boolean, mutable: Mutable = immutable, vararg tags: Tag) = property(name, poetType, isInterface, mutable, COLLECTIONTYP.NONE, Initializer.EMPTY, mutableSetOf(), TYP.DEFAULT_INT, Tags.of(*tags))
    fun property(name: String, poetType: TypeName, isInterface: Boolean, mutable: Mutable = immutable, tags: Tags) = property(name, poetType, isInterface, mutable, COLLECTIONTYP.NONE, Initializer.EMPTY, mutableSetOf(), TYP.DEFAULT_INT, tags)
    fun property(name: String, poetType: TypeName, isInterface: Boolean, mutable: Mutable = immutable, length: Int = TYP.DEFAULT_INT, vararg tags: Tag) = property(name, poetType, isInterface, mutable, COLLECTIONTYP.NONE, Initializer.EMPTY, mutableSetOf(), length, Tags.of(*tags))
    fun property(name: String, poetType: TypeName, isInterface: Boolean, mutable: Mutable = immutable, length: Int = TYP.DEFAULT_INT, tags: Tags) = property(name, poetType, isInterface, mutable, COLLECTIONTYP.NONE, Initializer.EMPTY, mutableSetOf(), length, tags)
    fun property(name: String, poetType: TypeName, isInterface: Boolean, collectionType: COLLECTIONTYP = COLLECTIONTYP.NONE, vararg tags: Tag) = property(name, poetType, isInterface, immutable, collectionType, Initializer.EMPTY, mutableSetOf(), TYP.DEFAULT_INT, Tags.of(*tags))
    fun property(name: String, poetType: TypeName, isInterface: Boolean, collectionType: COLLECTIONTYP = COLLECTIONTYP.NONE, tags: Tags = Tags.NONE) = property(name, poetType, isInterface, immutable, collectionType, Initializer.EMPTY, mutableSetOf(), TYP.DEFAULT_INT, tags)
    fun property(name: String, poetType: TypeName, isInterface: Boolean, mutable: Mutable = immutable, initializer: Initializer = Initializer.EMPTY, length: Int = TYP.DEFAULT_INT, vararg tags: Tag) = property(name, poetType, isInterface, mutable, COLLECTIONTYP.NONE, initializer, mutableSetOf(), length, Tags.of(*tags))
    fun property(name: String, poetType: TypeName, isInterface: Boolean, mutable: Mutable = immutable, initializer: Initializer = Initializer.EMPTY, length: Int = TYP.DEFAULT_INT, tags: Tags) = property(name, poetType, isInterface, mutable, COLLECTIONTYP.NONE, initializer, mutableSetOf(), length, tags)
    fun property(name: String, poetType: TypeName, isInterface: Boolean, mutable: Mutable = immutable, collectionType: COLLECTIONTYP = COLLECTIONTYP.NONE, vararg tags: Tag) = property(name, poetType, isInterface, mutable, collectionType, Initializer.EMPTY, mutableSetOf(), TYP.DEFAULT_INT, Tags.of(*tags))
    fun property(name: String, poetType: TypeName, isInterface: Boolean, mutable: Mutable = immutable, collectionType: COLLECTIONTYP = COLLECTIONTYP.NONE, tags: Tags) = property(name, poetType, isInterface, mutable, collectionType, Initializer.EMPTY, mutableSetOf(), TYP.DEFAULT_INT, tags)
    fun property(name: String, poetType: TypeName, isInterface: Boolean, mutable: Mutable = immutable, collectionType: COLLECTIONTYP = COLLECTIONTYP.NONE, initializer: Initializer = Initializer.EMPTY, modifiers: MutableSet<KModifier> = mutableSetOf(), length: Int = TYP.DEFAULT_INT, vararg tags: Tag) = property(name, poetType, isInterface, mutable, collectionType, initializer, modifiers, length, Tags.of(*tags))
    fun property(name: String, poetType: TypeName, isInterface: Boolean, mutable: Mutable = immutable, collectionType: COLLECTIONTYP = COLLECTIONTYP.NONE, initializer: Initializer = Initializer.EMPTY, modifiers: MutableSet<KModifier> = mutableSetOf(), length: Int = TYP.DEFAULT_INT, tags: Tags = Tags.NONE)

    // ===================================================================================================================================
    // ====================   Model properties   =========================================================================================
    // ===================================================================================================================================
    fun property(name: String, modelSubElementRefString: String, mutable: Mutable = immutable, vararg tags: Tag)                                = property(name, modelSubElementRefString, mutable, COLLECTIONTYP.NONE, Initializer.EMPTY, mutableSetOf(), TYP.DEFAULT_INT, Tags(*tags))
    fun property(name: String, modelSubElementRefString: String, mutable: Mutable = immutable, tags: Tags)                                      = property(name, modelSubElementRefString, mutable, COLLECTIONTYP.NONE, Initializer.EMPTY, mutableSetOf(), TYP.DEFAULT_INT, tags)
    fun property(name: String, modelSubElementRefString: String, mutable: Mutable = immutable, length: Int = TYP.DEFAULT_INT, vararg tags: Tag) = property(name, modelSubElementRefString, mutable, COLLECTIONTYP.NONE, Initializer.EMPTY, mutableSetOf(), length, Tags(*tags))
    fun property(name: String, modelSubElementRefString: String, mutable: Mutable = immutable, length: Int = TYP.DEFAULT_INT, tags: Tags)       = property(name, modelSubElementRefString, mutable, COLLECTIONTYP.NONE, Initializer.EMPTY, mutableSetOf(), length, tags)
    fun property(name: String, modelSubElementRefString: String, collectionType: COLLECTIONTYP = COLLECTIONTYP.NONE, vararg tags: Tag)        = property(name, modelSubElementRefString, immutable, collectionType,      Initializer.EMPTY, mutableSetOf(), TYP.DEFAULT_INT, Tags(*tags))
    fun property(name: String, modelSubElementRefString: String, collectionType: COLLECTIONTYP = COLLECTIONTYP.NONE, tags: Tags = Tags.NONE)  = property(name, modelSubElementRefString, immutable, collectionType,      Initializer.EMPTY, mutableSetOf(), TYP.DEFAULT_INT, tags)
    fun property(name: String, modelSubElementRefString: String, mutable: Mutable = immutable, collectionType: COLLECTIONTYP = COLLECTIONTYP.NONE, vararg tags: Tag)                        = property(name, modelSubElementRefString, mutable, collectionType,      Initializer.EMPTY, mutableSetOf(), TYP.DEFAULT_INT, Tags(*tags))
    fun property(name: String, modelSubElementRefString: String, mutable: Mutable = immutable, collectionType: COLLECTIONTYP = COLLECTIONTYP.NONE, tags: Tags)                              = property(name, modelSubElementRefString, mutable, collectionType,      Initializer.EMPTY, mutableSetOf(), TYP.DEFAULT_INT, tags)
    fun property(name: String, modelSubElementRefString: String, mutable: Mutable = immutable, initializer: Initializer = Initializer.EMPTY, length: Int = TYP.DEFAULT_INT, vararg tags: Tag) = property(name, modelSubElementRefString, mutable, COLLECTIONTYP.NONE, initializer, mutableSetOf(), length, Tags(*tags))
    fun property(name: String, modelSubElementRefString: String, mutable: Mutable = immutable, initializer: Initializer = Initializer.EMPTY, length: Int = TYP.DEFAULT_INT, tags: Tags)       = property(name, modelSubElementRefString, mutable, COLLECTIONTYP.NONE, initializer, mutableSetOf(), length, tags)
    fun property(name: String, modelSubElementRefString: String, mutable: Mutable = immutable, collectionType: COLLECTIONTYP = COLLECTIONTYP.NONE, initializer: Initializer = Initializer.EMPTY, length: Int = TYP.DEFAULT_INT, vararg tags: Tag) = property(name, modelSubElementRefString, mutable, collectionType, initializer, mutableSetOf(), length, Tags(*tags))
    fun property(name: String, modelSubElementRefString: String, mutable: Mutable = immutable, collectionType: COLLECTIONTYP = COLLECTIONTYP.NONE, initializer: Initializer = Initializer.EMPTY, modifiers: MutableSet<KModifier>, length: Int = TYP.DEFAULT_INT, tags: Tags)

    fun property(name: String, modelRefString: String, modelSubelement: MODELREFENUM, mutable: Mutable = immutable, vararg tags: Tag)                                = property(name, modelRefString, modelSubelement, mutable, COLLECTIONTYP.NONE, Initializer.EMPTY, mutableSetOf(), TYP.DEFAULT_INT, Tags(*tags))
    fun property(name: String, modelRefString: String, modelSubelement: MODELREFENUM, mutable: Mutable = immutable, tags: Tags)                                      = property(name, modelRefString, modelSubelement, mutable, COLLECTIONTYP.NONE, Initializer.EMPTY, mutableSetOf(), TYP.DEFAULT_INT, tags)
    fun property(name: String, modelRefString: String, modelSubelement: MODELREFENUM, mutable: Mutable = immutable, length: Int = TYP.DEFAULT_INT, vararg tags: Tag) = property(name, modelRefString, modelSubelement, mutable, COLLECTIONTYP.NONE, Initializer.EMPTY, mutableSetOf(), length,          Tags(*tags))
    fun property(name: String, modelRefString: String, modelSubelement: MODELREFENUM, mutable: Mutable = immutable, length: Int = TYP.DEFAULT_INT, tags: Tags)       = property(name, modelRefString, modelSubelement, mutable, COLLECTIONTYP.NONE, Initializer.EMPTY, mutableSetOf(), length,          tags)
    fun property(name: String, modelRefString: String, modelSubelement: MODELREFENUM, collectionType: COLLECTIONTYP = COLLECTIONTYP.NONE, vararg tags: Tag)        = property(name, modelRefString, modelSubelement, immutable, collectionType,      Initializer.EMPTY, mutableSetOf(), TYP.DEFAULT_INT, Tags(*tags))
    fun property(name: String, modelRefString: String, modelSubelement: MODELREFENUM, collectionType: COLLECTIONTYP = COLLECTIONTYP.NONE, tags: Tags = Tags.NONE)  = property(name, modelRefString, modelSubelement, immutable, collectionType,      Initializer.EMPTY, mutableSetOf(), TYP.DEFAULT_INT, tags)
    fun property(name: String, modelRefString: String, modelSubelement: MODELREFENUM, mutable: Mutable = immutable, collectionType: COLLECTIONTYP = COLLECTIONTYP.NONE, vararg tags: Tag) = property(name, modelRefString, modelSubelement, mutable, collectionType,      Initializer.EMPTY, mutableSetOf(), TYP.DEFAULT_INT, Tags(*tags))
    fun property(name: String, modelRefString: String, modelSubelement: MODELREFENUM, mutable: Mutable = immutable, collectionType: COLLECTIONTYP = COLLECTIONTYP.NONE, tags: Tags)       = property(name, modelRefString, modelSubelement, mutable, collectionType,      Initializer.EMPTY, mutableSetOf(), TYP.DEFAULT_INT, tags)
    fun property(name: String, modelRefString: String, modelSubelement: MODELREFENUM, mutable: Mutable = immutable, initializer: Initializer = Initializer.EMPTY, length: Int = TYP.DEFAULT_INT, vararg tags: Tag) = property(name, modelRefString, modelSubelement, mutable, COLLECTIONTYP.NONE,      initializer, mutableSetOf(), length, Tags(*tags))
    fun property(name: String, modelRefString: String, modelSubelement: MODELREFENUM, mutable: Mutable = immutable, initializer: Initializer = Initializer.EMPTY, length: Int = TYP.DEFAULT_INT, tags: Tags)       = property(name, modelRefString, modelSubelement, mutable, COLLECTIONTYP.NONE,      initializer, mutableSetOf(), length, tags)
    fun property(name: String, modelRefString: String, modelSubelement: MODELREFENUM, mutable: Mutable = immutable, collectionType: COLLECTIONTYP = COLLECTIONTYP.NONE, initializer: Initializer = Initializer.EMPTY, length: Int = TYP.DEFAULT_INT, vararg tags: Tag) = property(name, modelRefString, modelSubelement, mutable, collectionType,      initializer, mutableSetOf(), length, Tags(*tags))
    fun property(name: String, modelRefString: String, modelSubelement: MODELREFENUM, mutable: Mutable = immutable, collectionType: COLLECTIONTYP = COLLECTIONTYP.NONE, initializer: Initializer = Initializer.EMPTY, modifiers: MutableSet<KModifier> = mutableSetOf(), length: Int = TYP.DEFAULT_INT, tags: Tags)


    fun property(name: String, modelSubElementRef: IDslRef, mutable: Mutable = immutable, vararg tags: Tag) = property(name, modelSubElementRef, mutable, COLLECTIONTYP.NONE, Initializer.EMPTY, mutableSetOf(), TYP.DEFAULT_INT, Tags(*tags))
    fun property(name: String, modelSubElementRef: IDslRef, mutable: Mutable = immutable, collectionType: COLLECTIONTYP = COLLECTIONTYP.NONE, vararg tags: Tag) = property(name, modelSubElementRef, mutable, collectionType, Initializer.EMPTY, mutableSetOf(), TYP.DEFAULT_INT, Tags(*tags))
    fun property(name: String, modelSubElementRef: IDslRef, mutable: Mutable = immutable, collectionType: COLLECTIONTYP = COLLECTIONTYP.NONE, initializer: Initializer = Initializer.EMPTY, length: Int = TYP.DEFAULT_INT, vararg tags: Tag) = property(name, modelSubElementRef, mutable, collectionType, initializer, mutableSetOf(), length, Tags(*tags))
    fun property(name: String, modelSubElementRef: IDslRef, mutable: Mutable = immutable, collectionType: COLLECTIONTYP = COLLECTIONTYP.NONE, initializer: Initializer = Initializer.EMPTY, modifiers: MutableSet<KModifier> = mutableSetOf(), length: Int = TYP.DEFAULT_INT, tags: Tags = Tags.NONE)
}

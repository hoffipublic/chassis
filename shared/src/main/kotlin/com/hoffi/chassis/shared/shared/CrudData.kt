package com.hoffi.chassis.shared.shared

import com.hoffi.chassis.shared.dsl.IDslRef

open class CrudData(businessName: String, targetDslRef: IDslRef, sourceDslRef: IDslRef, val crud: CRUD)
    : AHasCopyBoundrysData(businessName, targetDslRef, sourceDslRef)
{
    override fun toString() = "Crud('$businessName', ${String.format("%-6s", crud)}, '${targetDslRef.toString(2)}' <-- '${sourceDslRef.toString(2)}', " +
            theCopyBoundrys.values.joinToString("") { it.toString() }.ifBlank { "COPYALL" } + ")"

    sealed class CRUD {
        //override fun toString() = "$simpleName${if(this.variant::class.simpleName != "DEFAULT") "(${this.variant::class.simpleName})" else ""}"
        val simpleName = this::class.simpleName!!
        abstract val variant: SEALEDVARIANT
        sealed class SEALEDVARIANT
        class CREATE(override val variant: CREATEVARIANT = CREATEVARIANT.DEFAULT) : CRUD() {
            override fun toString() = "$simpleName${if(variant::class.simpleName != "DEFAULT") "(${variant::class.simpleName})" else ""}"
            sealed class CREATEVARIANT(): SEALEDVARIANT() {
                data object DEFAULT : CREATEVARIANT()
            }
        }
        class READ(override val variant: READVARIANT = READVARIANT.JOIN) : CRUD() {
            val viaJoins: READ
                get() = READ
            val viaSelects: READ
                get() = READSELECT
            val viaAllVariants: READ
                get() = READALLVARIANTS

            override fun toString() = "$simpleName${if(variant::class.simpleName != "DEFAULT") "(${variant::class.simpleName})" else ""}"
            sealed class READVARIANT(): SEALEDVARIANT() {
                data object ALLVARIANTS : READVARIANT()
                data object JOIN : READVARIANT()
                data object SELECT : READVARIANT()
            }
        }
        class UPDATE(override val variant: UPDATEVARIANT = UPDATEVARIANT.DEFAULT) : CRUD() {
            override fun toString() = "$simpleName${if(variant::class.simpleName != "DEFAULT") "(${variant::class.simpleName})" else ""}"
            sealed class UPDATEVARIANT(): SEALEDVARIANT() {
                data object DEFAULT : UPDATEVARIANT()
            }
        }
        class DELETE(override val variant: DELETEVARIANT = DELETEVARIANT.DEFAULT) : CRUD() {
            override fun toString() = "$simpleName${if(variant::class.simpleName != "DEFAULT") "(${variant::class.simpleName})" else ""}"
            sealed class DELETEVARIANT(): SEALEDVARIANT() {
                data object DEFAULT : DELETEVARIANT()
            }
        }
        companion object {
            val CREATE = CREATE()
            val READ = READ()
            val READALLVARIANTS = READ(CRUD.READ.READVARIANT.ALLVARIANTS)
            val READSELECT = READ(CRUD.READ.READVARIANT.SELECT)
            val UPDATE = UPDATE()
            val DELETE = DELETE()
            val entries = listOf(CREATE, READ, UPDATE, DELETE)
        }
        fun isSameAs(other: Any?) = this == other && variant == (other as CRUD).variant
        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (other !is CRUD) return false
            //if (!super.equals(other)) return false
            if (simpleName != other.simpleName) return false
            // variant is not part of equals here, as all variants go to the same generated target-class
            //if (variant != other.variant) return false

            return true
        }
        override fun hashCode(): Int {
            var result = simpleName.hashCode()
            // variant is not part of equals here, as all variants go to the same generated target-class
            //result = 31 * result + variant.hashCode()
            return result
        }
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is CrudData) return false
        if (businessName != other.businessName) return false
        if (targetDslRef != other.targetDslRef) return false
        if (sourceDslRef != other.sourceDslRef) return false
        if (crud != other.crud) return false
        return true
    }
    override fun hashCode(): Int {
        var result = businessName.hashCode()
        result = 31 * result + targetDslRef.hashCode()
        result = 31 * result + sourceDslRef.hashCode()
        result = 31 * result + crud.hashCode()
        return result
    }
}

class SynthCrudData private constructor(businessName: String, targetDslRef: IDslRef, sourceDslRef: IDslRef, crud: CRUD, val via: String)
    : CrudData(businessName, targetDslRef, sourceDslRef, crud) {
    override fun toString() = "Synth${super.toString()}->\"$via\""
    companion object {
        fun create(targetDslRef: IDslRef, sourceDslRef: IDslRef, originalCrud: CrudData, via: String): SynthCrudData {
            val synthCrudData = SynthCrudData(originalCrud.businessName, targetDslRef, sourceDslRef, originalCrud.crud, via)
            synthCrudData.theCopyBoundrys.putAll(originalCrud.theCopyBoundrys)
            return synthCrudData
        }
    }
}

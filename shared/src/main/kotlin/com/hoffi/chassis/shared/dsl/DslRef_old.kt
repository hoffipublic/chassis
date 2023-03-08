package com.hoffi.chassis.shared.dsl

//import com.hoffi.chassis.chassismodel.C
//import com.hoffi.chassis.chassismodel.dsl.DslException
//
//data class DslRefAtom_old(val dslStructuralTypeEither: DslStructuralTypeEither, val atomName: String)
///**
// * a means of an unambiguous reference chassis-dsl elements e.g. either a (model|api|...)group, a model|api|... within it or a (model|api|...)Subtype(dtoType,tableType,filler,...)
// *       0          1      2       3            4            5
// * "discriminator:group:element:subelement:finerGrained1:finerGrained2
// */
//sealed class DslRef_old {
//    protected val refStringParts = mutableListOf<String>()
//    val discriminatorString = refStringParts[0]
//    val groupString = refStringParts[1]
//    val elementString = refStringParts[2]
//    val subElementString = refStringParts[3]
//    val finer1 = refStringParts[4]
//    val finer2 = refStringParts[5]
//    val finer3 = refStringParts[6]
//    protected val dslRefParts = mutableListOf<DslRefAtom_old>()
//    val dslDiscriminatorRef = dslRefParts[0]
//    val dslGroupRef = dslRefParts[1]
//    val dslElementRef = dslRefParts[2]
//    val dslSubElementRef = dslRefParts[3]
//    val dslfiner1Ref = dslRefParts[4]
//    val dslfiner2Ref = dslRefParts[5]
//    val dslfiner3Ref = dslRefParts[6]
//    fun joinRefStringParts() = refStringParts.joinToString(":")
//
//    companion object {
//        val NULL = NULLREF()
//        fun assertPlainString(s: String) = s.also { assert(s.indexOf(";") < 0 && s.indexOf(":") < 0) }
//        fun extractDiscriminatorString(refString: String)         = refString.split(";").first()
//        fun extractGroupString(refString: String)                 = try { refString.split(";").drop(1).first() } catch(ex: Exception) { throw DslException("DslRef_old no group (field 2): '$refString'", ex) }
//        fun extractElementString(refString: String)               = try { refString.split(";").drop(2).first() } catch(ex: Exception) { throw DslException("DslRef_old no element (field 3): '$refString'", ex) }
//        fun extractSubElementString(refString: String)            = try { refString.split(";").drop(3).first() } catch(ex: Exception) { throw DslException("DslRef_old no subElement/model (field 4): '$refString'", ex) }
//        fun extractFinerString(refString: String, level: Int = 1) = try { refString.split(";").drop(3+level).first() } catch(ex: Exception) { throw DslException("DslRef_old no finer (field 5): '$refString'", ex) }
//    }
//
//    class NULLREF(val name: String = C.NULLSTRING) : DslRef_old()
//
//    class DslDiscriminatorRef constructor(parentDslRef: DslRef_old, discriminatorName: String) : DslRef_old() {
//        //region class DslDiscriminatorRef
//        init {
//            refStringParts.add(assertPlainString(discriminatorName))
//            dslRefParts.add(this)
//        }
//        override fun toString(): String = joinRefStringParts()
//
//        companion object {
//            val NULL = DslDiscriminatorRef(DslRef_old.NULL, C.NULLSTRING)
//            fun from(refString: String) = DslDiscriminatorRef(DslRef_old.NULL, extractDiscriminatorString(refString))
//        }
//        //endregion
//    }
//    class DslGroupRef constructor(parentDslRef: DslDiscriminatorRef, groupName: String) : DslRef_old() {
//        //region class DslGroupRef
//        init {
//            with (refStringParts) {
//                add(parentDslRef.discriminatorString)
//                add(assertPlainString(groupName))
//            }
//            dslRefParts.add(parentDslRef)
//            dslRefParts.add(this)
//        }
//        override fun toString(): String = joinRefStringParts()
//
//        //region companion object { ...
//        companion object {
//            val NULL = DslGroupRef(DslDiscriminatorRef.NULL, C.NULLSTRING)
//            fun from(refString: String): DslGroupRef {
//                val dslDiscriminatorRef = DslDiscriminatorRef(DslRef_old.NULL, DslRef_old.extractDiscriminatorString(refString))
//                return DslGroupRef(dslDiscriminatorRef, DslRef_old.extractGroupString(refString))
//            }
//        }
//        //endregion
//    }
//    class DslElementRef constructor(parentDslRef: DslGroupRef, val elementName: String) : DslRef_old() {
//        //region class DslElementRef
//        init {
//            with (refStringParts) {
//                add(parentDslRef.discriminatorString)
//                add(parentDslRef.groupString)
//                add(assertPlainString(elementName))
//            }
//            dslRefParts.add(parentDslRef.dslDiscriminatorRef)
//            dslRefParts.add(parentDslRef.dslGroupRef)
//            dslRefParts.add(this)
//        }
//        override fun toString(): String = joinRefStringParts()
//
//        //region companion object { ...
//        companion object {
//            val NULL = DslElementRef(DslGroupRef.NULL, C.NULLSTRING)
//            fun from(refString: String): DslElementRef {
//                val dslDiscriminatorRef = DslDiscriminatorRef(DslRef_old.NULL, extractDiscriminatorString(refString))
//                val dslGroupRef = DslGroupRef(dslDiscriminatorRef, extractGroupString(refString))
//                return DslElementRef(dslGroupRef, extractElementString(refString))
//            }
//        }
//    }
//
//    class ModelDtoRef private constructor(val modelRef: ModelRef) : DslRef_old(), ModelEmbossing {
//        override fun toString(): String = "ModelDtoRef(modelRef=$modelRef)"
//        companion object {
//            fun from(modelRef: ModelRef) = ModelDtoRef(modelRef)
//        }
//        override fun equals(other: Any?): Boolean {
//            if (this === other) return true
//            if (other !is ModelDtoRef) return false
//            if (modelRef != other.modelRef) return false
//            return true
//        }
//        override fun hashCode(): Int = modelRef.hashCode()
//    }
//    class ModelTableRef private constructor(val modelRef: ModelRef) : DslRef_old(), ModelEmbossing {
//        override fun toString(): String = "ModelTableRef(modelRef=$modelRef)"
//        companion object {
//            fun from(modelRef: ModelRef) = ModelTableRef(modelRef)
//        }
//        override fun equals(other: Any?): Boolean {
//            if (this === other) return true
//            if (other !is ModelTableRef) return false
//            if (modelRef != other.modelRef) return false
//            return true
//        }
//        override fun hashCode(): Int = modelRef.hashCode()
//    }
//    class ModelFillerRef private constructor(val modelRef: ModelRef) : DslRef_old(), ModelEmbossing {
//        override fun toString(): String = "ModelFillerRef(modelRef=$modelRef)"
//        companion object {
//            fun from(modelRef: ModelRef) = ModelFillerRef(modelRef)
//        }
//        override fun equals(other: Any?): Boolean {
//            if (this === other) return true
//            if (other !is ModelFillerRef) return false
//            if (modelRef != other.modelRef) return false
//            return true
//        }
//        override fun hashCode(): Int = modelRef.hashCode()
//    }
//    class ApigroupRef(groupName: String) : GroupRef(groupName)
//    class ApiRef(elementName: String) : ElementRef(elementName)
//    class ApiFunRef(apiRef: ApiRef) : DslRef_old()
//
//
//    class FinerGrainedRef private constructor(val subElementName: String, val elementRef: ElementRef) : DslRef_old(), FinerGrainedEmbossing {
//        override fun toString(): String = "FinerGrainedRef('${subElementName}' below modelRef=$elementRef)"
//        companion object {
//            fun from(subElementName: String, elementRef: ElementRef) = FinerGrainedRef(subElementName, elementRef)
//        }
//        override fun equals(other: Any?): Boolean {
//            if (this === other) return true
//            if (other !is FinerGrainedRef) return false
//            if (subElementName != other.subElementName) return false
//            if (elementRef != other.elementRef) return false
//            return true
//        }
//        override fun hashCode(): Int {
//            var result = subElementName.hashCode()
//            result = 31 * result + elementRef.hashCode()
//            return result
//        }
//    }
//
//    /** generic equals overload for all DslRef_old's */
//    override fun equals(other: Any?): Boolean {
//        if (this === other) return true
//        if (other !is DslElementRef) return false
//
//        if (refStringParts != other.refStringParts) return false
//
//        return true
//    }
//    /** generic equals overload for all DslRef_old's */
//    override fun hashCode(): Int {
//        return refStringParts.hashCode()
//    }
//}
//
//interface ModelEmbossing
//interface ApiEmbossing
//interface FinerGrainedEmbossing

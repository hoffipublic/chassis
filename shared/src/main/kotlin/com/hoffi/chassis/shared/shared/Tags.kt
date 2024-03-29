package com.hoffi.chassis.shared.shared

sealed class Tag {
    override fun toString() = this::class.simpleName!!
    class DEFAULT_INITIALIZER : Tag()
    class NO_DEFAULT_INITIALIZER : Tag()
    class CONSTRUCTOR : Tag()
    class CONSTRUCTOR_INSUPER : Tag()
    class COLLECTION_IMMUTABLE : Tag()
    class HASH_MEMBER : Tag()
    class PRIMARY : Tag()
    class TO_STRING_MEMBER : Tag()
    class TRANSIENT : Tag()
    class NULLABLE : Tag()
    class NULLABLE_GENERICTYPE : Tag()
    companion object {
        val DEFAULT_INITIALIZER = DEFAULT_INITIALIZER()       // overrule default for constructors and functions
        val NO_DEFAULT_INITIALIZER = NO_DEFAULT_INITIALIZER() // overrule default for properties
        val CONSTRUCTOR = CONSTRUCTOR()
        val CONSTRUCTOR_INSUPER = CONSTRUCTOR_INSUPER()
        val COLLECTION_IMMUTABLE = COLLECTION_IMMUTABLE()
        val HASH_MEMBER = HASH_MEMBER()
        val PRIMARY = PRIMARY()
        val TO_STRING_MEMBER = TO_STRING_MEMBER()
        val TRANSIENT = TRANSIENT()
        val NULLABLE = NULLABLE()
        val NULLABLE_GENERICTYPE = NULLABLE_GENERICTYPE()
    }
}

class Tags {
    override fun toString(): String = tags.joinToString()
    val tags = mutableSetOf<Tag>()
    constructor(vararg tags: Tag) {
        this.tags.addAll(tags)
    }
    constructor(tags: Set<Tag>) {
        this.tags.addAll(tags)
    }
    fun copy() = Tags().also { it.tags.addAll(tags) }

    companion object {
        val NONE = Tags()
        fun of(vararg tags: Tag) = Tags(*tags)
    }

    fun isNotEmpty(): Boolean = tags.isNotEmpty()
    infix operator fun contains(other: Tags) = tags.containsAll(other.tags)
    infix operator fun contains(other: Tag) = tags.contains(other)
    operator fun plus(other: Tags) { tags.addAll(other.tags) }
    operator fun plus(other: Tag) { tags.add(other) }
    operator fun minus(other: Tags) { tags.removeAll(other.tags) }
    operator fun minus(other: Tag) { tags.remove(other) }

    override fun hashCode(): Int = tags.hashCode()
    override fun equals(other: Any?): Boolean {
        return if (this === other) return true else if (other !is Tags) return false
        else tags.size == other.tags.size && tags.toSet() == other.tags.toSet()
    }
}

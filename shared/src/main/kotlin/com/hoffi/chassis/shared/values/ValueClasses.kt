package com.hoffi.chassis.shared.values

//class DslPath(var pathOkio: Path = C.NULLSTRING.toPath()) {
//    override fun toString() = "dsl:$pathOkio"
//    fun asPath(normalize: Boolean) = if (normalize) pathOkio.normalized() else pathOkio
//    companion object {
//        val NULL = DslPath(C.NULLSTRING.toPath())
//        fun String.toDslPath(normalize: Boolean): DslPath = DslPath(this.toPath(normalize))
//        fun Path.toDslPath(normalize: Boolean): DslPath = DslPath(if (normalize) this.normalized() else this)
//        fun File.toDslPath(normalize: Boolean): DslPath = DslPath(this.toOkioPath(normalize))
//        fun java.nio.file.Path.toDslPath(normalize: Boolean) = DslPath(this.toOkioPath(normalize))
//    }
//    override fun equals(other: Any?): Boolean {
//        if (this === other) return true
//        if (other is Path) return pathOkio == other
//        if (other !is DslPath) return false
//        return pathOkio == other.pathOkio
//    }
//    override fun hashCode() = pathOkio.hashCode()
//    operator fun div(child: String): DslPath = DslPath(this.pathOkio / child)
//    operator fun div(child: Path): DslPath = DslPath(this.pathOkio / child)
//    //operator fun divAssign(child: String) { this.pathOkio /= child }
//    //operator fun divAssign(child: Path) { this.pathOkio /= child }
//}

package com.hoffi.chassis.shared

import com.squareup.kotlinpoet.ClassName
import java.nio.file.Paths

data class GenTargetDir(
    val basedir: String,
    val subdirs: List<String> = emptyList()
) {
    fun dirPath() = Paths.get(basedir, *(subdirs.toTypedArray()))
    fun dirPath(className: ClassName) = Paths.get(basedir, *(subdirs + className.packageName.split('.')).toTypedArray())
}
fun GenTargetDir.newSubdir(vararg subdirOfBasedir: String): GenTargetDir {
    return this.copy(subdirs = subdirOfBasedir.toList())
}
fun GenTargetDir.addSubdir(vararg extendedSubdir: String): GenTargetDir {
    val newSubdirs = subdirs + extendedSubdir.toList()
    return this.copy(subdirs = newSubdirs)
}

//fun main() {
//    val genTargetDir = GenTargetDir("eins", listOf("zwei/drei", "vier"))
//    println(genTargetDir.dirPath())
//    println(genTargetDir.dirPath(GenTargetDir::class.java.asClassName()))
//    println()
//    println(genTargetDir.newSubdir("new1/new2", "new3").dirPath())
//    println(genTargetDir.addSubdir("new1/new2", "new3").dirPath())
//}

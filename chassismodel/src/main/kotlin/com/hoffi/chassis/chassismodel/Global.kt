package com.hoffi.chassis.chassismodel

import kotlin.math.abs
import kotlin.math.roundToInt

fun main(@Suppress("UNUSED_PARAMETER") args: Array<String>) {
    if (true) {
        val copyableHeading = "String related extension functions".padForHeader(78)
        println(copyableHeading)
    } else {
        casingToStdout()
    }
}

    //
    //#!/bin/bash # e.g. executable file ~/bin/header
    //if [[ -z $1 ]]; then echo "no args!" >&2 ; exit 1 ; fi
    //kheader.kts "$@" | tee /dev/tty | pbcopy
    //
    //#!/usr/bin/env kscript # e.g. executable file ~/bin/kheader.kts
    //import kotlin.math.abs
    //import kotlin.math.roundToInt
    //if (args.isEmpty()) { System.err.println("no args given! synopsis: header [Center|Left|Right] [Filled] 'some multiline string'") ; System.exit(-1) }
    //var orientation = 1
    //var padWithPadChar = false
    //var skipArgs = 0
    //for (arg in args.take(args.size - 1)) {
    //    when {
    //        arg.equals("LEFT", ignoreCase = true) -> { orientation = 1 ; skipArgs++ }
    //        arg.equals("RIGHT", ignoreCase = true) -> { orientation = 2 ; skipArgs++ }
    //        arg.equals("CENTER", ignoreCase = true) -> { orientation = 3 ; skipArgs++ }
    //        arg.equals("FILLED", ignoreCase = true) -> { padWithPadChar = true ; skipArgs++ }
    //    }
    //}
    //println(args.drop(skipArgs).joinToString(" ").padForHeader(78, orientation, padWithPadChar = padWithPadChar) )

public fun <T> Iterable<T>.joinToStringQuoted(limit: Int = -1, truncated: CharSequence = "...", transform: ((T) -> CharSequence)? = null): String {
    return joinTo(StringBuilder(), "\", \"", "\"", "\"", limit, truncated, transform).toString()
}
public fun <T> Iterable<T>.joinToStringSingleQuoted(limit: Int = -1, truncated: CharSequence = "...", transform: ((T) -> CharSequence)? = null): String {
    return joinTo(StringBuilder(), "', '", "'", "'", limit, truncated, transform).toString()
}

/** titlecase first char only */
fun String.C() = replaceFirstChar { c -> if (c.isLowerCase()) c.titlecase(C.LOCALE) else c.toString() }
/** downcase first  char only */
fun String.c() = replaceFirstChar { c -> c.lowercase() }

// better use via instantiating a MixedCaseString value class, otherwise polluting the String class.
//fun String.toCamelCase() = MixedCaseString(this).toCamelCase()
//fun String.toUpperCamelCase() = MixedCaseString(this).toUpperCamelCase()
//fun String.toLowerCamelCase() = MixedCaseString(this).toLowerCamelCase()
//fun String.toSnakeCase() = MixedCaseString(this).toSnakeCase()
//fun String.toUpperSnakeCase() = MixedCaseString(this).toUpperSnakeCase()
//fun String.toLowerSnakeCase() = MixedCaseString(this).toLowerSnakeCase()
//fun String.toUpperTitleSnakeCase() = MixedCaseString(this).toUpperTitleSnakeCase()
//fun String.toLowerTitleSnakeCase() = MixedCaseString(this).toLowerTitleSnakeCase()
//fun String.toKebabCase() = MixedCaseString(this).toKebabCase()
//fun String.toUpperKebabCase() = MixedCaseString(this).toUpperKebabCase()
//fun String.toLowerKebabCase() = MixedCaseString(this).toLowerKebabCase()
//fun String.toUpperTitleKebabCase() = MixedCaseString(this).toUpperTitleKebabCase()
//fun String.toLowerTitleKebabCase() = MixedCaseString(this).toLowerTitleKebabCase()
@JvmInline
value class MixedCaseString(private val s: String) {
    object RE {
        val camelValidationRegex = "^[^-_]*".toRegex()
        // val camelRegex = "(?<=[a-zA-Z])[A-Z]".toRegex() // also split between consecutive uppercase letters
        val camelRegex = "(?<=[a-z])[A-Z]".toRegex() // keep consecutive uppercase letters together
        val fromMixedRegex = "[-_]+[^-_]*".toRegex()
        val fromMixedFirst = "^[^-_]+(?=[-_])".toRegex()
        val fromMixedCharsRegex = "[-_]+".toRegex()
        val snakeKebabTitleRegex = "([-_]+[a-zA-Z0-9])".toRegex()
    }
    // camel conversion only occurs if it IS a mixed-string (meaning it contains [-_] somewhere) otherwise the string won't be altered
    private fun toLowerCamelFirstPart(wholeString: String): MixedCaseString = MixedCaseString(RE.fromMixedFirst.replace(wholeString) { it.value.lowercase() })
    private fun toCamelConsecutiveParts(): String = RE.fromMixedRegex.replace(s) { it.value.replace(RE.fromMixedCharsRegex, "").lowercase().replaceFirstChar { innerMatch->innerMatch.titlecase(C.LOCALE)} }

    fun toCamelCase(): String { if (s.isEmpty()) return ""; val firstChar = s[0]
                              return toLowerCamelFirstPart(s).toCamelConsecutiveParts().replaceFirstChar {firstChar} }
    fun toLowerCamelCase(): String = toLowerCamelFirstPart(s).toCamelConsecutiveParts()
    fun toUpperCamelCase(): String = toLowerCamelFirstPart(s).toCamelConsecutiveParts().replaceFirstChar { it.titlecase(C.LOCALE) }
    fun toSnakeCase(): String      = RE.camelRegex.replace(s) { "_${it.value}" }.replace(RE.fromMixedCharsRegex, "_")
    fun toLowerSnakeCase(): String = toSnakeCase().lowercase()
    fun toUpperSnakeCase(): String = toSnakeCase().uppercase()
    fun toLowerTitleSnakeCase(): String = RE.snakeKebabTitleRegex.replace(toLowerSnakeCase()) { it.value.uppercase() }
    fun toUpperTitleSnakeCase(): String = toLowerTitleSnakeCase().replaceFirstChar { c -> c.titlecase(C.LOCALE) }
    fun toKebabCase(): String      = RE.camelRegex.replace(s) { "-${it.value}" }.replace(RE.fromMixedCharsRegex, "-")
    fun toLowerKebabCase(): String = toKebabCase().lowercase()
    fun toUpperKebabCase(): String = toKebabCase().uppercase()
    fun toLowerTitleKebabCase(): String = RE.snakeKebabTitleRegex.replace(toLowerKebabCase()) { it.value.uppercase() }
    fun toUpperTitleKebabCase(): String =  toLowerTitleKebabCase().replaceFirstChar { c -> c.titlecase(C.LOCALE) }
}

fun casingToStdout() {
    val exampleList = listOf(
        "oneTwoThree", "OneTwoThree", "oneTWOThree", "ONETwoThree",
        "one-Two_Three", "One-Two_Three", "one-TWO_Three", "ONE-Two_Three", "one-two_three", "ONE-TWO-THREE",
        "one--Two___Three", "One--Two___Three", "one--TWO___Three", "ONE--Two___Three", "one--two___three", "ONE--TWO--THREE",
        "one--Two_-_Three", "One--Two-_-Three", "one--TWO_-_Three", "ONE--Two_-_Three", "one--two_-_three", "ONE--TWO--THREE",
        "",
        "single"
    )

    print("| " + "original".padCenter(16))
    print(" | " + "Camel".padCenter(16))
    print(" | " + "UpperCamel".padCenter(16))
    print(" | " + "LowerCamel".padCenter(16))
    print(" | " + "Snake".padCenter(16))
    print(" | " + "UpperSnake".padCenter(16))
    print(" | " + "LowerSnake".padCenter(16))
    print(" | " + "UpTitleSnake".padCenter(16))
    print(" | " + "LoTitleSnake".padCenter(16))
    print(" | " + "Kebab".padCenter(16))
    print(" | " + "UpperKebab".padCenter(16))
    print(" | " + "LowerKebab".padCenter(16))
    print(" | " + "UpTitleKebab".padCenter(16))
    print(" | " + "LoTitleKebab".padCenter(16))
    println(" |")
    for (ex in exampleList) {
        print("| " + "%-16s".format(ex))
        print(" | " + "%-16s".format(MixedCaseString(ex).toCamelCase()))
        print(" | " + "%-16s".format(MixedCaseString(ex).toUpperCamelCase()))
        print(" | " + "%-16s".format(MixedCaseString(ex).toLowerCamelCase()))
        print(" | " + "%-16s".format(MixedCaseString(ex).toSnakeCase()))
        print(" | " + "%-16s".format(MixedCaseString(ex).toUpperSnakeCase()))
        print(" | " + "%-16s".format(MixedCaseString(ex).toLowerSnakeCase()))
        print(" | " + "%-16s".format(MixedCaseString(ex).toUpperTitleSnakeCase()))
        print(" | " + "%-16s".format(MixedCaseString(ex).toLowerTitleSnakeCase()))
        print(" | " + "%-16s".format(MixedCaseString(ex).toKebabCase()))
        print(" | " + "%-16s".format(MixedCaseString(ex).toUpperKebabCase()))
        print(" | " + "%-16s".format(MixedCaseString(ex).toLowerKebabCase()))
        print(" | " + "%-16s".format(MixedCaseString(ex).toUpperTitleKebabCase()))
        print(" | " + "%-16s".format(MixedCaseString(ex).toLowerTitleKebabCase()))
        println(" |")
    }
}

//====================================================================================================
//==================   String.padCenter() and String.padForHeader(boxLines = 1)   ====================
//====================================================================================================
fun String.padCenter(fixedLength: Int = 120, padChar: Char = ' ', postfix: String = "", prefix: String = ""): String {
    val totalLength = this.length + prefix.length + postfix.length
    if (totalLength >= fixedLength) { return "$prefix$this$postfix" } // line doesn't fit into totalLength
    val targetStringLength = fixedLength - postfix.length - prefix.length
    val leftover = targetStringLength - this.length
    val result = padChar.toString().repeat((leftover/2)) + this + padChar.toString().repeat((leftover/2f).roundToInt())
    return "$prefix$result$postfix"
}

/**
 * pads the string to given fixedLength
 * prepending and appending header chars of length headerCharsFixedLength (values < 0 will be treated as 0)
 *
 * fixedLength is the length of the returned string _including_ the headerChars (=totalLength)
 * orientation <= 1 LEFT, 2 RIGHT, 3 CENTERED
 * headerCharsFixedLength is the length of the headerChars that are prepended and appended
 * if padWithPadChar == true pad with headerChars instead of spacesAroundChars (obviously making headerCharsFixedLength being ignored)
 * boxLines: if >0 add lines of fixedLength of headerChar before and after
 */
fun String.padForHeader(
    fixedLength: Int = 120,
    orientation: Int = 1,
    headerChar: Char = '=',
    headerCharsFixedLength: Int = 3,
    spacesAround: Int = 2,
    spacesAroundChar: Char = ' ',
    padWithPadChar: Boolean = false,
    boxLines: Int = 1
): String {
    val pad = headerChar.toString()
    val lines = this.split("""\n|\\n""".toRegex())
    val bl = pad.repeat(fixedLength)
    val resultLines = MutableList(boxLines) { bl }
    for (line in lines) {
        if (line.length >= fixedLength) { resultLines.add(line); continue } // line doesn't fit into totalLength
        val headerCharsLength = if (headerCharsFixedLength < 0) 0 else headerCharsFixedLength
        var padBefore = headerCharsLength
        var spacesBefore = spacesAround
        var padAfter = headerCharsLength
        var spacesAfter = spacesAround
        val usedSpace = line.length + padBefore + spacesBefore + spacesAfter + padAfter
        val leftover = fixedLength - usedSpace
        when (orientation) {
            in Int.MIN_VALUE..1 -> { // LEFT
                if (padWithPadChar) {
                    padAfter += leftover // leftover might be negative, in which case this is actually a minus
                    if (padAfter <= 0) {
                        spacesAfter += padAfter
                        padAfter = 1
                        if (spacesAfter < 0) {
                            spacesBefore += spacesAfter
                            spacesAfter = 1
                            if (spacesBefore < 0) {
                                padBefore += spacesBefore
                                spacesBefore = 1
                                if (padBefore <= 0) {
                                    padBefore = 1
                                }
                            }
                        }
                    }
                } else {
                    spacesAfter += leftover // leftover might be negative, in which case this is actually a minus
                    if (spacesAfter <= 0) {
                        padAfter += spacesAfter
                        spacesAfter = 1
                        if (padAfter < 0) {
                            spacesBefore += padAfter
                            padAfter = 1
                            if (spacesBefore < 0) {
                                padBefore += spacesBefore
                                spacesBefore = 1
                                if (padBefore <= 0) {
                                    padBefore = 1
                                }
                            }
                        }
                    }
                }
                val usedSpace = line.length + padBefore + spacesBefore + padAfter + spacesAfter
                val leftover = fixedLength - usedSpace
                if (padBefore > abs(leftover)) {
                    padBefore += leftover // shorten padBefore as it is long enough
                } else {
                    when (leftover) {
                        in Int.MIN_VALUE..-4 -> { spacesAfter = 0; spacesBefore = 0; padAfter = 0; padBefore = 0 }
                        -3 -> { spacesAfter--; spacesBefore--; padAfter--; if (padBefore > 1) { padBefore--; padAfter++ } }
                        -2 -> { spacesAfter--; spacesBefore--; if (padBefore > 1) { padBefore--; padAfter++ } }
                        -1 -> { spacesAfter--; if (padBefore > 1) { padBefore--; padAfter++ } }
                        else -> { // leftover was >= 0
                            if (padWithPadChar) {
                                padAfter += leftover
                            } else {
                                spacesAfter += leftover
                            }
                        }
                    }
                }
            }

            2 -> { // RIGHT
                if (padWithPadChar) {
                    padBefore += leftover // leftover might be negative, in which case this is actually a minus
                    if (padBefore <= 0) {
                        spacesBefore += padBefore
                        padBefore = 1
                        if (spacesBefore < 0) {
                            spacesAfter += spacesBefore
                            spacesBefore = 1
                            if (spacesAfter < 0) {
                                padAfter += spacesAfter
                                spacesAfter = 1
                                if (padAfter <= 0) {
                                    padAfter = 1
                                }
                            }
                        }
                    }
                } else {
                    spacesBefore += leftover // leftover might be negative, in which case this is actually a minus
                    if (spacesBefore <= 0) {
                        padBefore += spacesBefore
                        spacesBefore = 1
                        if (padBefore < 0) {
                            spacesAfter += padBefore
                            padBefore = 1
                            if (spacesAfter < 0) {
                                padAfter += spacesAfter
                                spacesAfter = 1
                                if (padAfter <= 0) {
                                    padAfter = 1
                                }
                            }
                        }
                    }
                }
                val usedSpace = line.length + padBefore + spacesBefore + padAfter + spacesAfter
                val leftover = fixedLength - usedSpace
                if (padAfter > abs(leftover)) {
                    padAfter += leftover // shorten padBefore as it is long enough
                } else {
                    when (leftover) {
                        in Int.MIN_VALUE..-4 -> { spacesAfter = 0; spacesBefore = 0; padAfter = 0; padBefore = 0 }
                        -3 -> { spacesBefore--; spacesAfter--; padBefore--; if (padAfter > 1) { padAfter--; padBefore++ } }
                        -2 -> { spacesBefore--; spacesAfter--; if (padAfter > 1) { padAfter--; padBefore++ } }
                        -1 -> { spacesBefore--; if (padAfter > 1) { padAfter--; padBefore++ } }
                        else -> { // new leftover was >= 0
                            if (padWithPadChar) {
                                padBefore += leftover
                            } else {
                                spacesBefore += leftover
                            }
                        }
                    }
                }
            }

            in 3..Int.MAX_VALUE -> { // CENTERED
                if (leftover >= 0) {
                    if (padWithPadChar) {
                        padBefore += (leftover / 2f).roundToInt()
                        padAfter  += (leftover / 2)
                    } else {
                        spacesBefore += (leftover / 2f).roundToInt()
                        spacesAfter  += (leftover / 2)
                    }
                } else {
                    if (padWithPadChar) {
                        padBefore += (leftover / 2f).roundToInt()
                        padAfter  += (leftover / 2)
                        if (padBefore < 0) {
                            spacesBefore += (leftover / 2f).roundToInt()
                            padBefore = 1
                            if (spacesBefore < 0) {
                                spacesBefore = 1
                            }
                        }
                        if (padAfter < 0) {
                            spacesAfter += (leftover / 2)
                            padAfter = 1
                            if (spacesAfter < 0) {
                                spacesAfter = 1
                            }
                        }
                    } else {
                        spacesBefore += (leftover / 2f).roundToInt()
                        spacesAfter  += (leftover / 2)
                        if (spacesBefore < 0) {
                            padBefore += (leftover / 2f).roundToInt()
                            spacesBefore = 1
                            if (padBefore < 0) {
                                padBefore = 1
                            }
                        }
                        if (spacesAfter < 0) {
                            padAfter += (leftover / 2)
                            spacesAfter = 1
                            if (padAfter < 0) {
                                padAfter = 1
                            }
                        }
                    }

                    val usedSpace = line.length + padBefore + spacesBefore + padAfter + spacesAfter
                    val leftover = fixedLength - usedSpace
                    if (padBefore > abs(leftover)) {
                        padBefore += leftover // shorten padBefore as it is long enough
                    } else {
                        when (leftover) {
                            in Int.MIN_VALUE..-4 -> { spacesAfter = 0; spacesBefore = 0; padAfter = 0; padBefore = 0 }
                            -3 -> { spacesAfter--; spacesBefore--; padBefore--; if (padAfter > 1) { padAfter--; padBefore++ } }
                            -2 -> { spacesAfter--; spacesBefore--; if (padAfter > 1) { padAfter--; padBefore++ } }
                            -1 -> { spacesAfter--; if (padAfter > 1) { padAfter--; padBefore++ } }
                            else -> { // new leftover was >= 0
                                if (padWithPadChar) {
                                    padBefore += (leftover / 2f).roundToInt()
                                    padAfter  += (leftover / 2)
                                } else {
                                    spacesBefore += (leftover / 2f).roundToInt()
                                    spacesAfter  += (leftover / 2)
                                }
                            }
                        }
                        if (padBefore == 0 && spacesBefore > 0) { padBefore++ ; spacesBefore-- }
                        if (padAfter  == 0 && spacesAfter  > 0) { padAfter++  ; spacesAfter-- }
                    }
                }
            }
        }
        val s = pad.repeat(padBefore) + spacesAroundChar.toString().repeat(spacesBefore) + line + spacesAroundChar.toString().repeat(spacesAfter) + pad.repeat(padAfter)
        resultLines.add(s)
    }
    resultLines.addAll(MutableList(boxLines) { bl })
    return resultLines.joinToString("\n")
}

//fun main(@Suppress("UNUSED_PARAMETER") args: Array<String>) {
//    var i = 2
//    for (total in i..26) {
//        println("1234567890".repeat(3) + " ${i++}")
//        println("long8rer".padCenter(total, '^', postfix =  "|", prefix = "|") + "|")
//        println(
//            "1\none\\nthree\nlong8rer".padForHeader(
//                total,
//                orientation = 3, // 1=LEFT, 2=RIGHT, 3=CENTERED
//                headerCharsFixedLength = 5,
//                padWithPadChar = true,
//                boxLines = 1).replace("\n", "|\n") +
//                    "|"
//        )
//    }
//}

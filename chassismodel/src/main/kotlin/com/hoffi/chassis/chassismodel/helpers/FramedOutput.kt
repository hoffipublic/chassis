package com.hoffi.chassis.chassismodel.helpers

import kotlinx.coroutines.*
import kotlinx.coroutines.channels.Channel
import okio.Buffer
import okio.BufferedSource
import okio.buffer
import okio.source
import java.io.File
import java.util.concurrent.TimeUnit

object FramedOutput {
    // @formatter:off
    const val HTOP =       "╭╴"
    const val HMIDDLE =    "│ "
    const val HMIDDLESEP = "├─"
    const val OTOP =       "╭╴"
    const val OMIDDLE =    "│ "
    const val OBOTTOM =    "╰─"
    const val MIN = 25
    // @formatter:on

    val CRNLRE = "\\R".toRegex()
    enum class WHERE { FIRST, MIDDLE, LAST }
    data class LINES(val s: String, val max: Int)


    fun cmdHeader(s: String): LINES {
        return cmdHeader(s.split(CRNLRE))
    }

    fun cmdHeader(lines: List<String>): LINES {
        val max = lines.maxOf { it.length }
        return LINES(lines.joinToString("\n$HMIDDLE", HTOP, "\n$HMIDDLESEP${"─".repeat(max.coerceAtLeast(MIN))}"), max)
    }

    fun cmdOutput(s: String, hasHeader: Boolean = true, maxSoFar: Int = 25, where: WHERE = WHERE.MIDDLE): LINES {
        val lines = s.split("\n")
        val max = lines.maxOf { it.length }
        return LINES(when (where) {
            WHERE.MIDDLE -> lines.joinToString("\n$OMIDDLE", OMIDDLE)
            WHERE.FIRST  -> lines.joinToString("\n$OMIDDLE", OTOP)
            WHERE.LAST   -> lines.joinToString("\n$OMIDDLE", OBOTTOM)
        }, max.coerceAtLeast(maxSoFar))
    }

    fun cmdEnd(max: Int = 25): String {
        return OBOTTOM + "─".repeat(max.coerceAtLeast(MIN))
    }


    suspend fun produceSomeStrings(channel: Channel<String>) {
        val bufferedCmdOutput = "/Users/hoffi/gitRepos/scratch/scratch/some.sh".runCommand() //"some.sh".runCommand()
        val buffer = Buffer()
        var byteCount: Long
        while (bufferedCmdOutput.read(buffer, 8192L).also { byteCount = it } != -1L) {
            val tmpS = buffer.readUtf8(byteCount)
            channel.send(tmpS)
        }
        channel.close()
    }

    suspend fun toOut(channel: Channel<String>) = coroutineScope {
        var i=0
        for (somePartOfOutput in channel) {
            print(somePartOfOutput.split("\\R".toRegex()).joinToString("\n") {
                if (it.isNotBlank()) "${"%5d".format(++i)}: $it" else it }
            )
        }
        println("( channel completely drained. (now delaying just for demonstration purposes )")
        delay(1500L)
    }

    fun main(@Suppress("UNUSED_PARAMETER") args: Array<String>) {
        val channel = Channel<String>(1)
        runBlocking {
            // start producer on different Dispatcher Scope than outJob
            val producerJob: Deferred<Unit> =
                async(Dispatchers.IO) {
                    println("starting producer...")
                    produceSomeStrings(channel)
                    println("( producer ready. )")
                }
            val outJob: Deferred<Unit> =
                async {
                    println("starting toOut() ...")
                    toOut(channel)
                    println("( toOut() ready. )")
                }
        }
        println("main() finished.")
    }

    fun String.runCommand(
        workingDir: File = File("."),
        timeoutAmount: Long = 60L,
        timeoutUnit: TimeUnit = TimeUnit.SECONDS
    ): BufferedSource = ProcessBuilder("\\s".toRegex().split(this))
        .directory(workingDir)
        .redirectOutput(ProcessBuilder.Redirect.PIPE)
        .redirectError(ProcessBuilder.Redirect.PIPE)
        .start()
        .inputStream.source().buffer()
}

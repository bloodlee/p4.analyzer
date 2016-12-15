package org.yli.p4analyzer

import com.google.common.base.Strings
import com.google.gson.Gson
import com.perforce.p4java.core.file.DiffType
import java.io.BufferedReader
import java.io.File
import java.io.InputStreamReader
import java.nio.charset.Charset
import java.util.*

/**
 * Created by jali on 11/28/2016.
 */

fun main(args: Array<String>) {
    val filePattern = Regex("====\\s+(.+)#\\d+\\s+\\((.+)\\)\\s+====")
    val addLocPattern = Regex("add\\s(\\d+)\\schunks\\s(\\d)+\\slines")
    val deletedLocPattern = Regex("deleted\\s(\\d+)\\schunks\\s(\\d)+\\slines")
    val changedLocPattern = Regex("changed\\s(\\d+)\\schunks\\s(\\d)+\\s/\\s(\\d)+\\slines")

    val p4Conn = P4Connection(args[0], args[1])
    p4Conn.connect()

    val lastCl = Integer.valueOf(args[2])

    val users = TreeMap<String, P4ClStatistic>()

    println("start query...")

    var stopped = false
    while (!stopped) {
        println("get next 100 cls...")
        val clSummaries = p4Conn.getChangeListsOfSpecificFolder(args[3], 2000)

        var index = 0;
        for (clSummary in clSummaries) {
            ++index
            val cl = p4Conn.getChangeList(clSummary.id)

            if (cl.username !in users) {
                users[cl.username] = P4ClStatistic()
            }

            if (cl.id < lastCl) {
                println("touch the last cl. Stop the query.")
                stopped = true
                break
            }

            println("working on ${cl.id} ($index/100}...")

            val statistic = users[cl.username] ?: continue
            statistic.addCl(cl.id)

            val diffInputStream = cl.getDiffs(DiffType.SUMMARY_DIFF)

            val r = BufferedReader(InputStreamReader(diffInputStream))

            var buffer: String?

            val lines = ArrayList<String>()
            for (buffer in r.lines()) {
                if (!Strings.isNullOrEmpty(buffer.trim())) {
                    lines.add(buffer)
                }
            }

            var inDifferences = false

            var i: Int = 0
            while (i < lines.size) {
                if (lines[i].startsWith("Differences")) {
                    inDifferences = true
                } else {
                    if (inDifferences) {
                        while (i < lines.size && lines[i].startsWith("====")) {
                            val result = filePattern.matchEntire(lines[i])

                            if (result != null) {
                                if ("text".equals(result.groups[2]!!.value)) {
                                    val filePath = result.groups[1]!!.value

                                    val addMatch = addLocPattern.matchEntire(lines[++i])
                                    val deleteMatch = deletedLocPattern.matchEntire(lines[++i])
                                    val changedMatch = changedLocPattern.matchEntire(lines[++i])

                                    if (addMatch != null && deleteMatch != null && changedMatch != null) {
                                        statistic.addStatisticalData(filePath, Integer.valueOf(addMatch.groups[2]!!.value),
                                                Integer.valueOf(deleteMatch.groups[2]!!.value),
                                                Integer.valueOf(changedMatch.groups[3]!!.value))
                                    }
                                }
                            }
                            ++i
                        }
                        ++i
                    }
                }
                ++i
            }
        }
    }

    println("print the result")
    val message = Gson().toJson(users)
    println(message)

    org.apache.commons.io.FileUtils.writeStringToFile(File("data.json"), message, Charset.defaultCharset())

    for ((key, value) in users) {
        println("$key\t${value.totalChangeLoc()}\t${value.totalClCount()}\t${value.totalTouchedFiles()}")
    }
}
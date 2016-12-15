package org.yli.p4analyzer

import java.util.*

/**
 * Created by jali on 11/28/2016.
 */
class P4ClStatistic {

    private val cls = ArrayList<Int>();

    private val files:MutableMap<String, Int> = TreeMap<String, Int>()

    private var addedLoc = 0;

    private var removedLoc = 0;

    private var changedLoc = 0;

    /**
     * Add a new change list.
     */
    fun addCl(cl: Int) {
        cls.add(cl)
    }

    fun addStatisticalData(filePath: String, addedLoc: Int, removedLoc: Int, changedLoc: Int) {
        if (filePath !in files) {
            files[filePath] = 0
        }

        val value = files[filePath]
        if (value != null) {
            files[filePath] = value + 1
        }

        this.addedLoc += addedLoc
        this.removedLoc += removedLoc
        this.changedLoc += changedLoc
    }

    fun totalClCount() : Int {
        return cls.size
    }

    fun totalChangeLoc() : Int {
        return addedLoc + removedLoc + changedLoc
    }

    fun totalTouchedFiles() : Int {
        return files.size
    }
}
package org.yli.p4analyzer

import com.google.common.collect.Lists
import com.perforce.p4java.core.IChangelist
import com.perforce.p4java.core.IChangelistSummary
import com.perforce.p4java.core.file.IFileSpec
import com.perforce.p4java.impl.generic.core.file.FileSpec
import com.perforce.p4java.option.server.ChangelistOptions
import com.perforce.p4java.server.IOptionsServer
import com.perforce.p4java.server.ServerFactory

/**
 * Created by jali on 11/28/2016.
 */
class P4Connection(val p4user: String, val p4passwd: String, val p4port: String = "perforce:1666") {

    private val p4Server: IOptionsServer

    init {
        p4Server = ServerFactory.getOptionsServer("p4java://${p4port}", null)
    }

    /**
     * Connect to the p4 server
     */
    fun connect() {
        p4Server.connect()
        p4Server.userName = p4user

        // "p4 login -a"
        p4Server.login(p4passwd, true)
    }

    /**
     * Get the change list of a specific depot folder.
     *
     * @param depotPath the depot path.
     * @param clCount the count of most recent changelists
     *
     * @return list of changelist summary.
     */
    fun getChangeListsOfSpecificFolder(depotPath : String, clCount : Int = 100) : List<IChangelistSummary> {
        return p4Server.getChangelists(
                clCount, // max most recent
                Lists.newArrayList(FileSpec(depotPath)) as List<IFileSpec>?, // file specs
                null, // client name,
                null, // username,
                true, // include integrated
                true, // submitted only
                false, // pending only
                false); // long desc
    }

    /**
     * Get details of change list.
     */
    fun getChangeList(id: Int) : IChangelist {
        return p4Server.getChangelist(id, ChangelistOptions())
    }
}
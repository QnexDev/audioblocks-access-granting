package com.qnex.audioblocks.access

import com.qnex.audioblocks.access.model.PartnerInfo

class RemoteExcelPartnerReader(val fileName: String, val sheetName: String, val responsiblePerson: String) : Iterator<PartnerInfo>, Iterable<PartnerInfo> {

    override fun iterator(): Iterator<PartnerInfo> {
        return this
    }

    override fun hasNext(): Boolean {
        return false
    }

    override fun next(): PartnerInfo {
        return PartnerInfo("?", "?", "?")
    }

}
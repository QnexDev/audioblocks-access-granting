package com.qnex.audioblocks.access.task

import com.qnex.audioblocks.access.PartnerAccessService
import com.qnex.audioblocks.access.model.RunConfig
import com.qnex.audioblocks.access.sheet.GoogleSheetProvider

class PartnerAccessTaskContext(val runConfig: RunConfig, val partnerAccessService: PartnerAccessService, val googleSheetProvider: GoogleSheetProvider) {


}
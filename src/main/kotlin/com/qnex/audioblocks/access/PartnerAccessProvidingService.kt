package com.qnex.audioblocks.access

import com.qnex.audioblocks.access.client.AudioblocksAccessProvidingClient
import com.qnex.audioblocks.access.model.PartnerAccessProvidingConfig
import com.qnex.audioblocks.access.model.PartnerInfo
import com.qnex.audioblocks.access.sheet.GoogleSheetProvider
import com.qnex.audioblocks.access.sheet.RangeWriteMapper

class PartnerAccessProvidingService(val config: PartnerAccessProvidingConfig,
                                    val googleSheetProvider: GoogleSheetProvider,
                                    val audioblocksClient: AudioblocksAccessProvidingClient) {

    fun provideAccess(index: Int, partnerInfo: PartnerInfo) {
        audioblocksClient.sendEmail()

        googleSheetProvider.update(config.spreadsheetId, config.spreadsheetTabName, index, object : RangeWriteMapper<PartnerInfo> {

            override fun apply(value: PartnerInfo): List<Any?> {
                return listOf(value.email, value.lastName, value.name)
            }

        }, partnerInfo)

    }
}
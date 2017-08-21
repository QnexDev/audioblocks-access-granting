package com.qnex.audioblocks.access

import com.qnex.audioblocks.access.client.AudioblocksAccessProvidingClient
import com.qnex.audioblocks.access.model.PartnerAccessProvidingConfig
import com.qnex.audioblocks.access.model.PartnerInfo
import com.qnex.audioblocks.access.sheet.GoogleSheetProvider
import com.qnex.audioblocks.access.sheet.RangeWriteMapper
import java.text.SimpleDateFormat
import java.util.*

class PartnerAccessProvidingService(private val config: PartnerAccessProvidingConfig,
                                    private val googleSheetProvider: GoogleSheetProvider,
                                    private val audioblocksClient: AudioblocksAccessProvidingClient,
                                    private val datePattern:String = "dd.MM.yyyy") {

    private val simpleDateFormat = SimpleDateFormat(datePattern)

    fun provideAccess(index: Int, partnerInfo: PartnerInfo) {
//        audioblocksClient.sendEmail(partnerInfo)

        googleSheetProvider.update(config.spreadsheetId, config.spreadsheetTabName, index, object : RangeWriteMapper<String> {

            override fun apply(value: String): List<Any?> {
                return listOf(null, null, null, null,  value)
            }

        }, simpleDateFormat.format(Date()))
    }
}
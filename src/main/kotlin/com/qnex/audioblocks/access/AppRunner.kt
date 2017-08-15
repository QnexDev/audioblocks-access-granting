package com.qnex.audioblocks.access

import com.qnex.audioblocks.access.client.AudioblocksAccessProvidingClient
import com.qnex.audioblocks.access.model.PartnerAccessProvidingConfig
import com.qnex.audioblocks.access.model.PartnerInfo
import com.qnex.audioblocks.access.model.RunConfig
import com.qnex.audioblocks.access.sheet.GoogleSheetProvider
import com.qnex.audioblocks.access.sheet.RangeReadMapper
import java.nio.file.Paths
import java.util.*


object AppRunner {


    @JvmStatic fun main(args: Array<String>) {

        val clientSecretFilePath = "/home/qnex/audioblocks-access-granting/src/main/resources/client_secret.json"

        AppRunner.run(RunConfig(10, 20,
                clientSecretFilePath, "1BxiMVs0XRA5nFMdKvBdBZjgmUUqptlbs74OgvE2upms", "Class Data"))
    }

    @JvmStatic fun run(runConfig: RunConfig) {
        val random = Random()
        val googleSheetProvider = GoogleSheetProvider(Paths.get(runConfig.clientSecretFilePath))
        val audioblocksClient = AudioblocksAccessProvidingClient()

        val spreadsheetId = runConfig.spreadsheetId
        val spreadsheetTabName = runConfig.spreadsheetTabName

        val accessProvidingConfig = PartnerAccessProvidingConfig(spreadsheetId, spreadsheetTabName)
        val providingService = PartnerAccessProvidingService(accessProvidingConfig, googleSheetProvider, audioblocksClient)

        var index = 1

        googleSheetProvider.readAll(spreadsheetId, spreadsheetTabName, object : RangeReadMapper<Pair<PartnerInfo, String>> {

            override fun apply(values: List<Any?>): Pair<PartnerInfo, String> {
                return Pair(PartnerInfo(values[0] as String, values[1] as String, values[2] as String), values[4] as String)
            }

        }).asSequence()
                .filter { index == 1 }
                .filter { it.second == "???" }
                .forEach {
                    providingService.provideAccess(index, it.first)
                    index++

                    pause(random.nextInt(10).toLong())
                }


    }

    fun pause(ms: Long) {
        Thread.sleep(ms)
    }
}

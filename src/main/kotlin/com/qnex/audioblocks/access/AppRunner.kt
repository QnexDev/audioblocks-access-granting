package com.qnex.audioblocks.access

import com.qnex.audioblocks.access.client.AudioblocksAccessProvidingClient
import com.qnex.audioblocks.access.model.PartnerAccessProvidingConfig
import com.qnex.audioblocks.access.model.PartnerInfo
import com.qnex.audioblocks.access.model.RunConfig
import com.qnex.audioblocks.access.sheet.GoogleSheetProvider
import com.qnex.audioblocks.access.sheet.RangeReadMapper
import org.apache.http.impl.client.HttpClients
import java.nio.file.Paths
import java.util.*


object AppRunner {


    private fun buildArgsDefinitions(): List<InputArgumentParser.Definition> {
        return listOf(
                InputArgumentParser.Definition("l", "audiobloksLoginName"),
                InputArgumentParser.Definition("p", "audiobloksPassword"),
                InputArgumentParser.Definition("cs", "clientSecretFilePath"),
                InputArgumentParser.Definition("pf", "pauseTimeFrom"),
                InputArgumentParser.Definition("pt", "pauseTimeTo"),
                InputArgumentParser.Definition("si", "spreadsheetId"),
                InputArgumentParser.Definition("st", "spreadsheetTabName"),
                InputArgumentParser.Definition("dp", "datePattern"))
    }

    @JvmStatic fun main(args: Array<String>) {

        InputArgumentParser(buildArgsDefinitions()).parse(args, {
            RunConfig(
                    it["audiobloksLoginName"]!!,
                    it["audiobloksPassword"]!!,
                    it["clientSecretFilePath"]!!,
                    Integer.parseInt(it["pauseTimeFrom"]!!),
                    Integer.parseInt(it["pauseTimeTo"]!!),
                    it["spreadsheetId"]!!,
                    it["spreadsheetTabName"]!!,
                    it["datePattern"]!!)
        })

        val clientSecretFilePath = "/home/qnex/audioblocks-access-granting/src/main/resources/client_secret.json"

        AppRunner.run(RunConfig(
                "admin@air.com", "Footage123!", clientSecretFilePath,
                10, 20,
                "1BxiMVs0XRA5nFMdKvBdBZjgmUUqptlbs74OgvE2upms", "Class Data","dd.MM.yyyy"))
    }

    @JvmStatic fun run(runConfig: RunConfig) {
        val random = Random()
        val spreadsheetId = runConfig.spreadsheetId
        val spreadsheetTabName = runConfig.spreadsheetTabName

        val googleSheetProvider = GoogleSheetProvider(Paths.get(runConfig.clientSecretFilePath))

        HttpClients.createDefault().use { httpClient ->

            val audioblocksClient =
                    AudioblocksAccessProvidingClient(runConfig.audioblocksLoginName, runConfig.audioblocksPassword, httpClient)

            val accessProvidingConfig = PartnerAccessProvidingConfig(spreadsheetId, spreadsheetTabName)
            val providingService =
                    PartnerAccessProvidingService(accessProvidingConfig, googleSheetProvider, audioblocksClient, runConfig.datePattern)

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

                        pause(random.between(runConfig.pauseTimeFrom, runConfig.pauseTimeTo).toLong())
                    }
        }

    }

    fun pause(ms: Long) {
        Thread.sleep(ms)
    }

    fun Random.between(from: Int, to: Int): Int {
        return from + this.nextInt(to - from)
    }
}

package com.qnex.audioblocks.access

import com.qnex.audioblocks.access.client.AudioblocksAccessProvidingClient
import com.qnex.audioblocks.access.model.PartnerAccessProvidingConfig
import com.qnex.audioblocks.access.model.RunConfig
import com.qnex.audioblocks.access.sheet.GoogleSheetProvider
import com.qnex.audioblocks.access.task.*
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
                InputArgumentParser.Definition("a", "adminName"),
                InputArgumentParser.Definition("t", "taskName"),
                InputArgumentParser.Definition("dp", "datePattern"))
    }

    private fun buildActionTaskList(context: PartnerAccessTaskContext): List<ActionTask<*>> {
        return listOf(
                PartnerProvidingAccessTask("providing access", context),
                PartnerRemovingAccessTask("removing access", context))
    }

    @JvmStatic fun main(args: Array<String>) {

        val inputArgumentParser = InputArgumentParser(buildArgsDefinitions())

        val runConfig = inputArgumentParser.parse(args, {
            RunConfig(
                    it["audiobloksLoginName"]!!,
                    it["audiobloksPassword"]!!,
                    it["clientSecretFilePath"]!!,
                    Integer.parseInt(it["pauseTimeFrom"]!!),
                    Integer.parseInt(it["pauseTimeTo"]!!),
                    it["spreadsheetId"]!!,
                    it["spreadsheetTabName"]!!,
                    it["datePattern"]!!,
                    it["adminName"]!!,
                    it["taskName"]!!)
        })

        AppRunner.run(runConfig)
    }

    @JvmStatic fun run(runConfig: RunConfig) {
        val googleSheetProvider = GoogleSheetProvider(Paths.get(runConfig.clientSecretFilePath))

        HttpClients.createDefault().use { httpClient ->

            val audioblocksClient =
                    AudioblocksAccessProvidingClient(runConfig.audioblocksLoginName, runConfig.audioblocksPassword, httpClient)

            val accessProvidingConfig = PartnerAccessProvidingConfig(runConfig.spreadsheetId, runConfig.spreadsheetTabName)
            val providingService =
                    PartnerAccessService(accessProvidingConfig, googleSheetProvider, audioblocksClient, runConfig.datePattern)

            val context = PartnerAccessTaskContext(runConfig, providingService, googleSheetProvider)

            val actionTaskRegistry = ActionTaskRegistry(buildActionTaskList(context))
            val actionTaskManager = ActionTaskManager(actionTaskRegistry)

            actionTaskManager.startTask(runConfig.taskName)
        }

    }
}

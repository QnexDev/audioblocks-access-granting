package com.qnex.audioblocks.access

import com.qnex.audioblocks.access.client.AudioblocksAccessProvidingClient
import com.qnex.audioblocks.access.model.PartnerAccessProvidingConfig
import com.qnex.audioblocks.access.model.PartnerInfo
import com.qnex.audioblocks.access.model.ParsedRowData
import com.qnex.audioblocks.access.model.RunConfig
import com.qnex.audioblocks.access.sheet.GoogleSheetProvider
import com.qnex.audioblocks.access.sheet.RangeReadMapper
import org.apache.http.impl.client.HttpClients
import sun.util.resources.LocaleData
import java.nio.file.Paths
import java.text.SimpleDateFormat
import java.time.LocalDate
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
                InputArgumentParser.Definition("dp", "datePattern"))
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
                    it["adminName"]!!)
        })

        AppRunner.run(runConfig)
    }

    @JvmStatic fun run(runConfig: RunConfig) {
        val random = Random()
        val spreadsheetId = runConfig.spreadsheetId
        val spreadsheetTabName = runConfig.spreadsheetTabName
        val dateFormat = SimpleDateFormat(runConfig.datePattern)

        val googleSheetProvider = GoogleSheetProvider(Paths.get(runConfig.clientSecretFilePath))

        HttpClients.createDefault().use { httpClient ->

            val audioblocksClient =
                    AudioblocksAccessProvidingClient(runConfig.audioblocksLoginName, runConfig.audioblocksPassword, httpClient)

            val accessProvidingConfig = PartnerAccessProvidingConfig(spreadsheetId, spreadsheetTabName)
            val providingService =
                    PartnerAccessProvidingService(accessProvidingConfig, googleSheetProvider, audioblocksClient, runConfig.datePattern)

            val indexedIterator = googleSheetProvider.readAll(spreadsheetId, spreadsheetTabName,
                    filter = object : GoogleSheetProvider.IndexedReadFilter<ParsedRowData> {

                        override fun preReadApply(index: Int): Boolean {
                            return index > 1
                        }
                    },

                    rangeReadMapper = object : RangeReadMapper<ParsedRowData> {
                        override fun apply(values: List<Any?>): ParsedRowData {
                            val accessDate = resolveDateArg(values, 4)
                            val invitationDate = resolveDateArg(values, 5)
                            return ParsedRowData(
                                    PartnerInfo(
                                            resolveArg(values, 1),
                                            resolveArg(values, 2),
                                            resolveArg(values, 0),
                                            accessDate, invitationDate), resolveArg(values, 3))
                        }

                        private fun resolveArg(values: List<Any?>, index: Int) =
                                if (values.size >= index + 1) values[index] as String else null

                        private fun resolveDateArg(values: List<Any?>, index: Int): Date? {
                            val resolvedValue = resolveArg(values, index)
                            if (resolvedValue == null || resolvedValue.trim() == "") {
                                return null
                            }
                            if (resolvedValue.filter { it == '.' }.length == 1) {
                                return parseDate(resolvedValue + ".${LocalDate.now().year}")
                            }
                            if (resolvedValue.filter { it == '.' }.isEmpty()) {
                                return parseDate(resolvedValue + ".${LocalDate.now().month}" + ".${LocalDate.now().year}")
                            }
                            return parseDate(resolvedValue)
                        }

                        private fun parseDate(value: String?): Date? {
                            return if (value == null || value.trim() == "") null else dateFormat.parse(value)
                        }
                    })

            indexedIterator.asSequence()
                    .filter { parsedRowData ->
                        val filterResult = (parsedRowData.adminName == runConfig.adminName &&
                                parsedRowData.partnerInfo.accessDate == null
                                && parsedRowData.partnerInfo.invitationDate != null)
                        println("Filter result: $filterResult , model: $parsedRowData")
                        filterResult
                    }.forEach { (partnerInfo) ->
                        providingService.provideAccess(indexedIterator.index(), partnerInfo)
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

package com.qnex.audioblocks.access.task

import com.qnex.audioblocks.access.model.ParsedRowData
import com.qnex.audioblocks.access.model.PartnerInfo
import com.qnex.audioblocks.access.sheet.GoogleSheetProvider
import com.qnex.audioblocks.access.sheet.RangeReadMapper
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.util.*
import com.qnex.audioblocks.access.util.AppUtils.between
import com.qnex.audioblocks.access.util.AppUtils.pause

class PartnerProvidingAccessTask(private val taskName: String,
                                 private val accessTaskContext: PartnerAccessTaskContext) : ActionTask<PartnerInfo> {

    override fun start() {
        val runConfig = accessTaskContext.runConfig
        val random = Random()
        val spreadsheetId = runConfig.spreadsheetId
        val spreadsheetTabName = runConfig.spreadsheetTabName
        val dateFormat = SimpleDateFormat(runConfig.datePattern)
        val googleSheetProvider = accessTaskContext.googleSheetProvider
        val partnerAccessService = accessTaskContext.partnerAccessService


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
            partnerAccessService.provideAccess(indexedIterator.index(), partnerInfo)
            pause(random.between(runConfig.pauseTimeFrom, runConfig.pauseTimeTo).toLong())
        }
    }

    override fun stop() {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun pause() {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun continuee() {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun getName(): String {
        return taskName
    }
}


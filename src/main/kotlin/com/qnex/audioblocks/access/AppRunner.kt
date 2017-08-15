package com.qnex.audioblocks.access

import com.qnex.audioblocks.access.model.PartnerInfo
import com.qnex.audioblocks.access.model.RunConfig
import com.qnex.audioblocks.access.sheet.GoogleSheetProvider
import java.nio.file.Paths


object AppRunner {


    @JvmStatic fun main(args: Array<String>) {
        val clientSecretFilePath = "/home/qnex/audioblocks-access-granting/src/main/resources/client_secret.json"

        AppRunner.run(RunConfig("", "", 10, 20, clientSecretFilePath))
    }

    @JvmStatic fun run(runConfig: RunConfig) {
        val googleSheetProvider = GoogleSheetProvider(Paths.get(runConfig.clientSecretFilePath))

        // Prints the names and majors of students in a sample spreadsheet:
        // https://docs.google.com/spreadsheets/d/1BxiMVs0XRA5nFMdKvBdBZjgmUUqptlbs74OgvE2upms/edit
        val spreadsheetId = "1BxiMVs0XRA5nFMdKvBdBZjgmUUqptlbs74OgvE2upms"
        val values = googleSheetProvider.readAll(spreadsheetId, "Class Data")

        values.forEach { println(it.joinToString())}
//
// if (values.isEmpty()) {
//            println("No data found.")
//        } else {
//            for (row in values) {
//                // Print columns A and E, which correspond to indices 0 and 4.
//            }
//        }

        val providingService = PartnerAccessProvidingService(googleSheetProvider)

        providingService.provideAccess(PartnerInfo("?", "?", "?"))

    }
}

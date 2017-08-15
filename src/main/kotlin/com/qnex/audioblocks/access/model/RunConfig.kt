package com.qnex.audioblocks.access.model

data class RunConfig(
        val pauseTimeFrom: Int,
        val pauseTimeTo: Int,
        val clientSecretFilePath: String,
        val spreadsheetId: String,
        val spreadsheetTabName: String)
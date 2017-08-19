package com.qnex.audioblocks.access.model

data class RunConfig(
        val audioblocksLoginName: String,
        val audioblocksPassword: String,
        val clientSecretFilePath: String,
        val pauseTimeFrom: Int,
        val pauseTimeTo: Int,
        val spreadsheetId: String,
        val spreadsheetTabName: String,
        val datePattern: String,
        val adminName: String)
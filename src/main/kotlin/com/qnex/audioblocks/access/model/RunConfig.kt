package com.qnex.audioblocks.access.model

data class RunConfig(
        val googleName: String,
        val googlePassword: String,
        val pauseTimeFrom: Int,
        val pauseTimeTo: Int,
        val clientSecretFilePath: String)
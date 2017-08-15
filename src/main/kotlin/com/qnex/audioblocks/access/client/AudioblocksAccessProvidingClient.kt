package com.qnex.audioblocks.access.client

class AudioblocksAccessProvidingClient {

    var authorized = false

    fun authorize() {

        authorized = true
    }

    fun sendEmail() {
        if (!authorized) {
            authorize()
        }
    }

}
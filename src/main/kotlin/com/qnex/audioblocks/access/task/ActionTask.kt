package com.qnex.audioblocks.access.task

interface ActionTask<T> {

    fun start()

    fun stop()

    fun pause()

    fun continuee()

    fun getName(): String
}
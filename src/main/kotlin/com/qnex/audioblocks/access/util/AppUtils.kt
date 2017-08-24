package com.qnex.audioblocks.access.util

import java.util.*

object AppUtils {

    fun pause(ms: Long) {
        Thread.sleep(ms)
    }

    fun Random.between(from: Int, to: Int): Int {
        return from + this.nextInt(to - from)
    }
}
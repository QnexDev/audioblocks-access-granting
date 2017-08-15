package com.qnex.audioblocks.access.sheet

interface RangeWriteMapper<T> {

    fun apply(value: T): List<Any?>

    fun getRange(): String {
        return ""
    }


}

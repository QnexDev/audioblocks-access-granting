package com.qnex.audioblocks.access.sheet

interface RangeWriteMapper<T> {

    fun apply(values: T): List<Any>

    fun getRange(): String

}

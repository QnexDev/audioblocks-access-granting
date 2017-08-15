package com.qnex.audioblocks.access.sheet

interface RangeReadMapper<T>{

    fun apply(values: List<Any?>): T

    fun getRange(): String {
        return ""
    }

}

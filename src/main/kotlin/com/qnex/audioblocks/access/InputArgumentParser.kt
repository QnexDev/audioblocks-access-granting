package com.qnex.audioblocks.access

class InputArgumentParser(private val argsDefinitions: List<Definition>) {

    val fullDefinitions = buildFullDefinitions()

    private fun buildFullDefinitions(): Map<String, Definition> {
        val fullNameMap = argsDefinitions.associateByTo(mutableMapOf(), { d -> d.fullArgName })
        val shortNameMap = argsDefinitions.associateByTo(mutableMapOf(), { (shortArgName) -> shortArgName })
        checkArgNamesDuplication(fullNameMap, shortNameMap)
        fullNameMap += shortNameMap
        checkArgNamesDuplication(fullNameMap)
        return fullNameMap
    }

    fun parse(inputArgs: Array<String>): Map<String, String> {
        if (inputArgs.isEmpty()) {
            throw IllegalArgumentException("Wrong arguments size!")
        }
        try {
            return parseArguments(inputArgs).map {
                val definition = fullDefinitions[it.key] ?:
                        throw IllegalArgumentException("Wrong argument name: ${it.key}!")
                definition.fullArgName to it.value
            }.toMap()
        } catch(e: Exception) {
            throw IllegalArgumentException("Usage: ${getArgumentsDescription()}", e)
        }
    }

    private fun parseArguments(inputArgs: Array<String>): Map<String, String> {
        if (inputArgs.size % 2 != 0) {
            throw IllegalArgumentException("Wrong arguments size!")
        }

        val parsedArgs = mutableMapOf<String, String>()

        var i = 0
        while (i < inputArgs.size / 2) {
            val argName = inputArgs[i * 2].substring(1).trim()
            val argValue = inputArgs[i * 2 + 1].trim()
            parsedArgs[argName] = argValue
            i++
        }
        return parsedArgs
    }

    fun <R> parse(inputArgs: Array<String>, mapper: (Map<String, String>) -> R): R {
        return mapper.invoke(parse(inputArgs))
    }

    fun getArgumentsDescription(): String {
        return fullDefinitions.values.joinToString(" ",
                transform = { (shortArgName, fullArgName) -> buildArgumentDescription(shortArgName, fullArgName) })
    }

    private fun buildArgumentDescription(shortArgName: String, fullArgName: String): String {
        return "[-$shortArgName $fullArgName]"
    }

    private fun checkArgNamesDuplication(fullNameMap: Map<String, Definition>, shortNameMap: Map<String, Definition>) {
        if (fullNameMap.size < argsDefinitions.size || shortNameMap.size < argsDefinitions.size) {
            throw IllegalArgumentException("The argument definitions has duplicated elements")
        }
    }

    private fun checkArgNamesDuplication(fullAndShortNamesMap: Map<String, Definition>) {
        if (fullAndShortNamesMap.size < argsDefinitions.size * 2) {
            throw IllegalArgumentException("The argument definitions has duplicated elements")
        }
    }

    data class Definition(val shortArgName: String, val fullArgName: String, val description: String = "")
}
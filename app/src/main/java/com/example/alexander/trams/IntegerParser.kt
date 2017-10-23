package com.example.alexander.trams

import java.util.regex.*

object IntegerParser {
    fun getIntFromString(word: String): Int {
        val pattern = Pattern.compile("\\d+")
        val matcher = pattern.matcher(word)
        var start = 0
        var result = 0
        while (matcher.find(start)) {
            val value = word.substring(matcher.start(), matcher.end())
            result = Integer.parseInt(value)
            start = matcher.end()
        }
        return result
    }
}

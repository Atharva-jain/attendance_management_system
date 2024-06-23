package com.yeloe.attentanceapp.utils

class GenerateKeywords {
    companion object {
        fun generateKeywords(name: String): List<String> {
            val keywords = mutableListOf<String>()
            for (i in name.indices) {
                for (j in (i + 1)..name.length) {
                    keywords.add(name.slice(i until j))
                }
            }
            return keywords
        }
    }
}
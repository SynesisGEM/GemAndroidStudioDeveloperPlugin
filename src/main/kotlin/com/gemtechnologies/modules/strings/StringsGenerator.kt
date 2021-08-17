package com.gemtechnologies.modules.strings

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.gemtechnologies.modules.strings.models.Strings
import com.gemtechnologies.modules.strings.models.Translation
import com.gemtechnologies.modules.strings.models.TranslationInfo
import java.net.URL

class StringsGenerator {

    private val fixValueFormatUtils = FixValueFormatUtils()
    private val stringsFilesGenerator = StringsFilesGenerator()

    fun generate(path: String, namespaces: List<String>?) {

        val translationToke = object : TypeToken<ArrayList<TranslationInfo>>() {}.type

        val json = URL(URL_ALL).readText()
        val fromJson: ArrayList<TranslationInfo> = Gson().fromJson(json, translationToke)

        val strings = fromJson
            .map {
                val language = it.key.split("/")[2]
                val nameSpace = it.key.split("/")[3].replace(("[^\\p{Alpha}]+").toRegex(), "_").toLowerCase()
                Strings(
                    translation = "-$language",
                    nameSpace = nameSpace.trim(),
                    values = Gson().fromJson(URL(it.url).readText(), HashMap<String, String>()::class.java).map {
                        Translation(
                            key = it.key.trim(),
                            value = fixValueFormatUtils.fixParams(it.value).trim()
                        )
                    }
                )
            }

        val mainStringsNameSpace = "strings"

        val mainValues = strings.filter { it.nameSpace.equals(mainStringsNameSpace, true) }

        strings.filter { !it.nameSpace.equals(mainStringsNameSpace, true) }.forEach { secondaryValue ->
            mainValues.firstOrNull { it.translation == secondaryValue.translation }?.let {
                it.values = it.values - secondaryValue.values
            }
        }

        namespaces?.let {
            stringsFilesGenerator.generateFiles(
                path,
                strings.filter { namespaces.contains(it.nameSpace) })
        } ?: stringsFilesGenerator.generateFiles(path, strings)
    }

    fun getAllAvailableNameSpaces(): List<String> {
        val translationToke = object : TypeToken<ArrayList<TranslationInfo>>() {}.type

        val json = URL(URL_ALL).readText()
        val fromJson: ArrayList<TranslationInfo> = Gson().fromJson(json, translationToke)
        return fromJson.map {
            it.key.split("/")[3].replace(("[^\\p{Alpha}]+").toRegex(), "_").toLowerCase()
        }
            .toSet()
            .toList() - MAIN_NAMESPACE
    }

    companion object {
        private const val MAIN_NAMESPACE = "strings"
        private const val PROJECT_ID = "171f13ef-6c33-495a-b9f2-f5a0fce6ef99"
        private const val URL_ALL = "https://api.locize.app/download/$PROJECT_ID"
    }
}
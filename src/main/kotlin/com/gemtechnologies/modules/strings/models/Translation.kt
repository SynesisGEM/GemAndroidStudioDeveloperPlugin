package com.gemtechnologies.modules.strings.models

data class Translation(val key: String, val value: String) {

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Translation

        if (key != other.key) return false

        return true
    }
}
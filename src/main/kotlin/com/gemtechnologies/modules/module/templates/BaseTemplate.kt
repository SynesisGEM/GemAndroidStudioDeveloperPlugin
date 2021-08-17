package com.gemtechnologies.modules.module.templates

import com.gemtechnologies.modules.module.Config

abstract class BaseTemplate(config: Config) {
    open abstract fun getTemplate(): String
}
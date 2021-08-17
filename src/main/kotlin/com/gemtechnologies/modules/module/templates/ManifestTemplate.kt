package com.gemtechnologies.modules.module.templates

import com.gemtechnologies.modules.module.Config
import com.gemtechnologies.modules.module.templates.BaseTemplate

class ManifestTemplate(val config: Config) : BaseTemplate(config) {

    override fun getTemplate(): String {
        return "<manifest package=\"com.synesis.gem.${config.packageName}\" />\n"
    }
}
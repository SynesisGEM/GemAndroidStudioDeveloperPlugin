package com.gemtechnologies.modules.module.templates

import com.gemtechnologies.modules.module.Config
import com.gemtechnologies.modules.module.templates.BaseTemplate

class UseCaseTemplate(val config: Config) : BaseTemplate(config) {

    override fun getTemplate(): String {
        return "package ${config.getRootScreenImport()}.business.usecase\n" +
                "\n" +
                "class ${config.getUseCaseName()} {\n" +
                "}"
    }
}
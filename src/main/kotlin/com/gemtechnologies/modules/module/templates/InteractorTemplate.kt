package com.gemtechnologies.modules.module.templates

import com.gemtechnologies.modules.module.Config
import com.gemtechnologies.modules.module.templates.BaseTemplate

class InteractorTemplate(val config: Config) : BaseTemplate(config) {
    override fun getTemplate(): String {
        return "package ${config.getRootScreenImport()}.business.interactor\n" +
                "\n" +
                "import ${config.getUseCaseImport()}\n" +
                "\n" +
                "class ${config.getInteractorName()}(private val useCase: ${config.getUseCaseName()}) {\n" +
                "}"
    }
}
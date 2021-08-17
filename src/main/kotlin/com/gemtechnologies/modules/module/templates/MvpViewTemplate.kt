package com.gemtechnologies.modules.module.templates

import com.gemtechnologies.modules.module.Config
import com.gemtechnologies.modules.module.templates.BaseTemplate

class MvpViewTemplate(val config: Config) : BaseTemplate(config) {

    override fun getTemplate(): String {
        return "package ${config.getRootScreenImport()}.presentation.presenter\n" +
                "\n" +
                "import com.synesis.gem.core.ui.base.BaseView\n" +
                "\n" +
                "interface ${config.getMVPViewName()} : BaseView"
    }
}
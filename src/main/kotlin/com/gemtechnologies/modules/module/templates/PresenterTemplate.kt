package com.gemtechnologies.modules.module.templates

import com.gemtechnologies.modules.module.Config
import com.gemtechnologies.modules.module.templates.BaseTemplate

class PresenterTemplate(val config: Config) : BaseTemplate(config) {
    override fun getTemplate(): String {
        return  "package ${config.getRootScreenImport()}.presentation.presenter\n" +
                "\n" +
                "import com.synesis.gem.core.api.errorshandling.ErrorHandler\n" +
                "import com.synesis.gem.core.common.coroutines.DispatchersProvider\n" +
                "import com.synesis.gem.core.ui.base.BasePresenter\n" +
                "import ${config.getInteractorImport()}\n" +
                "import moxy.InjectViewState\n" +
                "\n" +
                "@InjectViewState\n" +
                "class ${config.getPresenterName()}(\n" +
                "        dispatchersProvider: DispatchersProvider,\n" +
                "        errorHandler: ErrorHandler,\n" +
                "        private val interactor: ${config.getInteractorName()}\n" +
                ") : BasePresenter<${config.getMVPViewName()}>(dispatchersProvider, errorHandler) {\n" +
                "}"
    }
}
package com.gemtechnologies.modules.api

import com.gemtechnologies.modules.api.forms.ModuleSelectorStep
import com.gemtechnologies.modules.api.forms.PackageInfoStep
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.ui.wizard.WizardModel

class AddComponentWizardModel(anAction: AnActionEvent?, title: String) : WizardModel(title) {

  init {
    add(ModuleSelectorStep())
    add(PackageInfoStep(anAction))
  }
}

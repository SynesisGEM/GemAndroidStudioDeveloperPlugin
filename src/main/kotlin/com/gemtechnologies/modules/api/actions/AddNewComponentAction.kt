package com.gemtechnologies.modules.api.actions

import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.gemtechnologies.modules.api.AddComponentWizard

class AddNewComponentAction : AnAction() {

  override fun actionPerformed(event: AnActionEvent) {
    AddComponentWizard.run(event)
  }
}

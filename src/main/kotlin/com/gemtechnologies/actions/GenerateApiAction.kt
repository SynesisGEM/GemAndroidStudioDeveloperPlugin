package com.gemtechnologies.actions

import com.gemtechnologies.modules.api.AddComponentWizard
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent

class GenerateApiAction : AnAction() {
    override fun actionPerformed(e: AnActionEvent) {
        try {
            AddComponentWizard.run(e)
        } catch (e: Exception) {
            e.printStackTrace()
            error(e)
        }
    }
}
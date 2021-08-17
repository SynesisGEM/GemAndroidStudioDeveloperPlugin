package com.gemtechnologies.actions

import com.gemtechnologies.modules.module.ModuleSettingsDialog
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent

class CreateNewModuleAction : AnAction() {
    override fun actionPerformed(e: AnActionEvent) {
        generate(e)
    }

    private fun generate(anActionEvent: AnActionEvent) {
        val dialog = ModuleSettingsDialog(anActionEvent.project!!.basePath!!, anActionEvent)
        dialog.pack()
        dialog.setLocationRelativeTo(null);  // *** this will center your app ***
        dialog.isVisible = true
    }
}
package com.gemtechnologies.actions

import com.gemtechnologies.modules.strings.StringsGenerator
import com.gemtechnologies.utils.NotificationManager
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent

class GenerateStringsAction : AnAction() {

    override fun actionPerformed(event: AnActionEvent) {
        event.project?.basePath?.let {
            try {
                StringsGenerator().generate(it, null)
                NotificationManager.showInfoNotification(
                    id = "GenerateStringsAction",
                    title = "Strings created successfully",
                    message = "Strings created successfully",
                    project = event.project!!
                )
            } catch (e: Exception) {
                e.printStackTrace()
                NotificationManager.showErrorNotification(
                    id = "GenerateStringsAction",
                    title = "Strings creation failed",
                    message = "Strings creation failed with exception: ${e.message}",
                    project = event.project!!
                )
            }
        }
    }
}
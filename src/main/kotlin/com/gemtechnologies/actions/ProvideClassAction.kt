package com.gemtechnologies.actions

import com.gemtechnologies.modules.provide.DaggerClassProvider
import com.gemtechnologies.utils.NotificationManager
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent

class ProvideClassAction : AnAction() {
    override fun actionPerformed(event: AnActionEvent) {
        try {
            DaggerClassProvider().provideClass(event)
            NotificationManager.showInfoNotification(
                id = "ProvideClassAction",
                title = "Dagger class provider",
                message = "Provide method copied to clipboard",
                project = event.project!!
            )
        } catch (e: Exception) {
            e.printStackTrace()
            NotificationManager.showErrorNotification(
                id = "ProvideClassAction",
                title = "Dagger class provider",
                message = "Provide failed with exception: ${e.message}",
                project = event.project!!
            )
        }
    }
}
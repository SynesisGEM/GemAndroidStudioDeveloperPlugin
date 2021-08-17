package com.gemtechnologies.utils

import com.intellij.notification.*
import com.intellij.openapi.project.Project

class NotificationManager {
    companion object {
        fun showInfoNotification(
            id: String,
            title: String,
            message: String,
            project: Project
        ) {
            Notification(
                id,
                title,
                message,
                NotificationType.INFORMATION
            )
                .notify(project)
        }

        fun showErrorNotification(
            id: String,
            title: String,
            message: String,
            project: Project
        ) {
            Notification(
                id,
                title,
                message,
                NotificationType.ERROR
            )
                .notify(project)
        }
    }
}
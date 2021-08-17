package com.gemtechnologies

import com.intellij.ide.plugins.IdeaPluginDescriptor
import com.intellij.ide.plugins.PluginManager
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.extensions.PluginId

class DeveloperPluginComponent {

    companion object {
        val instance: DeveloperPluginComponent
            get() = ApplicationManager.getApplication().getComponent(DeveloperPluginComponent::class.java)
    }
}
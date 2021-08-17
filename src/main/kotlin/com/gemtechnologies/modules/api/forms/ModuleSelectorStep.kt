package com.gemtechnologies.modules.api.forms

import com.gemtechnologies.modules.api.AddComponentWizardModel
import com.gemtechnologies.modules.api.common.SelectedSettingsHolder
import com.gemtechnologies.modules.api.common.Settings
import com.gemtechnologies.modules.api.common.SettingsManager
import com.gemtechnologies.modules.api.common.StorageUtils
import com.gemtechnologies.utils.NotificationManager
import com.intellij.ide.DataManager
import com.intellij.openapi.actionSystem.DataConstants
import com.intellij.openapi.actionSystem.PlatformDataKeys
import com.intellij.openapi.project.Project
import com.intellij.ui.wizard.WizardNavigationState
import com.intellij.ui.wizard.WizardStep
import java.awt.event.MouseEvent
import java.awt.event.MouseListener
import javax.swing.*


class ModuleSelectorStep : WizardStep<AddComponentWizardModel>() {
    lateinit var rootPanel: JPanel
    lateinit var savedSettingsList: JList<String>
    lateinit var modulesList: JList<String>
    lateinit var title: JLabel
    private var clickCount = 0

    override fun prepare(wizardNavigationState: WizardNavigationState): JComponent {
        rootPanel.revalidate()
        val foldersList: ListModel<String> = StorageUtils.getFoldersList()
        modulesList.model = foldersList
        if (foldersList.size > 3) {
            modulesList.selectedIndex = 0
        }
        savedSettingsList.model = getSettingsListModel()

        DataManager.getInstance().dataContextFromFocusAsync.onSuccess {
            it.getData(PlatformDataKeys.PROJECT)?.let {
                title.addMouseListener(object : MouseListener {
                    override fun mouseClicked(e: MouseEvent?) {
                        if (clickCount < 5) {
                            NotificationManager.showInfoNotification(
                                id = "ModuleSelectorStep$clickCount",
                                title = "Piy",
                                message = "Piy",
                                project = it
                            )
                        } else {
                            NotificationManager.showInfoNotification(
                                id = "ModuleSelectorStep$clickCount",
                                title = "Piy",
                                message = "hyle klikaesh, pishi kod",
                                project = it
                            )
                        }
                        clickCount++
                    }

                    override fun mousePressed(e: MouseEvent?) {
                    }

                    override fun mouseReleased(e: MouseEvent?) {
                    }

                    override fun mouseEntered(e: MouseEvent?) {
                    }

                    override fun mouseExited(e: MouseEvent?) {
                    }

                })
            }
        }
        return rootPanel
    }

    private fun getSettingsListModel(): DefaultListModel<String> {
        val settingsListModel: DefaultListModel<String> = DefaultListModel<String>()
        SettingsManager.load()
        val settingsList: List<Settings> = SettingsManager.getSettingsList()
        for (settings in settingsList) {
            settingsListModel.addElement(settings.componentName)
        }
        return settingsListModel
    }

    override fun onNext(model: AddComponentWizardModel): WizardStep<*> {
        if (!savedSettingsList.isSelectionEmpty) {
            val settings: Settings = SettingsManager.getSettingsList()[savedSettingsList.selectedIndex]
            SelectedSettingsHolder.settings = settings
        }
        if (!modulesList.isSelectionEmpty) {
            SelectedSettingsHolder.settings =
                SelectedSettingsHolder.settings?.copy(moduleName = modulesList.selectedValue as String) ?: Settings(
                    moduleName = modulesList.selectedValue as String
                )
        }
        return super.onNext(model)
    }
}
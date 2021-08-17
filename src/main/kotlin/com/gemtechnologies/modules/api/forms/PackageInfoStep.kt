package com.gemtechnologies.modules.api.forms

import com.gemtechnologies.modules.api.AddComponentWizardModel
import com.gemtechnologies.modules.api.common.SelectedSettingsHolder
import com.gemtechnologies.modules.api.common.Settings
import com.gemtechnologies.modules.api.common.SettingsManager
import com.gemtechnologies.modules.api.common.StorageUtils.toFirstUpperCase
import com.gemtechnologies.modules.api.kotlinbuilders.KotlinApiBuilder
import com.gemtechnologies.utils.NotificationManager
import com.intellij.openapi.actionSystem.ActionManager
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.ui.wizard.WizardNavigationState
import com.intellij.ui.wizard.WizardStep
import com.gemtechnologies.modules.api.kotlinbuilders.ApiGenerationConfiguration
import java.awt.event.KeyEvent
import java.awt.event.KeyListener
import javax.swing.*

class PackageInfoStep(private val anAction: AnActionEvent?) : WizardStep<AddComponentWizardModel?>() {
    lateinit var rootPanel: JPanel
    lateinit var headerPanel: JPanel
    lateinit var swaggerPanel: JPanel
    lateinit var agentPanel: JPanel
    lateinit var header: JLabel
    lateinit var packageNameLabel: JLabel
    lateinit var componentNameTextField: JTextField
    lateinit var domainTextField: JTextField
    lateinit var swaggerUrlTextField2: JTextField
    lateinit var useCheckBox: JCheckBox

    override fun prepare(wizardNavigationState: WizardNavigationState): JComponent {
        rootPanel.revalidate()
        SelectedSettingsHolder.let {
            componentNameTextField.text = it.settings!!.componentName
            domainTextField.text = it.settings!!.domainName
            swaggerUrlTextField2.text = it.settings!!.swaggerUrl
        }
        setKeyListeners()
        setWizardFinishButtonProperties(wizardNavigationState)
        return rootPanel
    }

    private fun setWizardFinishButtonProperties(wizardNavigationState: WizardNavigationState) {
        swaggerUrlTextField2.addKeyListener(object : KeyListener {
            override fun keyTyped(e: KeyEvent) {
                checkIfCanEnableFinishButton(wizardNavigationState)
            }

            override fun keyPressed(e: KeyEvent) {
                checkIfCanEnableFinishButton(wizardNavigationState)
            }

            override fun keyReleased(e: KeyEvent) {
                checkIfCanEnableFinishButton(wizardNavigationState)
            }
        })
        wizardNavigationState.FINISH.setName("MAKE IT OHUENNO")
        if (!canFinish()) {
            wizardNavigationState.FINISH.isEnabled = false
        }
    }

    private fun setKeyListeners() {
        val textChangeKeyListener: KeyListener = object : KeyListener {
            override fun keyTyped(e: KeyEvent) {
                packageNameLabel.text = domainTextField.text + "." + componentNameTextField.text
            }

            override fun keyPressed(e: KeyEvent) {
                packageNameLabel.text = domainTextField.text + "." + componentNameTextField.text
            }

            override fun keyReleased(e: KeyEvent) {
                packageNameLabel.text = domainTextField.text + "." + componentNameTextField.text
            }
        }
        componentNameTextField.addKeyListener(textChangeKeyListener)
        domainTextField.addKeyListener(textChangeKeyListener)
    }

    private fun checkIfCanEnableFinishButton(wizardNavigationState: WizardNavigationState) {
        if (canFinish()) {
            wizardNavigationState.FINISH.isEnabled = true
        }
    }

    override fun onFinish(): Boolean {
        if (canFinish()) {
            updateSettingsValues()
            saveSettings()
            buildKotlinApi()
        }
        return super.onFinish()
    }

    private fun canFinish(): Boolean {
        return (componentNameTextField.text.isNotBlank()
                && domainTextField.text.isNotBlank()
                && swaggerUrlTextField2.text.isNotBlank())
    }

    private fun saveSettings() {
        SelectedSettingsHolder.settings?.let {
            SettingsManager.add(it)
            SettingsManager.save()
        }
    }

    private fun updateSettingsValues() {
        SelectedSettingsHolder.settings = SelectedSettingsHolder.settings!!.copy(
            componentName = componentNameTextField.text,
            domainName = domainTextField.text,
            swaggerUrl = swaggerUrlTextField2.text,
        )
    }

    private fun buildKotlinApi() {
        val configuration = ApiGenerationConfiguration(
            swaggerUrlTextField2.text,
            SelectedSettingsHolder.settings!!.packageName!!,
            toFirstUpperCase(componentNameTextField.text),
            SelectedSettingsHolder.settings!!.moduleName!!,
            useCheckBox.isSelected
        )
        try {
            val kotlinApiBuilder = KotlinApiBuilder(configuration)
            kotlinApiBuilder.build()
            kotlinApiBuilder.generateFiles()

            anAction?.let {
                ActionManager.getInstance().getActionOrStub("ReloadFromDisk")?.actionPerformed(anAction)
            }

            anAction?.project?.let {
                NotificationManager.showInfoNotification(
                    id = "PackageInfoStep",
                    title = "Generate Api",
                    message = "Generate Api Finished Successful",
                    project = it
                )
            }
        } catch (e: Exception) {
            e.printStackTrace()
            anAction?.project?.let {
                NotificationManager.showErrorNotification(
                    id = "PackageInfoStep",
                    title = "Generate Api",
                    message = "Generate Api Failed With Exception: $e",
                    project = it
                )
            }
        }
    }
}
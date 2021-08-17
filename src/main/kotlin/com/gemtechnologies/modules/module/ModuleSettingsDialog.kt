package com.gemtechnologies.modules.module

import com.gemtechnologies.utils.NotificationManager.Companion.showErrorNotification
import com.gemtechnologies.utils.NotificationManager.Companion.showInfoNotification
import com.intellij.openapi.actionSystem.ActionManager
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.project.ProjectManager
import java.awt.event.ActionEvent
import javax.swing.JButton
import javax.swing.JDialog
import javax.swing.JPanel
import javax.swing.JTextField

class ModuleSettingsDialog(private val path: String, anActionEvent: AnActionEvent?) : JDialog() {
    private var contentPane: JPanel? = null
    private var buttonCreate: JButton? = null
    private var buttonCancel: JButton? = null
    private var moduleName: JTextField? = null
    private var packageName: JTextField? = null
    private var classesPrefix: JTextField? = null
    private var screenName: JTextField? = null

    init {
        setContentPane(contentPane)
        isModal = true
        getRootPane().defaultButton = buttonCreate
        buttonCreate!!.addActionListener { e: ActionEvent -> create(anActionEvent) }
        buttonCancel!!.addActionListener { e: ActionEvent? -> dispose() }
    }

    private fun create(anActionEvent: AnActionEvent?) {
        if (moduleName!!.text.isNotEmpty() && packageName!!.text.isNotEmpty() && classesPrefix!!.text.isNotEmpty()) {
            try {
                ModuleGenerator(
                    path = path,
                    moduleName = moduleName!!.text,
                    packageName = packageName!!.text,
                    classesPrefix = classesPrefix!!.text,
                    screenName = screenName!!.text
                )
                    .generate()

                val am = ActionManager.getInstance()
                val sync = am.getAction("Android.SyncProject")

                anActionEvent?.let {
                    sync.actionPerformed(it)
                    showInfoNotification(
                        id = "ModuleSettingsDialog",
                        title = "New module created",
                        message = "Project sync started",
                        project = it.project!!
                    )
                }
            } catch (e: Exception) {
                e.printStackTrace()
                anActionEvent?.let {
                    showErrorNotification(
                        id = "ModuleSettingsDialog",
                        title = "New module creation failed",
                        message = "New module creation failed with exception: ${e.message}",
                        project = it.project!!
                    )
                }
            }
            dispose()
        }
    }

    companion object {
        @JvmStatic
        fun main(args: Array<String>) {
            val dialog = ModuleSettingsDialog("", null)
            dialog.pack()
            dialog.isVisible = true
            System.exit(0)
        }
    }
}
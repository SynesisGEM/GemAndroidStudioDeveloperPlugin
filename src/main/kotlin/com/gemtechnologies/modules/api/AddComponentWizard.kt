package com.gemtechnologies.modules.api

import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.ui.wizard.WizardDialog
import java.awt.Dimension
import javax.swing.SwingUtilities

class AddComponentWizard(anAction: AnActionEvent?) : WizardDialog<AddComponentWizardModel>(
    true,
    true,
    AddComponentWizardModel(anAction, "Generate API via SWAGGER")
) {

    override fun getWindowPreferredSize(): Dimension {
        return Dimension(WIDTH, HEIGHT)
    }

    companion object {
        const val WIDTH = 1024
        const val HEIGHT = 500

        fun run(anAction: AnActionEvent?) {
            println(AddComponentWizard(anAction).showAndGet())
        }

        @JvmStatic
        fun main(args: Array<String>) {
            SwingUtilities.invokeLater { AddComponentWizard.run(null) }
        }
    }
}

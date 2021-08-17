package com.gemtechnologies.modules.provide

import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.editor.Document
import com.intellij.openapi.fileEditor.FileDocumentManager
import com.intellij.openapi.fileEditor.FileEditorManager
import com.intellij.openapi.vfs.VirtualFile
import java.awt.Toolkit
import java.awt.datatransfer.Clipboard
import java.awt.datatransfer.StringSelection

class DaggerClassProvider {

    fun provideClass(anActionEvent: AnActionEvent) {
        anActionEvent.project?.let { project ->
            val currentDoc: Document = FileEditorManager.getInstance(project).selectedTextEditor!!.document
            val currentFile: VirtualFile? = FileDocumentManager.getInstance().getFile(currentDoc)
            currentFile?.let {
                val provide = createProvide(currentDoc.text, currentFile.nameWithoutExtension)
                copyToClipboard(provide)
            }
        }
    }

    fun createProvide(classText: String, nameWithoutExtension: String): String {
        val startClass = classText.indexOfFirst { it == '{' }
        val startConstructor = classText.indexOfFirst { it == '(' }
        val endConstructor = classText.indexOfFirst { it == ')' }
        val isConstructorEmpty = startConstructor > endConstructor
        val withoutConstructor = startConstructor >= startClass
        return if (isConstructorEmpty || withoutConstructor || startConstructor == -1) {
            "\t@Provides\n" +
                    "\tfun provide$nameWithoutExtension() = $nameWithoutExtension()"
        } else {
            val classes = mutableListOf<String>()
            classText.substring(startConstructor, endConstructor).split(",").forEach {
                it.split(":").lastOrNull()?.let {
                    classes.add(it.replace("\n", "").trim())
                }
            }
            val stringBuilder = StringBuilder()
            stringBuilder
                .append("\t@Provides\n")
                .append("\tfun provide$nameWithoutExtension(\n")
            classes.forEachIndexed { index, s ->
                val last = index == classes.size - 1
                val postfix = if (last) {
                    "\n"
                } else {
                    ",\n"
                }
                stringBuilder.append(s[0].toLowerCase() + s.substring(1) + ": " + s + postfix)
            }
            stringBuilder.append("\t) = $nameWithoutExtension(\n")
            classes.forEachIndexed { index, s ->
                val last = index == classes.size - 1
                val postfix = if (last) {
                    "\n"
                } else {
                    ",\n"
                }
                stringBuilder.append(s[0].toLowerCase() + s.substring(1) + postfix)
            }
            stringBuilder.append("\t)")

            stringBuilder.toString()
        }
    }

    private fun copyToClipboard(str: String) {
        val clipboard = getSystemClipboard()
        clipboard.setContents(StringSelection(str), null)
    }

    private fun getSystemClipboard(): Clipboard {
        return Toolkit.getDefaultToolkit().systemClipboard
    }

    companion object {

        @JvmStatic
        fun main(args: Array<String>) {
            println(
                DaggerClassProvider().createProvide(
                    "class GetScrollDownStateUseCase {\n" +
                            "\n" +
                            "}",
                    "GetScrollDownStateUseCase"
                )
            )
        }
    }
}
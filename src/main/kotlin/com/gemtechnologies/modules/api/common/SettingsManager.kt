package com.gemtechnologies.modules.api.common

import com.intellij.openapi.project.ProjectManager
import com.intellij.openapi.roots.ProjectRootManager
import com.intellij.openapi.vfs.VirtualFile
import java.io.*

object SettingsManager {
    private const val DEFAULT_FILE_NAME = "ApiGenerationSettings.sav"

    private val contentRoots: Array<VirtualFile> =
        ProjectRootManager.getInstance(ProjectManager.getInstance().openProjects[0]).contentRoots

    private val savedFilePath: String = contentRoots[0].path + "/" + DEFAULT_FILE_NAME
    private val settingsList: HashSet<Settings> = hashSetOf()

    fun add(settings: Settings) {
        settingsList.add(settings)
    }

    fun getSettingsList(): List<Settings> {
        return settingsList.toList()
    }

    fun setSettingsList(settingsList: MutableList<Settings>) {
        this.settingsList.clear()
        this.settingsList.addAll(settingsList)
    }

    fun save() {
        var objectOutputStream: ObjectOutputStream? = null
        val fileOutputStream: FileOutputStream
        try {
            fileOutputStream = FileOutputStream(savedFilePath, true)
            objectOutputStream = ObjectOutputStream(fileOutputStream)
            objectOutputStream.writeObject(settingsList)
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            if (objectOutputStream != null) {
                try {
                    objectOutputStream.close()
                } catch (e: IOException) {
                    e.printStackTrace()
                }
            }
        }
    }

    fun load() {
        var objectinputstream: ObjectInputStream? = null
        try {
            if (File(savedFilePath).exists()) {
                val streamIn = FileInputStream(savedFilePath)
                objectinputstream = ObjectInputStream(streamIn)
                settingsList.addAll(objectinputstream.readObject() as HashSet<Settings>)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            if (objectinputstream != null) {
                try {
                    objectinputstream.close()
                } catch (e: IOException) {
                    e.printStackTrace()
                }
            }
        }
    }
}
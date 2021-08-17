package com.gemtechnologies.modules.api.common

import com.intellij.ide.DataManager
import com.intellij.openapi.actionSystem.DataConstants
import com.intellij.openapi.fileEditor.FileEditorManager
import com.intellij.openapi.project.Project
import com.intellij.openapi.roots.ProjectRootManager
import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.TypeSpec
import org.apache.commons.io.FileUtils
import java.io.File
import java.io.IOException
import java.nio.file.FileSystems
import java.nio.file.Files
import javax.swing.DefaultListModel
import javax.swing.ListModel

object StorageUtils {

    @JvmStatic
    fun getFoldersList(): ListModel<String> {
        val listModel = DefaultListModel<String>()
        val currentProject = DataManager.getInstance().dataContext.getData(DataConstants.PROJECT) as Project?
        if (currentProject != null) {
            val contentRoots = ProjectRootManager.getInstance(currentProject).contentRoots
            for (virtualFile in contentRoots) {
                if (virtualFile.isDirectory && virtualFile.isWritable) {
                    listModel.addElement(virtualFile.name)
                }
            }
            val contentRootsFromAllModules =
                ProjectRootManager.getInstance(currentProject).contentRootsFromAllModules[0].children
            for (virtualFile in contentRootsFromAllModules) {
                if (virtualFile.isDirectory && virtualFile.isWritable) {
                    listModel.addElement(virtualFile.name)
                }
            }
        }
        return listModel
    }

    fun generateFiles(
        moduleName: String?,
        packageName: String?,
        subPackage: String?,
        classTypeSpec: TypeSpec,
        imports: List<String>
    ) {
        try {
            val builder: FileSpec.Builder = FileSpec.builder(
                if (subPackage != null) "$packageName.$subPackage" else packageName!!,
                classTypeSpec.name!!
            )
            imports.forEach {
                val split = it.split(".")
                builder.addImport(packageName + "." + split.dropLast(1).joinToString("."), split.last())
            }
            val kotlinFile = builder
                .addType(classTypeSpec)
                .indent("    ")
                .build()
            val projectPath: String
            val currentProject = DataManager.getInstance().dataContext.getData(DataConstants.PROJECT) as Project?
            projectPath = if (!moduleName.isNullOrBlank()) {
                FileEditorManager.getInstance(currentProject!!).project.basePath + "/" + moduleName + "/src/main/java/"
            } else {
                FileEditorManager.getInstance(currentProject!!).project.basePath + "/src/main/java/"
            }
            val path = FileSystems.getDefault().getPath(projectPath)
            if (!Files.exists(path)) {
                Files.createDirectories(path)
            }
            kotlinFile.writeTo(path)
            kotlinFile.writeTo(System.out)
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    @JvmStatic
    fun toFirstUpperCase(text: String): String {
        return text.substring(0, 1).toUpperCase() + text.substring(1)
    }

    @JvmStatic
    fun toFirstLowerCase(text: String): String {
        return text.substring(0, 1).toLowerCase() + text.substring(1)
    }

    fun removePreviousFiles(moduleName: String?, packageName: String) {
        try {
            val currentProject = DataManager.getInstance().dataContext.getData(DataConstants.PROJECT) as Project?
            val projectPath = if (!moduleName.isNullOrBlank()) {
                FileEditorManager.getInstance(currentProject!!).project.basePath + "/" + moduleName + "/src/main/java/"
            } else {
                FileEditorManager.getInstance(currentProject!!).project.basePath + "/src/main/java/"
            }
            val file = File("$projectPath/${packageName.replace(".", "/")}")
            FileUtils.deleteDirectory(file)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
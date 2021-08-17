package com.gemtechnologies.modules.api.kotlinbuilders

import com.google.gson.annotations.SerializedName
import com.intellij.ide.impl.ProjectUtil
import com.squareup.kotlinpoet.*
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import io.reactivex.Completable
import io.reactivex.Single
import io.swagger.models.*
import io.swagger.models.parameters.BodyParameter
import io.swagger.models.parameters.PathParameter
import io.swagger.models.parameters.QueryParameter
import io.swagger.models.properties.*
import io.swagger.parser.SwaggerParser
import okhttp3.MultipartBody
import okhttp3.ResponseBody
import org.apache.commons.io.FileUtils
import com.gemtechnologies.modules.api.additional.data.ApiPath
import com.gemtechnologies.modules.api.additional.data.Parameter
import com.gemtechnologies.modules.api.common.StorageUtils
import retrofit2.http.*
import retrofit2.http.Path
import java.io.File
import java.io.FileNotFoundException
import java.net.URL
import java.net.UnknownHostException
import java.nio.charset.StandardCharsets
import java.util.zip.GZIPInputStream
import kotlin.collections.set

class KotlinApiBuilder(
    private val apiGenerationConfiguration: ApiGenerationConfiguration
) {
    companion object {
        const val OK_RESPONSE = "200"
        const val ARRAY_SWAGGER_TYPE = "array"
        const val INTEGER_SWAGGER_TYPE = "integer"
        const val NUMBER_SWAGGER_TYPE = "number"
        const val STRING_SWAGGER_TYPE = "string"
        const val BOOLEAN_SWAGGER_TYPE = "boolean"
        const val REF_SWAGGER_TYPE = "ref"


        const val PREFIX_API = "api"
        const val PREFIX_MODELS = "models"
        const val PREFIX_DTO = "dto"
        const val PREFIX_COMMON = "common"
    }

    private val swaggerModel: Swagger = try {
        val file = File(ProjectUtil.getBaseDir(), "swagger.json")
        if (file.exists()) {
            file.delete()
        }
        val path = try {
            val json = try {
                String(GZIPInputStream(URL(apiGenerationConfiguration.swaggerUrl).openStream()).readBytes())
            } catch (e: Exception) {
                URL(apiGenerationConfiguration.swaggerUrl).readText()
            }
            FileUtils.writeStringToFile(file, json, StandardCharsets.UTF_8)
            file.path
        } catch (e: Exception) {
            apiGenerationConfiguration.swaggerUrl
        }

        SwaggerParser().read(path)
    } catch (unknown: UnknownHostException) {
        unknown.printStackTrace()
        Swagger()
    } catch (illegal: IllegalStateException) {
        illegal.printStackTrace()
        Swagger()
    } catch (notFound: FileNotFoundException) {
        notFound.printStackTrace()
        Swagger()
    }

    private lateinit var apiInterfaceTypeSpec: Map<String, TypeSpec>
    private val responseBodyModelListTypeSpec: ArrayList<TypeSpec> = ArrayList()
    private val enumListTypeSpec: ArrayList<TypeSpec> = ArrayList()

    fun build() {
        StorageUtils.removePreviousFiles(apiGenerationConfiguration.moduleName, apiGenerationConfiguration.packageName)
        createEnumClasses()
        apiInterfaceTypeSpec = createApiRetrofitInterface(createApiResponseBodyModel(), emptyList<TypeSpecWrapper>())
    }

    fun generateFiles() {
        val classesWithSubPackages = arrayListOf<TypeSpecWrapper>() //<ClassName,(SubPackageName, TypeSpec)>

        //v1 generation
//    responseBodyModelListTypeSpec.forEach check@{ typeSpec ->
//      var packageName = PREFIX_DTO
//      try {
//        apiInterfaceTypeSpec.forEach { pack, apiTypeSpec ->
//          apiTypeSpec.funSpecs.forEach { apiFunSpec ->
//            (apiFunSpec.returnType as? ParameterizedTypeName)?.typeArguments?.forEach {
//              if ((it as? TypeVariableName)?.name?.equals(typeSpec.name, true) == true) {
//                packageName = "$pack"
//              }
//            }
//            apiFunSpec.parameters.forEach { parameterSpec ->
//              if ((parameterSpec.type as? ClassName)?.canonicalName?.equals(typeSpec.name, true) == true) {
//                packageName = "$pack"
//              }
//            }
//          }
//        }
//      } catch (e: Exception) {
////          e.printStackTrace()
//      }
//
//      classesWithSubPackages.put(typeSpec.name!!, TypeSpecWrapper(packageName, typeSpec))
//    }
//
//    classesWithSubPackages.values.forEach { typeSpecWrapper ->
//      updatePackage(typeSpecWrapper, classesWithSubPackages.values.toList())
//    }
//
//    classesWithSubPackages.values.forEach {
//      if (it.subPackage == PREFIX_DTO) {
//        it.subPackage = PREFIX_COMMON
//      }
//    }


        //v2 generation

        apiInterfaceTypeSpec.forEach { pack, apiTypeSpec ->
            val responseClasses = apiTypeSpec.funSpecs.mapNotNull { it.returnType as? TypeVariableName }
                .map { it.name } + apiTypeSpec.funSpecs.mapNotNull { it.returnType as? ParameterizedTypeName }
                .map { it.typeArguments }.flatten().mapNotNull { it as TypeVariableName }.map { it.name }

            val requestClasses =
                apiTypeSpec.funSpecs.mapNotNull { it.parameters }.flatten().mapNotNull { it.type as? ClassName }
                    .map { it.canonicalName }

            val names = responseClasses + requestClasses

            names.forEach { name ->
                responseBodyModelListTypeSpec.firstOrNull { it.name == name }?.apply {

//          ((propertySpecs.mapNotNull { it.type as? TypeVariableName }.map { it.name } + propertySpecs.mapNotNull { it.type as? ParameterizedTypeName }.map { it.typeArguments }.flatten().mapNotNull { it as? TypeVariableName }.map { it.name })
//            .filter { propertyName ->
//              responseBodyModelListTypeSpec.any { it.name == propertyName }
//            }
//            + name)
//            .forEach { n ->
                    val innerClassesNames = ((propertySpecs.mapNotNull { it.type as? TypeVariableName }
                        .map { it.name } + propertySpecs.mapNotNull { it.type as? ParameterizedTypeName }
                        .map { it.typeArguments }.flatten().mapNotNull { it as? TypeVariableName }.map { it.name }))

                    val typeSpecWrapper = classesWithSubPackages.firstOrNull { it.typeSpec.name == name }

                    if (typeSpecWrapper == null) {
                        classesWithSubPackages.add(TypeSpecWrapper(pack.toLowerCase(), this))
                    } else if (typeSpecWrapper.subPackage != pack) {
                        typeSpecWrapper.subPackage = PREFIX_COMMON
//              }
                    }

                    //TODO OPTIMIZE

                    responseBodyModelListTypeSpec
                        .filter {
                            innerClassesNames.contains(it.name)
                        }
                        .forEach { ts ->
                            val typeSpecWrapper = classesWithSubPackages.firstOrNull { it.typeSpec.name == ts.name }
                            if (typeSpecWrapper == null) {
                                classesWithSubPackages.add(TypeSpecWrapper(pack.toLowerCase(), ts))
                            } else if (typeSpecWrapper.subPackage != pack) {
                                typeSpecWrapper.subPackage = PREFIX_COMMON
                            }
                        }
                }
            }
        }

        responseBodyModelListTypeSpec.forEach { ts ->
            if (classesWithSubPackages.firstOrNull { it.typeSpec.name == ts.name } == null) {
                classesWithSubPackages.add(TypeSpecWrapper(PREFIX_COMMON, ts))
            }
        }

        classesWithSubPackages.forEach {
            val imports = generateImports(it.typeSpec, classesWithSubPackages)
            StorageUtils.generateFiles(
                moduleName = apiGenerationConfiguration.moduleName,
                packageName = apiGenerationConfiguration.packageName,
                subPackage = "${it.subPackage.toLowerCase()}.$PREFIX_MODELS",
                classTypeSpec = it.typeSpec,
                imports = imports
            )
        }

//    val createApiRetrofitInterface = createApiRetrofitInterface(createApiResponseBodyModel(), classesWithSubPackages)

        createApiRetrofitInterface(
            createApiResponseBodyModel(),
            classesWithSubPackages
        ).forEach { subPackage, typeSpec ->
            val imports = generateImports(typeSpec, classesWithSubPackages)

            StorageUtils.generateFiles(
                moduleName = apiGenerationConfiguration.moduleName,
                packageName = apiGenerationConfiguration.packageName,
                subPackage = "${subPackage.toLowerCase()}.$PREFIX_API",
                classTypeSpec = typeSpec,
                imports = imports
            )
        }

        for (typeSpec in enumListTypeSpec) {
            StorageUtils.generateFiles(
                apiGenerationConfiguration.moduleName,
                apiGenerationConfiguration.packageName,
                null,
                typeSpec,
                emptyList()
            )
        }
    }

    private fun generateImportsForDataClass(
        typeSpec: TypeSpec,
        classesWithPackages: List<TypeSpecWrapper>
    ): ArrayList<String> {
        val imports = arrayListOf<String>()
        typeSpec.primaryConstructor?.parameters?.map { it.name }?.forEach { paramName ->
            classesWithPackages.firstOrNull { it.typeSpec.name == paramName }?.let {

            }
        }
        (typeSpec.superclass as? TypeVariableName)?.let { superClass ->
            classesWithPackages.firstOrNull { it.typeSpec.name == superClass.name }?.let {
                imports.add("${it.subPackage}.$PREFIX_MODELS.${it.typeSpec.name}")
            }
        }
        return imports
    }

    private fun generateImports(u: TypeSpec, cl: List<TypeSpecWrapper>): List<String> {
        val imports = hashSetOf<String>()
        try {
            u.funSpecs.forEach { funSpec ->
                (funSpec.returnType as? ParameterizedTypeName)?.typeArguments?.forEach {
                    val name = (it as? TypeVariableName)?.name
                    val find = cl.find { pair -> (pair.typeSpec.name?.equals(name, true)) == true }
                    if (null != find) {
                        imports.add("${find.subPackage.toLowerCase()}.$PREFIX_MODELS.${find.typeSpec.name}")
                    }
                }
                (funSpec.returnType as? TypeVariableName)?.let {
                    val find = cl.find { pair -> (pair.typeSpec.name?.equals(it.name, true)) == true }
                    if (null != find) {
                        imports.add("${find.subPackage.toLowerCase()}.$PREFIX_MODELS.${find.typeSpec.name}")
                    }
                }
                funSpec.parameters.forEach { parameterSpec ->
                    val name = (parameterSpec.type as? ClassName)?.canonicalName
                    val find = cl.find { pair -> (pair.typeSpec.name?.equals(name, true)) == true }
                    if (null != find) {
                        imports.add("${find.subPackage.toLowerCase()}.$PREFIX_MODELS.${find.typeSpec.name}")
                    }
                }
            }
            u.propertySpecs.forEach {
                (it.type as? ParameterizedTypeName)?.typeArguments?.forEach { parameterizedTypeName: TypeName ->
                    val name = getTypeVariableName(parameterizedTypeName)
                    val find = cl.find { it.typeSpec.name?.equals(name) == true }
                    if (null != find) {
                        imports.add("${find.subPackage.toLowerCase()}.$PREFIX_MODELS.${find.typeSpec.name}")
                    }
                }

                val name = (it.type as? TypeVariableName)?.name
                val find = cl.find { pair -> (pair.typeSpec.name?.equals(name, true)) == true }
                if (null != find) {
                    imports.add("${find.subPackage.toLowerCase()}.$PREFIX_MODELS.${find.typeSpec.name}")
                }
            }
            if ((u.superclass as TypeVariableName).name != "Any") {
                val name = (u.superclass as TypeVariableName).name
                val find = cl.find { pair -> (pair.typeSpec.name?.equals(name, true)) == true }
                if (find != null) {
                    imports.add("${find.subPackage.toLowerCase()}.$PREFIX_MODELS.${find.typeSpec.name}")
                    find.typeSpec.propertySpecs.forEach {
                        (it.type as? ParameterizedTypeName)?.typeArguments?.forEach { parameterizedTypeName: TypeName ->
                            val name = getTypeVariableName(parameterizedTypeName)
                            val find = cl.find { it.typeSpec.name?.equals(name) == true }
                            if (null != find) {
                                imports.add("${find.subPackage.toLowerCase()}.$PREFIX_MODELS.${find.typeSpec.name}")
                            }
                        }

                        val name = (it.type as? TypeVariableName)?.name
                        val find = cl.find { pair -> (pair.typeSpec.name?.equals(name, true)) == true }
                        if (null != find) {
                            imports.add("${find.subPackage.toLowerCase()}.$PREFIX_MODELS.${find.typeSpec.name}")
                        }
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return imports.toList()
    }

    private fun getTypeVariableName(parameterizedTypeName: TypeName): String? {
        return when (parameterizedTypeName) {
            is ParameterizedTypeName -> getTypeVariableName(parameterizedTypeName.typeArguments[0])
            is TypeVariableName -> parameterizedTypeName.name
            else -> null
        }
    }

    private fun updatePackage(typeSpecWrapper: TypeSpecWrapper, cl: List<TypeSpecWrapper>) {
        var subPackage = typeSpecWrapper.subPackage
        if (subPackage != PREFIX_DTO) {
            typeSpecWrapper.typeSpec.propertySpecs.forEach { propertySpec ->
                (propertySpec.type as? ParameterizedTypeName)?.typeArguments?.forEach { parameterizedTypeName: TypeName ->
                    val name = (parameterizedTypeName as? TypeVariableName)?.name
                    val find = cl.find { it.typeSpec.name?.equals(name) == true }
                    if (null != find) {
                        if (find.subPackage == PREFIX_DTO || find.subPackage == typeSpecWrapper.subPackage) {
                            find.subPackage = typeSpecWrapper.subPackage
                        } else {
                            find.subPackage = PREFIX_COMMON
                        }
                        updatePackage(find, cl)
                    }
                }
                val name = (propertySpec.type as? TypeVariableName)?.name
                val find = cl.find { it.typeSpec.name?.equals(name) == true }
                if (null != find) {
                    if (find.subPackage == PREFIX_DTO || find.subPackage == typeSpecWrapper.subPackage) {
                        find.subPackage = typeSpecWrapper.subPackage
                    } else {
                        find.subPackage = PREFIX_COMMON
                    }
                    updatePackage(find, cl)
                }
            }
        }
    }

    private data class TypeSpecWrapper(var subPackage: String, val typeSpec: TypeSpec)

    private fun createEnumClasses() {
        addOperationResponseEnums()
        addModelEnums()
    }

    private fun addModelEnums() {
        if (swaggerModel.definitions != null && swaggerModel.definitions.isNotEmpty()) {
            for (definition in swaggerModel.definitions) {
                if (definition.value != null && definition.value.properties != null) {
                    for (modelProperty in definition.value.properties) {
                        if (modelProperty.value is StringProperty) {
                            val enumDefinition = (modelProperty.value as StringProperty).enum
                            if (enumDefinition != null) {
                                val enumTypeSpecBuilder = TypeSpec.enumBuilder(modelProperty.key.capitalize())
                                for (constant in enumDefinition) {
                                    enumTypeSpecBuilder.addEnumConstant(
                                        constant.split(".").joinToString(separator = "") { it.capitalize() })
                                }
                                if (!enumListTypeSpec.contains(enumTypeSpecBuilder.build())) {
                                    enumListTypeSpec.add(enumTypeSpecBuilder.build())
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    private fun addOperationResponseEnums() {
        if (swaggerModel.paths != null && !swaggerModel.paths.isEmpty()) {
            for (path in swaggerModel.paths) {
                for (operation in path.value.operationMap) {
                    try {
                        for (parameters in operation.value.parameters) {
                            if (parameters is PathParameter) {
                                if (parameters.enum != null) {
                                    val enumTypeSpecBuilder = TypeSpec.enumBuilder(parameters.name.capitalize())
                                    for (constant in parameters.enum) {
                                        enumTypeSpecBuilder.addEnumConstant(constant)
                                    }
                                    if (!enumListTypeSpec.contains(enumTypeSpecBuilder.build())) {
                                        enumListTypeSpec.add(enumTypeSpecBuilder.build())
                                    }
                                }
                            }
                        }
                    } catch (error: Exception) {
                        error.printStackTrace()
                    }
                }
            }
        }
    }

    private fun createApiResponseBodyModel(): List<String> {
        val classNameList = ArrayList<String>()

        swaggerModel.definitions?.forEach { definition ->

            var modelClassTypeSpec: TypeSpec.Builder
            try {
                modelClassTypeSpec = TypeSpec.classBuilder(definition.key)
                classNameList.add(definition.key)
            } catch (error: IllegalArgumentException) {
                modelClassTypeSpec =
                    TypeSpec.classBuilder("Model" + definition.key.capitalize()).addModifiers(KModifier.DATA)
                classNameList.add("Model" + definition.key.capitalize())
            }

            if (definition.value != null) {
                val primaryConstructor = FunSpec.constructorBuilder()
                if (definition.value.properties != null) {

                    val hasChilds = hasChilds(definition, swaggerModel.definitions)
                    if (hasChilds) {
                        modelClassTypeSpec.addModifiers(KModifier.OPEN)
                    } else {
                        modelClassTypeSpec.addModifiers(KModifier.DATA)
                    }

                    for (modelProperty in definition.value.properties) {
                        val typeName: TypeName = getTypeName(modelProperty)
                        val propertySpec = PropertySpec.builder(modelProperty.key, typeName)
                            .addAnnotation(
                                AnnotationSpec.builder(SerializedName::class)
                                    .addMember("\"${modelProperty.key}\"")
                                    .build()
                            )
                            .initializer(modelProperty.key)
                            .build()
                        primaryConstructor.addParameter(modelProperty.key, typeName)
                        modelClassTypeSpec.addProperty(propertySpec)

                    }
                } else if (definition.value is ComposedModel) {
                    if (hasChilds(definition, swaggerModel.definitions)) {
                        modelClassTypeSpec.addModifiers(KModifier.OPEN)
                    }
                    val composedModel: ComposedModel = (definition.value as ComposedModel)
                    if (composedModel.child.properties != null) {
                        val ignored = composedModel.interfaces.firstOrNull()?.simpleRef?.let {
                            getParentProperties(swaggerModel.definitions, it)
                        } ?: emptyMap()
                        addSelfProperties(composedModel, primaryConstructor, modelClassTypeSpec, ignored)
                    }
                    if (composedModel.interfaces.isNotEmpty()) {
                        addParentParams(composedModel, modelClassTypeSpec, primaryConstructor)
                    }
                } else {
                    if (hasChilds(definition, swaggerModel.definitions)) {
                        modelClassTypeSpec.addModifiers(KModifier.OPEN)
                    }
                }

                modelClassTypeSpec.primaryConstructor(primaryConstructor.build())
                responseBodyModelListTypeSpec.add(modelClassTypeSpec.build())
            }
        }

        return classNameList
    }

    private fun hasChilds(modelToCheck: Map.Entry<String, Model>, definitions: MutableMap<String, Model>): Boolean {
        modelToCheck.value.let {
            definitions.forEach { definition ->
                if (definition.value is ComposedModel) {
                    val composedModel = (definition.value as ComposedModel);
                    for (interfaceModel in composedModel.interfaces) {
                        if (interfaceModel.simpleRef == modelToCheck.key) {
                            return true
                        }
                    }
                }
            }
        }
        return false
    }

    private fun addParentParams(
        composedModel: ComposedModel,
        modelClassTypeSpec: TypeSpec.Builder,
        primaryConstructor: FunSpec.Builder
    ) {
        val parent = composedModel.interfaces[0]

        parent?.simpleRef?.let {
            modelClassTypeSpec.superclass(TypeVariableName.invoke(it))
            val parentProperties = getParentProperties(swaggerModel.definitions, it)

            for (property in parentProperties) {
                val typeName: TypeName = getTypeName(property)
                primaryConstructor.addParameter(property.key, typeName)
                modelClassTypeSpec.addSuperclassConstructorParameter(property.key, typeName)
            }
        }
    }

    private fun addSelfProperties(
        composedModel: ComposedModel,
        primaryConstructor: FunSpec.Builder,
        modelClassTypeSpec: TypeSpec.Builder,
        ignored: Map<String, Property>
    ) {
        for (property in composedModel.child.properties) {
            if (ignored[property.key] == null) {
                val typeName: TypeName = getTypeName(property)
                val propertySpec = PropertySpec.builder(property.key, typeName)
                    .addAnnotation(
                        AnnotationSpec.builder(SerializedName::class)
                            .addMember("\"${property.key}\"")
                            .build()
                    )
                    .initializer(property.key)
                    .build()

                primaryConstructor.addParameter(property.key, typeName)
                modelClassTypeSpec.addProperty(propertySpec)
            }
        }
    }

    private fun getParentProperties(definitions: Map<String, Model>, parentName: String?): Map<String, Property> {
        val resultProperties = mutableMapOf<String, Property>()
        parentName?.let {
            val model = definitions[it]

            val properties = model?.properties
            if (properties != null) {
                resultProperties.putAll(properties)
            } else if (model is ComposedModel) {
                resultProperties.putAll(model.child.properties)
                if (!model.interfaces.isEmpty()) {
                    val parent = model.interfaces[0].simpleRef
                    resultProperties.putAll(getParentProperties(definitions, parent))
                }
                return resultProperties
            }
        }
        return resultProperties
    }

    private fun createApiRetrofitInterface(
        classNameList: List<String>,
        classesWithSubPackages: List<TypeSpecWrapper>
    ): Map<String, TypeSpec> {
        val map: HashMap<String, TypeSpec> = HashMap()
        swaggerModel.tags.forEach {

            val apiInterfaceTypeSpecBuilder = TypeSpec
                .interfaceBuilder("${it.name.capitalize()}Api")

            addApiPathMethods(it.name, apiInterfaceTypeSpecBuilder, classNameList, classesWithSubPackages)

            val typeSpec = apiInterfaceTypeSpecBuilder.build()

            map[it.name] = typeSpec
        }

        return map
    }

    private fun addApiPathMethods(
        endpoint: String,
        apiInterfaceTypeSpec: TypeSpec.Builder,
        classNameList: List<String>,
        classesWithSubPackages: List<TypeSpecWrapper>
    ) {
        println("endpoint = [${endpoint}], apiInterfaceTypeSpec = [${apiInterfaceTypeSpec}], classNameList = [${classNameList}]")
        println("-----------------------------")
        if (swaggerModel.paths != null && !swaggerModel.paths.isEmpty()) {
            for (path in swaggerModel.paths) {
                for (operation in path.value.operationMap) {

                    println(path.key.removePrefix("/"))

                    if (path.key.removePrefix("/").startsWith(endpoint, true)) {

                        val hasMultipart = operation.value.parameters.any { it.`in`.contains("formData") }
                        val customUrl = operation.value.parameters.any { it.`in`.contains("url") }

                        val annotationSpec: AnnotationSpec = when {
                            operation.key.name.contains(
                                "GET"
                            ) -> {
                                val builder = AnnotationSpec.builder(GET::class)

                                if (!customUrl) {
                                    builder.addMember("\"${path.key.removePrefix("/")}\"")
                                }

                                builder.build()
                            }
                            operation.key.name.contains(
                                "POST"
                            ) -> {
                                val builder = AnnotationSpec.builder(POST::class)

                                if (!customUrl) {
                                    builder.addMember("\"${path.key.removePrefix("/")}\"")
                                }

                                builder.build()
                            }
                            operation.key.name.contains(
                                "PUT"
                            ) -> {
                                val builder = AnnotationSpec.builder(PUT::class)

                                if (!customUrl) {
                                    builder.addMember("\"${path.key.removePrefix("/")}\"")
                                }

                                builder.build()
                            }
                            operation.key.name.contains(
                                "PATCH"
                            ) -> {
                                val builder = AnnotationSpec.builder(PATCH::class)

                                if (!customUrl) {
                                    builder.addMember("\"${path.key.removePrefix("/")}\"")
                                }

                                builder.build()
                            }
                            operation.key.name.contains(
                                "DELETE"
                            ) -> {
                                val builder = AnnotationSpec.builder(DELETE::class)

                                if (!customUrl) {
                                    builder.addMember("\"${path.key.removePrefix("/")}\"")
                                }

                                builder.build()
                            }
                            operation.key.name.contains(
                                "URL"
                            ) -> AnnotationSpec.builder(GET::class).build()
                            else -> AnnotationSpec.builder(GET::class).addMember("\"${path.key.removePrefix("/")}\"")
                                .build()
                        }

                        try {
                            val doc =
                                ((listOf(operation.value.summary + "\n") + getMethodParametersDocs(operation)).joinToString(
                                    "\n"
                                )).trim()

                            val returnedClass = if (hasMultipart) {
                                if (apiGenerationConfiguration.useCoroutines) {
                                    TypeVariableName.invoke(ResponseBody::class.java.name)
                                } else {
                                    Single::class.asClassName()
                                        .parameterizedBy(TypeVariableName.invoke(ResponseBody::class.java.name))
                                }
                            } else {
                                getReturnedClass(operation, classNameList)
                            }
                            val methodParameters = getMethodParameters(operation, classesWithSubPackages)
                            val builder = FunSpec.builder(operation.value.operationId)

                            if (hasMultipart) {
                                builder.addAnnotation(AnnotationSpec.builder(Multipart::class).build())
                            }
                            val funSpec = builder.apply {
                                if (apiGenerationConfiguration.useCoroutines) {
                                    addModifiers(KModifier.SUSPEND, KModifier.ABSTRACT)
                                } else {
                                    addModifiers(KModifier.PUBLIC, KModifier.ABSTRACT)
                                }
                            }
                                .addAnnotation(annotationSpec)
                                .addParameters(methodParameters)
                                .returns(returnedClass)
                                .addKdoc("$doc\n")
                                .build()

                            apiInterfaceTypeSpec.addFunction(funSpec)
                        } catch (exception: Exception) {
                            exception.printStackTrace()
                        }
                    }
                }
            }
        }
    }

    private fun parseAnnotationFromAdditionalMethod(apiPath: ApiPath): AnnotationSpec {
        return when {
            apiPath.type.contains("GET", true) -> {
                val builder = AnnotationSpec.builder(GET::class)
                apiPath.path?.let {
                    builder.addMember("\"${it.removePrefix("/")}\"")
                }
                builder.build()
            }
            apiPath.type.contains("POST", true) -> {
                val builder = AnnotationSpec.builder(POST::class)
                apiPath.path?.let {
                    builder.addMember("\"${it.removePrefix("/")}\"")
                }
                builder.build()
            }
            apiPath.type.contains("PUT", true) -> {
                val builder = AnnotationSpec.builder(PUT::class)
                apiPath.path?.let {
                    builder.addMember("\"${it.removePrefix("/")}\"")
                }
                builder.build()
            }
            apiPath.type.contains("PATCH", true) -> {
                val builder = AnnotationSpec.builder(PATCH::class)
                apiPath.path?.let {
                    builder.addMember("\"${it.removePrefix("/")}\"")
                }
                builder.build()
            }
            apiPath.type.contains("DELETE", true) -> {
                val builder = AnnotationSpec.builder(DELETE::class)
                apiPath.path?.let {
                    builder.addMember("\"${it.removePrefix("/")}\"")
                }
                builder.build()
            }
            apiPath.type.contains("Multipart", true) -> AnnotationSpec.builder(Multipart::class).build()
            else -> {
                val builder = AnnotationSpec.builder(GET::class)
                apiPath.path?.let {
                    builder.addMember("\"${it.removePrefix("/")}\"")
                }
                builder.build()
            }
        }
    }

    private fun parseParametersFromAdditionalMethod(apiPath: List<Parameter>): Iterable<ParameterSpec> {
        return apiPath.map { parametr ->
            if (parametr.annotation.isBlank()) {
                ParameterSpec.builder(parametr.name, MultipartBody.Part::class.java).build()
            } else {
                when {
                    parametr.annotation.contains("body", true) -> {
                        ParameterSpec.builder(
                            parametr.name,
                            ClassName.bestGuess(parametr.type).requiredOrNullable(true)
                        )
                            .addAnnotation(AnnotationSpec.builder(Body::class).build()).build()
                    }
                    //TODO
//          parametr.annotation.contains("path", true) -> {
//            ParameterSpec.builder(parametr.name, MultipartBody.Part::class.java)
//              .addAnnotation(AnnotationSpec.builder(Path::class).build()).build()
//          }
//          parametr.annotation.contains("query", true) -> {
//            ParameterSpec.builder(parametr.name, MultipartBody.Part::class.java)
//              .addAnnotation(AnnotationSpec.builder(Query::class).addMember("\"${parametr.name}\"").build()).build()
//          }
                    parametr.annotation.contains("formData", true) -> {
                        ParameterSpec.builder(parametr.name, MultipartBody.Part::class.java)
                            .addAnnotation(AnnotationSpec.builder(Part::class).build()).build()
                    }
                    parametr.annotation.contains("Url", true) -> {
                        ParameterSpec.builder(parametr.name, String::class.asClassName())
                            .addAnnotation(AnnotationSpec.builder(Url::class).build()).build()
                    }
                    else -> {
                        ParameterSpec.builder(parametr.name, Any::class.java).build()
                    }
                }
            }
        }
    }

    private fun getMethodParametersDocs(operation: MutableMap.MutableEntry<HttpMethod, Operation>): Iterable<String> {
        return operation.value.parameters.filterNot { it.description.isNullOrBlank() }
            .map { "@param ${it.name} ${it.description}" }
    }

    private fun getTypeName(modelProperty: Map.Entry<String, Property>): TypeName {
        val property = modelProperty.value
        return when {
            property.type == REF_SWAGGER_TYPE ->
                TypeVariableName.invoke((property as RefProperty).simpleRef).requiredOrNullable(property.required)

            property.type == ARRAY_SWAGGER_TYPE -> {
                val arrayProperty = property as ArrayProperty
                getTypedArray(arrayProperty.items).requiredOrNullable(arrayProperty.required)
            }
            property is MapProperty -> {
                getMapTypeName(modelProperty)
            }
            else -> getKotlinClassTypeName(property.type, property.format).requiredOrNullable(property.required)
        }
    }

    private fun getMapTypeName(mapProperty: Map.Entry<String, Property>): TypeName {
        val property = (mapProperty.value as MapProperty).additionalProperties

        property?.let {
            //https://swagger.io/docs/specification/data-models/dictionaries/
            return Map::class.asClassName().parameterizedBy(
                TypeVariableName.invoke(String::class.simpleName!!),
                getTypeNameByProperty(property)
            )
                .requiredOrNullable(property.required)
        }

        return getKotlinClassTypeName(mapProperty.value.type, mapProperty.value.format)
            .requiredOrNullable(mapProperty.value.required)
    }

    private fun getMethodParameters(
        operation: MutableMap.MutableEntry<HttpMethod, Operation>,
        classesWithSubPackages: List<TypeSpecWrapper>
    ): Iterable<ParameterSpec> {
        return operation.value.parameters.mapNotNull { parameter: io.swagger.models.parameters.Parameter ->
            // Transform parameters in the format foo.bar to fooBar
            val name = parameter.name.split('.').mapIndexed { index, s -> if (index > 0) s.capitalize() else s }
                .joinToString("")
            when (parameter.`in`) {
                "body" -> {
                    ParameterSpec.builder(name, getBodyParameterSpec(parameter, classesWithSubPackages))
                        .addAnnotation(AnnotationSpec.builder(Body::class).build()).build()
                }
                "path" -> {
                    val type =
                        getKotlinClassTypeName((parameter as PathParameter).type, parameter.format).requiredOrNullable(
                            parameter.required
                        )
                    ParameterSpec.builder(name, type)
                        .addAnnotation(AnnotationSpec.builder(Path::class).addMember("\"${parameter.name}\"").build())
                        .build()
                }
                "query" -> {
                    if ((parameter as QueryParameter).type == ARRAY_SWAGGER_TYPE) {
                        val type =
                            List::class.asClassName().parameterizedBy(getKotlinClassTypeName(parameter.items.type))
                                .requiredOrNullable(parameter.required)
                        ParameterSpec.builder(name, type)
                    } else {
                        val type = getKotlinClassTypeName(
                            parameter.type,
                            parameter.format
                        ).requiredOrNullable(parameter.required)
                        ParameterSpec.builder(name, type)
                    }.addAnnotation(AnnotationSpec.builder(Query::class).addMember("\"${parameter.name}\"").build())
                        .build()
                }
                "formData" -> {
                    ParameterSpec.builder(name, MultipartBody.Part::class.java)
                        .addAnnotation(AnnotationSpec.builder(Part::class).build()).build()
                }
                "url" -> {
                    ParameterSpec.builder(name, String::class.java)
                        .addAnnotation(AnnotationSpec.builder(Url::class).build()).build()
                }
                else -> null
            }
        }
    }

    private fun getBodyParameterSpec(parameter: io.swagger.models.parameters.Parameter, classesWithSubPackages: List<TypeSpecWrapper>): TypeName {
        val bodyParameter = parameter as BodyParameter
        val schema = bodyParameter.schema

        return when (schema) {
            is RefModel -> {
                classesWithSubPackages.firstOrNull { it.typeSpec.name == schema.simpleRef }?.let {
                    ClassName.bestGuess("${apiGenerationConfiguration.packageName}.${it.subPackage}.$PREFIX_MODELS.${it.typeSpec.name}")
                        .requiredOrNullable(parameter.required)
                } ?: ClassName.bestGuess(schema.simpleRef.capitalize()).requiredOrNullable(parameter.required)
            }

            is ArrayModel -> getTypedArray(schema.items).requiredOrNullable(parameter.required)

            else -> {
                val bodyParameter1 = parameter.schema as? ModelImpl ?: ModelImpl()

                if (STRING_SWAGGER_TYPE == bodyParameter1.type) {
                    String::class.asClassName().requiredOrNullable(parameter.required)
                } else {
                    ClassName.bestGuess(parameter.name.capitalize()).requiredOrNullable(parameter.required)
                }
            }
        }
    }

    private fun getTypedArray(items: Property): TypeName {
        val typeProperty = getTypeNameByProperty(items)
        return List::class.asClassName().parameterizedBy(typeProperty)
    }

    private fun getTypeNameByProperty(items: Property): TypeName {
        val typeProperty = when (items) {
            is LongProperty -> TypeVariableName.invoke(Long::class.simpleName!!)
            is IntegerProperty -> TypeVariableName.invoke(Int::class.simpleName!!)
            is FloatProperty -> TypeVariableName.invoke(Float::class.simpleName!!)
            is DoubleProperty -> TypeVariableName.invoke(Double::class.simpleName!!)
            is RefProperty -> TypeVariableName.invoke(items.simpleRef)
            is ArrayProperty -> {
                if (items.format == null && items.items != null) {
                    getTypedArray(items.items);
                } else {
                    getKotlinClassTypeName(items.type, items.format)
                }
            }
            else ->
                getKotlinClassTypeName(items.type, items.format)
        }
        return typeProperty
    }

    private fun TypeName.requiredOrNullable(required: Boolean) = if (required) this else copy(nullable = true)

    private fun getReturnedClass(
        operation: MutableMap.MutableEntry<HttpMethod, Operation>,
        classNameList: List<String>
    ): TypeName {
        val useCoroutines = apiGenerationConfiguration.useCoroutines
        try {
            if (operation.value.responses[OK_RESPONSE]?.schema != null &&
                operation.value.responses[OK_RESPONSE]?.schema is RefProperty
            ) {
                val refProperty = (operation.value.responses[OK_RESPONSE]?.schema as RefProperty)
                var responseClassName = refProperty.simpleRef
                responseClassName = getValidClassName(responseClassName, refProperty)

                if (classNameList.contains(responseClassName)) {
                    return if (useCoroutines) {
                        TypeVariableName.invoke(responseClassName)
                    } else {
                        Single::class.asClassName().parameterizedBy(TypeVariableName.invoke(responseClassName))
                    }
                }
            } else if (operation.value.responses[OK_RESPONSE]?.schema != null &&
                operation.value.responses[OK_RESPONSE]?.schema is ArrayProperty
            ) {
                val refProperty = (operation.value.responses[OK_RESPONSE]?.schema as ArrayProperty)
                var responseClassName = (refProperty.items as RefProperty).simpleRef
                responseClassName = getValidClassName(responseClassName, (refProperty.items as RefProperty))

                if (classNameList.contains(responseClassName)) {
                    return if (useCoroutines) {
                        List::class.asClassName().parameterizedBy(TypeVariableName.invoke(responseClassName))
                    } else {
                        Single::class.asClassName().parameterizedBy(
                            List::class.asClassName().parameterizedBy(TypeVariableName.invoke(responseClassName))
                        )
                    }
                }
            }
        } catch (error: ClassCastException) {
           error.printStackTrace()
        }

        return if (useCoroutines) {
            Unit::class.asClassName()
        } else {
            Completable::class.asClassName()
        }
    }

    private fun getValidClassName(responseClassName: String, refProperty: RefProperty): String {
        var className = responseClassName
        try {
            TypeSpec.classBuilder(className)
        } catch (error: IllegalArgumentException) {
            if (refProperty.simpleRef != null) {
                className = "Model" + refProperty.simpleRef.capitalize()
            }
        }
        return className
    }

    private fun getKotlinClassTypeName(type: String, format: String? = null): TypeName {
        return when (type) {
            ARRAY_SWAGGER_TYPE -> TypeVariableName.invoke(List::class.simpleName!!)
            STRING_SWAGGER_TYPE -> TypeVariableName.invoke(String::class.simpleName!!)
            NUMBER_SWAGGER_TYPE -> TypeVariableName.invoke(Double::class.simpleName!!)
            INTEGER_SWAGGER_TYPE -> {
                when (format) {
                    "int64" -> TypeVariableName.invoke(Long::class.simpleName!!)
                    else -> TypeVariableName.invoke(Int::class.simpleName!!)
                }
            }
            else -> TypeVariableName.invoke(type.capitalize())
        }
    }

/*private fun getPropertyInitializer(type: String): String {
    return when (type) {
        ARRAY_SWAGGER_TYPE -> "ArrayList()"
        INTEGER_SWAGGER_TYPE -> "0"
        STRING_SWAGGER_TYPE -> "\"\""
        BOOLEAN_SWAGGER_TYPE -> "false"
        else -> "null"
    }
}*/


//  fun getGeneratedModelsString(): String {
//    var generated = ""
//    for (typeSpec in responseBodyModelListTypeSpec) {
//      generated += StorageUtils.generateString(proteinApiConfiguration.packageName, typeSpec)
//    }
//    return generated
//  }
//
//  fun getGeneratedEnums(): String {
//    var generated = ""
//    for (typeSpec in enumListTypeSpec) {
//      generated += StorageUtils.generateString(proteinApiConfiguration.packageName, typeSpec)
//    }
//    return generated
//  }
}

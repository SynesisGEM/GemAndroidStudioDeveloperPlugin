package com.gemtechnologies.modules.api.common

import com.gemtechnologies.modules.api.common.StorageUtils.toFirstLowerCase
import java.io.Serializable

data class Settings(
    val moduleName: String = "data",
    val componentName: String? = "net",
    val domainName: String? = "com.synesis.gem",
    val swaggerUrl: String = "https://gem-stage-2.appspot.com/swagger.json"
) {
    val packageName: String
        get() = domainName + "." + toFirstLowerCase(componentName!!)
}
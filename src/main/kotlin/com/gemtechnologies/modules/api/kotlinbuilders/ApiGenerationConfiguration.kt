package com.gemtechnologies.modules.api.kotlinbuilders

data class ApiGenerationConfiguration(
  override val swaggerUrl: String,
  override val packageName: String,
  override val componentName: String,
  override val moduleName: String,
  override val useCoroutines: Boolean
) : ApiConfiguration

interface ApiConfiguration {
  val swaggerUrl: String
  val packageName: String
  val componentName: String
  val moduleName: String
  val useCoroutines: Boolean
}

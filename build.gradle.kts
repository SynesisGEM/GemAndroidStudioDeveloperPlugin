plugins {
    id("org.jetbrains.kotlin.jvm").version("1.5.10")
    id("org.jetbrains.intellij").version("1.0")
}

group = "com.gemtechnologies"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    implementation("com.google.code.gson:gson:2.8.7")
    implementation("com.squareup:kotlinpoet:1.9.0")
    implementation("io.reactivex.rxjava2:rxjava:2.1.16")
    implementation("com.squareup.retrofit2:retrofit:2.4.0")
    val swaggerVersion = "1.5.16"
    implementation("io.swagger:swagger-annotations:$swaggerVersion")
    implementation("io.swagger:swagger-core:$swaggerVersion") {
        exclude("org.slf4j")
    }
    implementation("io.swagger:swagger-models:$swaggerVersion") {
        exclude("org.slf4j")
    }
    implementation("io.swagger:swagger-parser:1.0.33") {
        exclude("org.slf4j")
    }
}

intellij {
    version.set("2020.3")
}

tasks {
    patchPluginXml {
        changeNotes.set(
            """
            Велкоме
        """.trimIndent()
        )
    }
}
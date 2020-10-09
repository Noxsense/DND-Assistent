/*
 * This file was generated by the Gradle 'init' task.
 *
 * This generated file contains a sample Kotlin library project to get you started.
 */

plugins {
  id("java-library")

  // Apply the Kotlin JVM plugin to add support for Kotlin.
  id("org.jetbrains.kotlin.jvm")

  application
}

dependencies {
  // libary module.
  implementation(project(":core"))

  // -- Tests ---

  // Use the Kotlin test library.
  testImplementation("org.jetbrains.kotlin:kotlin-test")

  // Use the Kotlin JUnit integration.
  testImplementation("org.jetbrains.kotlin:kotlin-test-junit")
}

java {
    sourceCompatibility = JavaVersion.VERSION_1_8
    targetCompatibility = JavaVersion.VERSION_1_8
}

application {
    // Define the main class for the application.
    mainClassName = "de.nox.dndassistant.cui.TermGuiKt"
}

// https://discuss.gradle.org/t/kotlin-jvm-app-missing-main-manifest-attribute/31413/2
tasks.withType<Jar>() {
  manifest {
    attributes["Main-Class"] = "de.nox.dndassistant.cui.TermGuiKt"
  }
  // configurations["compileClasspath"].forEach { file: File ->
  //   from(zipTree(file.absoluteFile))
  // }
  from(configurations.runtimeClasspath.get().map { if (it.isDirectory) it else zipTree(it) }){
      exclude("META-INF/*.RSA", "META-INF/*.SF", "META-INF/*.DSA")
  }
}
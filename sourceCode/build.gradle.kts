
// github.com/Kotlin/kmm-sample

buildscript {
  // ext.kotlin_version = "1.4.0"
  // ext.android_version = "4.0.1"
  repositories {
    gradlePluginPortal()
    jcenter()
    google()
    mavenCentral()
  }
  dependencies {
    classpath("org.jetbrains.kotlin:kotlin-gradle-plugin:1.4.0")
    classpath("org.jetbrains.kotlin:kotlin-serialization:1.4.0")
    classpath("com.android.tools.build:gradle:4.0.1")

    // classpath("org.jlleitschuh.gradle.ktlint:ktlint:9.2.1") // TODO
  }
}

repositories {
  mavenCentral()
}

allprojects {
  group = "de.noxsense"
  version = "0.2.0-SNAPSHOT"

  apply {
    // plugin("org.jlleitschuh.gradle.ktlint") // TODO
  }

  repositories {
    google()
    mavenCentral()
    jcenter()
  }
}

tasks.create<Delete>("clean") {
  delete(rootProject.buildDir)
}

open class GreetingTask: DefaultTask() {
  @TaskAction
  fun greet() {
    println("Hello greettings, https://docs.gradle.org/current/userguide/custom_tasks.html")
  }
}

tasks.register("runDebug", Exec::class) {
  dependsOn(":android-app:installDebug")
  description = "Install the app"
  group = "build" // to display
  commandLine = "adb shell am start -n de.nox.dndassistant.app.debug/de.nox.dndassistant.app.MainActivity -a android.intent.action.MAIN -c android.intent.category.LAUNCHER".split(" ")
}

import org.gradle.internal.os.OperatingSystem
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.mapOf

plugins {
    id("com.github.ben-manes.versions") version "0.36.0"
    java
    application
    id("com.github.johnrengelman.shadow") version "6.1.0"
} 

repositories {
    jcenter()
    mavenCentral()
}

application {
    mainClassName = "com.cburch.logisim.Main"
}

dependencies {
    implementation(fileTree("lib") {
        include("**/*.jar")
    })
    implementation("org.hamcrest:hamcrest:2.2")
    implementation("javax.help:javahelp:2.0.05")
    implementation("com.fifesoft:rsyntaxtextarea:3.1.0")
    implementation("net.sf.nimrod:nimrod-laf:1.2")
    implementation("org.drjekyll:colorpicker:1.3")
    implementation("org.drjekyll:fontchooser:2.4")
    implementation("at.swimmesberger:swingx-core:1.6.8")
    implementation("org.slf4j:slf4j-api:1.7.30")
    implementation("org.slf4j:slf4j-simple:1.7.30")
    implementation("com.github.weisj:darklaf-core:2.5.5")

    testImplementation("ch.qos.logback:logback-classic:1.2.3")
    testImplementation("ch.qos.logback:logback-core:1.2.3")
    testImplementation("junit:junit:4.13")
}

java {
    sourceCompatibility = JavaVersion.VERSION_11
    targetCompatibility = JavaVersion.VERSION_11
}

task<Jar>("sourcesJar") {
    group = "build"
    description = "Creates a source jar archive."
    dependsOn.add("classes")
    classifier = "src"

    from(sourceSets.main.get().allSource)
}

tasks.register("jpackage") {
    group = "build"
    description = "Makes the platform specific packages"
    dependsOn("shadowJar")
    doFirst {
      val folder = File("$buildDir/dist")
        if (!folder.exists()) {
        if (!folder.mkdirs()) throw GradleException("Unable to create directory \"$buildDir/dist\"")
      }
    }
    doLast {
      val df = SimpleDateFormat("yyyy")
        val year = df.format(Date())
        val parameters = ArrayList<String>()
        val javaHome = System.getProperty("java.home") ?: throw GradleException("java.home is not set")
      val cmd = javaHome + File.separator + "bin" + File.separator + "jpackage"
      parameters.add(if (cmd.contains(" ")) "\"" + cmd + "\"" else cmd)
      parameters.add("--input")
      parameters.add("build/libs")
      parameters.add("--main-class")
      parameters.add("com.cburch.logisim.Main")
      parameters.add("--main-jar")
      parameters.add(project.name + '-' + project.version + "-all.jar")
      parameters.add("--name")
      parameters.add(project.name)
      parameters.add("--app-version")
      parameters.add(project.version as String)
      parameters.add("--copyright")
      parameters.add("Copyright © 2001–" + year + " Carl Burch, BFH, HEIG-VD, HEPIA, Holy Cross, et al.")
      parameters.add("--dest")
      parameters.add("build/dist")
      if (OperatingSystem.current().isLinux) {
         parameters.add("--file-associations")
         parameters.add("support/jpackage/linux/file.jpackage")
         parameters.add("--icon")
         parameters.add("support/jpackage/linux/logisim-icon-128.png")
         parameters.add("--install-dir")
         parameters.add("/opt")
         parameters.add("--linux-shortcut")
         val processBuilder1 = ProcessBuilder()
         processBuilder1.command(parameters)
         val process1 = processBuilder1.start()
         if (process1.waitFor() != 0) {
            throw GradleException("Error while executing jpackage")
         }
         parameters.add("--type")
         parameters.add("rpm")
         val processBuilder2 = ProcessBuilder()
         processBuilder2.command(parameters)
         val process2 = processBuilder2.start()
         if (process2.waitFor() != 0) {
            throw GradleException("Error while executing jpackage")
         }
      } else if (OperatingSystem.current().isWindows) {
         parameters.add("--file-associations")
         parameters.add("support/jpackage/windows/file.jpackage")
         parameters.add("--icon")
         parameters.add("support/jpackage/windows/Logisim-evolution.ico")
         parameters.add("--type")
         parameters.add("msi")
         parameters.add("--win-menu-group")
         parameters.add("logisim")
         parameters.add("--win-shortcut")
         parameters.add("--win-dir-chooser")
         parameters.add("--win-menu")
         val processBuilder1 = ProcessBuilder()
         processBuilder1.command(parameters)
         val process1 = processBuilder1.start()
         if (process1.waitFor() != 0) {
            throw GradleException("Error while executing jpackage")
         }
      } else if (OperatingSystem.current().isMacOsX) {
         parameters.add("--resource-dir")
         parameters.add("support/jpackage/macos")
         parameters.add("--file-associations")
         parameters.add("support/jpackage/macos/file.jpackage")
         parameters.add("--icon")
         parameters.add("support/jpackage/macos/Logisim-evolution.icns")
         parameters.add("--type")
         parameters.add("dmg")
         val processBuilder1 = ProcessBuilder()
         processBuilder1.command(parameters)
         val process1 = processBuilder1.start()
         if (process1.waitFor() != 0) {
            throw GradleException("Error while executing jpackage")
         }
      }
    }
}

tasks {
    jar {
        manifest {
            attributes.putAll(mapOf(
                    "Implementation-Title" to name,
                    "Implementation-Version" to archiveVersion
            ))
        }

        from(".") {
            include("LICENSE")
            include("README.md")
        }
    }
    shadowJar {
        from(".") {
            include("LICENSE")
            include("README.md")
        }
    }
}


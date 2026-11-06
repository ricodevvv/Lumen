import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar

plugins {
    java
    id("com.gradleup.shadow") version "8.3.6"
}

allprojects {
    group = "com.ricodev"
    version = "1.0.0"

    repositories {
        mavenCentral()
        maven("https://hub.spigotmc.org/nexus/content/repositories/snapshots/")
        maven("https://oss.sonatype.org/content/repositories/snapshots")
        maven("https://repo.codemc.io/repository/maven-releases/")
        maven("https://repo.extendedclip.com/content/repositories/placeholderapi/")
        maven("https://repo.essentialsx.net/releases/")
        maven("https://repo.rosewooddev.io/repository/public/")
        maven("https://jitpack.io")
    }
}

subprojects {
    apply(plugin = "java")

    java {
        toolchain {
            languageVersion.set(JavaLanguageVersion.of(21))
        }
    }

    tasks.withType<JavaCompile>().configureEach {
        options.encoding = "UTF-8"
        options.release.set(8)
        options.compilerArgs.addAll(listOf("-Xlint:-options", "-Xmaxerrs", "10000"))
    }
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(21))
    }
}

dependencies {
    implementation(project(":api"))
    implementation(project(":core"))
    implementation(project(":modern"))
    implementation(project(":display"))
}

tasks.named<ShadowJar>("shadowJar") {
    archiveClassifier.set("")
    archiveVersion.set("")

    val libs = "com.ricodev.lumen.libs"
    mapOf(
        "dev.dejvokep.boostedyaml" to "boostedyaml",
        "org.snakeyaml" to "snakeyaml",
        "com.zaxxer.hikari" to "hikari",
        "com.cryptomorin.xseries" to "xseries",
        "net.kyori" to "kyori",
        "com.google.gson" to "gson"
    ).forEach { (from, to) -> relocate(from, "$libs.$to") }

    exclude("META-INF/maven/**", "META-INF/versions/**", "module-info.class", "org/jetbrains/annotations/**", "org/intellij/lang/annotations/**", "com/google/errorprone/**")
}

tasks.named("build") {
    dependsOn(tasks.named("shadowJar"))
}

tasks.named<Jar>("jar") {
    enabled = false
}

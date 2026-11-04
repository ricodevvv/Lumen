import org.apache.tools.ant.filters.ReplaceTokens

dependencies {
    compileOnly("org.spigotmc:spigot-api:1.8.8-R0.1-SNAPSHOT") {
        isTransitive = false
    }
    compileOnly("com.google.guava:guava:17.0")
    compileOnly("commons-lang:commons-lang:2.6")
    compileOnly("org.yaml:snakeyaml:1.15")
    compileOnly("org.avaje:ebean:2.8.1")
    compileOnly("net.md-5:bungeecord-chat:1.16-R0.4")
    compileOnly("org.jetbrains:annotations:26.0.2")
    compileOnly("com.github.retrooper:packetevents-spigot:2.14.0")

    implementation(project(":api"))
    implementation("com.github.cryptomorin:XSeries:14.0.0")
    implementation("dev.dejvokep:boosted-yaml:1.3.7")
    implementation("com.google.code.gson:gson:2.11.0")
    implementation("com.zaxxer:HikariCP:4.0.3") {
        exclude(group = "org.slf4j")
    }

    compileOnly("me.clip:placeholderapi:2.11.6") {
        isTransitive = false
    }
    compileOnly("com.github.MilkBowl:VaultAPI:1.7.1") {
        isTransitive = false
    }
    compileOnly("net.essentialsx:EssentialsX:2.21.2") {
        isTransitive = false
    }
    compileOnly("org.black_ixx:playerpoints:3.3.3") {
        isTransitive = false
    }
    compileOnly("com.github.LeonMangler:SuperVanish:6.2.19") {
        isTransitive = false
    }
}

tasks.processResources {
    val projectVersion = project.version.toString()
    inputs.property("version", projectVersion)
    filesMatching("plugin.yml") {
        filter<ReplaceTokens>("tokens" to mapOf("VERSION" to projectVersion))
    }
}

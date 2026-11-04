plugins {
    `java-library`
}

dependencies {
    compileOnly("org.spigotmc:spigot-api:1.8.8-R0.1-SNAPSHOT") {
        isTransitive = false
    }
    compileOnly("com.google.guava:guava:17.0")
    compileOnly("com.google.code.gson:gson:2.2.4")
    compileOnly("commons-lang:commons-lang:2.6")
    compileOnly("org.yaml:snakeyaml:1.15")
    compileOnly("org.avaje:ebean:2.8.1")
    compileOnly("net.md-5:bungeecord-chat:1.16-R0.4")
    compileOnly("org.jetbrains:annotations:26.0.2")

    api("net.kyori:adventure-api:4.26.1")
    api("net.kyori:adventure-text-minimessage:4.26.1")
    api("net.kyori:adventure-text-serializer-legacy:4.26.1")
    api("net.kyori:adventure-text-serializer-plain:4.26.1")
}

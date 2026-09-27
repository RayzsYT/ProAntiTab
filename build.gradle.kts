plugins {
    id("java")
    alias(libs.plugins.shadow) // ShadowJar
    alias(libs.plugins.run.paper) // Run Paper
}

group = providers.gradleProperty("group").get()
version = providers.gradleProperty("version").get()

repositories {
    mavenCentral()
    maven("https://repo.extendedclip.com/releases/") {
        name = "placeholder-api"
    }
    maven("https://oss.sonatype.org/content/repositories/snapshots") {
        name = "bungeecord-repo"
    }
    maven("https://repo.papermc.io/repository/maven-public/") {
        name = "papermc"
    }
    maven("https://jitpack.io") {
        name = "jitpack"
    }
    maven("https://repo.helpch.at/releases/") {
        name = "placeholderapi"
    }
    maven("https://repo.velocitypowered.com/snapshots/") {
        name = "velocity"
    }
    maven("https://libraries.minecraft.net/") {
        name = "minecraft-libraries"
    }
    maven("https://repo.viaversion.com") {
        name = "viaversion-repo"
    }
    maven("https://repo.william278.net/velocity") {
        name = "william278-velocity"
    }
    maven("https://repo.william278.net/papiproxybridge") {
        name = "william278-papiproxybridge"
    }
    maven("https://central.sonatype.com/repository/maven-snapshots/") {
        name = "central-snapshots"
    }
}

dependencies {
    annotationProcessor(libs.velocity.api) // Velocity API Annotation Processor

    compileOnly(libs.placeholder.api) // Placeholder API

    compileOnly(libs.paper.api) // Paper API
    compileOnly(libs.folia.api) // Folia API
    compileOnly(libs.netty) // Netty
    compileOnly(libs.bungee.cord) // BungeeCord API
    compileOnly(libs.bungee.cord.protocol) // BungeeCord Protocol
    compileOnly(libs.auth.lib) // Mojang Authlib
    compileOnly(libs.group.manager) // GroupManager
    compileOnly(libs.brigadier) // Brigadier
    compileOnly(libs.data.fixer.upper) // DataFixerUpper
    compileOnly(libs.mojang.logging) // Mojang Logging
    compileOnly(libs.maven.resolver.provider) // Maven Resolver Provider
    compileOnly(libs.maven.resolver.connector.basic) // Maven Resolver Connector Basic
    compileOnly(libs.maven.resolver.transport.http) // Maven Resolver Transport HTTP
    compileOnly(libs.log4j.core) // Log4j Core
    compileOnly(libs.waterfall.api) // Waterfall API
    compileOnly(libs.papi.proxy.bridge) // PAPIProxyBridge
    compileOnly(libs.velocity.api) // Velocity API
    compileOnly(libs.velocity.proxy) { // Velocity Proxy
        exclude(group = "com.velocitypowered", module = "velocity-proxy-log4j2-plugin")
    }
    compileOnly(libs.sisu.guice) // Sisu Guice
    compileOnly(libs.configurate.hocon) // Configurate HOCON
    compileOnly(libs.luck.perms) // LuckPerms API
    compileOnly(libs.via.version.api) // ViaVersion API

    implementation(libs.adventure.api) // Adventure API
    implementation(libs.mini.message) // MiniMessage API
    implementation(libs.adventure.platform.api) // Adventure Platform API
    implementation(libs.adventure.platform.bungeecord) // Adventure Platform BungeeCord
    implementation(libs.adventure.platform.bukkit) // Adventure Platform Bukkit
    implementation(libs.examination.api) // Examination API
    implementation(libs.jetbrains.annotations) // JetBrains Annotations
}

tasks {
  runServer {
    // Configure the Minecraft version for our task.
    // This is the only required configuration besides applying the plugin.
    // Your plugin's jar (or shadowJar if present) will be used automatically.
    minecraftVersion("1.21.11")
  }
}

java {
  toolchain {
    languageVersion = JavaLanguageVersion.of(21)
  }
}

tasks.jar {
  enabled = false
}

tasks.shadowJar {
  // Bundle everything on the runtime classpath (the `implementation`
  // dependencies and their transitive graph). `compileOnly` server APIs are
  // never part of `runtimeClasspath`, so they stay out of the final JAR.
  configurations = listOf(project.configurations.runtimeClasspath.get())

  // Removes the default "-all" suffix from the generated JAR file name,
  // keeping the output name clean (e.g., ProAntiTab-2.5.1.jar).
  archiveClassifier.set("")

  // Gson is provided by every supported server platform; bundling it would
  // shadow the server's copy.
  exclude("com/google/gson/**")

  // Examination/option classes are supplied by the platform's Adventure stack.
  exclude("net/kyori/option/**")

  // Avoid clashing with the platform's own Adventure service registration.
  exclude("META-INF/services/net.kyori.adventure.text.event.DataComponentValueConverterRegistry\$Provider")

  // NOTE: Adventure is intentionally NOT relocated. The plugin calls native
  // platform APIs (Paper's Player#sendMessage(Component), Velocity's
  // CommandSource#sendMessage(Component), ...) which require the original
  // `net.kyori.adventure` types; relocating them would break those calls.
  // If you ever bundle a library that is *not* referenced by platform APIs,
  // relocate it here, e.g.:
  // relocate("com.example.lib", "${project.group}.libs.example")
}

tasks.build {
  dependsOn("shadowJar")
}

tasks.withType<JavaCompile>().configureEach {
  options.encoding = "UTF-8"
}

tasks.withType<ProcessResources>().configureEach {
    val props = mapOf(
      "name" to providers.gradleProperty("name").get(),
      "main" to providers.gradleProperty("main").get(),
      "version" to providers.gradleProperty("version").get(),
      "description" to providers.gradleProperty("description").get(),
      "website" to providers.gradleProperty("website").get(),
      "author" to providers.gradleProperty("author").get(),
      "apiVersion" to providers.gradleProperty("apiVersion").get(),
      "foliaSupported" to providers.gradleProperty("foliaSupported").get(),
    )

    inputs.properties(props)
    filteringCharset = "UTF-8"

    filesMatching("plugin.yml") {
        expand(props)
    }

    val proxy = mapOf(
      "name" to providers.gradleProperty("name").get(),
      "bungeeMain" to providers.gradleProperty("bungeeMain").get(),
      "version" to providers.gradleProperty("version").get(),
      "description" to providers.gradleProperty("description").get(),
      "website" to providers.gradleProperty("website").get(),
      "author" to providers.gradleProperty("author").get(),
    )

    inputs.properties(proxy)
    filteringCharset = "UTF-8"

    filesMatching("bungee.yml") {
        expand(proxy)
    }
}

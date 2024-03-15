plugins {
    id("java")
    id("application")
    id("com.github.johnrengelman.shadow") version "8.1.1"
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(17))
    }
}

repositories {
    mavenCentral()
    maven("https://repo.opencollab.dev/main")
}

dependencies {
    implementation("com.nukkitx.digraph:digraph-parser:1.1.0-SNAPSHOT")
    implementation("org.jsoup:jsoup:1.17.2")
    implementation("net.sf.jopt-simple:jopt-simple:5.0.4")
    implementation("org.apache.commons:commons-text:1.11.0")

    compileOnly("org.projectlombok:lombok:1.18.30")
    annotationProcessor("org.projectlombok:lombok:1.18.30")
}

application {
    mainClass.set("org.cloudburstmc.protocolparser.Main")
}

tasks.shadowJar {
    archiveVersion.set("")
    archiveClassifier.set("")
    archiveBaseName.set("ProtocolParser")
}
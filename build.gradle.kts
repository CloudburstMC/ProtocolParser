plugins {
    id("java")
    id("application")
    id("com.github.johnrengelman.shadow") version "8.1.1"
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(8))
    }
}

repositories {
    mavenCentral()
    maven("https://repo.opencollab.dev/maven-snapshots")
}

dependencies {
    implementation("com.nukkitx.digraph:digraph-parser:1.1.0-SNAPSHOT")
    implementation("org.jsoup:jsoup:1.13.1")
    implementation("net.sf.jopt-simple:jopt-simple:5.0.4")

    compileOnly("org.projectlombok:lombok:1.18.24")
    annotationProcessor("org.projectlombok:lombok:1.18.24")
}

application {
    mainClass.set("org.cloudburstmc.protocolparser.Main")
}

tasks.shadowJar {
    archiveVersion.set("")
    archiveClassifier.set("")
    archiveBaseName.set("ProtocolParser")
}
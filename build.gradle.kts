plugins {
    kotlin("jvm") version "1.3.72"
    application
}

group = "us.dragonma"
version = "1.0.4"

repositories {
    mavenLocal()
    jcenter()
}

dependencies {
    implementation(platform(kotlin("bom")))
    implementation(kotlin("stdlib-jdk8"))
    implementation("us.dragonma", "getopt", "1.0.1")
}

application {
    mainClassName = "us.dragonma.backgrounds.update.AppKt"
}

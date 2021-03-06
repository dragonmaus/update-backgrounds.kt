plugins {
    kotlin("jvm") version "1.4.30"
    application
}

group = "us.dragonma"
version = "1.1.1"

repositories {
    mavenLocal()
    jcenter()
}

dependencies {
    implementation(platform(kotlin("bom")))
    implementation(kotlin("stdlib-jdk8"))
    implementation("us.dragonma", "getopt", "1.0.2")
}

application {
    mainClass.set("us.dragonma.backgrounds.update.AppKt")
}

plugins {
	kotlin("jvm") version "2.0.0"
	application
}

group = "us.dragonma"
version = "1.1.2"

repositories {
	mavenLocal()
	mavenCentral()
}

dependencies {
	implementation(platform(kotlin("bom")))
	implementation(kotlin("stdlib"))
	implementation("us.dragonma", "getopt", "1.0.3")
}

tasks.jar {
	manifest {
		attributes["Main-Class"] = "us.dragonma.backgrounds.update.AppKt"
	}
	configurations["compileClasspath"].forEach { file: File ->
		from(zipTree(file.absoluteFile))
	}
	duplicatesStrategy = DuplicatesStrategy.INCLUDE
}

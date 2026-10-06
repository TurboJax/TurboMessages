plugins {
  `java-library`
  `maven-publish`
}

group = "org.turbojax"
version = "1.2.1"

repositories {
  mavenCentral()
}

dependencies {
  compileOnly(libs.adventure.api)
  compileOnly(libs.adventure.minimessage)
  compileOnly(libs.jspecify)
  compileOnly(libs.snakeyaml)
  compileOnly(libs.slf4j)
}

java {
  toolchain {
    languageVersion = JavaLanguageVersion.of(21)
  }

  withJavadocJar()
}

tasks.register("go") {
  dependsOn("clean", "build", "publish")
}

publishing {
  publications {
    create<MavenPublication>("maven") {
      from(components["java"])

      artifactId = "messages"
    }
  }

  repositories {
    maven {
      name = "turbo-maven"
      url = uri("https://maven.turbojax.org/releases/")
      credentials {
        username = project.findProperty("mavenUser") as String? ?: System.getenv("MAVEN_USERNAME")
        password = project.findProperty("mavenPass") as String? ?: System.getenv("MAVEN_PASSWORD")
      }
    }
  }
}
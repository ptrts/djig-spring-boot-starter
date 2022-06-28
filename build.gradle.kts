plugins {
    id("io.spring.dependency-management") version "1.0.11.RELEASE"
    id("java-library")
}

group = "org.taruts.dynamic-java-code-stored-in-git"
version = "001"

java.sourceCompatibility = JavaVersion.VERSION_17

configurations {
    compileOnly {
        extendsFrom(configurations["annotationProcessor"])
    }
}

repositories {
    mavenCentral()
    maven {
        name = "s3MavenRepo"
        url = uri("s3://maven.taruts.net")
        // AwsCredentials without a configuration closure means that there are credentials in a gradle.properties file.
        // Our credentials are in the gradle.properties in the project itself.
        credentials(AwsCredentials::class)
    }
}

dependencies {

    // Lombok
    compileOnly("org.projectlombok:lombok")
    annotationProcessor("org.projectlombok:lombok")

    implementation("we.git-implementations:dynamic-api:001")
}

tasks.named<Test>("test") {
    useJUnitPlatform()
}

dependencyManagement {
    imports {
        mavenBom("org.springframework.boot:spring-boot-dependencies:2.7.0")
    }
}

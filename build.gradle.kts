plugins {
    id("io.spring.dependency-management") version "1.0.11.RELEASE"
    id("java-library")
    id("maven-publish")
}

group = "org.taruts.djig"
version = "001"

java.sourceCompatibility = JavaVersion.VERSION_17

configurations {
    compileOnly {
        extendsFrom(configurations["annotationProcessor"])
    }
}

repositories {
    mavenLocal()
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

    // Generation of META-INF/spring-configuration-metadata.json
    annotationProcessor("org.springframework.boot:spring-boot-configuration-processor")

    // Lombok
    compileOnly("org.projectlombok:lombok")
    annotationProcessor("org.projectlombok:lombok")

    // Spring Boot + WebFlux
    api("org.springframework.boot:spring-boot-starter-webflux")
    annotationProcessor("org.springframework.boot:spring-boot-configuration-processor")

    // Spring Retry
    implementation("org.springframework:spring-aspects")
    implementation("org.springframework.retry:spring-retry:1.3.3")

    // GitLab API
    implementation("org.gitlab4j:gitlab4j-api:5.0.1")

    // Our util libraries
    implementation("org.taruts:taruts-git-utils:1.0.0")
    implementation("org.taruts:taruts-process-utils:1.0.2")

    // Utils
    implementation("org.apache.commons:commons-lang3")
    implementation("commons-io:commons-io:2.11.0")
    implementation("com.google.guava:guava:31.1-jre")

    // Reflections
    implementation("org.reflections:reflections:0.10.2")

    implementation("org.taruts.djig:djig-dynamic-api:1.0.0")
}

tasks.named<Test>("test") {
    useJUnitPlatform()
}

dependencyManagement {
    imports {
        mavenBom("org.springframework.boot:spring-boot-dependencies:2.7.0")
    }
}

configure<JavaPluginExtension> {
    withSourcesJar()
}

publishing {
    publications {
        create<MavenPublication>("java") {
            // We use the java-library plugin in this project. The java-library is based upon the java plugin.
            // During the build process, the java plugin creates a so-called component which is a collection of things to publish.
            // The maven-publish plugin can create publications from components.
            // that the maven-publish can use. The component is named "java" after the java plugin.
            from(components["java"])

            // Also we use the plugin io.spring.dependency-management.
            // This plugin enables us not to specify versions manually for those dependencies of the project
            // that Spring libraries work with.
            // But by default the dependency versions in the java component are those specified manually.
            // This configuration is needed to change this default.
            versionMapping {
                usage("java-api") {
                    fromResolutionResult()
                }
                usage("java-runtime") {
                    fromResolutionResult()
                }
            }
        }
    }
    repositories {
        maven {
            name = "s3MavenRepo"
            url = uri("s3://maven.taruts.net")
            authentication {
                // AwsImAuthentication means that the credentials are in an AWS profile on the computer
                // Only the author of this project has those credentials and can upload dynamic-api artifacts
                register("aws", AwsImAuthentication::class)
            }
        }
    }
}

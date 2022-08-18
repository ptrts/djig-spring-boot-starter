package org.taruts.djig.core.childContext.builds;

import lombok.Getter;

import java.util.List;

@Getter
public enum BuildSystem {
    MAVEN(
            List.of("pom.xml"),
            "mvn",
            "mvnw",
            List.of("compile"),
            "target/classes",
            null
    ),
    GRADLE(
            List.of(
                    "settings.gradle",
                    "settings.gradle.kts",
                    "build.gradle",
                    "build.gradle.kts"
            ),
            "gradle",
            "gradlew",
            List.of("classes"),
            "build/classes/java/main",
            "build/resources/main"
    ),
    ;

    private final List<String> fileNames;

    private final String executableName;

    private final String wrapperScriptName;

    private final List<String> parameters;

    private final String classesDirectory;

    private final String resourceDirectory;

    BuildSystem(
            List<String> fileNames,
            String executableName,
            String wrapperScriptName,
            List<String> parameters,
            String classesDirectory,
            String resourceDirectory
    ) {
        this.executableName = executableName;
        this.wrapperScriptName = wrapperScriptName;
        this.fileNames = fileNames;
        this.parameters = parameters;
        this.classesDirectory = classesDirectory;
        this.resourceDirectory = resourceDirectory;
    }
}

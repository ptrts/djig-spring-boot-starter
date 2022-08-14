package org.taruts.djig.core.childContext.builds.types;

import org.apache.commons.io.FileUtils;
import org.taruts.djig.core.childContext.builds.BuildType;
import org.taruts.djig.core.childContext.builds.Builder;
import org.taruts.djig.core.childContext.builds.DynamicProjectBuild;
import org.taruts.processUtils.ProcessRunner;

import java.io.File;

public class GradleWrapperBuilder implements Builder {

    @Override
    public BuildType getBuildType() {
        return BuildType.GRADLE_WRAPPER;
    }

    public DynamicProjectBuild build(File projectSourceDirectory) {

        ProcessRunner.runBat("gradlew", projectSourceDirectory, "classes");

        File classesDirectory = FileUtils.getFile(projectSourceDirectory, "build/classes/java/main");
        if (!classesDirectory.exists() || !classesDirectory.isDirectory()) {
            throw new IllegalStateException();
        }

        File resourcesDirectory = FileUtils.getFile(projectSourceDirectory, "build/resources/main");
        if (!resourcesDirectory.exists() || !resourcesDirectory.isDirectory()) {
            throw new IllegalStateException();
        }

        return new DynamicProjectBuild(classesDirectory, resourcesDirectory);
    }
}

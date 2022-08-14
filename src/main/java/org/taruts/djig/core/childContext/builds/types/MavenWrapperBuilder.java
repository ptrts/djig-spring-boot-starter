package org.taruts.djig.core.childContext.builds.types;

import org.apache.commons.io.FileUtils;
import org.taruts.djig.core.childContext.builds.BuildType;
import org.taruts.djig.core.childContext.builds.Builder;
import org.taruts.djig.core.childContext.builds.DynamicProjectBuild;
import org.taruts.processUtils.ProcessRunner;

import java.io.File;

public class MavenWrapperBuilder implements Builder {

    @Override
    public BuildType getBuildType() {
        return BuildType.MAVEN_WRAPPER;
    }

    public DynamicProjectBuild build(File projectSourceDirectory) {

        ProcessRunner.runCmd("mvnw", projectSourceDirectory, "compile");

        File classesDirectory = FileUtils.getFile(projectSourceDirectory, "target/classes");
        if (!classesDirectory.exists() || !classesDirectory.isDirectory()) {
            throw new IllegalStateException();
        }

        return new DynamicProjectBuild(classesDirectory, null);
    }
}

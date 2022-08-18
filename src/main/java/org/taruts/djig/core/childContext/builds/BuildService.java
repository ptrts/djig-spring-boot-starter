package org.taruts.djig.core.childContext.builds;

import org.apache.commons.io.FileUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.taruts.djig.core.DynamicProject;
import org.taruts.djig.core.childContext.builds.detect.BuildTypeDetector;
import org.taruts.processUtils.ProcessRunner;

import java.io.File;
import java.util.List;

public class BuildService {

    @Autowired
    private BuildTypeDetector buildTypeDetector;

    public DynamicProjectBuild build(DynamicProject dynamicProject) {
        File sourceDirectory = dynamicProject.getSourceDirectory();

        BuildTypeAndWrapperScript result = getBuildTypeAndWrapperScript(dynamicProject);
        BuildType buildType = result.buildType();
        String wrapperShellScriptFileName = result.wrapperScript();

        buildInternal(sourceDirectory, buildType, wrapperShellScriptFileName);

        return composeResult(sourceDirectory, buildType);
    }

    private BuildTypeAndWrapperScript getBuildTypeAndWrapperScript(DynamicProject dynamicProject) {
        File sourceDirectory = dynamicProject.getSourceDirectory();
        BuildType buildType = dynamicProject.getBuildType();

        if (buildType == null) {
            BuildTypeAndWrapperScript detectResult = buildTypeDetector.detect(sourceDirectory);
            if (detectResult == null) {
                throw new NullPointerException("Failed to detect the build type for project %s".formatted(sourceDirectory));
            }
            return detectResult;
        } else {
            if (buildType.isWrapper()) {
                BuildSystem buildSystem = buildType.getBuildSystem();
                String wrapperShellScriptFileName = buildTypeDetector.findWrapperShellScriptFileName(sourceDirectory, buildSystem);
                if (wrapperShellScriptFileName == null) {
                    throw new NullPointerException("There is no wrapper shell script %s in %s".formatted(
                            buildSystem.getWrapperScriptName(), sourceDirectory
                    ));
                }
                return new BuildTypeAndWrapperScript(buildType, wrapperShellScriptFileName);
            } else {
                return new BuildTypeAndWrapperScript(buildType, null);
            }
        }
    }

    private void buildInternal(File sourceDirectory, BuildType buildType, String wrapperShellScriptFileName) {
        BuildSystem buildSystem = buildType.getBuildSystem();

        String executableName = buildSystem.getExecutableName();
        List<String> parameters = buildSystem.getParameters();

        if (buildType.isWrapper()) {
            ProcessRunner.runScript(sourceDirectory, wrapperShellScriptFileName, parameters);
        } else {
            ProcessRunner.runExecutable(sourceDirectory, executableName, parameters);
        }
    }

    private DynamicProjectBuild composeResult(File sourceDirectory, BuildType buildType) {
        BuildSystem buildSystem = buildType.getBuildSystem();
        File classesDirectory = getClassesDirectory(sourceDirectory, buildSystem.getClassesDirectory());
        File resourceDirectory = getResourceDirectory(sourceDirectory, buildSystem.getResourceDirectory());
        return new DynamicProjectBuild(classesDirectory, resourceDirectory);
    }

    private File getClassesDirectory(File projectSourceDirectory, String classesDirectoryStr) {
        File classesDirectory = FileUtils.getFile(projectSourceDirectory, classesDirectoryStr);
        if (!classesDirectory.exists() || !classesDirectory.isDirectory()) {
            throw new IllegalStateException();
        }
        return classesDirectory;
    }

    private File getResourceDirectory(File projectSourceDirectory, String resourceDirectoryStr) {
        if (resourceDirectoryStr == null) {
            return null;
        }
        File resourcesDirectory = FileUtils.getFile(projectSourceDirectory, resourceDirectoryStr);
        if (!resourcesDirectory.exists() || !resourcesDirectory.isDirectory()) {
            throw new IllegalStateException();
        }
        return resourcesDirectory;
    }
}

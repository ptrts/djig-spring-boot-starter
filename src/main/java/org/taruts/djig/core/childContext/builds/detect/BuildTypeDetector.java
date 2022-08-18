package org.taruts.djig.core.childContext.builds.detect;

import org.apache.commons.lang3.SystemUtils;
import org.taruts.djig.core.childContext.builds.BuildSystem;
import org.taruts.djig.core.childContext.builds.BuildType;
import org.taruts.djig.core.childContext.builds.BuildTypeAndWrapperScript;

import java.io.File;
import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;

public class BuildTypeDetector {

    public BuildTypeAndWrapperScript detect(File directory) {
        return detectAll(directory)
                .stream()
                .findFirst()
                .orElse(null);
    }

    private List<BuildTypeAndWrapperScript> detectAll(File directory) {
        return Stream
                .of(BuildSystem.values())
                .map(buildSystem -> detectBuildSystem(directory, buildSystem))
                .filter(Objects::nonNull)
                .toList();
    }

    private BuildTypeAndWrapperScript detectBuildSystem(File directory, BuildSystem buildSystem) {
        boolean fileExists = FileSearchUtils.fileExists(
                directory,
                buildSystem.getFileNames()
        );
        if (fileExists) {
            String wrapperScript = findWrapperShellScriptFileName(directory, buildSystem);
            boolean isWrapper = wrapperScript != null;
            BuildType buildType = BuildType.get(buildSystem, isWrapper);
            return new BuildTypeAndWrapperScript(buildType, wrapperScript);
        }
        return null;
    }

    public String findWrapperShellScriptFileName(File directory, BuildSystem buildSystem) {
        String wrapperScriptName = buildSystem.getWrapperScriptName();
        String wrapperShellScriptFile = SystemUtils.IS_OS_WINDOWS
                ? FileSearchUtils.findFile(directory, wrapperScriptName + ".cmd", wrapperScriptName + ".bat")
                : FileSearchUtils.findFile(directory, wrapperScriptName, wrapperScriptName + ".sh");
        return wrapperShellScriptFile;
    }
}

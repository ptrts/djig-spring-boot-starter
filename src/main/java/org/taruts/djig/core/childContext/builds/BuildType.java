package org.taruts.djig.core.childContext.builds;

import lombok.Getter;

import java.util.Arrays;

@Getter
public enum BuildType {
    //@formatter:off
    MAVEN          (BuildSystem.MAVEN  , false),
    MAVEN_WRAPPER  (BuildSystem.MAVEN  , true),
    GRADLE         (BuildSystem.GRADLE , false),
    GRADLE_WRAPPER (BuildSystem.GRADLE , true),
    //@formatter:on
    ;

    private final BuildSystem buildSystem;

    private final boolean wrapper;

    BuildType(BuildSystem buildSystem, boolean wrapper) {
        this.buildSystem = buildSystem;
        this.wrapper = wrapper;
    }

    public static BuildType get(BuildSystem buildSystem, boolean isWrapper) {
        BuildType buildType = Arrays
                .stream(BuildType.values())
                .filter(testedBuildType ->
                        testedBuildType.getBuildSystem() == buildSystem
                                && testedBuildType.isWrapper() == isWrapper
                )
                .findFirst()
                .orElse(null);
        return buildType;
    }
}

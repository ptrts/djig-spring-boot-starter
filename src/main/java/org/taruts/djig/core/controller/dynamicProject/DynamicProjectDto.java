package org.taruts.djig.core.controller.dynamicProject;

import org.taruts.djig.core.childContext.builds.BuildType;

public record DynamicProjectDto(
        String url,
        String username,
        String password,
        String dynamicInterfacePackage,
        BuildType buildType
) {
}

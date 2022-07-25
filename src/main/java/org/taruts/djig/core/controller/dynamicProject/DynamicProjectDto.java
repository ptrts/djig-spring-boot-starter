package org.taruts.djig.core.controller.dynamicProject;

public record DynamicProjectDto(
        String url,
        String username,
        String password,
        String dynamicInterfacePackage
) {
}

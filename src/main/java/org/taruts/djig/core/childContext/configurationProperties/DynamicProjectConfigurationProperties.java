package org.taruts.djig.core.childContext.configurationProperties;

public record DynamicProjectConfigurationProperties(
        String url,
        String username,
        String password,
        String dynamicInterfacePackage
) {
}

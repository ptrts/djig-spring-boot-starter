package org.taruts.djig.core.configurationProperties;

public record DynamicProjectConfigurationProperties(
        String url,
        String username,
        String password,
        String dynamicInterfacePackage
) {
}

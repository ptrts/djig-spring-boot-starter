package org.taruts.djig.core.configurationProperties;

import com.google.common.base.Functions;
import org.taruts.djig.core.DynamicProject;
import org.taruts.djig.core.childContext.remote.DynamicProjectGitRemote;
import org.taruts.djig.core.childContext.source.DynamicProjectSourceLocator;

import java.io.File;
import java.util.Map;
import java.util.stream.Collectors;

public class DynamicProjectConfigurationPropertiesMapper {

    public static Map<String, DynamicProject> map(Map<String, DjigConfigurationProperties.DynamicProject> dynamicProjectsConfigurationProperties) {
        return dynamicProjectsConfigurationProperties
                .entrySet()
                .stream()
                .map(entry -> {
                    String dynamicProjectName = entry.getKey();
                    DjigConfigurationProperties.DynamicProject dynamicProjectConfigurationProperties = entry.getValue();
                    return map(dynamicProjectName, dynamicProjectConfigurationProperties);
                })
                .collect(Collectors.toMap(DynamicProject::getName, Functions.identity()));
    }

    public static DynamicProject map(String dynamicProjectName, DjigConfigurationProperties.DynamicProject dynamicProjectConfigurationProperties) {
        DynamicProjectGitRemote remote = new DynamicProjectGitRemote(
                dynamicProjectConfigurationProperties.getUrl(),
                dynamicProjectConfigurationProperties.getUsername(),
                dynamicProjectConfigurationProperties.getPassword()
        );
        File sourceDirectory = DynamicProjectSourceLocator.getSourceDirectory(dynamicProjectName);
        return new DynamicProject(
                dynamicProjectName,
                remote,
                sourceDirectory,
                dynamicProjectConfigurationProperties.getDynamicInterfacePackage()
        );
    }
}

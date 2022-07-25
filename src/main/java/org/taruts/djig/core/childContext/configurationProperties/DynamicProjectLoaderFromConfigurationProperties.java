package org.taruts.djig.core.childContext.configurationProperties;

import org.springframework.beans.factory.annotation.Autowired;
import org.taruts.djig.core.DynamicProject;
import org.taruts.djig.core.DynamicProjectManager;

import javax.annotation.PostConstruct;

public class DynamicProjectLoaderFromConfigurationProperties {

    @Autowired
    private DjigConfigurationProperties djigConfigurationProperties;

    @Autowired
    private DynamicProjectManager dynamicProjectManager;

    @PostConstruct
    private void createAndLoadDynamicProjects() {
        djigConfigurationProperties.getDynamicProjects().forEach((dynamicProjectName, dynamicProjectConfigurationProperties) -> {
            DynamicProject dynamicProject = DynamicProjectConfigurationPropertiesMapper.map(dynamicProjectName, dynamicProjectConfigurationProperties);
            dynamicProjectManager.addProject(dynamicProject);
        });
    }
}

package org.taruts.djig.core.runtime;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.taruts.djig.core.DynamicProject;
import org.taruts.djig.core.DynamicProjectRepository;

@Component
public class DynamicImplementationSelector {

    @Autowired
    private DynamicProjectRepository dynamicProjectRepository;

    public <T> T select(Class<T> iface, String dynamicProjectName) {
        DynamicProject dynamicProject = dynamicProjectRepository.getProject(dynamicProjectName);
        return dynamicProject.getContext().getBean(iface);
    }
}

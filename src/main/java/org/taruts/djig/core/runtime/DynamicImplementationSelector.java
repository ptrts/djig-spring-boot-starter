package org.taruts.djig.core.runtime;

import org.springframework.beans.factory.annotation.Autowired;
import org.taruts.djig.core.DynamicProject;
import org.taruts.djig.core.DynamicProjectRepository;

public class DynamicImplementationSelector {

    @Autowired
    private DynamicProjectRepository dynamicProjectRepository;

    public <T> T select(Class<T> iface, String dynamicProjectName) {
        DynamicProject dynamicProject = dynamicProjectRepository.getProject(dynamicProjectName);
        return dynamicProject.getContext().getBean(iface);
    }
}

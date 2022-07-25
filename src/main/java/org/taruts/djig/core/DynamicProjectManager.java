package org.taruts.djig.core;

import org.springframework.beans.factory.annotation.Autowired;
import org.taruts.djig.core.childContext.DynamicProjectContextManager;

public class DynamicProjectManager {

    @Autowired
    private DynamicProjectRepository dynamicProjectRepository;

    @Autowired
    private DynamicProjectContextManager dynamicProjectContextManager;

    public void addProject(DynamicProject dynamicProject) {
        dynamicProjectRepository.addProject(dynamicProject);
        dynamicProjectContextManager.init(dynamicProject);
    }
}

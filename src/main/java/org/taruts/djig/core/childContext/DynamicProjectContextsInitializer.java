package org.taruts.djig.core.childContext;

import org.springframework.beans.factory.annotation.Autowired;
import org.taruts.djig.core.DynamicProjectRepository;

import javax.annotation.PostConstruct;

/**
 * Loads the dynamic code on application startup
 */
public class DynamicProjectContextsInitializer {

    @Autowired
    private DynamicProjectContextManager dynamicProjectContextManager;

    @Autowired
    private DynamicProjectRepository dynamicProjectRepository;

    @PostConstruct
    private void init() {
        dynamicProjectRepository.forEachProject(dynamicProjectContextManager::init);
    }
}

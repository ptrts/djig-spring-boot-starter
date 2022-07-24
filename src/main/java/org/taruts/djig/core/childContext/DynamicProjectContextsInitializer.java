package org.taruts.djig.core.childContext;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import org.taruts.djig.core.DynamicProjectRepository;

import javax.annotation.PostConstruct;

/**
 * Loads the dynamic code on application startup
 */
// todo Каким образом мы будем убирать эту инициализацию в тестах, если библиотека наша не будет знать про профиля dev и prod
@Profile({"dev", "prod"})
@Component
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

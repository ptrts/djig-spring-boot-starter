package org.taruts.djig.core;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.web.reactive.handler.SimpleUrlHandlerMapping;
import org.taruts.djig.core.childContext.DynamicProjectContextManager;
import org.taruts.djig.core.childContext.DynamicProjectContextsInitializer;
import org.taruts.djig.core.childContext.configurationProperties.DjigConfigurationProperties;
import org.taruts.djig.core.childContext.configurationProperties.DynamicProjectLoaderFromConfigurationProperties;
import org.taruts.djig.core.childContext.gradleBuild.DynamicProjectGradleBuildService;
import org.taruts.djig.core.childContext.remote.CloneRetryTemplate;
import org.taruts.djig.core.childContext.remote.DynamicProjectCloner;
import org.taruts.djig.core.childContext.remote.GitlabHookRegistrar;
import org.taruts.djig.core.controller.dynamicProject.DynamicProjectController;
import org.taruts.djig.core.controller.refresh.RefreshController;
import org.taruts.djig.core.mainContext.proxy.DynamicComponentProxyRegistrar;
import org.taruts.djig.core.runtime.DynamicImplementationSelector;

import java.util.Map;

@AutoConfiguration
@EnableAsync
@EnableConfigurationProperties(DjigConfigurationProperties.class)
public class DjigAutoConfiguration {

    @Bean
    DynamicProjectRepository dynamicProjectRepository() {
        return new DynamicProjectRepository();
    }

    @Bean
    DynamicProjectManager dynamicProjectManager() {
        return new DynamicProjectManager();
    }

    @Bean
    DynamicProjectLoaderFromConfigurationProperties dynamicProjectLoaderFromConfigurationProperties() {
        return new DynamicProjectLoaderFromConfigurationProperties();
    }

    @Bean
    DynamicProjectGradleBuildService dynamicProjectGradleBuildService() {
        return new DynamicProjectGradleBuildService();
    }

    @Bean
    CloneRetryTemplate cloneRetryTemplate() {
        return new CloneRetryTemplate();
    }

    @Bean
    DynamicProjectCloner dynamicProjectCloner() {
        return new DynamicProjectCloner();
    }

    @Bean
    @ConditionalOnProperty("djig.hook.host")
    GitlabHookRegistrar gitlabHookRegistrar() {
        return new GitlabHookRegistrar();
    }

    @Bean
    DynamicProjectContextManager dynamicProjectContextManager() {
        return new DynamicProjectContextManager();
    }

    @Bean
    DynamicProjectContextsInitializer dynamicProjectContextsInitializer() {
        return new DynamicProjectContextsInitializer();
    }

    @Bean
    DynamicComponentProxyRegistrar dynamicComponentProxyRegistrar() {
        return new DynamicComponentProxyRegistrar();
    }

    @Bean
    DynamicImplementationSelector dynamicImplementationSelector() {
        return new DynamicImplementationSelector();
    }

    @Bean
    SimpleUrlHandlerMapping djigHandlerMapping(
            @Value("djig.controller.refresh.path") String refreshPath,
            @Value("djig.controller.dynamic-project.path") String dynamicProjectPath
    ) {
        return new SimpleUrlHandlerMapping(
                Map.of(
                        refreshPath, refreshController(),
                        dynamicProjectPath, dynamicProjectController()
                )
        );
    }

    @Bean
    RefreshController refreshController() {
        return new RefreshController();
    }

    @Bean
    DynamicProjectController dynamicProjectController() {
        return new DynamicProjectController();
    }
}
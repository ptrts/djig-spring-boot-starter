package org.taruts.djig.core;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.task.TaskExecutionAutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.core.Ordered;
import org.taruts.djig.core.childContext.DynamicProjectContextManager;
import org.taruts.djig.core.childContext.builds.BuildService;
import org.taruts.djig.core.childContext.builds.detect.BuildTypeDetector;
import org.taruts.djig.core.childContext.context.DjigProxyHandlerMapping;
import org.taruts.djig.core.childContext.remote.CloneRetryTemplate;
import org.taruts.djig.core.childContext.remote.DynamicProjectCloner;
import org.taruts.djig.core.childContext.remote.GitlabHookRegistrar;
import org.taruts.djig.core.configurationProperties.DjigConfigurationProperties;
import org.taruts.djig.core.configurationProperties.DynamicProjectLoaderFromConfigurationProperties;
import org.taruts.djig.core.controller.dynamicProject.DynamicProjectController;
import org.taruts.djig.core.controller.refresh.RefreshController;
import org.taruts.djig.core.mainContext.proxy.DynamicComponentProxyRegistrar;
import org.taruts.djig.core.runtime.DynamicImplementationSelector;

@AutoConfiguration
@AutoConfigureAfter(TaskExecutionAutoConfiguration.class)
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
    BuildTypeDetector buildTypeDetector() {
        return new BuildTypeDetector();
    }

    @Bean
    BuildService buildService() {
        return new BuildService();
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
    DynamicComponentProxyRegistrar dynamicComponentProxyRegistrar() {
        return new DynamicComponentProxyRegistrar();
    }

    @Bean
    DynamicImplementationSelector dynamicImplementationSelector() {
        return new DynamicImplementationSelector();
    }

    @Bean
    RefreshController refreshController() {
        return new RefreshController();
    }

    @Bean
    DynamicProjectController dynamicProjectController() {
        return new DynamicProjectController();
    }

    @Bean
    DjigProxyHandlerMapping djigProxyHandlerMapping() {
        return new DjigProxyHandlerMapping(Ordered.HIGHEST_PRECEDENCE);
    }
}

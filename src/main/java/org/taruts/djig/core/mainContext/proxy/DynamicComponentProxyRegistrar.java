package org.taruts.djig.core.mainContext.proxy;

import org.reflections.Reflections;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.support.AbstractBeanDefinition;
import org.springframework.beans.factory.support.AutowireCandidateQualifier;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.BeanDefinitionRegistryPostProcessor;
import org.springframework.boot.context.properties.bind.BindResult;
import org.springframework.boot.context.properties.bind.Binder;
import org.springframework.context.EnvironmentAware;
import org.springframework.core.env.Environment;
import org.springframework.util.StringUtils;
import org.taruts.djig.core.DynamicProject;
import org.taruts.djig.core.DynamicProjectQualifier;
import org.taruts.djig.core.configurationProperties.DjigConfigurationProperties;
import org.taruts.djig.core.configurationProperties.DynamicProjectConfigurationPropertiesMapper;
import org.taruts.djig.dynamicApi.DynamicComponent;

import java.util.Map;
import java.util.Set;

/**
 * {@link BeanDefinitionRegistryPostProcessor} registering a {@link DynamicComponentProxy} for every dynamic interface.
 */
public class DynamicComponentProxyRegistrar implements BeanDefinitionRegistryPostProcessor, EnvironmentAware {

    private static final Class<DynamicComponentProxyFactory> FACTORY_CLASS = DynamicComponentProxyFactory.class;
    private static final String FACTORY_BEAN_NAME = StringUtils.uncapitalize(FACTORY_CLASS.getSimpleName());

    private Environment environment;

    @Override
    public void setEnvironment(Environment environment) {
        this.environment = environment;
    }

    @Override
    public void postProcessBeanDefinitionRegistry(BeanDefinitionRegistry registry) throws BeansException {

        registerFactory(registry);

        Map<String, DynamicProject> dynamicProjects = loadDynamicProjects();

        dynamicProjects.forEach((projectName, projectProperties) -> {
            Set<Class<? extends DynamicComponent>> dynamicInterfaces = getDynamicInterfaces(projectProperties.getDynamicInterfacePackage());
            dynamicInterfaces.forEach(dynamicComponentInterface ->
                    registerProxy(registry, dynamicComponentInterface, projectName)
            );
        });
    }

    private void registerFactory(BeanDefinitionRegistry registry) {

        AbstractBeanDefinition beanDefinition = BeanDefinitionBuilder
                .rootBeanDefinition(FACTORY_CLASS)
                .getBeanDefinition();

        registry.registerBeanDefinition(FACTORY_BEAN_NAME, beanDefinition);
    }

    private Map<String, DynamicProject> loadDynamicProjects() {
        DjigConfigurationProperties djigConfigurationProperties = loadDjigApplicationProperties();
        return DynamicProjectConfigurationPropertiesMapper.map(djigConfigurationProperties.getDynamicProjects());
    }

    /**
     * This is how we get a populated type-safe properties object while being in the {@link BeanDefinitionRegistryPostProcessor} phase.
     * We cannot just autowire it, because autowiring does not work in this phase.
     * <a href="https://stackoverflow.com/a/65727823/2304456">a stackoverflow link</a>
     */
    private DjigConfigurationProperties loadDjigApplicationProperties() {
        BindResult<DjigConfigurationProperties> bindResult = Binder
                .get(environment)
                .bind(DjigConfigurationProperties.PREFIX, DjigConfigurationProperties.class);
        return bindResult.get();
    }

    private Set<Class<? extends DynamicComponent>> getDynamicInterfaces(String packageName) {
        Reflections reflections = new Reflections(packageName);
        return reflections.getSubTypesOf(DynamicComponent.class);
    }

    private void registerProxy(BeanDefinitionRegistry registry, Class<? extends DynamicComponent> dynamicInterface, String projectName) {
        String beanName =
                StringUtils.uncapitalize(dynamicInterface.getSimpleName()) +
                        '_' +
                        projectName.replaceAll("-", "_");

        AbstractBeanDefinition beanDefinition = BeanDefinitionBuilder
                .rootBeanDefinition(dynamicInterface)
                .setFactoryMethodOnBean("createProxy", FACTORY_BEAN_NAME)
                .addConstructorArgValue(dynamicInterface)
                .getBeanDefinition();

        beanDefinition.addQualifier(new AutowireCandidateQualifier(DynamicProjectQualifier.class, projectName));

        registry.registerBeanDefinition(beanName, beanDefinition);
    }

    @Override
    public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) throws BeansException {
    }
}

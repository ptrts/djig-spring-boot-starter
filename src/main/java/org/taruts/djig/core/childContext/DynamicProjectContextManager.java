package org.taruts.djig.core.childContext;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.event.ContextClosedEvent;
import org.springframework.context.event.EventListener;
import org.taruts.djig.core.DynamicProject;
import org.taruts.djig.core.DynamicProjectQualifier;
import org.taruts.djig.core.DynamicProjectRepository;
import org.taruts.djig.core.childContext.classLoader.DynamicProjectClassLoader;
import org.taruts.djig.core.childContext.context.GradleProjectApplicationContext;
import org.taruts.djig.core.childContext.gradleBuild.DynamicProjectGradleBuild;
import org.taruts.djig.core.childContext.gradleBuild.DynamicProjectGradleBuildService;
import org.taruts.djig.core.childContext.remote.DynamicProjectCloner;
import org.taruts.djig.core.mainContext.proxy.DynamicComponentProxy;
import org.taruts.djig.dynamicApi.DynamicComponent;

import java.io.File;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

@Slf4j
public class DynamicProjectContextManager {

    @Autowired
    private ApplicationContext mainContext;

    @Autowired
    private DynamicProjectCloner dynamicProjectCloner;

    @Autowired
    private DynamicProjectGradleBuildService dynamicProjectGradleBuildService;

    @Autowired
    private DynamicProjectRepository dynamicProjectRepository;

    @Autowired
    private ApplicationContext applicationContext;

    @SneakyThrows
    public void init(DynamicProject dynamicProject) {
        File sourceDirectory = dynamicProject.getSourceDirectory();
        if (sourceDirectory.exists()) {
            FileUtils.forceDelete(sourceDirectory);
        }
        refresh(dynamicProject);
    }

    public void refresh(DynamicProject dynamicProject) {
        GradleProjectApplicationContext newChildContext = createNewChildContext(dynamicProject);
        setNewDelegatesInMainContext(dynamicProject, newChildContext);
        closeOldChildContextAndSetNewReference(dynamicProject, newChildContext);
    }

    private GradleProjectApplicationContext createNewChildContext(DynamicProject dynamicProject) {

        // Clone
        dynamicProjectCloner.cloneWithRetries(
                dynamicProject.getRemote(),
                dynamicProject.getSourceDirectory()
        );

        // Build
        DynamicProjectGradleBuild build = dynamicProjectGradleBuildService.build(dynamicProject.getSourceDirectory());

        DynamicProjectClassLoader childClassLoader = new DynamicProjectClassLoader(
                dynamicProject.getSourceDirectory(),
                build.classesDirectory(),
                build.resourcesDirectory()
        );

        GradleProjectApplicationContext newContext = new GradleProjectApplicationContext(mainContext, childClassLoader);

        newContext.refresh();
        return newContext;
    }

    private void setNewDelegatesInMainContext(DynamicProject dynamicProject, GradleProjectApplicationContext newContext) {

        ConfigurableListableBeanFactory beanFactory = ((ConfigurableApplicationContext) mainContext).getBeanFactory();

        String thisProjectName = dynamicProject.getName();

        @SuppressWarnings("rawtypes")
        Map<String, DynamicComponentProxy> proxyBeansMap = DjigBeanFactoryAnnotationUtils.qualifiedBeansOfType(
                beanFactory,
                DynamicComponentProxy.class,
                DynamicProjectQualifier.class,
                thisProjectName
        );

        proxyBeansMap.forEach((proxyBeanName, proxy) -> {

            Class<?>[] proxyInterfaces = proxy.getClass().getInterfaces();

            Class<? extends DynamicComponent> dynamicProxyInterface = Stream
                    .of(proxyInterfaces)
                    .filter(DynamicComponent.class::isAssignableFrom)
                    .map(iface -> {
                        //noinspection unchecked
                        return (Class<? extends DynamicComponent>) iface;
                    })
                    .findAny()
                    .orElseThrow(() ->
                            new IllegalStateException(
                                    "A DynamicComponentProxy must implement one of the interfaces extending DynamicComponent"
                            )
                    );

            Map<String, ? extends DynamicComponent> dynamicImplementationsMap = newContext.getBeansOfType(dynamicProxyInterface);

            List<? extends DynamicComponent> implementations = dynamicImplementationsMap
                    .values()
                    .stream()
                    .filter(currentImplementation -> !(currentImplementation instanceof DynamicComponentProxy))
                    .toList();
            DynamicComponent childContextDynamicImplementation = implementations.get(0);

            //noinspection unchecked
            proxy.setDelegate(childContextDynamicImplementation);
        });
    }

    private void closeOldChildContextAndSetNewReference(DynamicProject project, GradleProjectApplicationContext newChildContext) {
        // Closing the old context
        GradleProjectApplicationContext oldChildContext = project.getContext();
        if (oldChildContext != null) {
            oldChildContext.close();
        }

        // Saving the new context
        project.setContext(newChildContext);
    }

    @EventListener(ContextClosedEvent.class)
    public void closeAllContexts(ContextClosedEvent event) {
        if (event.getApplicationContext() != applicationContext) {
            return;
        }

        dynamicProjectRepository.forEachProject(dynamicProject -> {
            GradleProjectApplicationContext childContext = dynamicProject.getContext();
            if (childContext != null) {
                childContext.close();
            }
            dynamicProject.setContext(null);
        });
    }
}

package org.taruts.djig.core.childContext.context;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.AnnotationAwareOrderComparator;
import org.springframework.http.server.PathContainer;
import org.springframework.http.server.RequestPath;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.web.reactive.HandlerMapping;
import org.springframework.web.reactive.handler.AbstractHandlerMapping;
import org.springframework.web.server.ServerWebExchange;
import org.taruts.djig.core.DynamicProject;
import org.taruts.djig.core.DynamicProjectRepository;
import org.taruts.djig.core.configurationProperties.DjigConfigurationProperties;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class DjigProxyHandlerMapping extends AbstractHandlerMapping {

    @Autowired
    private DynamicProjectRepository dynamicProjectRepository;

    @Autowired
    private DjigConfigurationProperties djigConfigurationProperties;

    public DjigProxyHandlerMapping(int order) {
        super();
        setOrder(order);
    }

    @Override
    protected Mono<?> getHandlerInternal(ServerWebExchange exchange) {

        ServerHttpRequest request = exchange.getRequest();
        RequestPath requestPath = request.getPath();

        PathContainer contextPath = requestPath.contextPath();

        PathContainer pathWithinApplication = requestPath.pathWithinApplication();
        String pathWithinApplicationStr = pathWithinApplication.value();

        String dynamicProjectControllerPathStr = djigConfigurationProperties.getController().getDynamicProject().getPath()
                .replaceAll("^/", "")
                .replaceAll("/$", "");
        dynamicProjectControllerPathStr = "/" + dynamicProjectControllerPathStr;
        PathContainer dynamicProjectControllerPath = PathContainer.parsePath(dynamicProjectControllerPathStr);

        if (!pathWithinApplicationStr.startsWith(dynamicProjectControllerPathStr)) {
            return Mono.empty();
        }

        if (pathWithinApplicationStr.equals(dynamicProjectControllerPathStr)) {
            return Mono.empty();
        }

        // Next in a path after the prefix must go a project name
        int prefixSize = dynamicProjectControllerPath.elements().size();
        String projectName = pathWithinApplication.subPath(prefixSize + 1).elements().get(0).value();

        DynamicProject dynamicProject = dynamicProjectRepository.getProject(projectName);
        if (dynamicProject == null) {
            return Mono.empty();
        }

        // The project name is transferred from the path within application to the context path
        String modifiedContextPath = contextPath.value() + dynamicProjectControllerPath + "/" + projectName;
        ServerWebExchange modifiedExchange = exchange
                .mutate()
                .request(requestBuilder -> requestBuilder
                        .contextPath(modifiedContextPath)
                )
                .build();

        Map<String, HandlerMapping> childHandlerMappingsMap = dynamicProject.getContext().getBeansOfType(HandlerMapping.class);
        List<HandlerMapping> childHandlerMappings = new ArrayList<>(childHandlerMappingsMap.values());
        AnnotationAwareOrderComparator.sort(childHandlerMappings);

        Mono<Object> handlerMono = Flux
                .fromIterable(childHandlerMappings)
                .concatMap(childHandlerMapping -> {
                    Mono<Object> childHandlerMono = childHandlerMapping.getHandler(modifiedExchange);
                    return childHandlerMono;
                })
                .take(1)
                .singleOrEmpty();

        return handlerMono;
    }
}

package org.taruts.djig.core.childContext.builds;

import org.springframework.beans.factory.annotation.Autowired;
import org.taruts.djig.core.DynamicProject;

import javax.annotation.PostConstruct;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

public class BuildService {

    @Autowired
    private List<Builder> builders;

    private Map<BuildType, Builder> builderMap;

    public DynamicProjectBuild build(DynamicProject dynamicProject) {
        Builder builder = builderMap.get(dynamicProject.getBuildType());
        if (builder == null) {
            throw new IllegalStateException("No builder found for buildType %s".formatted(dynamicProject.getBuildType()));
        }
        return builder.build(dynamicProject.getSourceDirectory());
    }

    @PostConstruct
    private void init() {
        builderMap = builders
                .stream()
                .collect(
                        Collectors.toMap(
                                Builder::getBuildType,
                                Function.identity()
                        )
                );
    }
}

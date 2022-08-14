package org.taruts.djig.core.controller.dynamicProject;

import org.taruts.djig.core.DynamicProject;
import org.taruts.djig.core.childContext.remote.DynamicProjectGitRemote;
import org.taruts.djig.core.childContext.source.DynamicProjectSourceLocator;

import java.io.File;

public class DynamicProjectDtoMapper {
    public static DynamicProject map(String projectName, DynamicProjectDto dto) {
        DynamicProjectGitRemote remote = new DynamicProjectGitRemote(
                dto.url(),
                dto.username(),
                dto.password()
        );

        File sourceDirectory = DynamicProjectSourceLocator.getSourceDirectory(projectName);

        return new DynamicProject(
                projectName,
                remote,
                sourceDirectory,
                dto.dynamicInterfacePackage(),
                dto.buildType()
        );
    }
}

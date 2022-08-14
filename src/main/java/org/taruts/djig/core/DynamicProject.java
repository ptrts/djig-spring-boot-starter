package org.taruts.djig.core;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.taruts.djig.core.childContext.builds.BuildType;
import org.taruts.djig.core.childContext.context.GradleProjectApplicationContext;
import org.taruts.djig.core.childContext.remote.DynamicProjectGitRemote;

import java.io.File;

@RequiredArgsConstructor
@Getter
@Setter
public class DynamicProject {
    final String name;
    final DynamicProjectGitRemote remote;
    final File sourceDirectory;
    final String dynamicInterfacePackage;
    final BuildType buildType;
    GradleProjectApplicationContext context;
}

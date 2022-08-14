package org.taruts.djig.core.childContext.builds;

import java.io.File;

public interface Builder {

    BuildType getBuildType();

    DynamicProjectBuild build(File projectSourceDirectory);
}

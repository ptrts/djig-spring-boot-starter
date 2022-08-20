package org.taruts.djig.core.configurationProperties;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.taruts.djig.core.childContext.builds.BuildType;

import java.util.HashMap;
import java.util.Map;

@ConfigurationProperties(DjigConfigurationProperties.PREFIX)
@Getter
@Setter
public class DjigConfigurationProperties {

    public static final String PREFIX = "djig";

    Map<String, DynamicProject> dynamicProjects = new HashMap<>();

    Hook hook = new Hook();

    Controller controller = new Controller();

    @Getter
    @Setter
    public static class DynamicProject {
        String url;
        String username;
        String password;
        String dynamicInterfacePackage;
        BuildType buildType;
        HostingType hostingType;
    }

    @Getter
    @Setter
    public static class Hook {
        String protocol;
        String host;
        boolean sslVerification = false;
        String secretToken;
    }

    @Getter
    @Setter
    public static class Controller {

        Element refresh = new Element();
        Element dynamicProject = new Element();

        @Getter
        @Setter
        public static class Element {
            String path;
        }
    }
}

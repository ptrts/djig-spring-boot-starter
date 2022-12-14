package org.taruts.djig.core.childContext.remote;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.gitlab4j.api.GitLabApi;
import org.gitlab4j.api.GitLabApiException;
import org.gitlab4j.api.models.Project;
import org.gitlab4j.api.models.ProjectHook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.web.context.WebServerApplicationContext;
import org.springframework.core.Ordered;
import org.springframework.web.util.UriComponents;
import org.springframework.web.util.UriComponentsBuilder;
import org.taruts.djig.core.DynamicProject;
import org.taruts.djig.core.DynamicProjectRepository;
import org.taruts.djig.core.OurSmartLifecycle;
import org.taruts.djig.core.configurationProperties.DjigConfigurationProperties;
import org.taruts.djig.core.utils.DjigStringUtils;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.util.Comparator;
import java.util.List;
import java.util.function.BiConsumer;

/**
 * For any dynamic project registers a webhook in the dynamic code GitLab project, after the application has started
 * and removes the hook on shutdown.
 * The webhook notifies the application when a new version of the dynamic code has been pushed.
 */
@Slf4j
public class GitlabHookRegistrar extends OurSmartLifecycle implements Ordered {

    private static final Comparator<URL> URL_COMPARATOR = Comparator
            .comparing(URL::getHost)
            .thenComparing(URL::getPort)
            .thenComparing(URL::getPath);

    @Autowired
    private DjigConfigurationProperties djigConfigurationProperties;

    @Autowired
    private WebServerApplicationContext webServerApplicationContext;

    @Autowired
    private DynamicProjectRepository dynamicProjectRepository;

    @Override
    public int getOrder() {
        return -1;
    }

    @Override
    public void doStop() {
        dynamicProjectRepository.forEachProject(this::deleteDynamicProjectHooks);
    }

    @Override
    public void doStart() {
        dynamicProjectRepository.forEachProject(this::replaceHook);
    }

    private void replaceHook(DynamicProject dynamicProject) {

        URL hookUrl = getHookUrl(dynamicProject);
        if (hookUrl == null) {
            return;
        }
        withGitLabProject(dynamicProject, (gitLabApi, gitLabProject) -> {
            deleteHooksByUrl(gitLabApi, gitLabProject, hookUrl);
            addHook(gitLabApi, gitLabProject, hookUrl);
        });
    }

    private void addHook(GitLabApi gitLabApi, Project gitLabProject, URL hookUrl) {
        DjigConfigurationProperties.Hook hookProperties = djigConfigurationProperties.getHook();

        boolean enableSslVerification = hookProperties.isSslVerification();
        String secretToken = hookProperties.getSecretToken();

        ProjectHook enabledHooks = new ProjectHook();

        //@formatter:off
        enabledHooks.setPushEvents               (true);
        enabledHooks.setPushEventsBranchFilter   ("master");
        enabledHooks.setIssuesEvents             (false);
        enabledHooks.setConfidentialIssuesEvents (false);
        enabledHooks.setMergeRequestsEvents      (false);
        enabledHooks.setTagPushEvents            (false);
        enabledHooks.setNoteEvents               (false);
        enabledHooks.setConfidentialNoteEvents   (false);
        enabledHooks.setJobEvents                (false);
        enabledHooks.setPipelineEvents           (false);
        enabledHooks.setWikiPageEvents           (false);
        enabledHooks.setRepositoryUpdateEvents   (false);
        enabledHooks.setDeploymentEvents         (false);
        enabledHooks.setReleasesEvents           (false);
        enabledHooks.setDeploymentEvents         (false);
        //@formatter:on

        try {
            gitLabApi.getProjectApi().addHook(gitLabProject.getId(), hookUrl.toString(), enabledHooks, enableSslVerification, secretToken);
        } catch (GitLabApiException e) {
            throw new RuntimeException(e);
        }
    }

    private void deleteDynamicProjectHooks(DynamicProject dynamicProject) {
        URL hookUri = getHookUrl(dynamicProject);
        if (hookUri == null) {
            return;
        }
        withGitLabProject(
                dynamicProject,
                (gitLabApi, gitLabProject) -> deleteHooksByUrl(gitLabApi, gitLabProject, hookUri)
        );
    }

    @SneakyThrows
    private URL getHookUrl(DynamicProject dynamicProject) {
        DjigConfigurationProperties.Hook hookProperties = djigConfigurationProperties.getHook();

        String host = hookProperties.getHost();
        if (StringUtils.isBlank(host)) {
            return null;
        }

        String protocol = hookProperties.getProtocol();

        int ourServletContainerPort = webServerApplicationContext.getWebServer().getPort();

        String refreshPath = djigConfigurationProperties.getController().getRefresh().getPath();
        refreshPath = DjigStringUtils.ensureEndsWithSlash(refreshPath);
        return UriComponentsBuilder
                .newInstance()
                .scheme(protocol)
                .host(host)
                .port(ourServletContainerPort)
                .replacePath(refreshPath)
                .path(dynamicProject.getName())
                .build()
                .toUri()
                .toURL();
    }

    @SneakyThrows
    private void withGitLabProject(DynamicProject dynamicProject, BiConsumer<GitLabApi, Project> useProject) {
        DynamicProjectGitRemote remoteProperties = dynamicProject.getRemote();
        String repositoryUrlStr = remoteProperties.url();
        String username = remoteProperties.username();
        String password = remoteProperties.password();

        URI repositoryUri = URI.create(repositoryUrlStr);

        String projectPath = repositoryUri.getPath().replaceAll("^/(.*)\\.git$", "$1");

        UriComponents gitlabUriComponents = UriComponentsBuilder
                .fromUri(repositoryUri)
                .replacePath(null)
                .build();
        String gitlabUrlStr = gitlabUriComponents.toString();

        try (
                GitLabApi gitLabApi = GitLabApi.oauth2Login(gitlabUrlStr, username, password, true)
        ) {
            Project project = gitLabApi
                    .getProjectApi()
                    .getOptionalProject(projectPath)
                    .orElseThrow(() -> new IllegalStateException("Project not found by path %s".formatted(projectPath)));
            useProject.accept(gitLabApi, project);
        }
    }

    private void deleteHooksByUrl(GitLabApi gitLabApi, Project project, URL hookUrl) {
        List<ProjectHook> hooksToDelete = findHooksToDeleteByUrl(gitLabApi, project, hookUrl);
        hooksToDelete.forEach(hookToDelete -> {
            try {
                gitLabApi.getProjectApi().deleteHook(hookToDelete);
            } catch (GitLabApiException e) {
                throw new RuntimeException(e);
            }
        });
    }

    @SneakyThrows
    private List<ProjectHook> findHooksToDeleteByUrl(GitLabApi gitLabApi, Project project, URL hookUrl) {
        List<ProjectHook> allProjectHooks = gitLabApi.getProjectApi().getHooks(project.getId());
        return allProjectHooks.stream().filter(currentHook -> {
            try {
                URL currentHookUrl = new URL(currentHook.getUrl());
                return URL_COMPARATOR.compare(currentHookUrl, hookUrl) == 0;
            } catch (MalformedURLException e) {
                // All bad URLs must be deleted
                return true;
            }
        }).toList();
    }
}

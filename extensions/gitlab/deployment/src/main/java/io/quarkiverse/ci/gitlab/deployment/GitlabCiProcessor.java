package io.quarkiverse.ci.gitlab.deployment;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.jboss.logging.Logger;

import io.quarkiverse.ci.common.Projects;
import io.quarkiverse.ci.gitlab.spi.GeneratedGitlabCiResourceBuildItem;
import io.quarkus.deployment.annotations.BuildProducer;
import io.quarkus.deployment.annotations.BuildStep;
import io.quarkus.deployment.builditem.FeatureBuildItem;
import io.quarkus.deployment.builditem.GeneratedFileSystemResourceBuildItem;
import io.quarkus.devtools.project.BuildTool;
import io.quarkus.devtools.project.QuarkusProject;
import io.quarkus.devtools.project.QuarkusProjectHelper;
import io.quarkus.qute.Engine;
import io.quarkus.qute.Template;
import io.quarkus.qute.TemplateInstance;

public class GitlabCiProcessor {

    private static final Logger LOG = Logger.getLogger(GitlabCiProcessor.class);
    private static final String FEATURE = "gitlab-ci-generator";
    private static final String DEFAULT_JAVA_VERSION = "21";

    @BuildStep
    FeatureBuildItem feature() {
        return new FeatureBuildItem(FEATURE);
    }

    @BuildStep
    void generatePipeline(GitlabCiConfiguration config,
            BuildProducer<GeneratedGitlabCiResourceBuildItem> pipeline) {
        if (!config.generation().enabled()) {
            LOG.info("GitLab CI pipeline generation is disabled. Skipping.");
            return;
        }
        Path projectRootDir = Projects.getProjectRoot();
        Engine engine = Engine.builder().addDefaults().build();
        Template template = engine.parse(getTemplateContent("gitlab-ci.yml.qute"));

        String fileName = ".gitlab-ci.yml";
        Map<String, Object> params = Map.<String, Object> of(
                "name", config.name(),
                "image", getDockerImage(projectRootDir, config),
                "cacheKey", cacheKey(projectRootDir),
                "cachePaths", cachePaths(projectRootDir),
                "buildCommand", buildCommand(projectRootDir),
                "testCommand", testCommand(projectRootDir));

        TemplateInstance templateInstance = template.data(params);
        String content = templateInstance.render();
        pipeline.produce(new GeneratedGitlabCiResourceBuildItem(fileName, content));
    }

    @BuildStep
    void savePipeline(List<GeneratedGitlabCiResourceBuildItem> items,
            BuildProducer<GeneratedFileSystemResourceBuildItem> fileSystemResources) {
        Path projectRootDir = Projects.getProjectRoot();

        for (var item : items) {
            Path pipelineFile = projectRootDir.resolve(item.getName());
            String resourcePath = pipelineFile.toAbsolutePath().toString();
            fileSystemResources.produce(new GeneratedFileSystemResourceBuildItem(resourcePath, item.getContent().getBytes()));
        }
    }

    private boolean hasMavenWrapper(Path projectDir) {
        return projectDir.resolve("mvnw").toFile().exists();
    }

    private boolean hasGradleWrapper(Path projectDir) {
        return projectDir.resolve("gradlew").toFile().exists();
    }

    private String getDockerImage(Path projectDir, GitlabCiConfiguration config) {
        Optional<String> javaVersion = getJavaVersion(projectDir);
        if (javaVersion.isPresent()) {
            return "openjdk:" + javaVersion.get() + "-jdk";
        }
        return config.image();
    }

    private String cacheKey(Path projectDir) {
        BuildTool buildTool = QuarkusProjectHelper.detectExistingBuildTool(projectDir);
        return switch (buildTool) {
            case MAVEN -> "$CI_COMMIT_REF_SLUG-maven";
            case GRADLE -> "$CI_COMMIT_REF_SLUG-gradle";
            default -> throw new IllegalStateException("Unexpected value: " + buildTool);
        };
    }

    private String cachePaths(Path projectDir) {
        BuildTool buildTool = QuarkusProjectHelper.detectExistingBuildTool(projectDir);
        return switch (buildTool) {
            case MAVEN -> "- .m2/repository/";
            case GRADLE -> "- .gradle/wrapper/\n    - .gradle/caches/";
            default -> throw new IllegalStateException("Unexpected value: " + buildTool);
        };
    }

    private String buildCommand(Path projectDir) {
        BuildTool buildTool = QuarkusProjectHelper.detectExistingBuildTool(projectDir);
        return switch (buildTool) {
            case MAVEN ->
                hasMavenWrapper(projectDir) ? "./mvnw clean package -DskipTests=true" : "mvn clean package -DskipTests=true";
            case GRADLE -> hasGradleWrapper(projectDir) ? "./gradlew build" : "gradle clean build";
            default -> throw new IllegalStateException("Unexpected value: " + buildTool);
        };
    }

    private String testCommand(Path projectDir) {
        BuildTool buildTool = QuarkusProjectHelper.detectExistingBuildTool(projectDir);
        return switch (buildTool) {
            case MAVEN -> hasMavenWrapper(projectDir) ? "./mvnw verify" : "mvn verify";
            case GRADLE -> hasGradleWrapper(projectDir) ? "./gradlew test" : "gradle test";
            default -> throw new IllegalStateException("Unexpected value: " + projectDir);
        };
    }

    private Optional<String> getJavaVersion(Path projectDir) {
        try {
            QuarkusProject project = QuarkusProjectHelper.getProject(projectDir);
            return Optional.of(String.valueOf(project.getJavaVersion().getAsInt()));
        } catch (Exception e) {
            return Optional.empty();
        }
    }

    private Path getWorkingDirectory() {
        return Paths.get(System.getProperty("user.dir"));
    }

    private String getTemplateContent(String resourcePath) {
        try (var is = getClass().getClassLoader().getResourceAsStream(resourcePath)) {
            if (is == null)
                throw new IllegalArgumentException("Template not found: " + resourcePath);
            return new String(is.readAllBytes());
        } catch (Exception e) {
            throw new RuntimeException("Failed to read template: " + resourcePath, e);
        }
    }
}
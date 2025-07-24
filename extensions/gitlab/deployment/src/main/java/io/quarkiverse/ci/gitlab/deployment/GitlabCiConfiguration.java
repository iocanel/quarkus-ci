package io.quarkiverse.ci.gitlab.deployment;

import static io.quarkus.runtime.annotations.ConfigPhase.BUILD_TIME;

import java.util.Optional;

import io.quarkus.runtime.annotations.ConfigRoot;
import io.smallrye.config.ConfigMapping;
import io.smallrye.config.WithDefault;

@ConfigRoot(phase = BUILD_TIME)
@ConfigMapping(prefix = "quarkus.gitlab-ci")
public interface GitlabCiConfiguration {

    /**
     * The generated pipeline name.
     */
    @WithDefault("build")
    String name();

    /**
     * The Docker image to use for the pipeline
     */
    @WithDefault("openjdk:21-jdk")
    String image();

    /**
     * The generation configuration.
     */
    Generation generation();

    /**
     * The JDK configuration.
     */
    Jdk jdk();

    interface Jdk {

        /**
         * The JDK distribution to use.
         */
        @WithDefault("openjdk")
        String distribution();

        /**
         * The JDK version to use.
         **/
        Optional<String> version();
    }

    interface Generation {
        /**
         * Whether to enable the GitLab CI generation at build time.
         */
        @WithDefault("false")
        boolean enabled();
    }
}

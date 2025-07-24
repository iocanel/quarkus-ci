package io.quarkiverse.ci.common;

import java.util.Optional;

import io.quarkus.devtools.project.BuildTool;

public enum JdkDistribution {
    TEMURIN("eclipse-temurin"),
    CORRETTO("amazoncorretto"),
    ZULU("azul/zulu-openjdk"),
    OPENJDK("openjdk");

    private final String dockerImagePrefix;

    JdkDistribution(String dockerImagePrefix) {
        this.dockerImagePrefix = dockerImagePrefix;
    }

    public String getDockerImage(String javaVersion) {
        boolean isVersionless = javaVersion == null || javaVersion.isBlank() || "latest".equalsIgnoreCase(javaVersion);

        return switch (this) {
            case TEMURIN -> isVersionless
                    ? dockerImagePrefix + ":latest"
                    : dockerImagePrefix + ":" + javaVersion + "-jdk";

            case CORRETTO -> isVersionless
                    ? dockerImagePrefix + ":latest"
                    : dockerImagePrefix + ":" + javaVersion;

            case ZULU -> isVersionless
                    ? dockerImagePrefix + ":latest"
                    : dockerImagePrefix + ":" + javaVersion + "-jdk";

            case OPENJDK -> isVersionless
                    ? dockerImagePrefix + ":latest"
                    : dockerImagePrefix + ":" + javaVersion + "-jdk";
        };
    }

    public static Optional<String> getDockerImage(JdkDistribution jdk, Optional<String> javaVersion,
            Optional<BuildTool> buildTool) {
        String version = javaVersion
                .filter(v -> !v.isBlank() && !"latest".equalsIgnoreCase(v))
                .orElse(null);

        if (buildTool.isEmpty()) {
            // No build tool: fallback to plain JDK image
            return Optional.of(jdk.getDockerImage(version));
        }

        BuildTool tool = buildTool.get();
        switch (tool) {
            case MAVEN:
                switch (jdk) {
                    case TEMURIN:
                        return Optional.of("maven:" + (version == null
                                ? "eclipse-temurin"
                                : "3.9.11-eclipse-temurin-" + version));
                    case OPENJDK:
                        return Optional.of("maven:" + (version == null
                                ? "latest"
                                : "3.9.11-openjdk-" + version));
                    default:
                        return Optional.empty(); // No official Maven image for Corretto or Zulu
                }

            case GRADLE:
                switch (jdk) {
                    case TEMURIN:
                    case OPENJDK:
                    case CORRETTO:
                    case ZULU:
                        return Optional.of("gradle:" + (version == null
                                ? "latest"
                                : "8.7-jdk" + version));
                    default:
                        return Optional.empty();
                }

            default:
                return Optional.empty();
        }
    }

    public static JdkDistribution fromString(String distribution) {
        if (distribution == null) {
            return TEMURIN; // sensible default
        }

        try {
            return JdkDistribution.valueOf(distribution.toUpperCase());
        } catch (IllegalArgumentException e) {
            return TEMURIN;
        }
    }
}

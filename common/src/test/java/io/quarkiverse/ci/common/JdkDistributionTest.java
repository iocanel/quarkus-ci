package io.quarkiverse.ci.common;

import static org.junit.jupiter.api.Assertions.*;

import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import io.quarkus.devtools.project.BuildTool;

class JdkDistributionTest {

    @Test
    void testFromStringWithValidValues() {
        assertEquals(JdkDistribution.TEMURIN, JdkDistribution.fromString("temurin"));
        assertEquals(JdkDistribution.TEMURIN, JdkDistribution.fromString("TEMURIN"));
        assertEquals(JdkDistribution.OPENJDK, JdkDistribution.fromString("openjdk"));
        assertEquals(JdkDistribution.CORRETTO, JdkDistribution.fromString("corretto"));
        assertEquals(JdkDistribution.ZULU, JdkDistribution.fromString("zulu"));
    }

    @Test
    void testFromStringWithInvalidValues() {
        assertEquals(JdkDistribution.TEMURIN, JdkDistribution.fromString("invalid"));
        assertEquals(JdkDistribution.TEMURIN, JdkDistribution.fromString("unknown"));
        assertEquals(JdkDistribution.TEMURIN, JdkDistribution.fromString(""));
    }

    @Test
    void testFromStringWithNull() {
        assertEquals(JdkDistribution.TEMURIN, JdkDistribution.fromString(null));
    }

    @Test
    void testGetDockerImageWithVersion() {
        assertEquals("eclipse-temurin:21-jdk", JdkDistribution.TEMURIN.getDockerImage("21"));
        assertEquals("amazoncorretto:17", JdkDistribution.CORRETTO.getDockerImage("17"));
        assertEquals("azul/zulu-openjdk:11-jdk", JdkDistribution.ZULU.getDockerImage("11"));
        assertEquals("openjdk:8-jdk", JdkDistribution.OPENJDK.getDockerImage("8"));
    }

    @ParameterizedTest
    @ValueSource(strings = { "", "  ", "latest" })
    void testGetDockerImageWithVersionless(String version) {
        assertEquals("eclipse-temurin:latest", JdkDistribution.TEMURIN.getDockerImage(version));
        assertEquals("amazoncorretto:latest", JdkDistribution.CORRETTO.getDockerImage(version));
        assertEquals("azul/zulu-openjdk:latest", JdkDistribution.ZULU.getDockerImage(version));
        assertEquals("openjdk:latest", JdkDistribution.OPENJDK.getDockerImage(version));
    }

    @Test
    void testGetDockerImageWithNullVersion() {
        assertEquals("eclipse-temurin:latest", JdkDistribution.TEMURIN.getDockerImage(null));
        assertEquals("amazoncorretto:latest", JdkDistribution.CORRETTO.getDockerImage(null));
        assertEquals("azul/zulu-openjdk:latest", JdkDistribution.ZULU.getDockerImage(null));
        assertEquals("openjdk:latest", JdkDistribution.OPENJDK.getDockerImage(null));
    }

    @Test
    void testStaticGetDockerImageWithMavenTemurin() {
        Optional<String> result = JdkDistribution.getDockerImage(
                JdkDistribution.TEMURIN,
                Optional.of("21"),
                Optional.of(BuildTool.MAVEN));
        assertTrue(result.isPresent());
        assertEquals("maven:3.9.11-eclipse-temurin-21", result.get());
    }

    @Test
    void testStaticGetDockerImageWithMavenOpenJdk() {
        Optional<String> result = JdkDistribution.getDockerImage(
                JdkDistribution.OPENJDK,
                Optional.of("17"),
                Optional.of(BuildTool.MAVEN));
        assertTrue(result.isPresent());
        assertEquals("maven:3.9.11-openjdk-17", result.get());
    }

    @Test
    void testStaticGetDockerImageWithMavenNoVersion() {
        Optional<String> result = JdkDistribution.getDockerImage(
                JdkDistribution.TEMURIN,
                Optional.empty(),
                Optional.of(BuildTool.MAVEN));
        assertTrue(result.isPresent());
        assertEquals("maven:eclipse-temurin", result.get());
    }

    @Test
    void testStaticGetDockerImageWithMavenLatestVersion() {
        Optional<String> result = JdkDistribution.getDockerImage(
                JdkDistribution.OPENJDK,
                Optional.of("latest"),
                Optional.of(BuildTool.MAVEN));
        assertTrue(result.isPresent());
        assertEquals("maven:latest", result.get());
    }

    @Test
    void testStaticGetDockerImageWithMavenUnsupportedDistribution() {
        Optional<String> result = JdkDistribution.getDockerImage(
                JdkDistribution.CORRETTO,
                Optional.of("21"),
                Optional.of(BuildTool.MAVEN));
        assertFalse(result.isPresent());
    }

    @Test
    void testStaticGetDockerImageWithGradle() {
        Optional<String> result = JdkDistribution.getDockerImage(
                JdkDistribution.TEMURIN,
                Optional.of("21"),
                Optional.of(BuildTool.GRADLE));
        assertTrue(result.isPresent());
        assertEquals("gradle:8.7-jdk21", result.get());
    }

    @Test
    void testStaticGetDockerImageWithGradleNoVersion() {
        Optional<String> result = JdkDistribution.getDockerImage(
                JdkDistribution.ZULU,
                Optional.empty(),
                Optional.of(BuildTool.GRADLE));
        assertTrue(result.isPresent());
        assertEquals("gradle:latest", result.get());
    }

    @Test
    void testStaticGetDockerImageWithNoBuildTool() {
        Optional<String> result = JdkDistribution.getDockerImage(
                JdkDistribution.TEMURIN,
                Optional.of("21"),
                Optional.empty());
        assertTrue(result.isPresent());
        assertEquals("eclipse-temurin:21-jdk", result.get());
    }

    @Test
    void testStaticGetDockerImageWithBlankVersion() {
        Optional<String> result = JdkDistribution.getDockerImage(
                JdkDistribution.TEMURIN,
                Optional.of("  "),
                Optional.of(BuildTool.MAVEN));
        assertTrue(result.isPresent());
        assertEquals("maven:eclipse-temurin", result.get());
    }
}
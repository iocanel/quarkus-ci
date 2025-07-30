# Quarkus CI

A comprehensive Quarkus extension and CLI tool for generating and running CI/CD workflows for GitHub Actions and GitLab CI.

## Overview

Quarkus CI provides automated CI/CD workflow generation and local execution capabilities for Java projects. It detects your project structure (Maven/Gradle) and generates appropriate workflow configurations with sensible defaults while supporting extensive customization.

### Key Features

- **GitHub Actions Integration**: Generate workflow files with Java version detection, build tool wrapper support, and dependency caching
- **GitLab CI Integration**: Generate GitLab CI pipeline configurations optimized for Java projects
- **Local Workflow Execution**: Run GitHub Actions and GitLab CI pipelines locally using containerized environments
- **Smart Project Detection**: Automatically detects Maven/Gradle projects and configures appropriate build commands
- **Flexible Configuration**: Supports custom runners, JDK distributions, and build configurations
- **Command-Line Interface**: Standalone CLI tool for workflow generation and execution

## Architecture

The project consists of several Maven modules:

### Core Modules
- **`common/`** - Shared utilities and project detection logic
- **`cli/`** - PicoCLI-based command-line interface
- **`extensions/github/`** - Quarkus extension for GitHub Actions
- **`extensions/gitlab/`** - Quarkus extension for GitLab CI
- **`integration-tests/`** - End-to-end integration tests
- **`docs/`** - Antora-based documentation

### Extension Structure
Each extension follows the standard Quarkus extension pattern:
- **`spi/`** - Service Provider Interface with build items
- **`deployment/`** - Build-time processing and workflow generation
- **`runtime/`** - Runtime components (minimal for build-time extensions)

## Quick Start

### Prerequisites
- Java 17 or higher
- Maven 3.6+ or Gradle 6+

### Installation

Add the extension to your Quarkus project:

```xml
<dependency>
    <groupId>io.quarkiverse.ci</groupId>
    <artifactId>quarkus-ci-github</artifactId>
    <version>${quarkus-ci.version}</version>
</dependency>
```

Or for GitLab CI:

```xml
<dependency>
    <groupId>io.quarkiverse.ci</groupId>
    <artifactId>quarkus-ci-gitlab</artifactId>
    <version>${quarkus-ci.version}</version>
</dependency>
```

### CLI Usage

Build the CLI tool:

```bash
mvn clean package
cd cli && mvn quarkus:build
```

Generate a GitHub Actions workflow:

```bash
java -jar cli/target/quarkus-ci-cli-${quarkus-ci.version}.jar ci github generate-workflow
```

Generate a GitLab CI pipeline:

```bash
java -jar cli/target/quarkus-ci-cli-${quarkus-ci.version}.jar ci gitlab generate-pipeline
```

Run workflows locally:

```bash
# Run GitHub Actions locally using act
java -jar cli/target/quarkus-ci-cli-${quarkus-ci.version}.jar ci github run-workflow

# Run GitLab CI locally using gitlab-ci-local
java -jar cli/target/quarkus-ci-cli-${quarkus-ci.version}.jar ci gitlab run-pipeline
```

## Development

### Building the Project

```bash
# Build all modules
mvn clean package

# Build without tests
mvn clean package -DskipTests

# Build and install to local repository
mvn clean install

# Build native executable
mvn package -Pnative
```

### Running Tests

```bash
# Unit tests
mvn test

# Integration tests
mvn verify

# Integration tests only
mvn failsafe:integration-test
```

### Development with Make

The project includes a Makefile for common operations:

```bash
# Build the project
make build

# Build container images
make images

# Build everything
make all

# Clean build artifacts
make clean
```

## Configuration Options

### GitHub Actions Configuration

The GitHub extension supports various configuration options:

- **Java Version Detection**: Automatically detects Java version from project configuration
- **Build Tool Support**: Supports both Maven and Gradle with wrapper detection
- **Runner Configuration**: Configurable GitHub Actions runners
- **JDK Distribution**: Support for different JDK distributions (Temurin, Zulu, etc.)
- **Caching**: Automatic dependency caching configuration

### GitLab CI Configuration

The GitLab extension provides:

- **Pipeline Templates**: Pre-configured pipeline stages for Java projects
- **Docker Integration**: Container-based build environments
- **Artifact Management**: Automatic artifact handling
- **Cache Configuration**: Dependency and build caching

## Local Execution

### GitHub Actions with Act

The CLI provides integration with [act](https://github.com/nektos/act) for local GitHub Actions execution:

```bash
# Generate and run workflow locally
java -jar cli/target/quarkus-ci-cli-${quarkus-ci.version}.jar ci github run-workflow
```

### GitLab CI with gitlab-ci-local

Local GitLab CI execution using [gitlab-ci-local](https://github.com/firecow/gitlab-ci-local):

```bash
# Generate and run pipeline locally
java -jar cli/target/quarkus-ci-cli-${quarkus-ci.version}.jar ci gitlab run-pipeline
```

## Contributing

### Project Structure

- Follow existing code conventions and patterns
- Use the established module structure for new features
- Ensure proper test coverage for new functionality
- Update documentation for user-facing changes

### Development Commands

See the `CLAUDE.md` file for detailed development commands and project-specific guidance.

## License

This project is licensed under the Apache License 2.0 - see the [LICENSE](LICENSE) file for details.

## Resources

- [Quarkus Extensions Guide](https://quarkus.io/guides/writing-extensions)
- [GitHub Actions Documentation](https://docs.github.com/en/actions)
- [GitLab CI/CD Documentation](https://docs.gitlab.com/ee/ci/)
- [Quarkiverse](https://github.com/quarkiverse)

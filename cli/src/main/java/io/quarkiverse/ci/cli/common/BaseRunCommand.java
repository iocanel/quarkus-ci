package io.quarkiverse.ci.cli.common;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;
import java.util.List;
import java.util.Map;

import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.api.command.CreateContainerCmd;
import com.github.dockerjava.api.exception.DockerException;
import com.github.dockerjava.api.exception.NotFoundException;
import com.github.dockerjava.api.model.Bind;
import com.github.dockerjava.api.model.Frame;
import com.github.dockerjava.api.model.HostConfig;
import com.github.dockerjava.api.model.Volume;
import com.github.dockerjava.core.DefaultDockerClientConfig;
import com.github.dockerjava.core.DockerClientBuilder;
import com.github.dockerjava.core.command.LogContainerResultCallback;
import com.github.dockerjava.core.command.WaitContainerResultCallback;
import com.github.dockerjava.httpclient5.ApacheDockerHttpClient;

import picocli.CommandLine.ExitCode;
import picocli.CommandLine.Option;

public abstract class BaseRunCommand {

    @Option(names = { "--use-docker" }, description = "Force using Docker even if gitlab-ci-local binary is available in PATH.")
    public boolean forceDocker = false;

    public abstract String getContainerName();

    public abstract String getContainerImage();

    protected Integer runInDocker(Path projectRoot, List<String> command) {
        try (DockerClient dockerClient = createDockerClient()) {
            dockerClient.pingCmd().exec();

            removeExistingContainer(dockerClient, getContainerName());

            Volume workspace = new Volume("/input");
            Volume dockerSock = new Volume("/var/run/docker.sock");

            CreateContainerCmd createCmd = dockerClient.createContainerCmd(getContainerImage())
                    .withName(getContainerName())
                    .withCmd(command)
                    .withWorkingDir("/workspace")
                    .withTty(true)
                    .withStdinOpen(true)
                    .withAttachStdin(true)
                    .withAttachStdout(true)
                    .withAttachStderr(true)
                    .withHostConfig(HostConfig.newHostConfig()
                            .withPrivileged(true)
                            .withUsernsMode("host")
                            .withAutoRemove(true)
                            .withTmpFs(Map.of("/workspace", "rw"))
                            .withBinds(
                                    new Bind("/var/run/docker.sock", dockerSock),
                                    new Bind(projectRoot.toString(), workspace)));

            String containerId = createCmd.exec().getId();

            dockerClient.startContainerCmd(containerId).exec();
            dockerClient.attachContainerCmd(containerId)
                    .withStdIn(System.in)
                    .withStdOut(true)
                    .withStdErr(true)
                    .withFollowStream(true)
                    .withLogs(true)
                    .exec(new LogContainerResultCallback() {
                        @Override
                        public void onNext(Frame item) {
                            System.out.write(item.getPayload(), 0, item.getPayload().length);
                            System.out.flush();
                        }
                    });

            return dockerClient.waitContainerCmd(containerId).exec(new WaitContainerResultCallback()).awaitStatusCode();
        } catch (Exception e) {
            System.err.println("Docker error: " + e.getMessage());
            return ExitCode.SOFTWARE;
        }
    }

    public static Path getWorkingDirectory() {
        return Paths.get(System.getProperty("user.dir"));
    }

    public static DockerClient createDockerClient() {
        DefaultDockerClientConfig config = DefaultDockerClientConfig.createDefaultConfigBuilder().build();
        ApacheDockerHttpClient httpClient = new ApacheDockerHttpClient.Builder()
                .dockerHost(config.getDockerHost())
                .sslConfig(config.getSSLConfig())
                .maxConnections(100)
                .connectionTimeout(Duration.ofSeconds(30))
                .responseTimeout(Duration.ofSeconds(45))
                .build();
        return DockerClientBuilder.getInstance(config).withDockerHttpClient(httpClient).build();
    }

    public static void removeExistingContainer(DockerClient dockerClient, String containerName) {
        try {
            dockerClient.inspectContainerCmd(containerName).exec();
            // Container exists, remove it
            dockerClient.removeContainerCmd(containerName).withForce(true).exec();
            System.out.println("Removed existing container: " + containerName);
        } catch (NotFoundException e) {
            // Container doesn't exist, nothing to do
        } catch (DockerException e) {
            System.err.println("Error removing container: " + e.getMessage());
        }
    }
}

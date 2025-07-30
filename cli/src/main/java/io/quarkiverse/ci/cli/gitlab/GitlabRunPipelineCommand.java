package io.quarkiverse.ci.cli.gitlab;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.Callable;

import io.quarkiverse.ci.cli.common.BaseRunCommand;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;

@Command(name = "run-pipeline", sortOptions = false, mixinStandardHelpOptions = false, header = "Run GitLab CI pipeline locally using gitlab-ci-local.", headerHeading = "%n", commandListHeading = "%nCommands:%n", synopsisHeading = "%nUsage: ", optionListHeading = "%nOptions:%n")
public class GitlabRunPipelineCommand extends BaseRunCommand implements Callable<Integer> {

    @Parameters(arity = "0..*", paramLabel = "JOB_NAME", description = "Job name to run (default: runs all jobs)")
    List<String> jobNames = new ArrayList<>();

    @Option(names = { "-h", "--help" }, usageHelp = true, description = "Display this help message.")
    public boolean help;

    @Override
    public Integer call() throws Exception {
        Path projectRoot = findProjectRoot();

        if (!forceDocker) {
            // Try to use gitlab-ci-local from PATH first
            Optional<String> gitlabCiLocalPath = findGitlabCiLocalInPath();
            if (gitlabCiLocalPath.isPresent()) {
                System.out.println("Using gitlab-ci-local from PATH: " + gitlabCiLocalPath.get());
                return runGitlabCiLocalFromPath(gitlabCiLocalPath.get(), projectRoot);
            }
        }

        // Fallback to Docker
        System.out.println("Using gitlab-ci-local via Docker...");
        return runGitlabCiLocalViaDocker(projectRoot);
    }

    private Path findProjectRoot() {
        Path current = getWorkingDirectory();
        while (current != null && !current.getRoot().equals(current)) {
            if (current.resolve(".git").toFile().exists()) {
                return current;
            }
            current = current.getParent();
        }
        throw new RuntimeException("Could not find project root (.git directory not found)");
    }

    private Optional<String> findGitlabCiLocalInPath() {
        String pathEnv = System.getenv("PATH");
        if (pathEnv == null) {
            return Optional.empty();
        }

        String[] pathDirs = pathEnv.split(System.getProperty("path.separator"));
        for (String pathDir : pathDirs) {
            Path gitlabCiLocalPath = Paths.get(pathDir, "gitlab-ci-local");
            if (gitlabCiLocalPath.toFile().exists() && gitlabCiLocalPath.toFile().canExecute()) {
                return Optional.of(gitlabCiLocalPath.toString());
            }
            // Also check for gitlab-ci-local.exe on Windows (if it exists)
            Path gitlabCiLocalExePath = Paths.get(pathDir, "gitlab-ci-local.exe");
            if (gitlabCiLocalExePath.toFile().exists() && gitlabCiLocalExePath.toFile().canExecute()) {
                return Optional.of(gitlabCiLocalExePath.toString());
            }
        }
        return Optional.empty();
    }

    private Integer runGitlabCiLocalFromPath(String gitlabCiLocalPath, Path projectRoot)
            throws IOException, InterruptedException {
        List<String> command = new ArrayList<>();
        command.add(gitlabCiLocalPath);

        // Add job names if specified
        if (!jobNames.isEmpty()) {
            for (String jobName : jobNames) {
                command.add("--job");
                command.add(jobName);
            }
        }

        ProcessBuilder pb = new ProcessBuilder(command);
        pb.directory(projectRoot.toFile());
        pb.inheritIO();

        Process process = pb.start();
        return process.waitFor();
    }

    private Integer runGitlabCiLocalViaDocker(Path projectRoot) {
        System.out.println("Running gitlab-ci-local in Docker container...");
        return runGitlabCiLocalJobViaDocker(projectRoot);
    }

    private Integer runGitlabCiLocalJobViaDocker(Path projectRoot) {
        List<String> command = new ArrayList<>();
        command.add("gitlab-ci-local");

        if (!jobNames.isEmpty()) {
            for (String jobName : jobNames) {
                command.add("--job");
                command.add(jobName);
            }
        }
        return runInDocker(projectRoot, command);
    }

    @Override
    public String getContainerName() {
        return "quarkus-ci-gitlab-ci-local-runner";
    }

    @Override
    public String getContainerImage() {
        return "quarkus-ci-gitlab-ci-local:latest";
    }
}

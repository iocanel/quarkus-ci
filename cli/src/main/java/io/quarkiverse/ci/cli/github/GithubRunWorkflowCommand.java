package io.quarkiverse.ci.cli.github;

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

@Command(name = "run-workflow", sortOptions = false, mixinStandardHelpOptions = false, header = "Run GitHub Action workflow locally using act.", headerHeading = "%n", commandListHeading = "%nCommands:%n", synopsisHeading = "%nUsage: ", optionListHeading = "%nOptions:%n")
public class GithubRunWorkflowCommand extends BaseRunCommand implements Callable<Integer> {

    @Parameters(arity = "0..*", paramLabel = "ACT_ARGS", description = "Arguments to pass to act (e.g., -l, -n, -W workflow.yml)")
    List<String> actArgs = List.of("-j", "build");

    @Option(names = { "-h", "--help" }, usageHelp = true, description = "Display this help message.")
    public boolean help;

    @Override
    public String getContainerName() {
        return "quarkus-ci-act-runner";
    }

    @Override
    public String getContainerImage() {
        return "quarkus-ci-act:latest";
    }

    @Override
    public Integer call() throws Exception {
        Path projectRoot = findProjectRoot();

        if (!forceDocker) {
            // Try to use act from PATH first
            Optional<String> actPath = findActInPath();
            if (actPath.isPresent()) {
                System.out.println("Using act from PATH: " + actPath.get());
                return runActFromPath(actPath.get(), projectRoot);
            }
        }

        // Fallback to Docker
        System.out.println("Using act via Docker...");
        return runActViaDocker(projectRoot);
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

    private Optional<String> findActInPath() {
        String pathEnv = System.getenv("PATH");
        if (pathEnv == null) {
            return Optional.empty();
        }

        String[] pathDirs = pathEnv.split(System.getProperty("path.separator"));
        for (String pathDir : pathDirs) {
            Path actPath = Paths.get(pathDir, "act");
            if (actPath.toFile().exists() && actPath.toFile().canExecute()) {
                return Optional.of(actPath.toString());
            }
            // Also check for act.exe on Windows
            Path actExePath = Paths.get(pathDir, "act.exe");
            if (actExePath.toFile().exists() && actExePath.toFile().canExecute()) {
                return Optional.of(actExePath.toString());
            }
        }
        return Optional.empty();
    }

    private Integer runActFromPath(String actPath, Path projectRoot) throws IOException, InterruptedException {
        List<String> command = new ArrayList<>();
        command.add(actPath);
        command.addAll(actArgs);

        ProcessBuilder pb = new ProcessBuilder(command);
        pb.directory(projectRoot.toFile());
        pb.inheritIO();

        Process process = pb.start();
        return process.waitFor();
    }

    private Integer runActViaDocker(Path projectRoot) {
        // Prepare command
        List<String> command = new ArrayList<>();
        command.add("act");
        command.addAll(actArgs);
        return runInDocker(projectRoot, command);
    }
}

package io.quarkiverse.ci.cli;

import java.util.concurrent.Callable;

import io.quarkiverse.ci.cli.github.GithubGenerateWorkflowCommand;
import io.quarkiverse.ci.cli.github.GithubRunWorkflowCommand;
import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Model.CommandSpec;
import picocli.CommandLine.Option;
import picocli.CommandLine.Spec;

@Command(name = "github", header = "Github CLI", subcommands = {
        GithubGenerateWorkflowCommand.class,
        GithubRunWorkflowCommand.class,
})
public class GithubCommand implements Callable<Integer> {

    @Spec
    CommandSpec spec;

    @Option(names = { "-h", "--help" }, usageHelp = true, description = "Display this help message.")
    public boolean help;

    public Integer call() throws Exception {
        CommandLine generate = spec.subcommands().get("generate-workflow");
        return generate.execute();
    }
}

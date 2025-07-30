package io.quarkiverse.ci.cli;

import java.util.concurrent.Callable;

import io.quarkiverse.ci.cli.gitlab.GitlabGeneratePipelineCommand;
import io.quarkiverse.ci.cli.gitlab.GitlabRunPipelineCommand;
import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Model.CommandSpec;
import picocli.CommandLine.Option;
import picocli.CommandLine.Spec;

@Command(name = "gitlab", header = "GitLab CLI", subcommands = {
        GitlabGeneratePipelineCommand.class,
        GitlabRunPipelineCommand.class,
})
public class GitlabCommand implements Callable<Integer> {

    @Spec
    CommandSpec spec;

    @Option(names = { "-h", "--help" }, usageHelp = true, description = "Display this help message.")
    public boolean help;

    public Integer call() throws Exception {
        CommandLine generate = spec.subcommands().get("generate-pipeline");
        return generate.execute();
    }
}
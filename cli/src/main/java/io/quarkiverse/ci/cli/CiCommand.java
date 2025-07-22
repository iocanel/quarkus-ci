package io.quarkiverse.ci.cli;

import java.util.concurrent.Callable;

import io.quarkus.picocli.runtime.annotations.TopCommand;
import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Model.CommandSpec;
import picocli.CommandLine.Option;
import picocli.CommandLine.Spec;

@TopCommand
@Command(name = "ci", header = "CI CLI", subcommands = {
        GithubCommand.class,
})
public class CiCommand implements Callable<Integer> {

    @Spec
    CommandSpec spec;

    @Option(names = { "-h", "--help" }, usageHelp = true, description = "Display this help message.")
    public boolean help;

    public Integer call() throws Exception {
        CommandLine generate = spec.subcommands().get("generate");
        return generate.execute();
    }
}

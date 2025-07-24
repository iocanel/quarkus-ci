package io.quarkiverse.ci.cli.gitlab;

import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

import io.quarkiverse.ci.gitlab.spi.GeneratedGitlabCiResourceBuildItem;
import io.quarkus.builder.BuildResult;

public class GitlabGeneratePipelineHandler implements BiConsumer<Object, BuildResult> {

    @Override
    public void accept(Object context, BuildResult buildResult) {
        List<GeneratedGitlabCiResourceBuildItem> pipelines = buildResult
                .consumeMulti(GeneratedGitlabCiResourceBuildItem.class);
        Consumer<List<GeneratedGitlabCiResourceBuildItem>> consumer = (Consumer<List<GeneratedGitlabCiResourceBuildItem>>) context;
        consumer.accept(pipelines);
    }
}
package io.quarkiverse.ci.gitlab.spi;

import io.quarkus.builder.item.MultiBuildItem;

public final class GeneratedGitlabCiResourceBuildItem extends MultiBuildItem {

    private final String name;
    private final String content;

    public GeneratedGitlabCiResourceBuildItem(String name, String content) {
        this.name = name;
        this.content = content;
    }

    public String getName() {
        return name;
    }

    public String getContent() {
        return content;
    }
}
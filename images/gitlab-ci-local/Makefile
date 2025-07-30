# Makefile for building GitLab Runner Docker image with different targets

IMAGE_NAME := quarkus-ci-gitlab-ci-local
TAG := latest
FULL_IMAGE := $(IMAGE_NAME):$(TAG)
PLATFORMS := linux/amd64,linux/arm64
BUILDER_NAME := multiplatform-builder

.PHONY: help build build-multiplatform export-tar export-oci clean setup-builder

help: ## Show this help message
	@echo "Available targets:"
	@grep -E '^[a-zA-Z_-]+:.*?## .*$$' $(MAKEFILE_LIST) | awk 'BEGIN {FS = ":.*?## "}; {printf "  %-20s %s\n", $$1, $$2}'

setup-builder: ## Create and setup multiplatform builder
	@if ! docker buildx inspect $(BUILDER_NAME) >/dev/null 2>&1; then \
		echo "Creating multiplatform builder..."; \
		docker buildx create --name $(BUILDER_NAME) --use; \
	else \
		echo "Using existing builder: $(BUILDER_NAME)"; \
		docker buildx use $(BUILDER_NAME); \
	fi

build: ## Build image for current platform and load locally
	docker buildx build --tag $(FULL_IMAGE) --load .

build-multiplatform: setup-builder ## Build multiplatform image (stored in cache)
	docker buildx build --platform $(PLATFORMS) --tag $(FULL_IMAGE) .

export-tar: setup-builder ## Export multiplatform image as tar.gz
	@echo "Exporting multiplatform image to tar..."
	@mkdir -p output
	docker buildx build --platform $(PLATFORMS) --output type=tar,dest=output/$(IMAGE_NAME)-multiplatform.tar .
	@echo "Compressing tar file..."
	gzip -f output/$(IMAGE_NAME)-multiplatform.tar
	@echo "Exported to: output/$(IMAGE_NAME)-multiplatform.tar.gz"

clean: ## Clean up output directory and remove builder
	@echo "Cleaning up..."
	@rm -rf output/
	@if docker buildx inspect $(BUILDER_NAME) >/dev/null 2>&1; then \
		docker buildx rm $(BUILDER_NAME); \
	fi

.DEFAULT_GOAL := help

# Makefile for building act Docker image with different targets

IMAGE_NAME := quarkus-ci-act
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

build-and-load: setup-builder ## Build multiplatform + load current platform locally
	@echo "Building multiplatform image..."
	docker buildx build --platform $(PLATFORMS) --tag $(FULL_IMAGE) .
	@echo "Building and loading current platform..."
	docker buildx build --tag $(FULL_IMAGE) --load .

export-tar: setup-builder ## Export multiplatform image as tar.gz
	@echo "Exporting multiplatform image to tar..."
	@mkdir -p output
	docker buildx build --platform $(PLATFORMS) --output type=tar,dest=output/$(IMAGE_NAME)-multiplatform.tar .
	@echo "Compressing tar file..."
	gzip -f output/$(IMAGE_NAME)-multiplatform.tar
	@echo "Exported to: output/$(IMAGE_NAME)-multiplatform.tar.gz"

export-oci: setup-builder ## Export multiplatform image as OCI layout
	@echo "Exporting multiplatform image as OCI layout..."
	@mkdir -p output
	docker buildx build --platform $(PLATFORMS) --output type=oci,dest=output/$(IMAGE_NAME)-oci .
	@echo "Exported to: output/$(IMAGE_NAME)-oci/"

import-tar: ## Import tar.gz image (specify FILE=path/to/file.tar.gz)
	@if [ -z "$(FILE)" ]; then \
		echo "Usage: make import-tar FILE=path/to/file.tar.gz"; \
		exit 1; \
	fi
	@echo "Importing $(FILE)..."
	gunzip -c $(FILE) | docker load

clean: ## Clean up output directory and remove builder
	@echo "Cleaning up..."
	@rm -rf output/
	@if docker buildx inspect $(BUILDER_NAME) >/dev/null 2>&1; then \
		docker buildx rm $(BUILDER_NAME); \
	fi

run: ## Run act with default arguments
	@../../bin/act

run-list: ## List available workflows using act
	@../../bin/act -l

# Development targets
dev-build: build ## Alias for build (development)

dev-run: build run ## Build and run for development

.DEFAULT_GOAL := help
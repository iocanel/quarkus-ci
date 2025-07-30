FROM docker:27-dind

# Install system dependencies
RUN apk add --no-cache \
    bash \
    curl \
    git \
    make \
    ca-certificates

# Install Node.js and npm for gitlab-ci-local
RUN apk add --no-cache nodejs npm rsync

# Install gitlab-ci-local
RUN npm install -g gitlab-ci-local

# Copy entrypoint script
COPY entrypoint.sh /usr/local/bin/entrypoint.sh
RUN chmod +x /usr/local/bin/entrypoint.sh

# Create working directories
RUN mkdir -p /input /workspace
WORKDIR /workspace

EXPOSE 2376

ENTRYPOINT ["/usr/local/bin/entrypoint.sh"]
CMD ["gitlab-ci-local"]

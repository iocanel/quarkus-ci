FROM docker:27-dind

# Install system dependencies
RUN apk add --no-cache \
    bash \
    curl \
    git \
    make \
    nodejs \
    npm \
    jq \
    ca-certificates \
    rsync

# Install act
RUN curl -q https://raw.githubusercontent.com/nektos/act/master/install.sh | bash

# Create working directories
RUN mkdir -p /input /workspace
WORKDIR /workspace

# Copy entrypoint script
COPY entrypoint.sh /usr/local/bin/entrypoint.sh
RUN chmod +x /usr/local/bin/entrypoint.sh

# Expose Docker daemon port
EXPOSE 2376

ENTRYPOINT ["/usr/local/bin/entrypoint.sh"]
CMD ["act"]

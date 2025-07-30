#!/bin/bash

# Copy input files into writable /workspace
rsync -a /input/ /workspace/

# Execute the command passed to the container
exec "$@"

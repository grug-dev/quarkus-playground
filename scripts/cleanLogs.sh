#!/bin/bash

# Absolute path of the script's directory
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"

# Current working directory (where the user executed the script)
CURRENT_DIR="$(pwd)"

echo "Script is located in: $SCRIPT_DIR"
echo "Script was executed from: $CURRENT_DIR"

# Logs directory (parent of the script directory)
LOGS_DIR="$SCRIPT_DIR/../logs"

echo "Clearing logs at: $LOGS_DIR"
rm -rf "$LOGS_DIR"/*
echo "The logs directory has been cleared."


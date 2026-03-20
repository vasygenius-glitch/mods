#!/bin/bash

echo "Starting build process for Xaero Addon..."
cd addon
./gradlew build
cp build/libs/xaero-world-map-addon-1.0.0.jar ../xaero-world-map-addon-1.0.0.jar
echo "Build complete! Check xaero-world-map-addon-1.0.0.jar in the root folder."

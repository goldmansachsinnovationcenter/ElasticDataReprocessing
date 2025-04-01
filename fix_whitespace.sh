#!/bin/bash
find src -name "*.java" -type f -exec sed -i 's/[ \t]*$//' {} \;

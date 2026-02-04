#!/bin/bash

OUTPUT_FILE="all_kotlin_files.kt"

# Clear old output if exists
> "$OUTPUT_FILE"

echo "Combining Kotlin files into $OUTPUT_FILE ..."

find . -name "*.kt" ! -path "*/build/*" | while read file; do
  echo "===== FILE: $file =====" >> "$OUTPUT_FILE"
  cat "$file" >> "$OUTPUT_FILE"
  echo -e "\n\n" >> "$OUTPUT_FILE"
done

echo "Done! Output file: $OUTPUT_FILE"
#!/usr/bin/env bash
set -e

DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
cd "$DIR"

export JAVA_HOME="/Applications/Android Studio.app/Contents/jbr/Contents/Home"
export PATH="$JAVA_HOME/bin:/Applications/Android Studio.app/Contents/plugins/Kotlin/kotlinc/bin:$PATH"

echo "=========================================="
echo "Compiling and running all Lab 11 tasks..."
echo "=========================================="

mkdir -p out

tasks=("task01" "task02" "task03" "task03b" "task04" "task05" "task06" "task07" "task08" "task09" "task10" "task11" "task12" "task13" "task14" "task15")

for task in "${tasks[@]}"; do
    echo ""
    echo "------------------------------------------"
    echo "Running $task..."
    echo "------------------------------------------"
    kotlinc "src/main/kotlin/$task/Main.kt" -include-runtime -d "out/$task.jar"
    java -jar "out/$task.jar"
done

echo ""
echo "=========================================="
echo "All Lab 11 tasks ran successfully!"
echo "=========================================="


#!/bin/sh
# Сборка и запуск (нужен JDK 17+). Запуск: ./run.sh
set -e
mkdir -p out
javac -encoding UTF-8 -d out $(find src -name "*.java")
java -Dfile.encoding=UTF-8 -cp out center.Main

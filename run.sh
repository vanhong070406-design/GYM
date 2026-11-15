#!/usr/bin/env bash
# Ban macOS/Linux cua run.bat: bien dich lai roi chay ung dung.
set -euo pipefail
cd "$(dirname "$0")"

echo "[1/2] Bien dich ma nguon vao thu muc bin..."
rm -rf bin && mkdir -p bin
find src/main/java -name '*.java' > sources.txt
trap 'rm -f sources.txt' EXIT
javac -encoding UTF-8 -d bin @sources.txt

echo "[2/2] Khoi chay ung dung..."
java -Dfile.encoding=UTF-8 -cp "bin:lib/*" com.gymmanagement.Main

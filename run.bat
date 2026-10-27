@echo off
chcp 65001 >nul
if not exist out mkdir out
dir /s /b src\*.java > sources.txt
javac -encoding UTF-8 -d out @sources.txt
del sources.txt
java -Dfile.encoding=UTF-8 -cp out center.Main
pause

echo off
cd /D "%~dp0"
cd target
echo Current working directory: %cd%
java -jar server_sent_event-1.0.0-SNAPSHOT-fat.jar

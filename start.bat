@echo off
cd /d "%~dp0"
if not exist MainRaft.jar (javac MainRaft.java TextureGenerator.java NPC.java Animal.java Logger.java && jar cvfe MainRaft.jar MainRaft *.class)
java -jar MainRaft.jar
pause

#!/bin/bash
cd "$(dirname "$0")"
javac MainRaft.java TextureGenerator.java NPC.java Animal.java Logger.java && jar cvfe MainRaft.jar MainRaft *.class && java -jar MainRaft.jar

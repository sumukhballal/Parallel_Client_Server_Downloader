#!/bin/bash

# Kill all existing processes
sudo kill -9 $(ps aux | grep java | awk '{print $2}')
# Start Indexing server

cd indexserver
java -cp ./target main.IndexingServer &
cd ..

sleep 10
cd p2p3
java -cp ./target main.P2P 1 0 &
cd ..
sleep 10
cd p2p2
java -cp ./target main.P2P 1 1 file_3.txt &
cd ..
sleep 10
cd p2p1
java -cp ./target main.P2P 1 1 file_2.txt &
cd ..

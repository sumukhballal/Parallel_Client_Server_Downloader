#!/bin/bash

# Kill all existing processes
sudo kill -9 $(ps aux | grep java | awk '{print $2}')
# Start Indexing server

cd indexserver
java -cp ./target main.IndexingServer &
cd ..

sleep 10

cd p2p1
java -cp ./target main.P2P 1 0 &
cd ..
sleep 10
cd p2p2
java -cp ./target main.P2P 1 2 file_1.txt,file_2.txt,file_3.txt,file_4.txt,file_5.txt,file_6.txt,file_7.txt,file_8.txt,file_9.txt,file10.txt &
cd ..

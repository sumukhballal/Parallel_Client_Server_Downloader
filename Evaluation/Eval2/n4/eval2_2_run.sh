#!/bin/bash

# Kill all existing processes
sudo kill -9 $(ps aux | grep java | awk '{print $2}')
# Start Indexing server

cd indexserver
java -cp ./target main.IndexingServer &
cd ..

sleep 10

for (( i = 1; i <= 3; i++));
do
cd p2p$i
java -cp ./target main.P2P 1 0 &
cd ..
done

sleep 100

cd p2p4
java -cp ./target main.P2P 1 2 file_1.txt,file_2.txt,file_3.txt,file_4.txt,file_5.txt,file_6.txt,file_7.txt,file_8.txt,file_9.txt,file10.txt &
cd ..

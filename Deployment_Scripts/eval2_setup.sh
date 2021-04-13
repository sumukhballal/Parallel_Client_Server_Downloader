#!/bin/bash

mkdir -p ../Evaluation/Eval2/

mkdir -p ../Evaluation/Eval2/n2 # where n=2
mkdir -p ../Evaluation/Eval2/n4 # where n=2
mkdir -p ../Evaluation/Eval2/n8 # where n=2
mkdir -p ../Evaluation/Eval2/n16 # where n=2



./replicate_index_server_folder.sh "../Indexing_Server" "../Evaluation/Eval2/n2"
./replicate_index_server_folder.sh "../Indexing_Server" "../Evaluation/Eval2/n4"
./replicate_index_server_folder.sh "../Indexing_Server" "../Evaluation/Eval2/n8"
./replicate_index_server_folder.sh "../Indexing_Server" "../Evaluation/Eval2/n16"

dd if=/dev/zero of="../Evaluation/Eval2/file.txt"  bs=1000000  count=1

port=8001
for ((i = 1 ; i <= 2 ; i++));
do
	./replicate_p2p_folders_2.sh "../Peer_Node" "../Evaluation/Eval2/n2" $i $port
	
	for ((j = 1; j <= 10; j++));
	do
		cp -r ../Evaluation/Eval2/file.txt ../Evaluation/Eval2/n2/p2p$i/files/file_$j.txt
	done
	port=$(($port+1))
	index=$(($index+1))
done

rm -rf ../Evaluation/Eval2/n2/p2p2/files/*

port=8001
for ((i = 1 ; i <= 4 ; i++));
do
	./replicate_p2p_folders_2.sh "../Peer_Node" "../Evaluation/Eval2/n4" $i $port
	for ((j = 1; j <= 10; j++));
	do
	    cp -r ../Evaluation/Eval2/file.txt ../Evaluation/Eval2/n4/p2p$i/files/file_$j.txt
	done
       	port=$(($port+1))
	index=$(($index+1))
done

rm -rf ../Evaluation/Eval2/n4/p2p4/files/*


port=8001
for ((i = 1 ; i <= 8 ; i++));
do
        ./replicate_p2p_folders_2.sh "../Peer_Node" "../Evaluation/Eval2/n8" $i $port
        for ((j = 1; j <= 10; j++));
        do
            cp -r ../Evaluation/Eval2/file.txt ../Evaluation/Eval2/n8/p2p$i/files/file_$j.txt
        done

	port=$(($port+1))
        index=$(($index+1))
done

rm -rf ../Evaluation/Eval2/n8/p2p8/files/*


port=8001
for ((i = 1 ; i <= 16 ; i++));
do
        ./replicate_p2p_folders_2.sh "../Peer_Node" "../Evaluation/Eval2/n16" $i $port
        for ((j = 1; j <= 10; j++));
        do
            cp -r ../Evaluation/Eval2/file.txt ../Evaluation/Eval2/n16/p2p$i/files/file_$j.txt
        done

	port=$(($port+1))
        index=$(($index+1))
done

rm -rf ../Evaluation/Eval2/n16/p2p16/files/*

# Compile all 

./compile_all.sh "../Evaluation/Eval2/n2"
./compile_all.sh "../Evaluation/Eval2/n4"
./compile_all.sh "../Evaluation/Eval2/n8"
./compile_all.sh "../Evaluation/Eval2/n16"



cp -r ./eval2_1_run.sh ../Evaluation/Eval2/n2
cp -r ./eval2_2_run.sh ../Evaluation/Eval2/n4
cp -r ./eval2_3_run.sh ../Evaluation/Eval2/n8
cp -r ./eval2_4_run.sh ../Evaluation/Eval2/n16

cp -r ./calculate_avg.sh ../Evaluation/Eval2/n2
cp -r ./calculate_avg.sh ../Evaluation/Eval2/n4
cp -r ./calculate_avg.sh ../Evaluation/Eval2/n8
cp -r ./calculate_avg.sh ../Evaluation/Eval2/n16


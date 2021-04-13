#!/bin/bash


file_directory=$1

avg_time=0
count=0
total_time=0

for i in $(cat $file_directory/logs/client.log | grep avg_download_time | awk '{print $4}');
do
	total_time=$((${total_time}+${i}))
	count=$((${count}+1))		
done

avg_time=$((${total_time}/${count}))
echo "$avg_time milliseconds"

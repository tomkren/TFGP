#!/bin/bash

mkdir -p sources


cd sources


if [ -d TFGP ] ; then
	echo "No need to clone 'TFGP'."
	cd TFGP
	git pull
	cd ..
else
	echo "Start clone 'TFGP' ..."
	git clone https://github.com/tomkren/TFGP.git
fi


if [ -d dag-evaluate ] ; then
	echo "No need to clone 'dag-evaluate'."
	cd dag-evaluate
	git pull
	cd ..
else
	echo "Start clone 'dag-evaluate' ..."
	git clone https://github.com/martinpilat/dag-evaluate
fi

cd ..

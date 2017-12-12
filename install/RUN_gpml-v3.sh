#!/bin/bash

# PREREQUISITIES: git, python3 as python, java8 as java


. config_unix.sh



echo "NCPUS = $NCPUS"
echo "CONFIG = $CONFIG"
echo

./scripts/download_or_update_sources.sh


mkdir -p logs
mkdir -p logs/TFGP
mkdir -p logs/dag-evaluate

 
mkdir -p rundir
cd rundir

cp -a ../sources/TFGP/release/v3/. TFGP
cp -a ../sources/dag-evaluate/. dag-evaluate
 
cp -a ../datasets/. dag-evaluate/data
 
cp ../$CONFIG TFGP/$CONFIG
cp ../$CONFIG dag-evaluate/$CONFIG

 
 
cd dag-evaluate
python3 xmlrpc_interface.py ../../logs/dag-evaluate $NCPUS $CONFIG &> ../../logs/evaluate.log &
cd ..

cd TFGP
java -jar gpml.jar $CONFIG ../../logs/TFGP &> ../../logs/gp.log
cd ..

 
cd dag-evaluate
rm -rf cache
cd ..




cd ..


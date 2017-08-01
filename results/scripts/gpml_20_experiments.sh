#!/bin/bash

qsub -l walltime=5:30:0 -l select=1:ncpus=16:mem=48gb:scratch_local=128gb -v NCPUS=15,CONFIG=wilt-1.json -N wilt1 start_task.sh
qsub -l walltime=5:30:0 -l select=1:ncpus=16:mem=48gb:scratch_local=128gb -v NCPUS=15,CONFIG=wilt-2.json -N wilt2 start_task.sh
qsub -l walltime=5:30:0 -l select=1:ncpus=16:mem=48gb:scratch_local=128gb -v NCPUS=15,CONFIG=wilt-3.json -N wilt3 start_task.sh
qsub -l walltime=5:30:0 -l select=1:ncpus=16:mem=48gb:scratch_local=128gb -v NCPUS=15,CONFIG=wilt-4.json -N wilt4 start_task.sh
qsub -l walltime=5:30:0 -l select=1:ncpus=16:mem=48gb:scratch_local=128gb -v NCPUS=15,CONFIG=wilt-5.json -N wilt5 start_task.sh

qsub -l walltime=10:30:0 -l select=1:ncpus=16:mem=48gb:scratch_local=128gb -v NCPUS=15,CONFIG=wine-1.json -N wine1 start_task.sh
qsub -l walltime=10:30:0 -l select=1:ncpus=16:mem=48gb:scratch_local=128gb -v NCPUS=15,CONFIG=wine-2.json -N wine2 start_task.sh
qsub -l walltime=10:30:0 -l select=1:ncpus=16:mem=48gb:scratch_local=128gb -v NCPUS=15,CONFIG=wine-3.json -N wine3 start_task.sh
qsub -l walltime=10:30:0 -l select=1:ncpus=16:mem=48gb:scratch_local=128gb -v NCPUS=15,CONFIG=wine-4.json -N wine4 start_task.sh
qsub -l walltime=10:30:0 -l select=1:ncpus=16:mem=48gb:scratch_local=128gb -v NCPUS=15,CONFIG=wine-5.json -N wine5 start_task.sh

qsub -l walltime=10:30:0 -l select=1:ncpus=16:mem=48gb:scratch_local=128gb -v NCPUS=15,CONFIG=magic-1.json -N magic1 start_task.sh
qsub -l walltime=10:30:0 -l select=1:ncpus=16:mem=48gb:scratch_local=128gb -v NCPUS=15,CONFIG=magic-2.json -N magic2 start_task.sh
qsub -l walltime=10:30:0 -l select=1:ncpus=16:mem=48gb:scratch_local=128gb -v NCPUS=15,CONFIG=magic-3.json -N magic3 start_task.sh
qsub -l walltime=10:30:0 -l select=1:ncpus=16:mem=48gb:scratch_local=128gb -v NCPUS=15,CONFIG=magic-4.json -N magic4 start_task.sh
qsub -l walltime=10:30:0 -l select=1:ncpus=16:mem=48gb:scratch_local=128gb -v NCPUS=15,CONFIG=magic-5.json -N magic5 start_task.sh

qsub -l walltime=10:30:0 -l select=1:ncpus=16:mem=48gb:scratch_local=128gb -v NCPUS=15,CONFIG=ml-prove-1.json -N mlprove1 start_task.sh
qsub -l walltime=10:30:0 -l select=1:ncpus=16:mem=48gb:scratch_local=128gb -v NCPUS=15,CONFIG=ml-prove-2.json -N mlprove2 start_task.sh
qsub -l walltime=10:30:0 -l select=1:ncpus=16:mem=48gb:scratch_local=128gb -v NCPUS=15,CONFIG=ml-prove-3.json -N mlprove3 start_task.sh
qsub -l walltime=10:30:0 -l select=1:ncpus=16:mem=48gb:scratch_local=128gb -v NCPUS=15,CONFIG=ml-prove-4.json -N mlprove4 start_task.sh
qsub -l walltime=10:30:0 -l select=1:ncpus=16:mem=48gb:scratch_local=128gb -v NCPUS=15,CONFIG=ml-prove-5.json -N mlprove5 start_task.sh

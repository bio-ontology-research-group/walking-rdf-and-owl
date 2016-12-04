
gnodes=47675228
nodes=12
threads=64
chunk=$(($gnodes / $nodes))
echo $chunk
echo $gnodes
for index in $(seq 0 $chunk $gnodes);
do
echo "./deepwalk \
    graph/STRING.ttl  \
    embeddings/embbeding_${index} \
    nodes \
    $index \
    $(($index + $chunk))"
# Create a job file
      cat > job.sh << EOF
#!/bin/bash
#SBATCH --qos=hohndor_group
#SBATCH --nodes=1
#SBATCH --ntasks-per-node=1
#SBATCH --cpus-per-task=64
#SBATCH --exclusive
#SBATCH --output=outputs/job_$index.outputs
#SBATCH --time=15-4:00:00
module purge
module load slurm
module load gcc/4.7.4
module load blas
module load lapack
module load anaconda
module load acml

#input output nodes start_index stop_index
echo "./deepwalk \
    graph/STRING.ttl  \
    /projects/dragon/bio2vec/embeddings/embbeding_${index} \
    nodes \
    $index \
    $(($index + $chunk))"

echo "Job complete."
EOF
    # Submit job
    echo "Submitting job..."
    if sbatch job.sh; then
        echo "...success."
        rm job.sh
    else
        # if there was a problem, then I save the job in order folder.
        echo "...failed."
        mv job.sh folder/job.sh
    fi
done
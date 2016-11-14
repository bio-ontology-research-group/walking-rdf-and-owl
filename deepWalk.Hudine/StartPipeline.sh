groovy Scripts/BuildGraphHuDiNe.groovy
groovy Scripts/RDFWrapper.groovy -i GeneratedFiles/BuildGraph/InWrapper.rdf -o GeneratedFiles/Wrapper/OutWrapper.rdf -m GeneratedFiles/Wrapper/mapping.txt -d ../BuildGraph/data/ontology/data -c TRUE
groovy Scripts/Split.groovy
groovy Scripts/GetListExclud.groovy
for i in $(seq 1 5)
  do
    deepwalk --workers 16 --representation-size 32 --format edgelist --input GeneratedFiles/Split/fold$i/Train.txt --excludlist GeneratedFiles/ExcludList/exclud.txt --log DEBUG --output GeneratedFiles/DeepWalk/fold$i/out.txt --window-size 10 --number-walks 50 --walk-length 10 &
  done
wait
groovy Scripts/PrepareDatasets.groovy

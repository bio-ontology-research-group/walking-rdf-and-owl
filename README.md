# Walking RDF and OWL

Feature learning on RDF and OWL (i.e., Description Logic theories). 

Here are some scripts to facilitate building the graph, classifying it and learning its node representations:

To run: `groovy RDFWrapper` and follow instructions. The input is an RDF graph and the output file can be used as input for the modified [DeepWalk](https://github.com/phanein/deepwalk) tool available as part of this repository (https://github.com/bio-ontology-research-group/walking-rdf-and-owl/tree/master/deepwalk_rdf).

For example, to classify the RDF graph `RDFgraph.nt` using the OWL ontologies in `onto_dir` with the ELK reasoner, and writing an edge list representation of the inferred graph to `outWrapper.txt`, use the following command:
~~~~
groovy RDFWrapper.groovy -i RDFgraph.nt -o outWrapper.txt -m mappingFile.txt -d onto_dir -c true 
~~~~


To generate representations (embeddings) of the nodes (and edges) in the RDF graph, run 
~~~~
deepwalk --workers 64 --representation-size 256 --format edgelist --input outWrapper.txt  --output out.txt --window-size 5 --number-walks 500 --walk-length 40
~~~~
to learn an embedding of size `256` using `64` parallel workers based on `500` walks of length `40` for each node. The `deepwalk` needs to be the modified version contained in this repository so that object properties are taken into account during the walk.

We also provided the algorithm with the option to allow walking from specific nodes only, by adding an excludelist parameter, which contains the identifiers of nodes to be excluded from the walks and 
therefore restrict walks to those that start from the remaining nodes. This modified version may provide faster training. 

~~~~
deepwalk --workers 48  --walk-length 20 --window-size 10 --number-walks 100 --representation-size 512 --format edgelist --excludlist exnodes.txt  --input outWrapper.txt --output outDeep.txt
~~~~

To run the C++ multi-threaded implementation of the corpus generation modele, you need to have the C++ Boost libraries installed; on an Ubuntu system, you can do:
~~~
sudo apt-get install libboost-all-dev
~~~
You also need to install the [Boost Threadpool Header files](http://threadpool.sourceforge.net/). 
Once all header files and libraries are installed, just type  `make` to compile and run deepwalk
~~~~
./deepwalk edgelistfile.txt walksfile.txt
~~~~

## Classification support

The RDFWrapper script comes with built-in support for OWL classification. Use this when your RDF dataset contains references to ontologies _and_ the full ontology. In the script we provide, we use the ELK reasoner (which supports the OWL 2 EL profile) to classify the ontology and infer class assertion axioms for all individuals. These are added to the RDF dataset following classification and used to build the graph.

## Example

An example knowledge graph and the resulting embeddings can be found here:
* Knowledge graph: http://aber-owl.net/aber-owl/bio2vec/bio-knowledge-graph.n3
* Embeddings: http://aber-owl.net/aber-owl/bio2vec/bio-knowledge-graph.embeddings.gz

## How to cite

If you use our code, please cite our paper: Alsharani et al. Neuro-symbolic representation learning on biological knowledge graphs. Bioinformatics 2017. [link](https://academic.oup.com/bioinformatics/article/3760100/Neuro-symbolic-representation-learning-on)

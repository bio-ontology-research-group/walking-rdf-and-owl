# Walking RDF and OWL

machine learning over RDF. 

Here is some scripts to facilitate building the graph, classifying it and learn its node represenations

To run: `groovy RDFWrapper` and follow instructions. The input is an RDF graph and the output file can be used directly as input for the [DeepWalk](https://github.com/phanein/deepwalk) tool. 
For example, To classify the RDF graph against some ontologies using ELK and convert it to edgelist, use the following:
~~~~
groovy RDFWrapper.groovy -i RDFgraph.nt -o outWrapper.txt -m mappingFile.txt -d onto_dir -c true 
~~~~


For example, if the output of the RDFWrapper is `outWrapper.txt`, run 
~~~~
deepwalk --workers 64 --representation-size 256 --format edgelist --input outWrapper.txt  --output out.txt --window-size 5 --number-walks 500 --walk-length 40
~~~~
to learn an embedding of size `256` using `64` parallel workers based on `500` walks of length `40` for each node.

We also modified Deepwalk to allow walking from specific nodes only, by adding an excludelist parameter, which contains the IDs of nodes to be excluded from the walks and 
thus restrict walking to start from the remaining nodes. This modified version provides faster Deepwalk training. 

~~~~
deepwalk --workers 48  --walk-length 20 --window-size 10 --number-walks 100 --representation-size 512 --format edgelist --excludlist exnodes.txt  --input outWrapper.txt --output outDeep.txt
~~~~

To run the c++ multithreaded implementation of deepwalk, you need to have boost C++ libraries installed, on a Ubuntu system, you can do:
~~~
sudo apt-get install libboost-all-dev
~~~
You also need to install the [Boost Threadpool Header files](http://threadpool.sourceforge.net/). 
Once all header files and libraries are installed, just type  `make` to compile and run deepwalk
~~~~
./deepwalk edgelistfile.txt walksfile.txt
~~~~

## Classification support

The RDFWrapper script comes with inbuilt support for OWL classification. Use this when your RDF dataset contains references to ontologies _and_ the full ontology. We use the ELK reasoner, which supports the OWL 2 EL profile, to classify the ontology and infer class assertion axioms for all individuals. These are added to the RDF dataset following classification and used to build the graph.

## Example

An example knowledge graph and the resulting embeddings can be found here:
* Knowledge graph: http://aber-owl.net/aber-owl/bio2vec/bio-knowledge-graph.n3
* Embeddings: http://aber-owl.net/aber-owl/bio2vec/bio-knowledge-graph.embeddings.gz

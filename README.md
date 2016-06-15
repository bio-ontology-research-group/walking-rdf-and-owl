# Walking RDF and OWL

Some scripts to experiment with machine learning over RDF. 

The main outcome is RDFWrapper.groovy.

To run: `groovy RDFWrapper` and follow instructions.
The output file can be used directly as input for https://github.com/phanein/deepwalk (as adjacency list). As properties are reifed, run `deepwalk` with parameter `--undirected False`, and increase walk length. For undirected graphs, use the `RDFWrapper` parameter `-u true`.

The output file can be used directly as input for the [DeepWalk](https://github.com/phanein/deepwalk) tool. For example, if the output of the RDFWrapper is `test.txt`, run: `deepwalk --workers 64 --representation-size 256 --format edgelist --input input.txt --log DEBUG --output out.txt --window-size 5 --number-walks 500 --walk-length 40` to learn an embedding of size `256` using `64` parallel workers based on `500` walks of length `40` for each node.

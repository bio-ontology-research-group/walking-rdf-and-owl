# Walking RDF and OWL

Some scripts to experiment with machine learning over RDF. 

The main outcome is RDFWrapper.groovy.

To run: `groovy RDFWrapper` and follow instructions.
The output file can be used directly as input for https://github.com/phanein/deepwalk (as adjacency list). As properties are reifed, run `deepwalk` with parameter `--undirected False`, and increase walk length. For undirected graphs, use the `RDFWrapper` parameter `-u true`.

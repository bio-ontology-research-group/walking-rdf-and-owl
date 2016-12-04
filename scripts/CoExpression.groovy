new File("data/coxpresHSA.tsv").splitEachLine("\t") { line ->
  def symb1 = line[0]?.toUpperCase()
  def prop = line[1]
  def symb2 = line[2]?.toUpperCase()
  println "<http://bio2rdf.org/uniprot:${symb1}_HUMAN> <http://example.com/$prop> <http://bio2rdf.org/uniprot:${symb2}_HUMAN> ."
}

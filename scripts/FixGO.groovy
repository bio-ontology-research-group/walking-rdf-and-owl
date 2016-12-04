new File("data/uniprot2go.nt").splitEachLine("\t") { line ->
  def go = line[2].replaceAll(" .","")
  def goi = go.replaceAll(">","i>")
  println line[0]+" "+line[1]+" "+goi +" ."
  println goi+" <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> "+go+" ."
}

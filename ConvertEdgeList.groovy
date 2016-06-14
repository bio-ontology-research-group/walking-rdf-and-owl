def map = [:]
def counter = 10000000
new File("edgelist.txt").splitEachLine(" ") { line ->
  def go = line[1]
  go = go.replaceAll("http://purl.obolibrary.org/obo/GO_","")

  def protein = line[0]
  if (map[protein] == null) {
    map[protein] = counter
    counter += 1
  }
  println map[protein]+" "+go
}

def map = [:]
new File("interactor.nt").splitEachLine(" ") { line ->
  def id = line[0]
  def prot = line[2]
  map[id] = prot
}

def imap = [:].withDefault { [] }
new File("interaction.interactor.nt").splitEachLine(" ") { line ->
  def iid = line[0]
  def prot = line[2]
  imap[iid].add(map[prot])
}

def gmap = [:]
def counter = 10000000
imap.each { k, v ->
  if (v.size()>1) {
    def p1 = v[0]
    def p2 = v[1]
    if (gmap[p1] == null) {
      gmap[p1] = counter
      counter += 1
    }
    if (gmap[p2] == null) {
      gmap[p2] = counter
      counter += 1
    }
    println gmap[p1]+" "+gmap[p2]
  }
}

new File("edgelist.txt").splitEachLine(" ") { line ->
  def go = line[1]
  go = go.replaceAll("http://purl.obolibrary.org/obo/GO_","")

  def protein = "<"+line[0]+">"
  if (gmap[protein] == null) {
    gmap[protein] = counter
    counter += 1
  }
  println gmap[protein]+" "+go
}

def fout = new PrintWriter(new BufferedWriter(new FileWriter("mapping.txt")))
gmap.each { k, v ->
  fout.println("$k\t$v")
}
fout.flush()
fout.close()


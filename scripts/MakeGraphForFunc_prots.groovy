def map1 = [:].withDefault { new LinkedHashSet() }
def map2 = [:].withDefault { new LinkedHashSet() }
def map3 = [:] //stringid to geneid
def map4 = [:] //uniprot to stringid
def map5 = [:] //genesID to stringid
def map6 = [:] //stringid uniprot

def uniprotids = new LinkedHashSet()
new File("../data/goa_human.gaf").splitEachLine("\t") { line ->
  def id = line[1]
  uniprotids.add(id)
}

new File("../data/9606.protein.aliases.v10.txt").splitEachLine("\t") { line ->
  def stringid = line[0]
  def val = line[1]
  def type = line[2]
  if (type?.indexOf("GeneID")>-1) {
    map1[val].add(stringid)
    map3[stringid] = val
    map5[val] = stringid
  }
  if (type?.indexOf("UniProt_ID")>-1) {
    if (val in uniprotids) {
      map2[stringid].add(val)
      map4[val] = stringid
      map6[stringid] = val
    }
  }
}



// Add orthologous organisms funcs instead 
/*
counter = 0
def done = [:].withDefault { new LinkedHashSet() }
new File("../data/goa_human.gaf").splitEachLine("\t") { line ->
  if (!line[0].startsWith("!")) {
    def uid = line[1]
    def anno = line[4]?.replaceAll(":","_")
    if (!(anno in done[uid])) {
      done[uid].add(anno)
    //  def gid = map3[map4[uid]]
      println "<http://www.ncbi.nlm.nih.gov/prot/"+uid+"> <http://example.com/go/has_go_annotation>  <http://aber-owl.net/go/instance_$counter> ."
      println "<http://aber-owl.net/go/instance_$counter> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://purl.obolibrary.org/obo/$anno> ."
      counter += 1
    }
  }
}*/




new File("../data/9606.protein.actions.v10.txt").splitEachLine("\t") { line ->
  if (line[0].startsWith("9606")) {
    def id1 = line[0]
    def id2 = line[1]
    def type = line[2]
    def score = new Double(line[-1])
    if (score >=700 && map6[id1] && map6[id2]) {
      println "<http://www.ncbi.nlm.nih.gov/prot/"+map6[id1]+"> <http://example.com/string/$type> <http://www.ncbi.nlm.nih.gov/prot/"+map6[id2]+"> ."
    }
  }
}

new File("../data/Hsa2.v14-08.G19788-S12640.rma.mrgeo.d/").eachFile { file ->
  def gid = file.getName()
  file.splitEachLine("\t") { line ->
    def gid2 = line[0]
    def val = new Double(line[2])
    if (val >= 0.3 && map6[map5[gid]] && map6[map5[gid2]]) { // positive
      println "<http://www.ncbi.nlm.nih.gov/prot/"+map6[map5[gid]]+"> <http://example.com/pos_corr> <http://www.ncbi.nlm.nih.gov/prot/"+map6[map5[gid2]]+"> ."
    } else if (val <= -0.3 && map6[map5[gid]] && map6[map5[gid2]]) {
      println "<http://www.ncbi.nlm.nih.gov/prot/"+map6[map5[gid]]+"> <http://example.com/neg_corr> <http://www.ncbi.nlm.nih.gov/prot/"+map6[map5[gid2]]+"> ."
    }
  }
}



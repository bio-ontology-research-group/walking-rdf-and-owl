def map1 = [:].withDefault { new LinkedHashSet() }
def map2 = [:].withDefault { new LinkedHashSet() }
def map3 = [:]
def map4 = [:]
def map5 = [:]
def map6 = [:]

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
  }
  if (type?.indexOf("UniProt_ID")>-1) {
    if (val in uniprotids) {
      map2[stringid].add(val)
      map4[val] = stringid
    }
  }
}


new File("../data/hpo2umls.txt").splitEachLine("\t") { line -> 
  def hpo = line[0].replaceAll(":","_")
  def umls = line[1]
  map5[umls] = hpo
}


new File("../data/umls2do.txt").splitEachLine("\t") { line ->
  def umls = line[0]
  def doid = line[1].replaceAll(":","_")
  map6[umls] = doid
}

counter = 0
def done = [:].withDefault { new LinkedHashSet() }
new File("../data/goa_human.gaf").splitEachLine("\t") { line ->
  if (!line[0].startsWith("!")) {
    def uid = line[1]
    def anno = line[4]?.replaceAll(":","_")
    if (map4[uid] && map3[map4[uid]] && !(anno in done[uid])) {
      done[uid].add(anno)
      def gid = map3[map4[uid]]
      println "<http://www.ncbi.nlm.nih.gov/gene/"+gid+"> <http://example.com/go/has_go_annotation>  <http://aber-owl.net/go/instance_$counter> ."
      println "<http://aber-owl.net/go/instance_$counter> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://purl.obolibrary.org/obo/$anno> ."
      counter += 1
    }
  }
}

new File("../data/9606.protein.actions.v10.txt").splitEachLine("\t") { line ->
  if (line[0].startsWith("9606")) {
    def id1 = line[0]
    def id2 = line[1]
    def type = line[2]
    def score = new Double(line[-1])
    if (score >=700 && map3[id1] && map3[id2]) {
      println "<http://www.ncbi.nlm.nih.gov/gene/"+map3[id1]+"> <http://example.com/string/$type> <http://www.ncbi.nlm.nih.gov/gene/"+map3[id2]+"> ."
    }
  }
}


new File("../data/Hsa2.v14-08.G19788-S12640.rma.mrgeo.d/").eachFile { file ->
  def gid = file.getName()
  file.splitEachLine("\t") { line ->
    def gid2 = line[0]
    def val = new Double(line[2])
    if (val >= 0.3) { // positive
      println "<http://www.ncbi.nlm.nih.gov/gene/"+gid+"> <http://example.com/pos_corr> <http://www.ncbi.nlm.nih.gov/gene/"+gid2+"> ."
    } else if (val <= -0.3) {
      println "<http://www.ncbi.nlm.nih.gov/gene/"+gid+"> <http://example.com/neg_corr> <http://www.ncbi.nlm.nih.gov/gene/"+gid2+"> ."
    }
  }
}

//genes diseases associations
counter = 0
new File("../data/all_gene_disease_associations.tsv").splitEachLine("\t") { line ->
  def geneid = line[0]
  def umls = line[3].replaceAll("umls:","")
  //println umls
  if (map6[umls]){
    println "<http://www.ncbi.nlm.nih.gov/gene/"+ geneid+ "> <http://example.com/disGeNet/has_disease_annotation>  <http://aber-owl.net/do/instance_$counter> ."
    println "<http://aber-owl.net/do/instance_$counter> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://purl.obolibrary.org/obo/"+map6[umls]+"> ."
    counter +=1
  }
}

//sider sideeffects

counter = 0
new File("../data/meddra_all_se.tsv").splitEachLine("\t") { line ->
 def drugid = line[0]
 def umls = line[2]
 if (map5[umls]){
    println "<http://aber-owl.net/drug/"+ drugid+ "> <http://example.com/sider/has_sideeffect>  <http://aber-owl.net/pheno/instance_$counter> ."
    println "<http://aber-owl.net/pheno/instance_$counter> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://purl.obolibrary.org/obo/"+map5[umls]+"> ."
    counter+=1
 }

}


//sider indications
counter = 0
new File("../data/meddra_all_indications.tsv").splitEachLine("\t") { line ->
 def drugid = line[0]
 def umls = line[1] 
 if(map5[umls]){
    println "<http://aber-owl.net/drug/"+ drugid+ "> <http://example.com/sider/has_indication>  <http://aber-owl.net/pheno/instance_$counter> ."
    println "<http://aber-owl.net/pheno/instance_$counter> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://purl.obolibrary.org/obo/"+map5[umls]+"> ."
    counter+=1
 }
}

//stitch
new File("../data/actions_humans.v4.0.tsv").splitEachLine("\t") { line ->
def drugid = line[0]
def prot = line[1]
if (map3[prot]){
    println "<http://aber-owl.net/drug/"+ drugid+ "> <http://example.com/stitch/has_interaction> <http://www.ncbi.nlm.nih.gov/gene/"+ map3[prot] +"> ."
    }
}


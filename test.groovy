
def map3 = [:]
def map5 = [:]
def map6 = [:]
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


new File("../data/9606.protein.aliases.v10.txt").splitEachLine("\t") { line ->
  def stringid = line[0]
  def val = line[1]
  def type = line[2]
  if (type?.indexOf("GeneID")>-1) {
    map3[stringid] = val
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




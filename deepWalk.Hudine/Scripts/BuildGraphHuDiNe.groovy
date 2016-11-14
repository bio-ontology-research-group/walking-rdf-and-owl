def map1 = [:].withDefault { new LinkedHashSet() }
def map2 = [:].withDefault { new LinkedHashSet() }
def map3 = [:]
def map4 = [:]
def map5 = [:]
def map6 = [:]

new File("GeneratedFiles/BuildGraph/InWrapper.rdf").delete()
def Graph = new File("GeneratedFiles/BuildGraph/InWrapper.rdf")

println("Extraction of UNIPROT IDs from goa_human.gaf [START]")
def uniprotids = new LinkedHashSet()
new File("../BuildGraph/data/GO/goa_human.gaf").splitEachLine("\t") { line ->
	if (!line[0].startsWith("!")) {
		def id = line[1]
		uniprotids.add(id)
	}
}
println("Extraction of UNIPROT IDs from goa_human.gaf [END]")



println("Extraction of gene and protein aliases from 9606.protein.aliases.v10.txt [START]")
new File("../BuildGraph/data/PPI/9606.protein.aliases.v10.txt").splitEachLine("\t") { line ->
	if (!line[0].startsWith("#")) {
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
}
println("Extraction of gene and protein aliases from 9606.protein.aliases.v10.txt [END]")



def counter = 0

println("Integration of goa_human.gaf [START]")
def done = [:].withDefault { new LinkedHashSet() }
new File("../BuildGraph/data/GO/goa_human.gaf").splitEachLine("\t") { line ->
	if (!line[0].startsWith("!")) {
		def uid = line[1]
		def anno = line[4]?.replaceAll(":","_")
		if (map4[uid] && map3[map4[uid]] && !(anno in done[uid])) {
			done[uid].add(anno)
			def gid = map3[map4[uid]]
			Graph.append("<http://www.ncbi.nlm.nih.gov/gene/"+gid+"> <http://example.com/go/has_go_annotation> <http://aber-owl.net/go/instance_$counter> ."+"\n")
			Graph.append("<http://aber-owl.net/go/instance_$counter> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://purl.obolibrary.org/obo/$anno> ."+"\n")
			counter += 1
		}
	}
}
println("Integration of goa_human.gaf [END]")



println("Integration of 9606.protein.actions.v10.txt [START]")
new File("../BuildGraph/data/PPI/9606.protein.actions.v10.txt").splitEachLine("\t") { line ->
	if (line[0].startsWith("9606")) {
		def id1 = line[0]
		def id2 = line[1]
		def type = line[2]
		def score = new Double(line[-1])
		if (score >=700 && map3[id1] && map3[id2]) {
			Graph.append("<http://www.ncbi.nlm.nih.gov/gene/"+map3[id1]+"> <http://example.com/string/$type> <http://www.ncbi.nlm.nih.gov/gene/"+map3[id2]+"> ."+"\n")
		}
	}
}
println("Integration of 9606.protein.actions.v10.txt [END]")



println("Integration of Hsa2.v14-08.G19788-S12640.rma.mrgeo.d/ [START]")
new File("../BuildGraph/data/coexpresdb/Hsa2.v14-08.G19788-S12640.rma.mrgeo.d/").eachFile { file ->
	def gid = file.getName()
	file.splitEachLine("\t") { line ->
		def gid2 = line[0]
		def val = new Double(line[2])
		if (val >= 0.3) { // positive
			Graph.append("<http://www.ncbi.nlm.nih.gov/gene/"+gid+"> <http://example.com/pos_corr> <http://www.ncbi.nlm.nih.gov/gene/"+gid2+"> ."+"\n")
		} else if (val <= -0.3) {
			Graph.append("<http://www.ncbi.nlm.nih.gov/gene/"+gid+"> <http://example.com/neg_corr> <http://www.ncbi.nlm.nih.gov/gene/"+gid2+"> ."+"\n")
		}
	}
}
println("Integration of Hsa2.v14-08.G19788-S12640.rma.mrgeo.d/ [END]")



println("Mapping of UMLS and ICD9CM using doid.obo [START]")
def doid = ""
def umls = ""
def icd9cm = ""
new File("../BuildGraph/data/DO/doid.obo").splitEachLine(" ") { line ->

	if ( (line[0].startsWith("id:")) && (line[1].startsWith("DOID:")) ){
		doid=line[1]?.replaceAll(":","_")
	}

	if ( (line[0].startsWith("xref:")) && (line[1].startsWith("UMLS_CUI:")) ){
		umls=line[1]?.replaceAll("UMLS_CUI:","")
		map5[umls] = doid
	}

	if ( (line[0].startsWith("xref:")) && (line[1].startsWith("ICD9CM:")) ){
		icd9cm=line[1]?.replaceAll("ICD9CM:","").split("\\-")[0]
		map6[icd9cm] = doid
	}
}
println("Mapping of UMLS and ICD9CM using doid.obo [END]")



println("Integration of all_gene_disease_associations.tsv [START]")
new File("../BuildGraph/data/DisGeNET/all_gene_disease_associations.tsv").splitEachLine("\t") { line ->
	if( (!line[0].startsWith("#")) || (!line[0].startsWith("geneId")) ){
		def gid = line[0]
		def dumls = line[3]?.replaceAll("umls:","")
		if (map5[dumls]){
			Graph.append("<http://www.ncbi.nlm.nih.gov/gene/"+gid+"> <http://example.com/disGeNet/is_associated_with> <http://purl.obolibrary.org/obo/"+map5[dumls]+"> ."+"\n")
		}
	}
}
println("Integration of all_gene_disease_associations.tsv [END]")




println("Integration of disease_phenotypes.doa [START]")
counter = 0
new File("../BuildGraph/data/DiseasePhenotypes/disease_phenotypes.doa").splitEachLine("\t") { line ->
	if(!line[0].startsWith("!")){
		def did = line[1]?.replaceAll(":","_")
		def pheno = line[4]?.replaceAll(":","_")
		if (pheno.startsWith("HP_")) { //|| pheno.startsWith("MP_")) {
			Graph.append("<http://purl.obolibrary.org/obo/"+did+"> <http://example.com/pheno/has_disease_phenotype> <http://aber-owl.net/pheno/instance_$counter> ."+"\n")
			Graph.append("<http://aber-owl.net/pheno/instance_$counter> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://purl.obolibrary.org/obo/"+pheno+"> ."+"\n")
			counter += 1
		}
	}
}
println("Integration of disease_phenotypes.doa [END]")



println("Integration of modelphenotypes.txt [START]")
new File("../BuildGraph/data/modelphenotypes/modelphenotypes.txt").splitEachLine("\t") { line ->
	def entrez = line[-1]?.replaceAll('\\[',"")?.replaceAll('\\]',"").split(",")
	def pheno = line[-2]
	if (pheno.startsWith("HP:")) { // || pheno.startsWith("MP:")) {
		pheno = pheno.replaceAll(":","_")
		pheno = "http://purl.obolibrary.org/obo/"+pheno
		entrez.each { gid ->
			gid = gid.replaceAll("ENTREZ:","")?.trim()
			Graph.append("<http://www.ncbi.nlm.nih.gov/gene/"+gid+"> <http://example.com/pheno/has_gene_phenotype>  <http://aber-owl.net/pheno/instance_$counter> ."+"\n")
			Graph.append("<http://aber-owl.net/pheno/instance_$counter> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <$pheno> ."+"\n")
			counter += 1
		}
	}
}
println("Integration of modelphenotypes.txt [END]")



println("Integration of AllNet5.net [START]")
new File("../BuildGraph/data/HuDiNe/AllNet5.net").splitEachLine("\t") { line ->
	def icd1 = line[0]
	def icd2 = line[1]
	def corr = new Double(line[8])

	if (corr > 0) { // positive

		if (map6[icd1] && map6[icd2])
		{
			Graph.append("<http://purl.obolibrary.org/obo/"+map6[icd1]+"> <http://example.com/HuDiNe/positively_correlate_with> <http://purl.obolibrary.org/obo/"+map6[icd2]+"> ."+"\n")
		}
	}else if(corr < 0)
	{
		if (map6[icd1] && map6[icd2])
		{
			Graph.append("<http://purl.obolibrary.org/obo/"+map6[icd1]+"> <http://example.com/HuDiNe/negatively_correlate_with> <http://purl.obolibrary.org/obo/"+map6[icd2]+"> ."+"\n")
		}
	}
}
println("Integration of AllNet5.net [END]")

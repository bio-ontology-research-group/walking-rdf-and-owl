def descEdge = "http://example.com/pheno/has_disease_phenotype"
def edgeId = 0

new File("GeneratedFiles/Wrapper/mapping.txt").splitEachLine("\t"){ line ->

	def mappingUri = line[0]
	def mappingId = line[1]

	if( mappingUri?.indexOf(descEdge) > -1 ){
		edgeId = mappingId
	}

}

new File("GeneratedFiles/Render/OutRender.rdf").delete()
def outRender = new File("GeneratedFiles/Render/OutRender.rdf")

def mapInstance = [:]
def listFunction = new LinkedHashSet()

new File("GeneratedFiles/Wrapper/OutWrapper.rdf").eachLine { line ->
	def vertex1 = line.split(' ')[0]
	def vertex2 = line.split(' ')[1]
	def edge = line.split(' ')[2]

	if (edge == edgeId){
		mapInstance[vertex2]=vertex1
	}

	if (mapInstance[vertex1]){
		listFunction << [mapInstance[vertex1],vertex2]
	}

	if( (edge != edgeId) && (!mapInstance[vertex1]) ){
		outRender.append(line+"\n")
	}

}

println("Size of Disease Phenotype annotations: "+  listFunction.size())
for (i=0; i< listFunction.size(); i++){
	def (gene, phenotype) = listFunction[i]
	outRender.append(gene+' '+phenotype+' '+edgeId+"\n")
}

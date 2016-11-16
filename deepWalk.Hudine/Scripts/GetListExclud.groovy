def descVertex1 = "DOID_" //String that describe the right vertex of given edge in the mapping file
def descVertex2 = "DOID_" //String that describe the left vertex of given edge in the mapping file

new File("GeneratedFiles/ExcludList/exclud.txt").delete()
def excludList = new File("GeneratedFiles/ExcludList/exclud.txt")

println("Identification of list of Nodes to be excluded from the walks [START]")
new File("GeneratedFiles/Wrapper/mapping.txt").splitEachLine("\t") { line ->
	def mappingUri = line[0]
	def mappingId = line[1]
	if ( (mappingUri?.indexOf(descVertex1) == -1) && (mappingUri?.indexOf(descVertex2) == -1) ) {
		excludList.append(mappingId+"\n")
	}
}
println("Identification of list of Nodes to be excluded from the walks [END]")

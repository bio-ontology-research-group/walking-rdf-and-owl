import static groovyx.gpars.GParsPool.withPool

def edgeId = []
def descEdge = "http://example.com/HuDiNe/positively_correlate_with"

def ignoreEdgeId = []
def descEdgeIgnore = "http://example.com/HuDiNe/negatively_correlate_with"

def nbEdges = 0

println ("Looking for edge information ...")
new File("GeneratedFiles/Wrapper/mapping.txt").splitEachLine("\t"){ line ->

	def mappingUri = line[0]
	def mappingId = line[1]

	if( mappingUri?.indexOf(descEdge) > -1 ){
		edgeId.add(mappingId)
	}

	if( mappingUri?.indexOf(descEdgeIgnore) > -1 ){
		ignoreEdgeId.add(mappingId)
	}
}
println (edgeId.size()+" edges were selected to be considered for prediction. Their ids are: "+edgeId)
println (ignoreEdgeId.size()+" edges were selected to be ignored. Their ids are: "+ignoreEdgeId)

println ("Looking for occurrences of relevant edges ...")
new File("GeneratedFiles/Wrapper/OutWrapper.rdf").splitEachLine(" "){ line ->
	def outWrapperEdgeId = line[2]
	if( edgeId.contains(outWrapperEdgeId) ){
		nbEdges += 1
	}
}
println ("Number of occurrences of relevant edges is:"+nbEdges)

println ("Splitting the data for 5 fold cross validation ...")

def folds = [1,2,3,4,5]

withPool{	pool ->

	folds.eachParallel{	it ->

		println ("-".multiply((it-1)*20)+"[Fold${it}:] ### START ###")

		def foldSplitSize = nbEdges.intdiv(5)
		println ("-".multiply((it-1)*20)+"[Fold${it}:] Number of edges to be retained for testing in fold${it} is:"+ foldSplitSize)

		def tmpFoldSplitSize = foldSplitSize + 1

		new File("GeneratedFiles/Split/fold${it}/Test.txt").delete()
		def Test = new File("GeneratedFiles/Split/fold${it}/Test.txt")
		new File("GeneratedFiles/Split/fold${it}/Train.txt").delete()
		def Train = new File("GeneratedFiles/Split/fold${it}/Train.txt")
		def nthFold = it

		new File("GeneratedFiles/Wrapper/OutWrapper.rdf").eachLine { line ->

			def cVoutWrapperEdgeId = line.split(' ')[2]

			if ( edgeId.contains(cVoutWrapperEdgeId) && (nthFold > 1) ){
				tmpFoldSplitSize -= 1
				if(tmpFoldSplitSize == 0){
					nthFold -= 1
					tmpFoldSplitSize += (foldSplitSize + 1)
				}
			}

			if( edgeId.contains(cVoutWrapperEdgeId) && (foldSplitSize > 0) && (nthFold == 1) ){
				foldSplitSize -= 1
				Test.append(line+"\n")
			}else if( !ignoreEdgeId.contains(cVoutWrapperEdgeId) ){
				Train.append(line+"\n")
			}
		}
		println ("-".multiply((it-1)*20)+"[Fold${it}:] Fold ${it} ready...")
	}
}

println ("#####-----DONE-----#####")

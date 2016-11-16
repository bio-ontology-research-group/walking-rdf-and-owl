import static groovyx.gpars.GParsPool.withPool

def random = new Random() //Random class to be used to generate possible combinations for negative instance selection
def repSize = 0 //Representation size will be used to generate the header of arff files

def listVertex1 = new LinkedHashSet() //Will contain the list of nodes that may appear in the right side of an edge ()---(X)
def listVertex2 = new LinkedHashSet() //Will contain the list of nodes that may appear in the left side of an edge (X)---()
def listEdgeId = new LinkedHashSet()//List of edges to be taken into consideration in the prediction

def descVertex1 = "DOID_" //String that describe the right vertex of given edge in the mapping file
def descVertex2 = "DOID_" //String that describe the left vertex of given edge in the mapping file
def descEdge = "http://example.com/HuDiNe/positively_correlate_with" //String that describe a given edge to be considered

println("Extraction of IDs of relevant vertices & edges from the mapping file ...")

new File("GeneratedFiles/Wrapper/mapping.txt").splitEachLine("\t"){ line ->
	def mappingUri = line[0]
	def mappingId = line[1]

	if (mappingUri?.indexOf(descVertex1) > -1){
		listVertex1 << mappingId
	}

	if (mappingUri?.indexOf(descVertex2) > -1){
		listVertex2 << mappingId
	}

	if( mappingUri?.indexOf(descEdge) > -1 ){
		listEdgeId << mappingId
	}
}
println("[DEBUG:] The size of list of left vertices is: "+listVertex1.size())
println("[DEBUG:] The size of list of right vertices is: "+listVertex2.size())
println("[DEBUG:] The size of list of edges is: "+listEdgeId.size())

println("[DEBUG:] Generation of all possible combinations for the negative instances dataset [START]...")
def negativeSet = new LinkedHashSet()
/*
def listVertex1SubSet = new LinkedHashSet()
def listVertex2SubSet = new LinkedHashSet()

while (listVertex1SubSet.size() < listVertex1.size().intdiv(5))
{
		def index1 = random.nextInt(listVertex1.size())
		listVertex1SubSet << listVertex1[index1]
}

while (listVertex2SubSet.size() < listVertex2.size().intdiv(5))
{
		def index2 = random.nextInt(listVertex2.size())
		listVertex2SubSet << listVertex2[index2]
}
*/
if(descVertex1 == descVertex2){
	negativeSet = [listVertex1, listVertex2].combinations().findAll{	a,b->
		(a != b)
	}
}else{
	negativeSet = [listVertex1, listVertex2].combinations()
}
println("[DEBUG:] Size of all possible combinations: "+ negativeSet.size())
println("[DEBUG:] Generation of all possible combinations for the negative instances dataset [END]...")


println("!!! 5 fold cross validation: File processing !!! ...")

def folds =  [1,2,3,4,5]

class FoldClass{

	int idClass
	int testIndex
	int nbPosTestInst

	int trainIndex
	int nbPosTrainInst

	int m
	int n

	def embeddings = [:]
	def trueLink = new LinkedHashSet()
	def negativeSetFold = new LinkedHashSet()

	def generatedTrain = new LinkedHashSet()
	def generatedTest = new LinkedHashSet()

	FoldClass(int fold){
		this.idClass = fold
		this.nbPosTrainInst = 0
		this.nbPosTestInst = 0
		this.trainIndex = 0
		this.testIndex = 0
		this.m = 0
		this.n = 0
	}
}

withPool{	pool ->

	folds.collectParallel{	fold ->

		println("-".multiply((fold-1)*20)+"[Fold${fold}:]=============[START] ...")

		FoldClass fc = new FoldClass(fold)
		fc.negativeSetFold = negativeSet
		if (fc.idClass == fold){

			println("-".multiply((fold-1)*20)+"[Fold${fold}:] Reading node embeddings")
			new File("GeneratedFiles/DeepWalk/fold${fold}/out.txt").splitEachLine(" "){ line ->

				def outLineSize = line.size()
				def outVertexId = line[0]
				def representation = line - outVertexId

				repSize = representation.size()
				if ( outLineSize > 2 ){
					if ( (listVertex1.contains(outVertexId)) || (listVertex2.contains(outVertexId)) ){
						fc.embeddings[outVertexId] = representation
					}
				}
			}
			println("-".multiply((fold-1)*20)+"[Fold${fold}:]=====[DEBUG:] The number of extracted embeddings is: "+fc.embeddings.size())

			println("-".multiply((fold-1)*20)+"[Fold${fold}:] Generating headers of arff files")

			new File("GeneratedFiles/Prepare/fold${fold}/Test.arff").delete()
			def Test = new File("GeneratedFiles/Prepare/fold${fold}/Test.arff")

			new File("GeneratedFiles/Prepare/fold${fold}/Train.arff").delete()
			def Train = new File("GeneratedFiles/Prepare/fold${fold}/Train.arff")

			Test.append("@RELATION TestingDataset\n\n")
			Train.append("@RELATION TrainingDataset\n\n")

			(0..(repSize - 1)).each{
			Test.append("@ATTRIBUTE Vertex1Att${it} REAL\n")
			Train.append("@ATTRIBUTE Vertex1Att${it} REAL\n")
			}

			(0..(repSize - 1)).each{
			Test.append("@ATTRIBUTE Vertex2Att${it} REAL\n")
			Train.append("@ATTRIBUTE  Vertex2Att${it} REAL\n")
			}


			Test.append("@ATTRIBUTE class {yes,no}\n\n")
			Train.append("@ATTRIBUTE class {yes,no}\n\n")

			Test.append("@DATA\n")
			Train.append("@DATA\n")

			println("-".multiply((fold-1)*20)+"[Fold${fold}:] Appending positive training instances ...")

			new File("GeneratedFiles/Split/fold${fold}/Train.txt").splitEachLine(" ") { line ->
				def trainVertex1 = line[0]
				def trainVertex2 = line[1]
				def trainEdge = line[2]
				def trainIo = new LinkedHashSet()

				if ( listEdgeId.contains(trainEdge) )
				{
					trainIo = fc.embeddings[trainVertex1]	+ fc.embeddings[trainVertex2] + "yes"
					Train.append(trainIo.join(',') + '\n')

					fc.trueLink << [trainVertex1, trainVertex2]

					fc.nbPosTrainInst += 1

					if(trainIo.size() < 2*repSize+1){
						println("-".multiply((fold-1)*20)+"[Fold${fold}:]======[ERROR:] Wrong positive instance appended to the training dataset")
					}
				}
			}
			println("-".multiply((fold-1)*20)+"[Fold${fold}:]======[DEBUG:] The number of edges considered after processing the training dataset is: "+fc.trueLink.size())

			println("-".multiply((fold-1)*20)+"[Fold${fold}:] Appending positive testing instances ...")
			new File("GeneratedFiles/Split/fold${fold}/Test.txt").splitEachLine(" ") { line ->
				def testVertex1 = line[0]
				def testVertex2 = line[1]
				def testEdge = line[2]
				def testIo = new LinkedHashSet()

				if ( fc.embeddings[testVertex1] && fc.embeddings[testVertex2] )
				{
					testIo = fc.embeddings[testVertex1] + fc.embeddings[testVertex2] + "yes"
					Test.append(testIo.join(',') + '\n')

					fc.trueLink << [testVertex1, testVertex2]

					fc.nbPosTestInst += 1

					if(testIo.size() < 2*repSize+1){
						println("-".multiply((fold-1)*20)+"[Fold${fold}:]======[ERROR:] Wrong positive instance appended to the testing dataset")
					}
				}
			}
			println("-".multiply((fold-1)*20)+"[Fold${fold}:]======[DEBUG:] The number of edges considered after processing the testing dataset is: "+fc.trueLink.size())

			fc.negativeSetFold.removeAll(fc.trueLink)

			println("-".multiply((fold-1)*20)+"[Fold${fold}:] negativeSet size After remove of true links: "+ fc.negativeSetFold.size())

			fc.trainIndex = fc.negativeSetFold.size()

			while (fc.generatedTrain.size() < fc.nbPosTrainInst)
			{
			    fc.trainIndex = random.nextInt(fc.negativeSetFold.size())
					if(fc.negativeSetFold[fc.trainIndex].size() == 2){
						def (randomTrainVertex1, randomTrainVertex2) = fc.negativeSetFold[fc.trainIndex]
						if (fc.embeddings[randomTrainVertex1] && fc.embeddings[randomTrainVertex2]){
							fc.generatedTrain << fc.negativeSetFold[fc.trainIndex]
						}
					}
			}
			println("-".multiply((fold-1)*20)+"[Fold${fold}:] Negative instances for training dataset ramdomly selected ...")

			while( fc.m<fc.generatedTrain.size())
			{
				def (randomTrainVertex1, randomTrainVertex2) = fc.generatedTrain[fc.m]
				def randomTrainIo = new LinkedHashSet()
				randomTrainIo = fc.embeddings[randomTrainVertex1] + fc.embeddings[randomTrainVertex2] + "no"
				Train.append(randomTrainIo.join(',') + '\n')
				fc.m++
				if(randomTrainIo.size() < 2*repSize+1){
					println("-".multiply((fold-1)*20)+"[Fold${fold}:]======[ERROR:] Wrong negative instance appended to the training dataset")
				}
			}
			println("-".multiply((fold-1)*20)+"[Fold${fold}:] Negative instances for training dataset appended correctly ...")

			def trainLines = Train.readLines()

			def trainSize = trainLines.size()-(2*repSize+5)

			def trainPosSize = fc.generatedTrain.size()
			def trainNegSize = trainSize - trainPosSize

			def trainDecision = (trainPosSize == trainNegSize)

			println("-".multiply((fold-1)*20)+"[Fold${fold}:]=============[TRAINING SUMMARY] ...")

			println("-".multiply((fold-1)*20)+"[Fold${fold}:]=============Number of postive instances in the Training Dataset is: "+ trainPosSize)
			println("-".multiply((fold-1)*20)+"[Fold${fold}:]=============Number of negative instances in the Training Dataset is: "+ trainNegSize)
			println("-".multiply((fold-1)*20)+"[Fold${fold}:]=============Number of all instances in the Training Dataset is: "+ trainSize)
			println("-".multiply((fold-1)*20)+"[Fold${fold}:]=============Succesfull balancing of Training Dataset: "+ trainDecision )

			fc.negativeSetFold.removeAll(fc.generatedTrain)
			println("-".multiply((fold-1)*20)+"[Fold${fold}:] negativeSet size After remove of negative instances of training dataset: "+ fc.negativeSetFold.size())

			fc.testIndex = fc.negativeSetFold.size()

			while (fc.generatedTest.size() < fc.nbPosTestInst)
			{
					fc.testIndex = random.nextInt(fc.negativeSetFold.size())
					if(fc.negativeSetFold[fc.testIndex].size() == 2){
						def (randomTestVertex1, randomTestVertex2) = fc.negativeSetFold[fc.testIndex]
						if (fc.embeddings[randomTestVertex1] && fc.embeddings[randomTestVertex2]){
							fc.generatedTest << fc.negativeSetFold[fc.testIndex]
						}
					}
			}
			println("-".multiply((fold-1)*20)+"[Fold${fold}:] Negative instances for testing dataset ramdomly selected ...")

			while( fc.n<fc.generatedTest.size() )
			{
				def (randomTestvertex1, randomTestvertex2) = fc.generatedTest[fc.n]
				def randomTestIo = new LinkedHashSet()
				randomTestIo = fc.embeddings[randomTestvertex1] + fc.embeddings[randomTestvertex2] + "no"
				Test.append(randomTestIo.join(',') + '\n')
				fc.n++
				if(randomTestIo.size() < 2*repSize+1){
					println("-".multiply((fold-1)*20)+"[Fold${fold}:]======[ERROR:] Wrong negative instance appended to the testing dataset")
				}
			}
			println("-".multiply((fold-1)*20)+"[Fold${fold}:] Negative instances for testing dataset appended correctly ...")

			def testLines = Test.readLines()

			def testSize = testLines.size()-(2*repSize+5)

			def testPosSize = fc.generatedTest.size()
			def testNegSize = testSize - testPosSize

			def testDecision = (testPosSize == testNegSize)

			println("-".multiply((fold-1)*20)+"[Fold${fold}:]=============[TESTING SUMMARY] ...")

			println("-".multiply((fold-1)*20)+"[Fold${fold}:]=============Number of postive instances in the Testing Dataset is: "+ testPosSize)
			println("-".multiply((fold-1)*20)+"[Fold${fold}:]=============Number of negative instances in the Testing Dataset is: "+ testNegSize)
			println("-".multiply((fold-1)*20)+"[Fold${fold}:]=============Number of all instances in the Testing Dataset is: "+ testSize)
			println("-".multiply((fold-1)*20)+"[Fold${fold}:]=============Succesfull balancing of Testing Dataset: "+ testDecision)

			println("-".multiply((fold-1)*20)+"[Fold${fold}:]=============[END] ...")
		}
	}
}

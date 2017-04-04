import pdb
import numpy as np 
from sklearn.metrics import classification_report
import itertools
from sklearn import svm 
import random 
import sys



random.seed(42)


data_folder = '../../../../Documents/deepwalk_data/set1_data_cluster/'


args = sys.argv
setno = args[1]
mapping = args[2]
edgetype = args[3]


graph_ids = {}
deepwalk_map = {}
with open(data_folder + mapping) as f:
	for line in f:
		items = line.strip().split()
		node = items[0].split('/')[-1]
		nodetype = items[0].split('/')[-2]
		ID = items[1]
		graph_ids[node] = ID

edgeid = graph_ids[edgetype]


resfile = open('../oneClassSVM_results/'+setno+'.txt','w')

for j in range(5):

	graph_triples = [line.strip().split() for line in open(data_folder+'edgelist_'+setno+'_'+str(j)+'.txt')]
        subgraph = [triple for triple in graph_triples if triple[2] == edgeid]

	trip_arr = np.array(subgraph,dtype="int32")
	first = np.array(trip_arr[:,0])
	second = np.array(trip_arr[:,1])

	comb = list(itertools.product(set(first),set(second)))

	train_triple = [line.strip().split() for line in open(data_folder+'train_'+setno+'_'+str(j)+'.txt')]
	test_triple = [line.strip().split() for line in open(data_folder+'test_'+setno+'_'+str(j)+'.txt')]
	train_arr = np.array(train_triple, dtype="int32")
	test_arr = np.array(test_triple, dtype = "int32")

	train_pos = [(item[0],item[1]) for item in train_arr]
	test_pos = [(item[0],item[1]) for item in test_arr]

	combminus_train = set(comb) - set(train_pos)
	all_negatives = set(combminus_train) - set(test_pos)
	listcomb = list(all_negatives)
	test_neg = random.sample(listcomb,len(test_pos))


	outDeepfile = data_folder + 'outDeep_'+setno+'_'+str(j)+'.txt'
	print(outDeepfile)
	with open(outDeepfile,'r') as f:
		for line in f:
			items = line.strip().split()
			nodeid = int(items[0])
			fs_vec = items[1:]
			deepwalk_map[nodeid] = fs_vec

	print 'finsihed deepwalk map\n'

	positive_train_samples = list()
	positive_test_samples = list()
	negative_test_samples = list()
	negative_test_samples_all = list()

	#postive set train
	for (a,b) in train_pos:
		if a in deepwalk_map and b in deepwalk_map:
			node1_reps = deepwalk_map[a]
			node2_reps = deepwalk_map[b]
			features = np.append(node1_reps,node2_reps)
			positive_train_samples.append(features)


	for (a,b) in test_pos:
		if a in deepwalk_map and b in deepwalk_map:
			node1_reps = deepwalk_map[a]
			node2_reps = deepwalk_map[b]
			features = np.append(node1_reps, node2_reps)
			positive_test_samples.append(features)


	for (a,b) in test_neg:
		if a in deepwalk_map and b in deepwalk_map:
			node1_reps = deepwalk_map[a]
			node2_reps = deepwalk_map[b]
			features = np.append(node1_reps, node2_reps)
			negative_test_samples.append(features)


	print 'finished test positive and negatives\n'


	positive_train = np.array(positive_train_samples,dtype = 'float32')
	positive_test = np.array(positive_test_samples, dtype = 'float32')
	negative_test = np.array(negative_test_samples, dtype = 'float32')

	clf = svm.OneClassSVM(nu=0.1, kernel = 'poly', gamma = 0.1)
	clf.fit(positive_train)
	y_pred_train = clf.predict(positive_train)
	y_pred_test = clf.predict(positive_test)
	y_pred_neg_test = clf.predict(negative_test)

	print 'finished predicting\n'

	TP = y_pred_test[y_pred_test == 1].size
	FN = y_pred_test[y_pred_test == -1].size
	TN = y_pred_neg_test[y_pred_neg_test == -1].size
	FP = y_pred_neg_test[y_pred_neg_test == 1].size
	F1 = 2*TP/float(2*TP+FP+FN)


	print 'F1 measure: {}'.format(F1)

	resfile.write('fold: {}'.format(str(j))+'\n')
	resfile.write('F1 measure: {}'.format(F1) + '\n')


resfile.close()

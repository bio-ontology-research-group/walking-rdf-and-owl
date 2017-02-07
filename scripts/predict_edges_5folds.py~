import numpy as np 
import pdb
import networkx as nx 
import random
from sklearn import linear_model
from sklearn.metrics import roc_auc_score
from sklearn.cross_validation import train_test_split
from sklearn.metrics import classification_report
from random import randrange
import itertools
import sys

random.seed(42)

args = sys.argv
setno = args[1]
mapping = args[2]
edgetype = args[3]

graph_ids = {}
deepwalk_map = {}

with open(mapping) as f:
	for line in f:
		items = line.strip().split()
		node = items[0].split('/')[-1]
		nodetype = items[0].split('/')[-2]
		ID = items[1]
		graph_ids[node] = ID

edgeid = graph_ids[edgetype]

resfile = open('../results/'+setno+'.txt','w')

print('experiments of: ', setno)
for j in range(5):


	graph_triples = [line.strip().split() for line in open('../set1_data/edgelist_'+setno+'_'+str(j)+'.txt')]
	subgraph = [triple for triple in graph_triples if triple[2] == edgeid]
	#pdb.set_trace()

	trip_arr = np.array(subgraph,dtype="int32")
	first = np.array(trip_arr[:,0])
	second = np.array(trip_arr[:,1])
	comb = list(itertools.product(set(first),set(second)))


	train_triple = [line.strip().split() for line in open('../set1_data/train_'+setno+'_'+str(j)+'.txt')]
	test_triple = [line.strip().split() for line in open('../set1_data/test_'+setno+'_'+str(j)+'.txt')]

	train_arr = np.array(train_triple, dtype="int32")
	test_arr = np.array(test_triple, dtype = "int32")

	#converting to sets to do sets operation
	train_arr = [(item[0],item[1]) for item in train_arr]
	test_arr = [(item[0],item[1]) for item in test_arr]

	combminus_train = set(comb) - set(train_arr)
	combminus_trainandtest = set(combminus_train) - set(test_arr)
	listcomb = list(combminus_trainandtest)
	train_neg = random.sample(listcomb,len(train_arr))
	listcomb = set(listcomb) - set(train_neg)
	listcomb = list(listcomb)
	test_neg = random.sample(listcomb,len(test_arr))
	#pdb.set_trace()   


	outDeepfile = '../set1_data/outDeep_'+setno+'_'+str(j)+'.txt'
	print(outDeepfile)
	with open(outDeepfile,'r') as f:
		for line in f:
			items = line.strip().split()
			nodeid = int(items[0])
			fs_vec = items[1:]
			deepwalk_map[nodeid] = fs_vec



	positive_train_samples = list()
	negative_train_samples = list()
	positive_test_samples = list()
	negative_test_samples = list()

	#postive set train
	for (a,b) in train_arr:
		if a in deepwalk_map and b in deepwalk_map:
			node1_reps = deepwalk_map[a]
			node2_reps = deepwalk_map[b]
			features = np.append(node1_reps,node2_reps)
			positive_train_samples.append(features)
    

	for (a,b) in train_neg:
		if a in deepwalk_map and b in deepwalk_map:
			node1_reps = deepwalk_map[a]
			node2_reps = deepwalk_map[b]
			features = np.append(node1_reps, node2_reps)
			negative_train_samples.append(features)



	#postive set test
	for (a,b) in test_arr:
		if a in deepwalk_map and b in deepwalk_map:
			node1_reps = deepwalk_map[a]
			node2_reps = deepwalk_map[b]
			features = np.append(node1_reps, node2_reps)
			positive_test_samples.append(features)

	#negative set test
	for (a,b) in test_neg:
		if a in deepwalk_map and b in deepwalk_map:
			node1_reps = deepwalk_map[a]
			node2_reps = deepwalk_map[b]
			features = np.append(node1_reps, node2_reps)
			negative_test_samples.append(features)



	data_train_positive = np.array(positive_train_samples, dtype='float32')
	data_train_negative = np.array(negative_train_samples, dtype='float32')

	data_test_positive = np.array(positive_test_samples, dtype = 'float32')
	data_test_negative = np.array(negative_test_samples, dtype = 'float32')


	train_data = np.append(data_train_positive, data_train_negative, axis = 0)
	train_label = np.append(np.ones(data_train_positive.shape[0], dtype='int32'), np.zeros(data_train_negative.shape[0], dtype='int32'))
	
	test_data = np.append(data_test_positive, data_test_negative, axis = 0)
	test_label = np.append(np.ones(data_test_positive.shape[0], dtype = 'int32'), np.zeros(data_test_negative.shape[0], dtype = 'int32'))
	train_data_all = np.c_[train_data, train_label]
	test_data_all = np.c_[test_data, test_label]
	random.shuffle(train_data_all)
	random.shuffle(test_data_all)

	train_data = train_data_all[:,0:-1]
	train_labels = train_data_all[:,-1]
	test_data = test_data_all[:,0:-1]
	test_labels = test_data_all[:,-1]

	resfile.write('fold: {}'.format(str(j))+'\n')

	clf = linear_model.LogisticRegression(solver = 'sag')

	clf = clf.fit(train_data, train_labels)
	y_pred = clf.predict_proba(test_data)[:,1]
	y_pred_label = clf.predict(test_data)

	print('AUC:',roc_auc_score(test_labels,y_pred))
	print(classification_report(test_labels, y_pred_label))
    roc_score = roc_auc_score(test_labels,y_pred)
    resfile.write('AUC: {}\n'.format(roc_score))
	report = classification_report(test_labels, y_pred_label)
	resfile.write(report+'\n\n')

resfile.close()

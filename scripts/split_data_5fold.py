import numpy as np 
import pdb
from random import choice
from sklearn.cross_validation import KFold
import random
import sys

random.seed(42)


args = sys.argv
outWrapper = args[1]
mapping = args[2]
setno = args[3]
edgename = args[4]
graphmap = {}


with open(mapping) as f:
    for line in f:
        items = line.strip().split()
	    node = items[0].split('/')[-1]
        nodeid = items[1]
	    graphmap[node] = nodeid

edge_id = graphmap[edgename]
rdf = [line.strip().split() for line in open(outWrapper)]
sub = [(item[0],item[1],item[2]) for item in rdf if item[2] == edge_id]
rdf = [(item[0],item[1],item[2]) for item in rdf]
rdforig = set(rdf) 

print('All graph edges before dropping: {}'.format(len(rdforig)))
print('sub graph edges: {}'.format(len(sub)))
data = np.array(sub)
count = 0

#run 5fold cross validation, removing 20% edges from graph each time
kf = KFold(n = len(data), n_folds = 5, shuffle = True)
for train_idx, test_idx in kf:
	train_edges, test_edges = data[train_idx], data[test_idx]
	test = [(item[0],item[1],item[2]) for item in test_edges]
	rdfgraph = set(rdf) - set(test)

	edgelist = '../set1_data/edgelist_'+setno+'_'+str(count)+'.txt'
	train_file = '../set1_data/train_'+setno+'_'+str(count)+'.txt'
	test_file = '../set1_data/test_'+setno+'_'+str(count)+'.txt'
	rdfgraph = list(rdfgraph)
	rdfgraph = np.array(rdfgraph)
	np.savetxt(edgelist, rdfgraph, fmt = '%s')
	np.savetxt(train_file, train_edges, fmt = '%s')
	np.savetxt(test_file, test_edges, fmt = '%s')
        print('all graph edges after dropping 20 percent: {}'.format(len(rdfgraph)))
	print('train graph edges is: {}'.format(len(train_edges)))
	print('test graph edges is: {}'.format(len(test_edges)))	
	rdf = rdforig
	count = count + 1

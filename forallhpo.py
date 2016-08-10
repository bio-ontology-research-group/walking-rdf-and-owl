import mygene
import pdb
import subprocess
import sys

def symbol2entrezmap(filename):
    sym2entrez = {}
    with open(filename) as f:
        for line in f:
            items = line.rstrip('\n').split(':')
            sym = items[0].strip()
            entrez = items[1].strip()
            if sym not in sym2entrez:
                sym2entrez[sym] = entrez
    return sym2entrez

def hpo2genesmap(filename,genesmap):
    hpo2genes = {}
    with open(filename) as f:
        for line in f:
        	items = line.rstrip('\n').split('\t')
        	hp = items[1].strip()
        	genesym = items[0].strip()
        	if genesym in genesmap:
        		genentrez = genesmap[genesym]
			
        	if hp in hpo2genes:
        		hpo2genes[hp].append(genentrez)
        	else:
        		hpo2genes[hp] = [genentrez]
    return hpo2genes


def balance_and_process_for_weka(infile, outfile,embedding_size,genesset):
	#countneg = 0
        count = 0
	i = 0
	file1 = open(outfile,'w')
	for i in range(embedding_size):
	    file1.write('F{},'.format(i))
	file1.write('label\n')

	with open(infile) as f:
		for line in f:
			items = line.rstrip('\n').split('\t')
			genenode = items[0].split('/')
			if genenode[-1] in genesset:
				file1.write(','.join(items[1:])+',Y\n')
			if genenode[-1] not in genesset and count <= len(genesset):
				file1.write(','.join(items[1:])+ ',N\n')
				count = count+1

        return outfile



if __name__ == '__main__':

    entrezmap = symbol2entrezmap('../data/sym2entrezfull.txt')   
    hpomap = hpo2genesmap('../data/phenotypes.txt',entrezmap)
    
    for hp in hpomap.keys():
        geneset = set(hpomap[hp])
        if len(geneset) >= 20:
            outforwekafile = balance_and_process_for_weka('../data/outDeepMappedVer3Genes.txt','../wekafiles3/outWekaVer3_'+hp+'.csv',64,geneset)
            p = subprocess.call('echo '+hp+' Results: >> ../data/hpo_results.txt', shell = True)
            p = subprocess.call('java -cp ./weka.jar  weka.classifiers.functions.Logistic -t ../wekafiles3/'+outforwekafile+' -v -o >> ../data/hpo_results.txt',shell=True)
            p = subprocess.call('echo XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX >> ../data/hpo_results.txt', shell=True)

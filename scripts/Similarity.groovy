@Grab(group='colt', module='colt', version='1.2.0')

import cern.colt.matrix.*
import cern.colt.matrix.impl.*

def cosineSimilarity(DoubleMatrix1D a, DoubleMatrix1D b) {
  return a.zDotProduct(b)/Math.sqrt(a.zDotProduct(a)*b.zDotProduct(b))
}

def map = [:]
new File("embeddings-clean.txt").splitEachLine("\t") { line ->
  def id = line[0]
  DoubleMatrix1D m = new DenseDoubleMatrix1D(128)
  line[1..-1].eachWithIndex { val, i ->
    def v = new Double(val)
    m.setQuick(i, v)
  }
  map[id] = m
}

def searchvec = map[args[0]]
def results = [:]
map.each { k, v ->
  results[k] = cosineSimilarity(searchvec, v)
}
results.sort({ it.value }).each { k, v ->
  println "$k $v"
}

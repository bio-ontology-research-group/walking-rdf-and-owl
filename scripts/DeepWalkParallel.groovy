@Grab(group='org.codehaus.gpars', module='gpars', version='1.2.1')

// memory efficient and parallel (multi-node) implementation

// also, reads RDF data (N3)

import groovyx.gpars.GParsPool

def cli = new CliBuilder()
cli.with {
usage: 'Self'
  h longOpt:'help', 'this information'
  i longOpt:'input', 'input edge list', args:1, required:true
  o longOpt:'output', 'output file containing the walks',args:1, required:true
  n longOpt:'number', 'number of walks',args:1, required:true
  l longOpt:'length', 'length of walks', args:1, required:true
}
def opt = cli.parse(args)
if( !opt ) {
  //  cli.usage()
  return
}
if( opt.h ) {
    cli.usage()
    return
}

def n = new Integer(opt.n)
def l = new Integer(opt.l)

def fout = new PrintWriter(new BufferedWriter(new FileWriter(opt.o)))

Random random = new Random()


def graph = new LinkedHashMap(10000000).withDefault { new LinkedHashSet() } // Source -> Pair; edgelist representation
def nodemapping = [:]
def edgemapping = [:]
def nodecounter = 0
def edgecounter = 0

println "Building graph..."
new File(opt.i).splitEachLine("\\s+") { line ->
  def source = line[0]
  def edge = line[1]
  def target = line[2]
  def sourceid = null
  def edgeid = null
  def targetid = null
  if (nodemapping[source]) {
    sourceid = nodemapping[source]
  } else {
    sourceid = nodecounter
    nodemapping[source] = nodecounter
    nodecounter += 1
  }
  if (nodemapping[target]) {
    targetid = nodemapping[target]
  } else {
    targetid = nodecounter
    nodemapping[target] = nodecounter
    nodecounter += 1
  }
  if (edgemapping[edge]) {
    edgeid = edgemapping[edge]
  } else {
    edgeid = edgecounter
    edgemapping[edge] = edgecounter
    edgecounter += 1
  }
  Edge p = new Edge(edgeid, targetid)
  //  graph[sourceid][edgeid].add(targetid)
  graph[sourceid].add(p)
}

println "Walking..."
def walks = []
GParsPool.withPool {
  graph.keySet().eachParallel { source ->
    // walking forward
    if (graph[source].size()>0) { // if there are outgoing edges at all
      n.times {
	def walk = []
	def count = l
	def current = source
	
	walk << current
	while (count > 0) {
	  if (graph[current].size() > 0 ) { // if there are outgoing edges
	    def next = graph[current][random.nextInt(graph[current].size())] // select random outgoing edge
	    def target = next.node
	    def edge = next.edge
	    walk << edge << target
	    current = target
	  } else { // restart; use epsilon edge
	    def edge = Integer.MAX_VALUE
	    current = source
	    walk << edge << current
	  }
	  count -= 1
	}
	fout.println(walk.join(" "))
	      //walks << walk
      }
    }
  }
  
  //  walks.each { walk ->
  //    fout.println(walk.join(" "))
  //  }
}
fout.flush()
fout.close()

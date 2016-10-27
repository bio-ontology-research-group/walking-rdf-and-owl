@Grab(group='org.codehaus.gpars', module='gpars', version='1.2.1')
@Grab(group='net.sf.jung', module='jung-api', version='2.1.1')
@Grab(group='net.sf.jung', module='jung-graph-impl', version='2.1.1')
@Grab(group='net.sf.jung', module='jung-algorithms', version='2.1.1')


import groovyx.gpars.GParsPool
import edu.uci.ics.jung.graph.*
import edu.uci.ics.jung.algorithms.scoring.*

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

println "Building graph..."
DirectedGraph<Integer, LabelledEdge> g = new DirectedSparseGraph<Integer, LabelledEdge>()
new File(opt.i).splitEachLine("\\s+") { line ->
  def source = new Integer(line[0])
  def target = new Integer(line[1])
  def edge = new LabelledEdge()
  edge.id = random.nextDouble()
  edge.label = new Integer(line[2])
  g.addEdge(edge, source, target)
}

println "Computing node degree..."
def zerod = new LinkedHashSet()
g.getVertices().each { v ->
  if (g.inDegree(v) == 0 && g.outDegree(v) == 0) {
    zerod.add(v)
  }
}
println "Removing 0 degree nodes: $zerod"
zerod.each { g.removeVertex(it) }

println "Walking..."
GParsPool.withPool {
  g.getVertices().eachParallel { v ->
    def walks = []

    // walking forward
    if (g.outDegree(v) > 0) {
      n.times {
	def walk = []
	def count = l
	def current = v
	walk << current
	while (count > 0) {
	  def out = g.getOutEdges(v)
	  if (out.size()>0) {
	    def next = out[random.nextInt(out.size())]
	    def target = g.getDest(next)
	    walk << next.label << target
	  } else { // restart; use epsilon edge
	    def next = Integer.MAX_VALUE
	    def target = v
	    walk << next << target
	  }
	  count -= 1
	}
	walks << walk
      }
    }

    // walking backwards
    if (g.inDegree(v) > 0) {
      n.times {
	def walk = []
	def count = l
	def current = v
	walk << current
	while (count > 0) {
	  def out = g.getInEdges(v)
	  if (out.size()>0) {
	    def next = out[random.nextInt(out.size())]
	    def target = g.getSource(next)
	    walk << -next.label << target
	  } else { // restart; use epsilon edge
	    def next = null
	    def target = v
	    walk << next << target
	  }
	  count -= 1
	}
	//	println walk
	walks << walk
      }
    }
    walks.each { walk ->
      fout.println(walk.join(" "))
    }
  }
}
fout.flush()
fout.close()

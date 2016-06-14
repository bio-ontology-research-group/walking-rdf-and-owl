@Grapes(
    @Grab(group='org.apache.jena', module='apache-jena-libs', version='3.1.0', type='pom')
)

import org.apache.jena.rdf.model.*
import org.apache.jena.util.*

def cli = new CliBuilder()
cli.with {
usage: 'Self'
  h longOpt:'help', 'this information'
  i longOpt:'input', 'input RDF file', args:1, required:true
  o longOpt:'output', 'output file for DeepWalk algorithm',args:1, required:true
  m longOpt:'mapping-file', 'output mapping file; has numerical ids for all entities',args:1, required:true
  u longOpt:'undirected', 'build undirected graph (default: false)', args:1, required:false
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

def undirected = false
if (opt.u && opt.u != "false") {
  undirected = true
}

PrintWriter fout = new PrintWriter(new BufferedWriter(new FileWriter(opt.o)))
PrintWriter mout = new PrintWriter(new BufferedWriter(new FileWriter(opt.m)))

Model model = ModelFactory.createDefaultModel()

InputStream infile = FileManager.get().open( opt.i )

model.read(infile, null)

def counter = 1
def map = [:] // maps IRIs to ints; for input to deepwalk
model.listStatements().each { stmt ->
  def pred = stmt.getPredicate()
  def subj = stmt.getSubject()
  def obj = stmt.getObject()

  if (subj.isURIResource() && obj.isURIResource()) {
  
    if (map[pred] == null) {
      map[pred] = counter
      counter += 1
    }
    if (map[subj] == null) {
      map[subj] = counter
      counter += 1
    }
    if (map[obj] == null) {
      map[obj] = counter
      counter += 1
    }
    
    def predid = map[pred]
    def subjid = map[subj]
    def objid = map[obj]
    
    /* generate three nodes and directed edges */
    fout.println(subjid+" "+predid)
    fout.println(predid+" "+objid)
    
    // add reverse edges for undirected graph; need to double the walk length!
    if (undirected) {
      fout.println(objid+" "+predid)
      fout.println(predid+" "+subjid)
    }
  }
}

map.each { k, v ->
  mout.println("$k\t$v")
}

fout.flush()
fout.close()
mout.flush()
mout.close()

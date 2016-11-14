@Grapes([
	  @Grab(group='org.semanticweb.elk', module='elk-owlapi', version='0.4.3'),
	  @Grab(group='net.sourceforge.owlapi', module='owlapi-api', version='4.2.5'),
	  @Grab(group='net.sourceforge.owlapi', module='owlapi-apibinding', version='4.2.5'),
	  @Grab(group='net.sourceforge.owlapi', module='owlapi-impl', version='4.2.5'),
	  @Grab(group='net.sourceforge.owlapi', module='owlapi-parsers', version='4.2.5'),
	  @Grab(group='org.apache.jena', module='apache-jena-libs', version='3.1.0', type='pom')
	])

import org.semanticweb.owlapi.model.parameters.*
import org.semanticweb.elk.owlapi.ElkReasonerFactory;
import org.semanticweb.elk.owlapi.ElkReasonerConfiguration
import org.semanticweb.elk.reasoner.config.*
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.reasoner.*
import org.semanticweb.owlapi.reasoner.structural.StructuralReasoner
import org.semanticweb.owlapi.vocab.OWLRDFVocabulary;
import org.semanticweb.owlapi.model.*;
import org.semanticweb.owlapi.io.*;
import org.semanticweb.owlapi.owllink.*;
import org.semanticweb.owlapi.util.*;
import org.semanticweb.owlapi.search.*;
import org.semanticweb.owlapi.manchestersyntax.renderer.*;
import org.semanticweb.owlapi.reasoner.structural.*
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
  c longOpt:'classify', 'use an OWL reasoner to classify the RDF dataset (must be in RDF/XML) before graph generation (default: false)', args:1, required:false
  f longOpt:'format', 'RDF format; values are "RDF/XML", "N-TRIPLE", "TURTLE" and "N3" (default: RDF/XML)', args:1, required:false
  d longOpt:'ontology-directory', 'directory with ontologies to use for reasoning', args:1, required:false
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
def classify = false
if (opt.c && opt.c != "false") {
  classify = true
}
def format = "RDF/XML"
if (opt.f) {
  format = opt.f
}

PrintWriter fout = new PrintWriter(new BufferedWriter(new FileWriter(opt.o)))
PrintWriter mout = new PrintWriter(new BufferedWriter(new FileWriter(opt.m)))


def f = File.createTempFile("temp",".tmp")
if (classify) {
  OWLOntologyManager manager = OWLManager.createOWLOntologyManager()
  def oset = new LinkedHashSet()
  oset.add(manager.loadOntologyFromOntologyDocument(new File(opt.i)))
  if (opt.d) {
    new File(opt.d).eachFile { ofile ->
      oset.add(manager.loadOntologyFromOntologyDocument(ofile))
    }
  }
  OWLOntology ont = manager.createOntology(IRI.create("http://aber-owl.net/rdfwalker/t.owl"),oset)
  OWLDataFactory fac = manager.getOWLDataFactory()
  ConsoleProgressMonitor progressMonitor = new ConsoleProgressMonitor()
  OWLReasonerConfiguration config = new SimpleConfiguration(progressMonitor)
  ElkReasonerFactory f1 = new ElkReasonerFactory()
  OWLReasoner reasoner = f1.createReasoner(ont,config)
  def cc = 0
  new InferredClassAssertionAxiomGenerator().createAxioms(fac, reasoner).each { ax ->
    manager.addAxiom(ont, ax)
    cc += 1
  }
  manager.saveOntology(ont, IRI.create(f.toURI()))
  println "$cc axioms inferred."
}

def filename = null
if (classify) {
  filename = f.toURI()
} else {
  filename = opt.i
}


Model model = ModelFactory.createDefaultModel()
InputStream infile = FileManager.get().open( filename.toString() )

model.read(infile, null, format)

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
    fout.println(subjid+" "+objid+" "+predid)
    //    fout.println(predid+" "+objid)
    
    // add reverse edges for undirected graph; need to double the walk length!
    if (undirected) {
      fout.println(objid+" "+subjid+" "+predid)
      //      fout.println(predid+" "+subjid)
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

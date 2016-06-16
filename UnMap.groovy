def cli = new CliBuilder()
cli.with {
usage: 'Self'
  h longOpt:'help', 'this information'
  i longOpt:'input', 'input file (vectors from DeepWalk)', args:1, required:true
  o longOpt:'output', 'output file',args:1, required:true
  m longOpt:'mapping-file', 'mapping file used by conversion',args:1, required:true
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

def map = [:]
new File(opt.m).splitEachLine("\t") { line ->
  def uri = line[0]
  def id = line[1]
  map[id] = uri
}

PrintWriter fout = new PrintWriter(new BufferedWriter(new FileWriter(opt.o)))

def first = true
new File(opt.i).eachLine { line ->
  if (first) {first = false} else {
    line = line.split(" ")
    fout.print(map[line[0]])
    line[1..-1].each{fout.print("\t$it")}
    fout.println("")
  }
}
fout.flush()
fout.close()

def map = [:]
new File("mapping.txt").splitEachLine("\t") { line ->
  def id = line[0]
  def num = line[1]
  map[num] = id
}
print "Entity\tType"
128.times { print "\tE"+it }
println ""
new File("out.txt").splitEachLine(" ") { line ->
  if (new Integer(line[0]) < 9999999) {
    print "GO:"+line[0]+"\tGO"
  } else {
    print map[line[0]]+"\tPROTEIN"
  }
  line[1..-1].each { print "\t"+it }
  println ""
}

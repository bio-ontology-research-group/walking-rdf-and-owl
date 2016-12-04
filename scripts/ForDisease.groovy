def map = [:].withDefault { new TreeSet() }
new File("all_gene_disease_associations.tsv").splitEachLine("\t") { line ->
  if (! line[0].startsWith("#")) {
    def gname = line[1]
    def dis = line[4]
    map[dis].add(gname)
  }
}

print "Entity\tProstate cancer"
32.times { print "\tE"+it }
println ""

new File("forweka.txt").splitEachLine("\t") { line ->
  if (line[1] == "PROTEIN") {
    def prot = line[0].replaceAll("<http://bio2rdf.org/uniprot:","").replaceAll("_HUMAN>","")
    print prot
    if (prot in map["Diabetes Mellitus"] || prot in map["Retinitis Pigmentosa"]) {
      print "\tY"
    } else {
      print "\tN"
    }
    line[2..-1].each {
      print "\t$it"
    }
    println ""
  }
}

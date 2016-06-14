def dis2num = [:].withDefault { new Integer(0) }
new File("curated_gene_disease_associations.tsv").splitEachLine("\t") { line ->
  if (!line[0].startsWith("#")) {
    dis2num[line[0]] += 1
  }
}

println dis2num.sort { a, b -> a.value <=> b.value }

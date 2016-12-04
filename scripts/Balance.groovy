def pos = []
def neg = []
def header = null
def first = true
new File("prostatecancer.txt").eachLine { line ->
  if (first) {
    header = line
    first = false
  } else {
    def toks = line.split("\t")
    if (toks[1]=="Y") {
      pos << line
    } else {
      neg << line
    }
  }
}
println header

pos.each { println it }
neg = neg.sort { Math.random() }
neg[0..pos.size()].each { println it }

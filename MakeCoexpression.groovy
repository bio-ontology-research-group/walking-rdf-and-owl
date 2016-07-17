new File("data/Hsa2.v14-08.G19788-S12640.rma.mrgeo.d/").eachFile { file ->
  def gid = file.getName()
  file.splitEachLine("\t") { line ->
    def gid2 = line[0]
    def val = new Double(line[2])
    if (val >= 0.3) { // positive
      println "<http://www.ncbi.nlm.nih.gov/gene/"+gid+"> <http://example.com/pos_corr> <http://www.ncbi.nlm.nih.gov/gene/"+gid2+"> ."
    } else if (val <= -0.3) {
      println "<http://www.ncbi.nlm.nih.gov/gene/"+gid+"> <http://example.com/neg_corr> <http://www.ncbi.nlm.nih.gov/gene/"+gid2+"> ."
    }
  }
}

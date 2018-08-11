#include <math.h>
#include <string>
#include <sstream>
#include <algorithm>
#include <iterator>
#include <string.h>
#include <stdlib.h>
#include <iostream>
#include <map>
#include <set>
#include <bitset>
#include <pthread.h>
#include <fstream>
#include <climits>
#include <random>
#include <algorithm>
#include <unordered_set>
#include <unordered_map>
#include <boost/threadpool.hpp>
#include <boost/program_options.hpp>
#include <cstring>
#include <unistd.h>
#include <stdio.h>
#include <sys/types.h>

#define NUM_NODES 1000000
#define BUFFERSIZE 512
//#define THREADS 32

//#define NUMBER_WALKS 100
//#define LENGTH_WALKS 20

using namespace std;
using namespace boost::threadpool;
using namespace boost::program_options;

struct Edge {
  unsigned int edge ;
  unsigned int node ;
} ;

unordered_map<unsigned int, vector<Edge>> graph ;

random_device rd;
mt19937 rng(rd());
uniform_int_distribution<int> uni(0,INT_MAX);

int NUMBER_WALKS=100;
int LENGTH_WALKS=20;
int THREADS = 32;

ofstream fout;
boost::mutex mtx;

unordered_map<unsigned int, int> globalEdgeCount ; // edge TYPE (an int) to number of times found GLOBALLY
unordered_map<unsigned int, double> globalEdgeIC ;
unsigned int edgeCount ;

unordered_map<unsigned int, unordered_map<unsigned int, int>> localEdgeCount ; // node to edge TYPE (an int) to number of times found GLOBALLY
unordered_map<unsigned int, unordered_map<unsigned int, double>> localEdgeIC ;


void build_graph(string fname) {
  char buffer[BUFFERSIZE];
  graph.reserve(NUM_NODES) ;
  ifstream in(fname);
  while(in) {
    in.getline(buffer, BUFFERSIZE);
    if(in) {
      Edge e ;
      unsigned int source = atoi(strtok(buffer, " "));
      e.node = atoi(strtok(NULL, " ")) ;
      e.edge = atoi(strtok(NULL, " ")) ;
      graph[source].push_back(e) ;
      globalEdgeCount[e.edge]++;
      localEdgeCount[source][e.edge]++;
      edgeCount++;
    }
  }

  // computing the global edge IC
  for ( auto it = globalEdgeCount.begin(); it != globalEdgeCount.end(); ++it ) {
    unsigned int gEdge = it -> first ;
    int gEdgeCount = it -> second ;
    globalEdgeIC[gEdge] = -log(gEdgeCount/edgeCount);
  }
  // computing the local edge IC
  for ( auto it = localEdgeCount.begin(); it != localEdgeCount.end(); ++it ) {
    unsigned int lNode = it -> first ;
    unordered_map<unsigned int, int> m = it -> second ;
    for ( auto it2 = m.begin(); it2 != m.end(); ++it2 ) {
      unsigned int lEdge = it2 -> first ;
      int lEdgeCount = it2 -> second ;
      int lEdgeTotalCount = graph[lNode].size();
      localEdgeIC[lNode][lEdge] = -log(lEdgeCount / lEdgeTotalCount);
    }
  }
}

void walk(unsigned int source) {
  vector<vector<unsigned int>> walks(NUMBER_WALKS) ;
  if (graph[source].size()>0) { // if there are outgoing edges at all
    for (int i = 0 ; i < NUMBER_WALKS ; i++) {
      int count = LENGTH_WALKS ;
      int current = source ;
      walks[i].push_back(source) ;
      while (count > 0) {
	if (graph[current].size() > 0 ) { // if there are outgoing edges
	  unsigned int r = uni(rng) % graph[current].size();
	  Edge next = graph[current][r] ;
	  int target = next.node ;
	  int edge = next.edge ;
	  walks[i].push_back(edge) ;
	  walks[i].push_back(target) ;
	  current = target ;
	} else {
	  int edge = INT_MAX ; // null edge
	  current = source ;
	  walks[i].push_back(edge) ;
	  walks[i].push_back(current) ;
	}
	count-- ;
      }
    }
  }
  stringstream ss;
  for(vector<vector<unsigned int>>::iterator it = walks.begin(); it != walks.end(); ++it) {
    for(size_t i = 0; i < (*it).size(); ++i) {
      if(i != 0) {
	ss << " ";
      }
      ss << (*it)[i];
    }
    ss << "\n" ;
  }
  mtx.lock() ;
  fout << ss.str() ;
  fout.flush() ;
  mtx.unlock() ;
}

void generate_corpus() {

  pool tp(THREADS);
  for ( auto it = graph.begin(); it != graph.end(); ++it ) {
    unsigned int source = it -> first ;
    tp.schedule(boost::bind(&walk, source ) ) ;
  }
  cout << tp.pending() << " tasks pending." << "\n" ;
  tp.wait() ;
}

int main (int argc, char *argv[]) {

  options_description desc("Options:");
  desc.add_options()
    ("help", "produce help message")
    ("version,v", "print version string")
    ("walk-num,w", value<int>(), "number of walks")
    ("walk-length,l", value<int>(), "walk length")
    ("graph,g", value<string>(), "graph filename")
    ("output,o", value<string>(), "output filename")
    ("threads,t", value<int>(), "number of threads to use")
    ;
  variables_map vm;
  store(parse_command_line(argc, argv, desc), vm);
  notify(vm);
  if (vm.count("help")) {
    cout << desc << "\n";
    return 1;
  }
  NUMBER_WALKS = vm["walk-num"].as<int>();
  LENGTH_WALKS = vm["walk-length"].as<int>();
  THREADS = vm["threads"].as<int>();
    
  cout << "Building graph from " << vm["graph"].as<string>() << "\n" ; //argv[1]
  build_graph(vm["graph"].as<string>());
  cout << "Number of nodes in graph: " << graph.size() << "\n" ;
  cout << "Writing walks to " << vm["output"].as<string>() << "\n" ; // argv[2]
  fout.open(vm["output"].as<string>()) ;
  generate_corpus() ;
  fout.close() ;
}

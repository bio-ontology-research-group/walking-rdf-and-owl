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

// struct Edge {
//   unsigned int edge ;
//   unsigned int node ;
// } ;

//unordered_map<unsigned int, vector<Edge>> graph ;
unordered_map<unsigned int, unordered_map<unsigned int, vector<unsigned int>>> graph ; // node -> edge -> vector<nodes>

random_device rd;
mt19937 rng(rd());
uniform_int_distribution<int> uni(0,INT_MAX);

int NUMBER_WALKS;
int LENGTH_WALKS;
int THREADS;

ofstream fout;
boost::mutex mtx;

unordered_map<unsigned int, int> globalEdgeCount ; // edge TYPE (an int) to number of times found GLOBALLY
unordered_map<unsigned int, double> globalEdgeIC ;
unsigned int edgeCount ;

unordered_map<unsigned int, unordered_map<unsigned int, int>> localEdgeCount ; // node to edge TYPE (an int) to number of times found GLOBALLY
unordered_map<unsigned int, unordered_map<unsigned int, double>> localEdgeIC ;

unordered_map<unsigned int, int> nodeDegree ; 

void build_graph(string fname) {
  char buffer[BUFFERSIZE];
  graph.reserve(NUM_NODES) ;
  ifstream in(fname);
  while(in) {
    in.getline(buffer, BUFFERSIZE);
    if(in) {
      unsigned int source = atoi(strtok(buffer, " "));
      unsigned int target = atoi(strtok(NULL, " ")) ;
      unsigned int edge = atoi(strtok(NULL, " ")) ;
      graph[source][edge].push_back(target) ;
      globalEdgeCount[edge]++;
      localEdgeCount[source][edge]++;
      nodeDegree[source]++;
      edgeCount++;
    }
  }

  // computing the global edge IC
  for ( auto it = globalEdgeCount.begin(); it != globalEdgeCount.end(); ++it ) {
    unsigned int gEdge = it -> first ;
    int gEdgeCount = it -> second ;
    globalEdgeIC[gEdge] = -log((double)gEdgeCount/(double)edgeCount);
  }
  // computing the local edge IC
  for ( auto it = localEdgeCount.begin(); it != localEdgeCount.end(); ++it ) {
    unsigned int lNode = it -> first ;
    unordered_map<unsigned int, int> m = it -> second ;
    for ( auto it2 = m.begin(); it2 != m.end(); ++it2 ) {
      unsigned int lEdge = it2 -> first ;
      int lEdgeCount = it2 -> second ;
      int lEdgeTotalCount = nodeDegree[lNode];
      if (lEdgeCount < lEdgeTotalCount) {
	localEdgeIC[lNode][lEdge] = -1 * log((double)lEdgeCount/(double)lEdgeTotalCount);
      } else {
	localEdgeIC[lNode][lEdge] = 1 ;
      }
      //cout << "count: " << lEdgeCount << " Total: " << lEdgeTotalCount << " IC: " << localEdgeIC[lNode][lEdge] << " Val: " << lEdgeCount / lEdgeTotalCount << "\n" ;
    }
  }
}

void walk(unsigned int source) {
  vector<vector<unsigned int>> walks(NUMBER_WALKS) ;
  if (nodeDegree[source]>0) { // if there are outgoing edges at all
    for (int i = 0 ; i < NUMBER_WALKS ; i++) {
      int count = LENGTH_WALKS ;
      int current = source ;
      walks[i].push_back(source) ;
      while (count > 0) {
	if (nodeDegree[current] > 0 ) { // if there are outgoing edges
	  // setting up the distribution for local stratification
	  vector<double> v ;
	  map<int, unsigned int> evmap ; // corresponds to v in containing the actual edge
	  int j = 0 ;
	  for ( auto it = graph[current].begin(); it != graph[current].end(); ++it ) {
	    unsigned int edge = it->first;
	    v.push_back(localEdgeIC[current][edge]) ;
	    evmap[j] = edge;
	    j++;
	  }
	  discrete_distribution<unsigned int> distribution(v.begin(), v.end()) ;
	  // selecting the edge (locally stratified)
	  int selectedEdge = evmap[distribution(rng)];
	  unsigned int next = uni(rng) % graph[current][selectedEdge].size();
	  walks[i].push_back(selectedEdge) ;
	  walks[i].push_back(graph[current][selectedEdge][next]) ;
	  current = graph[current][selectedEdge][next] ;
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
  try {
    desc.add_options()
      ("help,h", "produce help message")
      ("version,v", "print version string")
      ("walk-num,w", value<int>()->default_value(50), "number of walks (default: 50)")
      ("walk-length,l", value<int>()->default_value(10), "walk length (default: 10)")
      ("threads,t", value<int>()->default_value(1), "number of threads to use (default: 1)")
      ("graph,g", value<string>()->required(), "edgelist filename")
      ("output,o", value<string>()->required(), "output filename")
      ;
    variables_map vm;
    store(parse_command_line(argc, argv, desc), vm);
    if (vm.count("help")>0) {
      cout << desc << "\n";
      return 1;
    }
    notify(vm);
  
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
  } catch(std::exception& e) {
    cerr << "Error: " << e.what() << "\n";
    cerr << desc << "\n" ;
    return false;
  } catch(...) {
    cerr << "Unknown error!" << "\n";
    return false;
  }
}

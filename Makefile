all: deepwalk

deepwalk: deepwalk.cc
	g++ -Ofast -funroll-loops -Wall -std=c++0x deepwalk.cc -o deepwalk -lboost_thread -lboost_system -lpthread

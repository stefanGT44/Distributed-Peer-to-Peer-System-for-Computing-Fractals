# Distributed-Peer-to-Peer-System-for-Computing-Fractals
A distributed peer to peer system for computing fractals, with <b>load balancing</b> (generating fractal images). 

## Overview
The peer to peer network is a complete graph where communication between nodes is <b>asynchronous and non-FIFO</b>.<br>
The system can <b>concurrently</b> compute one or more fractals. <br>
Fractals are computed using the [Chaos game](https://en.wikipedia.org/wiki/Chaos_game) algorithm. <br>
When nodes join/leave the network or when new fractal jobs are initiated, <b>the system reorganizes (rebalances) the workload</b>. <br>
A <b>bootstrap node/server</b> is used to connect/disconnect nodes to/from the network. <br>
The user can request for an image to be generated (the entire fractal, or a specified part of it) using the computed data up until that moment. <br>
The user can also ask for status information of all nodes in the system, which lists all active nodes with their work progress up until that moment. <br>
The system supports <b>scripted launching</b>, for running multiple nodes simultaneously and providing input commands to nodes using text files as input. <br>

![Alt text](slika6.png?raw=true "")<br>
![Alt text](slika3.png?raw=true "")<br>

## System details
To run the system, a MultipleServentStarter class is provided.<br> This class reads the configuration file and starts separate Node programs using the <b>ProcessBuilder</b>.<br>
For each node program the System.out, System.err and System.in are redirected to files /output/serventID_out.txt, /error/serventID_err.txt and /input/serventID_in.txt, to allow the user to supply all nodes with input commands simultaneously.<br>
The user can also interact with nodes using the CLI (command line interface).<br>
The sending of each message is <b>delayed</b> by a small random amount to <b>simulate a realistic distributed system</b> (because the system is tested locally on one machine).

### The network
The boot node keeps track of all nodes in the network, and has a known static address. <br>
New nodes in order to join the network must contact the boot node which provides them with a random node from the network. <br>
The new node exchanges information with the provided node, and a series of update messages are sent in multiple rounds to notify all the existing nodes about the new node. <br>
Finally the new node contacts the boot node to finish the joining process. <br>
The process of leaving the network is similar to the joining process.

### Load balancing

Each node is assigned a fractal region which it computes. This region depends on the number of active nodes in the network and the number of verticies of the polygon from which the fractal is calculated.<br>
A fracal region is the entire polygon or a sub-polygon. For an example:
If there are 3 nodes in the network and a triangle fractal is being computed, each node gets a different sub-triangle as a region to compute. <br>

![Alt text](images/frac1.png?raw=true "")<br><br>

If a 4th node joins the network, no rebalacing occurs. The new node is idle. Two new nodes are required to subdivide an existing triangle (sub-triangle). <br>
After the 5th node joins the network, a sub-triangle is divided into 3 smaller sub-triangles. Subdivision in to depth occurs only if all regions are on the same depth level.<br>
![Alt text](images/frac2.png?raw=true "")<br><br>

If <b>multiple fractals</b> are computed <b>concurrently</b>, nodes are equally split among them, and the workload for each fractal is then organized using the above explained method. 

### Supported commands:
* status \[X \[id\]\] - 
* start \[X]
* result X \[id\]
* stop

# README IN DEVELOPMENT

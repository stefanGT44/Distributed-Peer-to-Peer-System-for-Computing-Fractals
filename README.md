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
The sending of each message is <b>delayed</b> by a small random amount to <b>simulate a realistic distributed system</b> (because the system is tested locally on one machine).<br>

### The network
The bootstrap node keeps track of all nodes in the network, and has a known static address. <br>
New nodes in order to join the network must contact the boot node which provides them with a random node from the network. <br>
The new node exchanges information with the provided node, after whch a series of update messages are sent in multiple rounds to notify all the existing nodes about the new node. <br>
Finally the new node contacts the boot node to finish the joining process. <br>
The process of leaving the network is similar to the joining process. <br>

### Load balancing

Each node is assigned a fractal region which it computes. This region depends on the number of active nodes in the network and the number of vertices of the polygon from which the fractal is calculated.<br>
A fractal region is the entire polygon or a sub-polygon. For an example:
If there are 3 nodes in the network and a triangle fractal is being computed, each node gets a different sub-triangle as a region to compute. <br>

![Alt text](images/frac1.png?raw=true "")<br><br>

If a 4th node joins the network, no re-balancing occurs. The new node is idle. Two new nodes are required to subdivide an existing triangle (sub-triangle). <br>
After the 5th node joins the network, a sub-triangle is divided into 3 smaller sub-triangles. Subdivision in to a new depth level occurs only if all regions are on the same depth level.<br>
![Alt text](images/frac2.png?raw=true "")<br><br>

If <b>multiple fractals</b> are computed <b>concurrently</b>, nodes are equally split among them, and the workload for each fractal is then organized using the above explained method. 

#### Real example:
Seven nodes are active in the system and working on a triangle fractal. <br>
The status command has returned the fractal region ID and number of iterations for each node. <br>
In this example thanks to the region IDs we can see that Servent\[1\] is working on a sub-triangle of the original triangle, and the rest six servents are working on the six remaining sub-sub-triangles. <br><br>
![Alt text](images/dedeasd.png?raw=true "")<br><br>

After starting a square fractal concurrently, the job is rebalanced and now 3 nodes are working on 3 sub-triangles of the original triangle, and 4 nodes are working on the 4 sub-squares of the original square. <br><br>
![Alt text](images/dedeasd1.png?raw=true "")<br>

### Supported commands:
Arguments in \[\] are optional
* status \[X \[id\]\] - Shows the work progress of each active node in the system (the fractal job and the fractal region that the node is working on, and the number of iterations it has done). The optional argument X specifies the fractal, and optional argument id the region of the specified fractal X.
* start X - Starts the job X from the configuration file.
* result X \[id\] - Generates a PNG image of the specified fractal, or a specified region of the specified fractal if the id is provided.
* stop - Disconnects a node from the system and shuts down the node program. If provided to the MultipleServentStarter class, the entire system shuts down.

### Properties file (config):
Parameters are read and set during application launch and cannot be changed during operation.<br><br>
File structure:<br><br>
bootstrap=192.168.0.17,2000 - address and port of the bootstrap node<br>
weaklimit=1000 - not used<br>
stronglimit=2000 - not used<br>
servent_count=7 - number of nodes to start<br>
servent0=1100 - list of nodes with their port number<br>
servent1=1200<br>
servent2=1300<br>
servent3=1400<br>
servent4=1600<br>
servent5=1700<br>
servent6=1800<br>
servent7=1900<br>
job_count=4 - list of jobs which the user can start using the CLI<br>
job0_name=trougao<br>
job0_N=3 - number of verticies<br>
job0_P=0.5 - chaos game algorithm parameter<br>
job0_WH=1920x1080 - image resolution<br>
job0_points=300,100;960,900;1620,100 - verticy coordinates<br>
job1_name=kvadrat<br>
job1_N=4<br>
job1_P=0.5<br>
job1_WH=1920x1080<br>
job1_points=300,100;300,900;1620,900;1620,100<br>
job2_name=petougao<br>
job2_N=5<br>
job2_P=0.3<br>
job2_WH=1920x1080<br>
job2_points=670,990;1250,990;1250,390;670,390;90,690<br>

## Sidenote
This project was an assignment as a part of the course - Concurrent and Distributed Systems during the 8th semester at the Faculty of Computer Science in Belgrade. All system functionalities were defined in the assignment specifications.

## Contributors
- Stefan Ginic - <stefangwars@gmail.com>

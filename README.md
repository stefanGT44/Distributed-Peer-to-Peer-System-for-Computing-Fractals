# Distributed-Peer-to-Peer-System-for-Computing-Fractals
A distributed peer to peer system for computing fractals, with <b>load balancing</b> (generating fractal images). 

## Overview
The peer to peer network is a complete graph where communication between nodes is <b>asynchronous and non-FIFO</b>.<br>
The system can <b>concurrently</b> compute one or more fractals. <br>
Fractals are computed using the [Chaos game](https://en.wikipedia.org/wiki/Chaos_game) algorithm. <br>
When nodes join/leave the network, if it is optimal, <b>the system reorganizes (balances) the workload</b>. <br>
Nodes contact the <b>bootstrap node/server</b> for joining and leaving the network. <br>
The user can request for an image to be generated (the entire fractal, or a specified part of it) using the computed data up until that moment. <br>
The user can also ask for status information of all nodes in the system, which lists all active nodes with their work progress up until that moment. <br>

![Alt text](slika6.png?raw=true "")<br>
![Alt text](slika3.png?raw=true "")<br>

## System details

### Load balancing

Each node is assigned a fractal region which it computes. This region depends on the number of active nodes in the network and the number of verticies of the polygon from which the fractal is calculated.<br>
A fracal region is the entire polygon or a sub-polygon. For an example:
If there are 3 nodes in the network and a triangle fractal is being computed, each node gets a different sub-triangle as a region to compute. <br>

![Alt text](images/frac1.png?raw=true "")<br><br>

If a 4th node joins the network, no rebalacing occurs, 2 new nodes are required to subdivide an existing triangle (sub-triangle). <br>
After the 5th node joins the network, a sub-triangle is divided into 3 smaller sub-triangles.<br>
![Alt text](images/frac2.png?raw=true "")<br><br>

If <b>multiple fractals</b> are computed <b>concurrently</b>, nodes are equally split among them, and the workload for each fractal is then organized using the above exmplained method. 

# README IN DEVELOPMENT

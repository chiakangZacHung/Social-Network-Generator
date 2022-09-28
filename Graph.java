package org.example;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.HashMap;

public class Graph {

    //private final int V;
    //private final List<List<Integer>> adj;
    HashMap<String,LinkedList > adj;

    public Graph(int V) {
        adj = new HashMap<String, LinkedList>();
    }
    void addEdge(String source, String dest) {
        adj.computeIfAbsent(source,x->new LinkedList<String>()).add(dest);
    }
    HashMap<String,LinkedList > getAdj(){
        return adj;
    }

}

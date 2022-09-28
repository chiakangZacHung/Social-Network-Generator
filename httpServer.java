package org.example;
/**
 * @author Chiakang Hung
 */
import java.net.*;
import java.io.*;

import org.json.JSONArray;
import com.paypal.digraph.parser.GraphEdge;
import com.paypal.digraph.parser.GraphNode;
import com.paypal.digraph.parser.GraphParser;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

public class httpServer{
    //the post-lead nodes
    static ArrayList result=new ArrayList<>();
    //frequency map that counts the occurrences of each node
    static Map<String,Integer> frequencies = new HashMap<>();
    //the occurrences of each node
    static int count=0;
    /**
     *find nodes
     * @param node current node
     * @param endNode the exit node in the graph
     * @param path it stores the nodes in the current path
     * @param adj adjacency matrix of the  graph
     */
    static void dfs(String node, String endNode,ArrayList<String>path, HashMap<String, LinkedList> adj) {
        LinkedList<String> listofNodes = adj.get(node);
        //if current node equals end node, add the path to the frequency map and return
        if (node.equals(endNode)){
            ArrayList copy_path=new ArrayList<>();
            for(String item: path) {
                copy_path.add(item);
                frequencies.put(item, frequencies.getOrDefault(item, 0) + 1);
            }
            count+=1;
            return;
        }
        //iterate each to-node correponding to the current node and dfs
        for (String currentNode : listofNodes) {
            if (!path.contains(currentNode)) {
                path.add(currentNode);
                dfs(currentNode,endNode,path, adj);
                path.remove(currentNode);
            } else {
                return;
            }

        }
    }

    /**
     * read inputStream
     * @param inputStream
     * @return
     * @throws IOException
     */
    public static String read(InputStream inputStream) throws IOException {
        StringBuilder result = new StringBuilder();
        do {
            result.append((char) inputStream.read());
        } while (inputStream.available() > 0);
        return result.toString();
    }

    /**
     * send the response to client and write to the log file
     * @param clientSocket
     * @param input
     * @throws IOException
     */
    public static void outputToClient(Socket clientSocket,String input) throws IOException {
        OutputStream clientOutput = clientSocket.getOutputStream();
        clientOutput.write("HTTP/1.1 200 OK\r\n".getBytes());
        clientOutput.write("\r\n".getBytes());
        //send response to client
        clientOutput.write(input.getBytes());
        //write to log
        BufferedWriter writer = new BufferedWriter(new FileWriter("./log.txt"));
        writer.write(input);
        writer.close();
        clientOutput.write("\r\n\r\n".getBytes());
        clientOutput.flush();
        System.err.println("Client connection closed!");
        clientOutput.close();
    }

    /**
     * preprocess input from the client
     * @param inputLines input from the client
     * @return
     */
    public static String[] preprocess(String inputLines){
        String newline = System.getProperty("line.separator");
        String[] splitArr=inputLines.substring(inputLines.lastIndexOf("\n")).replace("e1","").replace("e2","").replace("h","").replaceAll("\\s","").split(",");
        String e1=splitArr[0].split(":")[1].replace("\"","");
        String e2=splitArr[1].split(":")[1].replace("\"","");
        String h=splitArr[2].split(":")[1].replace("\"","");
        String[] input_array=inputLines.split(",");
        String parsedInput=("digraph"+input_array[input_array.length-1].split("digraph")[1].split("}")[0]+"}").replace("\\n",newline);
        String [] res= {e1,e2,h,parsedInput};
        return res;
    }
    public static void main(String[] args) throws IOException {
        int port = 10000;
        ServerSocket serverSocket = new ServerSocket(port);
        JSONArray jsonArray = new JSONArray();
        System.err.println("Server is running on port: "+port);
        while(true){
            Socket clientSocket = serverSocket.accept();
            System.err.println("Client connected");
            String inputLines=read(clientSocket.getInputStream());
            //split
            try {
            String[] inputArr= preprocess(inputLines);
            String parsedInput=inputArr[3];
            //process graph
            InputStream stream = new ByteArrayInputStream(parsedInput.getBytes
                    (Charset.forName("UTF-8")));
            GraphParser parser = new GraphParser(stream);
            Map<String, GraphNode> nodes = parser.getNodes();
            Map<String, GraphEdge> edges = parser.getEdges();
                //initialize graph
                Graph graph = new Graph(nodes.size());
                //add edges to graph
                for (GraphEdge edge : edges.values()) {
                    graph.addEdge(edge.getNode1().getId(), edge.getNode2().getId());
                }
                //create adjacency matrix from graph
                HashMap<String,LinkedList>adj=graph.getAdj();
                //when exit node==h
                if(inputArr[2].equals(inputArr[1])){
                    result.add(inputArr[2]);
                }
                dfs(inputArr[2],inputArr[1],new ArrayList<>(),adj);


                //compute post-lead nodes
                for(String key:frequencies.keySet()){
                    if (frequencies.get(key)==count){
                        result.add(key);
                    }
                }
                System.out.println(result);
            }
            catch(Exception ex){
                outputToClient(clientSocket,"the input is not parsable");
            }

            outputToClient(clientSocket,("{"+result.toString().replace("[","").replace("]","")+"}"));
        }
    }
}


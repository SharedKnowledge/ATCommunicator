package htw_berlin.ba_timsitte.network;

import android.util.Log;

import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicInteger;

public class AODVNetworkProtocol {
    private static AODVNetworkProtocol instance = null;
    private ArrayList<RouteEntry> routingTable = new ArrayList<>();

    public ArrayList<RouteEntry> getRoutingTable() {
        return routingTable;
    }

    public ArrayList<Node> getNodeList() {
        return nodeList;
    }

    private ArrayList<Node> nodeList = new ArrayList<>();
    private AtomicInteger id = new AtomicInteger(0);
    private String TAG = "AODVNetworkProtocol";



    /**
     * Singleton constructor
     */
    private AODVNetworkProtocol(){

    }

    /**
     *
     * @return
     */
    public static AODVNetworkProtocol getInstance(){
        if (instance == null){
            instance = new AODVNetworkProtocol();
        }
        return instance;
    }

    public void broadcast(){
        //
    }

    public void discover(String message){
        // discover answer

    }

    /*
    Route Requests (RREQ)
     */
    public void routeRequest(){

    }

    /*
    Route Replies (RREP)
     */
    public void routeReply(){

    }

    /*
    Route Errors (RERR)
     */
    public void routeError(){

    }

    /**
     *
     */
    public void addNewNode(String name, double lat, double lon){
        // checking for overflow
        if (id.get() == Integer.MAX_VALUE){
            // Message pop up that no more Nodes can be added
        }
        int newId = id.incrementAndGet();
        Node node = new Node(newId, name, lat, lon);

        nodeList.add(node);
        Log.i(TAG, "Node: " + name + " with ID " + newId + " and GP " + lat + "/" + lon + " added.");
    }
}

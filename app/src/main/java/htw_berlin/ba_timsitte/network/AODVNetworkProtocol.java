package htw_berlin.ba_timsitte.network;

import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicInteger;

public class AODVNetworkProtocol {
    private static final AODVNetworkProtocol instance = new AODVNetworkProtocol();
    private ArrayList<RouteEntry> routingTable = new ArrayList<>();
    private ArrayList<Node> nodeList = new ArrayList<>();
    private AtomicInteger id;

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
        return instance;
    }

    private void broadcast(){
        //
    }

    private void discover(String message){
        // discover answer

    }

    /*
    Route Requests (RREQ)
     */
    private void routeRequest(){

    }

    /*
    Route Replies (RREP)
     */
    private void routeReply(){

    }

    /*
    Route Errors (RERR)
     */
    private void routeError(){

    }

    /**
     *
     */
    private void addNewNode(String name){
        // checking for overflow
        if (id.get() == Integer.MAX_VALUE){
            // Message pop up that no more Nodes can be added
        }
        int newId = id.incrementAndGet();
        Node node = new Node(newId);

        nodeList.add(node);
    }
}

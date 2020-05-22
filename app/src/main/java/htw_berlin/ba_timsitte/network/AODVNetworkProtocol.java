package htw_berlin.ba_timsitte.network;

import android.os.Handler;
import android.util.Log;

import java.io.IOException;
import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicInteger;

import htw_berlin.ba_timsitte.R;
import htw_berlin.ba_timsitte.communication.BluetoothService;
import htw_berlin.ba_timsitte.communication.Constants;

public class AODVNetworkProtocol {

    private String TAG = "AODVNetworkProtocol";

    private ArrayList<RouteEntry> routingTable = new ArrayList<>();
    private ArrayList<Node> nodeList = new ArrayList<>();
    private AtomicInteger id = new AtomicInteger(0);
    private final Handler mHandler;
    private int mState;
    private int mNewState;

    private String address = "FFFF";

    public static final int STATE_NONE = 0;       // we're doing nothing
    public static final int STATE_LISTEN = 1;     // now listening for incoming messages
    public static final int STATE_BUSY = 2;       // performing
    public static final int STATE_WRITING = 3;    // writing to the socket

    private final String end = "\r\n";
    private final String answer = "AT, OK" + end;

    BluetoothService mBluetoothService = null;


    /**
     *
     * @param handler
     * @param mBluetoothService
     */
    public AODVNetworkProtocol(Handler handler, BluetoothService mBluetoothService){
        Log.d(TAG, "AODVNetworkProtocol");
        mHandler = handler;
        mState = STATE_NONE;
        mNewState = mState;
        this.mBluetoothService = mBluetoothService;
    }

    /**
     * Update UI title according to the current state of the chat connection
     */
    private synchronized void updateUserInterfaceTitle() {
        mState = getState();
        Log.d(TAG, "updateUserInterfaceTitle() " + mNewState + " -> " + mState);
        mNewState = mState;

        // Give the new state to the Handler so the UI Activity can update
        mHandler.obtainMessage(AODVConstants.PROTOCOL_STATE_CHANGE, mNewState, -1).sendToTarget();
    }

    private class ownAddressThread extends Thread {
        String str = "AT+ADDR=" + address + end;
        public ownAddressThread(String address){
            Log.d(TAG, "ownAddressThread - own address: " + address);
            setAddress(address);
        }

        public void run(){
            byte[] send = str.getBytes();
            mBluetoothService.write(send);
        }
    }

    /**
     * Return the current connection state.
     */
    public synchronized int getState() {
        return mState;
    }

    private class BroadcastThread extends Thread {

        String str = "AT+DEST=FFFF" + end;

        public BroadcastThread(){
            Log.d(TAG, "BroadcastThread: constructor");
        }

        public void run() {
            byte[] send = str.getBytes();
            mBluetoothService.write(send);
        }

        public void write(){

        }

    }



    private class DiscoverThread extends Thread {

        public void run() {

        }
    }

    private class SendThread extends Thread {
        String message;
        String destination;
        int message_length;

        public void run() {
            // AT+DEST
            // wait
            // AT+SEND
            //wait
            // SENDED

        }

    }




    /**
     * Node a wants to send a packet to Node b (which is out of reach
     */
    private class RouteEstablishmentThread extends Thread {
        int destination; // hexa
        String broadcast = "AT+DEST=FFFF" + end;
        String rreq = "AT+SEND";
        String message;

        public RouteEstablishmentThread(int destination, String message){
            this.destination = destination;
            this.message = message;
        }

        @Override
        public void run() {

        }
    }

    /**
     * Route Requests (RREQ)
     * @param destination
     */
    public void transmitRouteRequest(String destination, int destSequence, String source, int sourceSequence, int hopCount){

    }

    public void receiveRouteRequest(String receivedFrom, String destination, int destSequence, String source, int sourceSequence, int hopCount){
        // make a reverse entry for source node
        addRouteEntry(source, receivedFrom, hopCount + 1);

        // check whether we have routes to the destination in our routingTable
        RouteEntry returnRouteEntry = lookUpRouteEntry(destination);
        if (returnRouteEntry != null){

        } else {
            transmitRouteRequest(destination, destSequence, source, sourceSequence, hopCount);
        }
    }

    public void transmitRouteReply(){

    }

    public void receiveRouteReply(){

    }

    /**
     *
     * @param destination
     * @param next
     * @param hopCount
     */
    public void addRouteEntry(String destination, String next, int hopCount){
        boolean exists = false;
        RouteEntry routeEntry = new RouteEntry(0, destination, next, hopCount);
        // check if routeEntry exists
        for (RouteEntry entry : routingTable){
            if (entry.equals(routeEntry)){
                exists = true;
                break;
            }
        }
        if (!exists){
            routingTable.add(routeEntry);
        }
    }

    /**
     * Checks if RouteEntry exists and if so returns the one with the least hops
     * @param destination
     * @return destination address of Node
     */
    public RouteEntry lookUpRouteEntry(String destination){
        int hopCount = 0;
        int newHopCount;
        boolean first = true;
        RouteEntry returnRouteEntry = null;
        for (RouteEntry routeEntry : routingTable){
            if (routeEntry.getDestination().equals(destination)){
                newHopCount = routeEntry.getHop_count();
                if (first){
                    hopCount = newHopCount;
                    first = false;
                    returnRouteEntry = routeEntry;
                } else {
                    if (newHopCount < hopCount){
                        returnRouteEntry = routeEntry;
                    }
                }
            }
        }
        return returnRouteEntry;
    }

    /**
     * Updates the Node list. Either by adding a new Node or by refreshing the TTL timer/active = true
     * @param addr
     */
    public void updateNodeListWithNode(String addr){
        Node node = lookUpNode(addr);
        // it doesn't exist yet
        if (node == null){
            Log.i(TAG, "updateNodeListWithNode: New Node added: " + addr);
            Node newNode = new Node(addr);
            nodeList.add(newNode);
        } else {
            Log.i(TAG, "updateNodeListWithNode: Existing Node refreshed.");
            node.setIs_active(true);
        }
    }

    /**
     * Checks whether a Node already exists or not and returns Node
     * @param addr
     * @return Node in case it already exists
     */
    public Node lookUpNode(String addr) {
        for (Node node : nodeList) {
            if (node.getAddr().equals(addr)) {
                return node;
            }
        }
        return null;
    }

    // ----------------- Getter/Setter -----------------

    public ArrayList<RouteEntry> getRoutingTable() {
        return routingTable;
    }

    public ArrayList<Node> getNodeList() {
        return nodeList;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

}

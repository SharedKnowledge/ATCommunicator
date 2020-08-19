package htw_berlin.ba_timsitte.network;

import android.os.Handler;
import android.util.Log;

import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicInteger;

import htw_berlin.ba_timsitte.communication.BluetoothService;

public class AODVNetworkProtocolfail {

    private String TAG = "AODVNetworkProtocolfail";

    private ArrayList<Route> routingTable = new ArrayList<>();
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

    public AODVNetworkProtocolfail(){
        mHandler = new Handler();
    }

    /**
     *
     * @param handler
     * @param mBluetoothService
     */
    public AODVNetworkProtocolfail(Handler handler, BluetoothService mBluetoothService){
        Log.d(TAG, "AODVNetworkProtocolfail");
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
    }

    /**
     * Return the current connection state.
     */
    public synchronized int getState() {
        return mState;
    }

    /**
     * Set up your own address. Needs to be done in the beginning
     */
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
     * Set up a new
     */
    private class SetNewDestination extends Thread{
        String str = "";
        public SetNewDestination(String destination){
            str = "AT+DEST=" + destination + end;
        }

        public void run() {
            byte[] send = str.getBytes();
            mBluetoothService.write(send);
        }
    }

    // ----------------- something -----------------

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

    public void handleAODVMessage(String source, String message){
        String[] parts = message.split("::");
        switch (parts[0]) {
            case "BRCT":
                Log.d(TAG, "handleAODVMessage: Broadcast message recognized.");
                if (validBRCTFormat(parts)){
                    receiveBroadcast(source, parts[1], parts[2]);
                } else {
                    Log.w(TAG, "Invalid RREQ format.");
                }
                break;
            case "RREQ":
                Log.d(TAG, "handleAODVMessage: RREQ message recognized.");
                if (validRREQFormat(parts)){
                    // RREQ rreq = new RREQ(parts[1], parts[2], parts[3])
                } else {
                    Log.w(TAG, "Invalid RREQ format.");
                }
                break;
            case "RREP":
                Log.d(TAG, "handleAODVMessage: RREP message recongized.");
                if (validRREPFormat(parts)){

                } else {
                    Log.w(TAG, "Invalid RREP format.");
                }
                break;
            case "RERR":
                Log.d(TAG, "handleAODVMessage: RERR message recongized.");
                if (validRERRFormat(parts)){

                } else {
                    Log.w(TAG, "Invalid RERR format.");
                }
                break;
            default:
                Log.d(TAG, "handleAODVMessage: No AODV message recognized.");
                break;
        }
    }

    // ----------------- Broadcast -----------------

    /**
     * Receiving BRCT format must be: "BRCT::"
     * @param message
     * @return
     */
    public boolean validBRCTFormat(String[] message){
        if (message.length == 3){
            try {
                double lat = Double.parseDouble(message[1]);
                double lon = Double.parseDouble(message[2]);
            } catch (NumberFormatException e){
                return false;
            }
            return true;
        }
        return false;
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

    public void receiveBroadcast(String source, String lat_str, String lon_str){
        double lat = 0;
        double lon = 0;
        try {
            lat = Double.parseDouble(lat_str);
            lon = Double.parseDouble(lon_str);
        } catch (NumberFormatException e){
            Log.e(TAG, "receiveBroadcast: ", e);
        }

        Node node = new Node(source, lat, lon);
        updateNodeList(node);
    }

    // ----------------- RREQ -----------------
    // format

    /**
     * Receiving RREQ format must be: ""
     * @param message
     * @return
     */
    public boolean validRREQFormat(String[] message){
        if (message.length == 7){
            try {
                int destSequence = Integer.parseInt(message[2]);
                int hopCount = Integer.parseInt(message[4]);
            } catch (NumberFormatException e) {
                return false;
            }
            return true;
        }
        return false;
    }

    /**
     * Send a Route Requests (RREQ)
     * Transmit-RREQ-format: RREQ::destination::destSequence::source::sourceSequence::hopCount
     */
    public void transmitRREQ(AODVRREQ AODVRREQ){

    }

    public void receiveRREQ(AODVRREQ AODVRREQ){
//        // make a reverse entry for source node
//        Route route = new Route(rreq.getSource(), receivedFrom, rreq.getDestSequence(), rreq.getHopCount() + 1);
//        addRouteEntry(route);
//
//        //two choices - we know a route to the destination -> RREP if not -> RREQ
//        // check whether we have routes to the destination in our routingTable
//        Route returnRoute = lookUpRouteEntry(rreq.getDestination());
//        // in case we know a route to the destination we send a RREP to the source and destination
//        if (returnRoute != null){
//            RREP rrepForSource = new RREP();
//            RREP rrepForDestination = new RREP();
//            transmitRREP(rrepForSource);
//            transmitRREP(rrepForDestination);
//        } else {
//            RREQ rreqForwarding = new RREQ();
//            transmitRREQ(rreqForwarding);
//        }
    }

    // ----------------- RREP -----------------

    /**
     * Receiving RREP format must be: "RREP::destination::destSequence::source::hopCount"
     * @param message
     * @return
     */
    public boolean validRREPFormat(String[] message){
        if (message.length == 5){
            try {
                int destSequence = Integer.parseInt(message[2]);
                int hopCount = Integer.parseInt(message[4]);
            } catch (NumberFormatException e) {
                return false;
            }
            return true;
        }
        return false;
    }

    public void transmitRREP(AODVRREP AODVRREP){

    }

    public void receiveRREP(){

    }

    // ----------------- RERR -----------------
    // Format: RERR:destination:destSequence

    public boolean validRERRFormat(String[] message){
        if (message.length == 3){
            try {
                int hopCount = Integer.parseInt(message[2]);
            } catch (NumberFormatException e) {
                return false;
            }
            return true;
        }
        return false;
    }

    public void transmitRERR(){

    }

    public void receiveRERR(){

    }

    // ----------------- helping methods -----------------

    /**
     * Checks whether route already exists in routingTable. In case it doesn't exist yet it adds the
     * route.
     */
    public void addRouteEntry(Route route){
        boolean exists = false;
        // check if routeEntry exists and compare sequence
        for (Route entry : routingTable){
            if (entry.equals(route)){
                exists = true;
                entry.setIs_active(true);
                break;
            }
        }
        if (!exists){
            routingTable.add(route);
        }
    }

    /**
     * Checks if RouteEntry exists and if so returns the one with the least hops
     * @param destination
     * @return destination address of Node
     */
    public Route lookUpRouteEntry(String destination){
        int hopCount = 0;
        int newHopCount;
        boolean first = true;
        Route returnRoute = null;
        for (Route route : routingTable){
            if (route.getDestination().equals(destination)){
                newHopCount = route.getHop_count();
                if (first){
                    hopCount = newHopCount;
                    first = false;
                    returnRoute = route;
                } else {
                    if (newHopCount < hopCount){
                        returnRoute = route;
                    }
                }
            }
        }
        return returnRoute;
    }

    /**
     * Updates the Node list. Either by adding a new Node or by refreshing the
     * TTL timer/active = true and gp data
     * @param node
     */
    public void updateNodeList(Node node){
        boolean found = false;
        for (Node existingNode : nodeList){
            if (existingNode.getAddr().equals(node.getAddr())){
                Log.i(TAG, "updateNodeListWithNode: Existing Node updated.");
                existingNode.setGp(node.getGp());
                existingNode.setIs_active(true);
                found = true;
            }
        }
        if (!found) {
            Log.i(TAG, "updateNodeListWithNode: New Node added: " + node.getAddr());
            nodeList.add(node);
        }
    }

    // ----------------- Getter/Setter -----------------

    public ArrayList<Route> getRoutingTable() {
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

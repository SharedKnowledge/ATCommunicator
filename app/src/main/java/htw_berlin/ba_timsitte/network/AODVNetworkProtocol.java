package htw_berlin.ba_timsitte.network;

import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import androidx.annotation.RequiresApi;

import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicInteger;


public class  AODVNetworkProtocol {

    private String TAG = "AODVNetworkProtocol";

    private ArrayList<Route> routingTable = new ArrayList<Route>();
    private ArrayList<RREQEntry> requestTable = new ArrayList<RREQEntry>();
    private AtomicInteger rreq_id = new AtomicInteger(0);
    private AtomicInteger sequence_number = new AtomicInteger(0);

    // Amount of time in milliseconds a route is marked as active in the routing
    private final int ROUTE_LIFETIME = 600000; // 10 minutes

    // Amount of hops until an RREQ is discarded
    private final int RREQ_LIFETIME = 3;

    // Amount of time between Broadcast / Hello messages
    private final int BROADCAST_INTERVAL = 300000; // 5 minutes

    private String ownNodeName;
    private final Handler mHandler;
    private final Handler rrepHandler; // to handle incoming RREPs
    private final Handler helloHandler; // handle outgoing Hellos

    // Runnable for broadcasting hello messages
    private Runnable mHelloRunnable = new Runnable() {
        @Override
        public void run() {
            Log.d(TAG, "mHelloRunnable: Broadcasting Hellos");
            AODVHello aodvHello = new AODVHello(ownNodeName);
            Message msg = mHandler.obtainMessage(AODVConstants.AODV_HELLO);
            msg.obj = aodvHello;
            mHandler.sendMessage(msg);
            helloHandler.postDelayed(this, BROADCAST_INTERVAL);
        }
    };

    public AODVNetworkProtocol(String name, Handler handler) {
        this.ownNodeName = name;
        mHandler = handler;
        rrepHandler = new Handler();
        helloHandler = new Handler();
        mHelloRunnable.run();
    }



    /**
     * Convert string to the corresponding aodv message and task
     * @param s
     */
    @RequiresApi(api = Build.VERSION_CODES.O)
    public void handleIncomingMessage(String receivedFrom, String s){
        try {
            String[] output = s.split("\\|");
                switch (output[0]){
                    case "1":
                        AODVRREQ rreq = new AODVRREQ(Integer.parseInt(output[1]), Integer.parseInt(output[2]),
                                Integer.parseInt(output[3]), Integer.parseInt(output[4]), Integer.parseInt(output[5]),
                                Integer.parseInt(output[6]), Integer.parseInt(output[7]), Integer.parseInt(output[8]),
                                Integer.parseInt(output[9]), output[10], Integer.parseInt(output[11]),
                                output[12], Integer.parseInt(output[13]));
                        Log.d(TAG, "handleIncomingMessage: RREQ from " + receivedFrom + " detected. " + rreq.toString());
                        RREQReceiver rreqReceiver = new RREQReceiver(rreq, receivedFrom);
                        rreqReceiver.startThread();
                        break;
                    case "2":
                        AODVRREP rrep = new AODVRREP(Integer.parseInt(output[1]), Integer.parseInt(output[2]),
                                Integer.parseInt(output[3]), Integer.parseInt(output[4]), Integer.parseInt(output[5]),
                                output[6], Integer.parseInt(output[7]), output[8], Integer.parseInt(output[9]));
                        Log.d(TAG, "handleIncomingMessage: RREO from " + receivedFrom + " detected. " + rrep.toString());
                        RREPReceiver rrepReceiver = new RREPReceiver(rrep,receivedFrom);
                        rrepReceiver.startThread();
                        break;
                    case "3":
                        AODVRERR rerr = new AODVRERR(Integer.parseInt(output[1]), Integer.parseInt(output[2]),
                                Integer.parseInt(output[3]), output[4], Integer.parseInt(output[5]));
                        Log.d(TAG, "handleIncomingMessage: RRER from " + receivedFrom + " detected. " + rerr.toString());
                        RERRReceiver rerrReceiver = new RERRReceiver(rerr);
                        rerrReceiver.startThread();
                        break;
                    case "4":
                        AODVRREP_ACK rrep_ack = new AODVRREP_ACK(Integer.parseInt(output[1]));
                        Log.d(TAG, "handleIncomingMessage: RREP-ACK from " + receivedFrom + " detected. " + rrep_ack.toString());
                        break;
                    case "5":
                        AODVPacket aodvPacket = new AODVPacket(output[1], output[2], output[3]);
                        Log.d(TAG, "handleIncomingMessage: AODV Packet from " + receivedFrom + " detected. " + aodvPacket.toString());
                        routeNeeded(aodvPacket);
                        break;
                    case "6":
                        AODVHello aodvHello = new AODVHello(output[1]);
                        Log.d(TAG, "handleIncomingMessage: AODV Hello from " + receivedFrom + " detected. " + aodvHello.toString());
                        HelloReceiver helloReceiver = new HelloReceiver(aodvHello, receivedFrom);
                        helloReceiver.startThread();
                        break;
                    default:
                        Log.d(TAG, "handleIncomingMessage: No AODV message type detected.");
                        break;
                }

        } catch (ArrayIndexOutOfBoundsException e1){
            Log.e(TAG, "checkForAODVMessage: Message size invalid.");
        } catch (NumberFormatException e2){
            Log.e(TAG, "checkForAODVMessage: String couldn't be converted to int");
        }
    }

    /**
     * Gets called whether there is a message to be send or to pass onto another node
     * @param aodvPacket packet which needs to be send
     */
    @RequiresApi(api = Build.VERSION_CODES.O)
    private void routeNeeded(AODVPacket aodvPacket) {
        Log.d(TAG, "BEGIN RouteNeeded Packet: " + aodvPacket.toString());

        // Check if there is an entry in our routing table
        Route r = getRouteFromRoutingTable(aodvPacket.getDestination());

        // In case we already have a route we can just forward it to the next node
        if (r != null){
            Log.i(TAG, "routeNeeded: Route found for " + aodvPacket.getDestination());
            // Create AODV Packet for handler
            Message msg = mHandler.obtainMessage(AODVConstants.AODV_PACKET);
            Bundle bundle = new Bundle();
            bundle.putString(AODVConstants.NEXT_ADDR, r.getNext());
            msg.setData(bundle);
            msg.obj = aodvPacket;
            mHandler.sendMessage(msg);
        }

        // If there is no route, aodv actually needs to perform
        else {
            Log.i(TAG, "routeNeeded: No route found for " + aodvPacket.getDestination());
            RREQCreater rreqCreater = new RREQCreater(aodvPacket.getDestination());
            rreqCreater.startThread();
        }
        Log.d(TAG, "END routeNeeded");
    }

// ------------------------------- Routing Table --------------------------------

    /**
     * Add or update a route in the routing table
     * @param newRoute Route that needs to be added
     */
    private synchronized void addRoute(Route newRoute){
        Log.d(TAG, "BEGIN addRoute: " + newRoute.toString());

        // Iterating through the routing table
        for (Route route : routingTable){
            // In case destination, nextHop and hopCount are the same, we update ttl and the sequenceNumber
            if (route.getDestination().equals(newRoute.getDestination())
                    &&route.getNext().equals(newRoute.getNext())
                    &&route.getHop_count() == newRoute.getHop_count()){
                route.setIs_active(true);
                route.setSequence(newRoute.getSequence());
                // Create info for the handler
                Message msg = mHandler.obtainMessage(AODVConstants.AODV_INFO);
                Bundle bundle = new Bundle();
                bundle.putString(AODVConstants.MESSAGE_BODY, "INFO " +  route.toString() + " updated.");
                msg.setData(bundle);
                mHandler.sendMessage(msg);
                return; // End method here
            }
        }

        // In case of a new route, add it to the routing table
        routingTable.add(newRoute);

        //Create info for the handler
        Message msg = mHandler.obtainMessage(AODVConstants.AODV_INFO);
        Bundle bundle = new Bundle();
        bundle.putString(AODVConstants.MESSAGE_BODY, "INFO " + newRoute.toString() +  " added.");
        msg.setData(bundle);
        mHandler.sendMessage(msg);

        Log.d(TAG, "END addRoute");
    }

    /**
     * Get the best and freshest route entry from the routing table
     * @param destination destination name
     * @return Route or null
     */
    private Route getRouteFromRoutingTable(String destination){
        Log.d(TAG, "BEGIN getRouteFromRoutingTable");

        Route bestRoute = null;

        // Iterating through the routing table
        for (Route route : routingTable){

            // Route needs to have the same destination and needs to be active
            if (route.getDestination().equals(destination) && route.isIs_active()){
                Log.i(TAG, "getRouteFromRoutingTable: Route found");

                // Check if it's the first route found, in that case it is automatically our best route
                if (bestRoute == null){
                    bestRoute = route;
                } else {

                    // Check for the sequence number, higher is always more up to date
                    if (route.getSequence() > bestRoute.getSequence()){
                        bestRoute = route;
                        Log.i(TAG, "getRouteFromRoutingTable: Route found with higher sequence number found");

                        // If sequenceNumber is the same the one with fewer hops wins
                    } else if (route.getSequence() == bestRoute.getSequence() &&
                            route.getHop_count() < bestRoute.getHop_count()){
                        bestRoute = route;
                        Log.i(TAG, "getRouteFromRoutingTable: Route found with better hop count found");
                    }
                }
            }
        }
        Log.d(TAG, "END getRouteFromRoutingTable");
        return bestRoute;
    }

    /**
     * Sets all routes which involve destination param to inactive
     * @param unreachableDestination unreachable destination
     */
    private synchronized void setRoutesInactive(String unreachableDestination){
        Log.d(TAG, "BEGIN setRoutesInactive");

        // Iterating through the routing table
        for (Route route : routingTable){

            // Set route with destination equal to unreachableDestination inactive
            if (route.getDestination().equals(unreachableDestination)){
                route.setIs_active(false);
                // Create info for the handler
                Message msg = mHandler.obtainMessage(AODVConstants.AODV_INFO);
                Bundle bundle = new Bundle();
                bundle.putString(AODVConstants.MESSAGE_BODY, "INFO " + route.toString() + " unreachable.");
                msg.setData(bundle);
                mHandler.sendMessage(msg);
                continue;
            }

            // Set route with next equal to unreachableDestination inactive
            if (route.getNext().equals(unreachableDestination)){
                route.setIs_active(false);
                // Create info for the handler
                Message msg = mHandler.obtainMessage(AODVConstants.AODV_INFO);
                Bundle bundle = new Bundle();
                bundle.putString(AODVConstants.MESSAGE_BODY, "INFO " + route.toString() + " unreachable.");
                msg.setData(bundle);
                mHandler.sendMessage(msg);
            }
        }
        Log.d(TAG, "END setRoutesInactive");
    }

// ------------------------------  Request Table -------------------------------

    /**
     * Checks if the request was already handled
     * @param aodvrreq AODVRREQ to check
     * @return true (rreq is our own), false if not
     */
    private boolean requestAlreadyKnown(AODVRREQ aodvrreq){
        Log.d(TAG, "BEGIN requestAlreadyKnown");

        boolean exists = false;
        int id = aodvrreq.getRreqId();
        String orig = aodvrreq.getOriginator();
        for (RREQEntry rreqEntry : requestTable){
            if (rreqEntry.getId() == id && rreqEntry.getOriginator().equals(orig)){
                exists = true;
                break;
            }
        }
        Log.d(TAG, "END requestAlreadyKnown");
        return exists;
    }


// ------------------------------- Getter/ Setter --------------------------------
    /**
     * Set the name of your node
     * @param ownNodeName own node name
     */
    public void setOwnNodeName(String ownNodeName) {
        this.ownNodeName = ownNodeName;
    }

    /**
     * Get the name of your node
     * @return own node name
     */
    public String getOwnNodeName() {
        return ownNodeName;
    }


// ------------------------------------ RREQ ------------------------------------

    /**
     * Class for creating new RREQs
     */
    private class RREQCreater extends Thread {
        private String destination;
        private AODVRREQ aodvrreq;

        @RequiresApi(api = Build.VERSION_CODES.O)
        RREQCreater(String destination) {
            this.destination = destination;
            int id = rreq_id.incrementAndGet();
            int seq_nr = sequence_number.incrementAndGet();
            this.aodvrreq = new AODVRREQ(0, 0, 0, 0,
                    0, 0, RREQ_LIFETIME, 1, id, destination, 0,
                    ownNodeName, seq_nr);
            Log.i(TAG, "RREQ created: " + aodvrreq.toString());
        }

        @RequiresApi(api = Build.VERSION_CODES.O)
        void startThread() {
            Log.d(TAG, "BEGIN RREQCreater startThread");
            start();
            RREQEntry rreqEntry = new RREQEntry(aodvrreq.getRreqId(), aodvrreq.getOriginator());
            requestTable.add(rreqEntry);

            // Create an RREQ for the handler
            Message msg = mHandler.obtainMessage(AODVConstants.AODV_RREQ);
            msg.obj = aodvrreq;
            mHandler.sendMessage(msg);

            mRREPRunnable.run();

            Log.d(TAG, "END RREQCreater startThread");
        }
        private Runnable mRREPRunnable = new Runnable() {
            @Override
            public void run() {
                Log.d(TAG, "rrepHandler: trying to find rrep");
                rrepHandler.postDelayed(this, 3000);
                //rrep = checkForRREP(rreq);
            }
        };
    }

    /**
     * Class for receiving RREQs
     */
    private class RREQReceiver extends Thread {
        private AODVRREQ aodvrreq;
        private String receivedFrom;

        RREQReceiver(AODVRREQ aodvrreq, String receivedFrom){
            this.aodvrreq = aodvrreq;
            this.receivedFrom = receivedFrom;
        }

        @RequiresApi(api = Build.VERSION_CODES.O)
        void startThread(){
            Log.d(TAG, "BEGIN RREQReceiver startThread");
            start();

            // Check if RREQ was already handled
            if (requestAlreadyKnown(aodvrreq)){
                Log.i(TAG, "RREQReceiver startThread: Already known RREQ, discard RREQ");
                interrupt();
            }
            int seq = sequence_number.get();

            // Add a route from the node we received the RREQ
            Route route1 = new Route(receivedFrom, receivedFrom, seq, 1, ROUTE_LIFETIME);
            addRoute(route1);

            // Add a route to the originator of the RREQ with receivedFrom as our nextHop
            Route route2 = new Route(aodvrreq.getOriginator(), receivedFrom,
                    aodvrreq.getOrigSequenceNumber(), aodvrreq.getHopCount(), ROUTE_LIFETIME);
            addRoute(route2);

            // Add to RREQ to our Request table
            int id = rreq_id.incrementAndGet();
            RREQEntry rreqEntry = new RREQEntry(id, aodvrreq.getOriginator());
            requestTable.add(rreqEntry);

            // Check for Route
            Route route = getRouteFromRoutingTable(aodvrreq.getDestination());

            // If there was an entry for a route we can create an RREP for it and unicast it
            // to receivedFrom
            if (route != null){
                // Create an RREP for the handler
                AODVRREP aodvrrep = new AODVRREP(0, 0, 0, 0,
                        route.getHop_count(), route.getDestination(), route.getSequence(),
                        aodvrreq.getOriginator(), ROUTE_LIFETIME);
                Message msg = mHandler.obtainMessage(AODVConstants.AODV_RREP);
                Bundle bundle = new Bundle();
                bundle.putString(AODVConstants.NEXT_ADDR, receivedFrom);
                msg.setData(bundle);
                msg.obj = aodvrrep;
                mHandler.sendMessage(msg);


            // If there is no route entry in our routing table for the destination of the RREQ
            } else {

                // If timeToLive reached 0 we discard the RREQ and cancel the process
                if (aodvrreq.getTimeToLive() <= 0){
                    // Create info for the handler
                    Message msg = mHandler.obtainMessage(AODVConstants.AODV_INFO);
                    Bundle bundle = new Bundle();
                    bundle.putString(AODVConstants.MESSAGE_BODY, "INFO " + aodvrreq.toInfoString() + " timeToLive of "
                            + aodvrreq.getOriginator() + " ended. RREQ discarded.");
                    msg.setData(bundle);
                    mHandler.sendMessage(msg);

                // Otherwise forward RREQ with hopCount + 1 and timeToLive - 1 via broadcast
                } else {
                    // Create an RREQ for the handler
                    aodvrreq.setHopCount(aodvrreq.getHopCount()+1);
                    aodvrreq.setTimeToLive(aodvrreq.getTimeToLive()-1);
                    Message msg = mHandler.obtainMessage(AODVConstants.AODV_RREQ);
                    msg.obj = aodvrreq;
                    mHandler.sendMessage(msg);
                }
            }
            Log.d(TAG, "END RREQReceiver startThread");
        }

    }

// ------------------------------------ RREP ------------------------------------

    /**
     * Class for receiving RREPs
     */
    private class RREPReceiver extends Thread {
        private AODVRREP aodvrrep;
        private String receivedFrom;

        RREPReceiver(AODVRREP aodvrrep, String receivedFrom){
            this.aodvrrep = aodvrrep;
            this.receivedFrom = receivedFrom;
        }

        @RequiresApi(api = Build.VERSION_CODES.O)
        void startThread(){
            Log.d(TAG, "BEGIN RREPReceiver startThread");
            start();

            // Increment own sequence number
            int seq = sequence_number.incrementAndGet();

            // Remark: destination is the one who sent the RREP, originator is the one
            // who sent the RREQ in the first place
            Route route1 = new Route(receivedFrom, receivedFrom, seq, 1, ROUTE_LIFETIME);
            aodvrrep.setHopCount(aodvrrep.getHopCount()+1);
            Route route2 = new Route(aodvrrep.getDestination(), receivedFrom, seq, aodvrrep.getHopCount(), ROUTE_LIFETIME);
            addRoute(route1);
            addRoute(route2);

            // Check if we are actually the originator of the initial RREQ
            if (aodvrrep.getOriginator().equals(ownNodeName)){
                // Create info for the handler
                Message msg = mHandler.obtainMessage(AODVConstants.AODV_INFO);
                Bundle bundle = new Bundle();
                bundle.putString(AODVConstants.MESSAGE_BODY, "INFO RREP of own RREQ received");
                msg.setData(bundle);
                mHandler.sendMessage(msg);
            } else {

                // First a Route towards the originator is needed before the the RREP can be hand off
                // to the nextHop towards the direction of the originator
                Route route = getRouteFromRoutingTable(aodvrrep.getOriginator());
                if (route != null) {
                    // Create an RREP for the handler
                    Message msg = mHandler.obtainMessage(AODVConstants.AODV_RREP);
                    Bundle bundle = new Bundle();
                    bundle.putString(AODVConstants.NEXT_ADDR, route.getNext());
                    msg.setData(bundle);
                    msg.obj = aodvrrep;
                    mHandler.sendMessage(msg);

                // If no route is found we throw an RRER
                } else {
                    // Create an RERR for the handler
                    AODVRERR aodvrerr = new AODVRERR(0, 0, 0,
                            aodvrrep.getOriginator(), aodvrrep.getDestSequenceNumber());
                    Message msg = mHandler.obtainMessage(AODVConstants.AODV_RERR);
                    Bundle bundle = new Bundle();
                    bundle.putString(AODVConstants.NEXT_ADDR, receivedFrom);
                    msg.setData(bundle);
                    msg.obj = aodvrerr;
                    mHandler.sendMessage(msg);
                }

            }
            Log.d(TAG, "END RREPReceiver startThread");
        }
    }

// ------------------------------------ RERR ------------------------------------

    /**
     * Class for receiving RRERs
     */
    private class RERRReceiver extends Thread {
        private AODVRERR aodvrerr;

        RERRReceiver(AODVRERR aodvrerr){
            this.aodvrerr = aodvrerr;
        }

        @RequiresApi(api = Build.VERSION_CODES.O)
        void startThread(){
            Log.d(TAG, "BEGIN RERRReceiver startThread");
            start();

            Message msg = mHandler.obtainMessage(AODVConstants.AODV_INFO);
            Bundle bundle = new Bundle();
            bundle.putString(AODVConstants.MESSAGE_BODY, "INFO " +  aodvrerr.toString() + " RECEIVED.");
            msg.setData(bundle);
            mHandler.sendMessage(msg);

            setRoutesInactive(aodvrerr.getUnreachableDestination());

            Log.d(TAG, "END RERRReceiver startThread");
        }
    }

// ----------------------------------- HELLO -----------------------------------

    /**
     * Class for receiving Hellos
     */
    private class HelloReceiver extends Thread {
        private AODVHello aodvHello;
        private String receivedFrom;

        HelloReceiver(AODVHello aodvHello, String receivedFrom){
            this.aodvHello = aodvHello;
            this.receivedFrom = receivedFrom;
        }

        @RequiresApi(api = Build.VERSION_CODES.O)
        void startThread(){
            Log.d(TAG, "BEGIN HelloReceiver startThread");
            start();
            Message msg = mHandler.obtainMessage(AODVConstants.AODV_INFO);
            Bundle bundle = new Bundle();
            bundle.putString(AODVConstants.MESSAGE_BODY, "INFO " +  aodvHello.toString() + " RECEIVED.");
            msg.setData(bundle);
            mHandler.sendMessage(msg);
            Route route = new Route(receivedFrom, receivedFrom, sequence_number.get() , 1, ROUTE_LIFETIME);
            addRoute(route);
            Log.d(TAG, "END HelloReceiver startThread");
        }
    }

}

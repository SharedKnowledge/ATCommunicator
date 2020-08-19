package htw_berlin.ba_timsitte.network;

import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import androidx.annotation.RequiresApi;

import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicInteger;

import htw_berlin.ba_timsitte.communication.Constants;

public class  AODVNetworkProtocol {

    private String TAG = "AODVNetworkProtocol";

    private ArrayList<Route> routingTable = new ArrayList<Route>();
    private ArrayList<RREQEntry> requestTable = new ArrayList<RREQEntry>();
    private AtomicInteger rreq_id = new AtomicInteger(0);
    private AtomicInteger sequence_number = new AtomicInteger(0);

    private String ownNodeName;
    private final Handler mHandler;
    private final Handler rrepHandler; // to handle incoming RREPs
    private int mState;
    private int mNewState;

    public AODVNetworkProtocol(String name, Handler handler) {
        this.ownNodeName = name;
        mHandler = handler;
        rrepHandler = new Handler();
    }

    /**
     * Update UI title according to the current state of the chat connection
     */
    private synchronized void updateUserInterfaceTitle() {
        mState = getState();
        Log.d(TAG, "updateUserInterfaceTitle() " + mNewState + " -> " + mState);
        mNewState = mState;

        // Give the new state to the Handler so the UI Activity can update
        mHandler.obtainMessage(Constants.MESSAGE_STATE_CHANGE, mNewState, -1).sendToTarget();
    }

    /**
     * Return the current connection state.
     */
    public synchronized int getState() {
        return mState;
    }

    /**
     * Convert string to the corresponding aodv message and task
     * @param s
     */
    @RequiresApi(api = Build.VERSION_CODES.O)
    public void handleIncomingMessages(String receivedFrom, String s){
        try {
            String[] output = s.split("|");
                switch (output[0]){
                    case "1":
                        AODVRREQ rreq = new AODVRREQ(Integer.parseInt(output[1]), Integer.parseInt(output[2]),
                                Integer.parseInt(output[3]), Integer.parseInt(output[4]), Integer.parseInt(output[5]),
                                Integer.parseInt(output[6]), Integer.parseInt(output[7]), Integer.parseInt(output[8]),
                                output[9], Integer.parseInt(output[10]), output[11], Integer.parseInt(output[12]));
                        Log.d(TAG, "handleIncomingMessages: RREQ detected.");
                        RREQReceiver rreqReceiver = new RREQReceiver(rreq, receivedFrom);
                        rreqReceiver.startThread();
                        break;
                    case "2":
                        AODVRREP rrep = new AODVRREP(Integer.parseInt(output[1]), Integer.parseInt(output[2]),
                                Integer.parseInt(output[3]), Integer.parseInt(output[4]), Integer.parseInt(output[5]),
                                output[6], Integer.parseInt(output[7]), output[8], Integer.parseInt(output[9]));
                        Log.d(TAG, "handleIncomingMessages: RREO detected.");
                        RREPReceiver rrepReceiver = new RREPReceiver(rrep);
                        rrepReceiver.startThread();
                        break;
                    case "3":
                        AODVRERR rrer = new AODVRERR(Integer.parseInt(output[1]), Integer.parseInt(output[2]),
                                Integer.parseInt(output[3]), output[4], Integer.parseInt(output[5]));
                        Log.d(TAG, "handleIncomingMessages: RRER detected.");
                        RRERReceiver rrerReceiver = new RRERReceiver(rrer);
                        rrerReceiver.startThread();
                        break;
                    case "4":
                        AODVRREP_ACK rrep_ack = new AODVRREP_ACK(Integer.parseInt(output[3]));
                        Log.d(TAG, "handleIncomingMessages: RREP-ACK detected.");
                        break;
                    default:
                        Log.d(TAG, "handleIncomingMessages: Message type detected.");
                        routeNeeded(output[1], output[2], output[3]);
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
     * @param body message that needs to be sent with aodv
     * @param originator originator name
     * @param destination destination name
     */
    @RequiresApi(api = Build.VERSION_CODES.O)
    private void routeNeeded(String originator, String destination, String body) {
        Log.d(TAG, "RouteNeeded: start");
        // check if there is an entry in our routing table
        Route r = getRouteFromRoutingTable(destination);
        // in case we already have a route we can just forward it to the next node
        if (r != null){
            Message msg = mHandler.obtainMessage(AODVConstants.IP_PACKET_SEND);
            Bundle bundle = new Bundle();
            bundle.putString(AODVConstants.NEXT, r.getNext());
            bundle.putString(AODVConstants.ORIGINATOR, originator);
            bundle.putString(AODVConstants.DESTINATION, destination);
            bundle.putString(AODVConstants.MESSAGE, body);
            msg.setData(bundle);
            mHandler.sendMessage(msg);
        }
        // if there is no route, aodv actually needs to perform
        else {
            RREQCreater rreqCreater = new RREQCreater(destination);
            rreqCreater.startThread();
            // erzeuge neuen RREQ mit RREQCreater
        }
    }

// ------------------------------- Routing Table --------------------------------

    /**
     * Add a Route to the routing table
     * @param route Route that needs to be added
     */
    private void addRoute(Route route){
        for (Route entry : routingTable){
            // if destination, nextHop and hopCount are the same, we update ttl and the sequenceNumber
            if (entry.getDestination().equals(route.getDestination())
                    &&entry.getNext().equals(route.getNext())
                    &&entry.getHop_count() == route.getHop_count()){
                entry.setIs_active(true);
                entry.setSequence(route.getSequence());
                return; // end method here
            }
        }
        // in case of a new route, add it to the routing table
        routingTable.add(route);
    }

    /**
     * Get the best and freshest route entry from the routing table
     * @param destination destination name
     * @return Route or null
     */
    private Route getRouteFromRoutingTable(String destination){
        Log.d(TAG, "getRouteFromRoutingTable: start");
        Route bestRoute = null;
        for (Route route : routingTable){
            // route needs to have the same destination and needs to be active
            if (route.getDestination().equals(destination) && route.isIs_active()){
                Log.i(TAG, "getRouteFromRoutingTable: Route found");
                if (bestRoute == null){
                    bestRoute = route;
                } else {
                    // check for the sequence number, higher is always more up to date
                    if (route.getSequence() > bestRoute.getSequence()){
                        bestRoute = route;
                        Log.i(TAG, "getRouteFromRoutingTable: Route found with higher sequence number found");
                    // if sequenceNumber is the same the one with fewer hops wins
                    } else if (route.getSequence() == bestRoute.getSequence() &&
                            route.getHop_count() < bestRoute.getHop_count()){
                        bestRoute = route;
                        Log.i(TAG, "getRouteFromRoutingTable: Route found with better hop count found");
                    }
                }

            }
        }
        return bestRoute;
    }

// ----------------------------- Own Request Table ------------------------------

    /**
     * Checks if the request was already handled
     * @param aodvrreq AODVRREQ to check
     * @return true (rreq is our own), false if not
     */
    private boolean ownRequestCheck(AODVRREQ aodvrreq){
        boolean exists = false;
        int id = aodvrreq.getRreqId();
        String orig = aodvrreq.getOriginator();
        for (RREQEntry rreqEntry : requestTable){
            if (rreqEntry.getId() == id && rreqEntry.getOriginator().equals(orig)){
                exists = true;
                break;
            }
        }
        return exists;
    }

    private AODVRREP checkForRREP(AODVRREQ rreq){
        Log.i(TAG, "checkForRREP: start");
        return null;
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
        private boolean timeoutReached = false;
        private AODVRREQ rreq = null;
        private AODVRREP rrep = null;
        private Route route = null;
        private final long timerInMilliseconds = 60000; // equals to 1 minute

        @RequiresApi(api = Build.VERSION_CODES.O)
        RREQCreater(String destination) {
            this.destination = destination;
            int id = rreq_id.incrementAndGet();
            int seq_nr = sequence_number.incrementAndGet();
            this.rreq = new AODVRREQ(0, 0, 0, 0,
                    0, 0, 1, id, this.destination, 0,
                    ownNodeName, seq_nr);
            Log.i(TAG, "new RREQ created: " + rreq.toString());
        }

        @RequiresApi(api = Build.VERSION_CODES.O)
        void startThread() {
            start();
            Log.i(TAG, "RREQCreater start");
            RREQEntry rreqEntry = new RREQEntry(rreq.getRreqId(), rreq.getOriginator());
            requestTable.add(rreqEntry);
            //
            // send RREQ with the handler
            Message message = mHandler.obtainMessage(AODVConstants.AODV_RREQ_SEND);
            message.obj = rreq.toString();
            mHandler.sendMessage(message);
            Log.i(TAG, "startThread: start waiting for RREP");
//            rrep = checkForRREP(rreq);
//            if (rrep != null) {
//                timeoutReached = true;
//            }
            Log.i(TAG, "RREQCreater: end");
            mRREPRunnable.run();


        }
        private Runnable mRREPRunnable = new Runnable() {
            @Override
            public void run() {
                Log.i(TAG, "rrepHandler: trying to find rrep");
                rrepHandler.postDelayed(this, 3000);
                rrep = checkForRREP(rreq);
            }
        };
    }

    /**
     * Class for receiving RREQs
     */
    private class RREQReceiver extends Thread {
        private AODVRREQ aodvrreq; // received rreq
        private String receivedFrom;

        RREQReceiver(AODVRREQ aodvrreq, String receivedFrom){
            this.aodvrreq = aodvrreq;
            this.receivedFrom = receivedFrom;
        }

        void startThread(){
            start();
            // check if rreq is our own
            if (ownRequestCheck(aodvrreq)){
                interrupt();
            }
            int id = sequence_number.get();
            // add route
            Route route1 = new Route(receivedFrom, receivedFrom, id, 1);
            addRoute(route1);
            // add route to originator
            Route route2 = new Route(aodvrreq.getDestination(), aodvrreq.getOriginator(), aodvrreq.getOrigSequenceNumber(), aodvrreq.getHopCount());
            addRoute(route2);
            // add to already seen rreq table
        }

    }

// ------------------------------------ RREP ------------------------------------

    /**
     * Class for receiving RREPs
     */
    private class RREPReceiver extends Thread {
        private AODVRREP aodvrrep;

        RREPReceiver(AODVRREP aodvrrep){
            this.aodvrrep = aodvrrep;
        }

        void startThread(){
            start();

        }
    }

// ------------------------------------ RRER ------------------------------------

    /**
     * Class for receiving RRERs
     */
    private class RRERReceiver extends Thread {
        private AODVRERR aodvrerr;

        RRERReceiver(AODVRERR aodvrerr){
            this.aodvrerr = aodvrerr;
        }

        void startThread(){
            start();
        }
    }
}

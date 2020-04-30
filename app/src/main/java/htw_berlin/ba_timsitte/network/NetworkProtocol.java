package htw_berlin.ba_timsitte.network;

import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicInteger;

public class NetworkProtocol {
    private static final NetworkProtocol instance = new NetworkProtocol();
    // private ArrayList<RoutingEntry> routingTable = new ArrayList<>();
    private ArrayList<Device> deviceList = new ArrayList<>();
    private AtomicInteger id;

    /**
     * Singleton constructor
     */
    private NetworkProtocol(){

    }

    /**
     *
     * @return
     */
    public static NetworkProtocol getInstance(){
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
    private void addNewDevice(String name){
        // checking for overflow
        if (id.get() == Integer.MAX_VALUE){
            // Message pop up that no more Devices can be added
        }
        int newId = id.incrementAndGet();
        Device device = new Device(newId);

        deviceList.add(device);
    }
}

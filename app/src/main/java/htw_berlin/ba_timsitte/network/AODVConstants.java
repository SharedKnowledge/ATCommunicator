package htw_berlin.ba_timsitte.network;

public interface AODVConstants {

    // Message types
    public static final int AODV_RREQ = 1;
    public static final int AODV_RREP = 2;
    public static final int AODV_RERR = 3;
    public static final int AODV_RERR_ACK = 4;
    public static final int AODV_PACKET = 5;
    public static final int AODV_HELLO = 6;
    public static final int AODV_INFO = 7; // Information output for protocol events

    // Key names
    public static final String MESSAGE_BODY = "MESSAGE_BODY"; // the body which needs to be send with AT
    public static final String NEXT_ADDR = "NEXT_ADDR";
}

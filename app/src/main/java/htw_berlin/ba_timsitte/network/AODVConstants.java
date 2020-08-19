package htw_berlin.ba_timsitte.network;

public interface AODVConstants {

    // Message types
    public static final int AODV_RREQ_SEND = 1;
    public static final int AODV_RREP_SEND = 2;
    public static final int AODV_RERR_SEND = 3;
    public static final int AODV_RERR_ACK_SEND = 4;
    public static final int IP_PACKET_SEND = 5;
    public static final int AODV_RREQ_RECEIVED = 6;
    public static final int AODV_RREP_RECEIVED = 7;
    public static final int AODV_RERR_RECEIVED = 8;
    public static final int AODV_RERR_ACK_RECEIVED = 9;
    public static final int IP_PACKET_RECEIVED = 10;

    // Key names
    public static final String NEXT = "NEXT";
    public static final String MESSAGE = "MESSAGE";
    public static final String ORIGINATOR = "ORIGINATOR";
    public static final String DESTINATION = "DESTINATION";
}

package htw_berlin.ba_timsitte.network;

public class RouteEntry {

    private int sequence;
    private Node destination;
    private Node next;
    private int hop_count;

    /*

     */
    public RouteEntry(int sequence, Node destinationDevice, Node nextDevice, int hop_count){
        this.sequence = sequence;
        this.destination = destinationDevice;
        this.next = nextDevice;
        this.hop_count = hop_count;
    }

    public int getSequence() {
        return sequence;
    }

    public void setSequence(int sequence) {
        this.sequence = sequence;
    }

    public Node getDestination() {
        return destination;
    }

    public void setDestination(Node destination) {
        this.destination = destination;
    }

    public Node getNext() {
        return next;
    }

    public void setNext(Node next) {
        this.next = next;
    }

    public int getHop_count() {
        return hop_count;
    }

    public void setHop_count(int hop_count) {
        this.hop_count = hop_count;
    }
}

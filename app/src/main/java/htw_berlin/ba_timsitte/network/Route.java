package htw_berlin.ba_timsitte.network;

import android.os.CountDownTimer;

public class Route {

    private String destination;
    private int destSequenceNumber;
    private String next;
    private int hop_count;
    private boolean is_active;
    private CountDownTimer timeToLive  = new CountDownTimer(600000, 0) {
        @Override
        public void onTick(long l) {
        }

        @Override
        public void onFinish() {
            is_active = false;
        }
    };

    /*

     */
    public Route(String destinationDevice, String nextDevice, int sequence, int hop_count){
        this.destination = destinationDevice;
        this.next = nextDevice;
        this.destSequenceNumber = sequence;
        this.hop_count = hop_count;
        this.is_active = true;
        this.timeToLive.start();
    }

    public int getSequence() {
        return destSequenceNumber;
    }

    public void setSequence(int sequence) {
        this.destSequenceNumber = sequence;
    }

    public String getDestination() {
        return destination;
    }

    public void setDestination(String destination) {
        this.destination = destination;
    }

    public String getNext() {
        return next;
    }

    public void setNext(String next) {
        this.next = next;
    }

    public int getHop_count() {
        return hop_count;
    }

    public void setHop_count(int hop_count) {
        this.hop_count = hop_count;
    }

    public boolean isIs_active() {
        return is_active;
    }

    public void setIs_active(boolean is_active) {
        this.is_active = is_active;
        if (this.is_active = false){
            this.timeToLive.cancel();
        } else {
            this.timeToLive.cancel();
            this.timeToLive.start();
        }
    }
}

package htw_berlin.ba_timsitte.network;

import android.os.CountDownTimer;
import androidx.annotation.NonNull;

public class Route {

    private String destination;
    private int destSequenceNumber;
    private String next;
    private int hop_count;
    private boolean is_active;
    private int lifetime;
    private CountDownTimer timeToLive  = new CountDownTimer(lifetime, 0) {
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
    public Route(String destination, String next, int sequence, int hop_count, int lifetime){
        this.destination = destination;
        this.next = next;
        this.destSequenceNumber = sequence;
        this.hop_count = hop_count;
        this.lifetime = lifetime;
        this.is_active = true;
        this.timeToLive.start();
    }

    @NonNull
    @Override
    public String toString() {
        return "ROUTE dest: " +  destination + " next: " + next + " hop: " + hop_count;
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

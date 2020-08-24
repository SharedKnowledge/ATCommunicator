package htw_berlin.ba_timsitte.network;

import android.os.Build;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;

public class AODVHello extends AODVMessage {
    private String destination;
    private int destSequenceNumber;

    public AODVHello(String destination, int destSequenceNumber) {
        super(6);
        this.destination = destination;
        this.destSequenceNumber = destSequenceNumber;
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    @NonNull
    @Override
    public String toString(){
        return String.join("|",Integer.toString(super.getType()), destination);
    }

    public String toInfoString(){
        return "HELLO orig: " + destination;
    }

    public String getDestination() {
        return destination;
    }

    public void setDestination(String destination) {
        this.destination = destination;
    }


    public int getDestSequenceNumber() {
        return destSequenceNumber;
    }

    public void setDestSequenceNumber(int destSequenceNumber) {
        this.destSequenceNumber = destSequenceNumber;
    }
}

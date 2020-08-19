package htw_berlin.ba_timsitte.network;

import androidx.annotation.NonNull;

public class AODVMessage {

    private int type;

    public AODVMessage(int type){
        this.type = type;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    @NonNull
    @Override
    public String toString() {
        return "Invalid AODV message";
    }
}

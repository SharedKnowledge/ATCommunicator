package htw_berlin.ba_timsitte.network;

import android.os.Build;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;

public class AODVRREP_ACK extends AODVMessage {

    private int reserved;

    public AODVRREP_ACK(int reserved){
        super(4);
        this.reserved = 0;
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    @NonNull
    @Override
    public String toString() {
        return String.join("|", Integer.toString(super.getType()), Integer.toString(reserved));
    }

    public String toInfoString(){
        return "RREP_ACK";
    }
}

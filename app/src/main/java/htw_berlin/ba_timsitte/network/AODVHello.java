package htw_berlin.ba_timsitte.network;

import android.os.Build;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;

public class AODVHello extends AODVMessage {
    private String originator;

    public AODVHello(String originator) {
        super(6);
        this.originator = originator;
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    @NonNull
    @Override
    public String toString(){
        return String.join("|",Integer.toString(super.getType()), originator);
    }

    public String toInfoString(){
        return "HELLO orig: " + originator;
    }

    public String getOriginator() {
        return originator;
    }

    public void setOriginator(String originator) {
        this.originator = originator;
    }


}

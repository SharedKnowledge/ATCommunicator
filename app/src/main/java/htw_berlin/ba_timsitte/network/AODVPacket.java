package htw_berlin.ba_timsitte.network;

import android.os.Build;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;

public class AODVPacket extends AODVMessage {
    private String originator;
    private String destination;
    private String body;

    public AODVPacket(String originator, String destination, String body) {
        super(5);
        this.originator = originator;
        this.destination = destination;
        this.body = body;
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    @NonNull
    @Override
    public String toString() {
        return String.join("|", Integer.toString(super.getType()), originator, destination, body);
    }

    public String getOriginator() {
        return originator;
    }

    public void setOriginator(String originator) {
        this.originator = originator;
    }

    public String getDestination() {
        return destination;
    }

    public void setDestination(String destination) {
        this.destination = destination;
    }

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }
}

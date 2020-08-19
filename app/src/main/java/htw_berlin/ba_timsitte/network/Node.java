package htw_berlin.ba_timsitte.network;

import android.os.Build;
import android.os.CountDownTimer;
import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.RequiresApi;

import org.osmdroid.util.GeoPoint;

import java.util.ArrayList;

public class Node implements Parcelable{

    private String addr;
    private boolean is_active = true;
    private GeoPoint gp;


    private CountDownTimer timeToLive  = new CountDownTimer(600000, 200000) {
        @Override
        public void onTick(long l) {
        }

        @Override
        public void onFinish() {
            is_active = false;
        }
    };


    /**
     * @param addr
     * @param lat
     * @param lon
     */
    public Node(String addr, double lat, double lon){
        this.addr = addr;
        this.gp = new GeoPoint(lat, lon);
        this.is_active = true;
        timeToLive.start();
    }

    private boolean refreshTimer(){
        timeToLive.cancel();
        timeToLive.start();
        return true;
    }

    public boolean isIs_active() {
        return is_active;
    }

    public void setIs_active(boolean is_active) {
        this.is_active = is_active;
        if (this.is_active = false){
            this.timeToLive.cancel();
        } else {
            this.timeToLive.start();
        }
    }

    public GeoPoint getGp() {
        return gp;
    }

    public void setGp(GeoPoint gp) {
        this.gp = gp;
    }

    public String getAddr() {
        return addr;
    }

    public void setAddr(String addr) {
        this.addr = addr;
    }

    // ----------------- make Node object parcelable -----------------

    protected Node(Parcel in) {
        addr = in.readString();
        is_active = in.readInt() == 1;
        double lat = in.readDouble();
        double lon = in.readDouble();
        gp = new GeoPoint(lat, lon);
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(addr);
        dest.writeInt(is_active ? 1 : 0);
        dest.writeDouble(gp.getLatitude());
        dest.writeDouble(gp.getLongitude());
    }

    public static final Creator<Node> CREATOR = new Creator<Node>() {
        @Override
        public Node createFromParcel(Parcel in) {
            return new Node(in);
        }

        @Override
        public Node[] newArray(int size) {
            return new Node[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

}

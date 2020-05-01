package htw_berlin.ba_timsitte.network;

import android.os.CountDownTimer;
import android.os.Parcel;
import android.os.Parcelable;

import org.osmdroid.util.GeoPoint;

import java.util.ArrayList;

public class Node {

    private int id;
    private boolean is_active = true;
    private GeoPoint gp = new GeoPoint(1.0,2.0);
    // list of known neighbours
    private ArrayList neighbourList= new ArrayList();

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
     *
     * @param id
     */
    public Node(int id){
        this.id = id;
        this.is_active = true;
        // 10 minutes time to live
        timeToLive.start();
    }

    private boolean refreshTimer(){
        timeToLive.cancel();
        timeToLive.start();
        return true;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public boolean isIs_active() {
        return is_active;
    }

    public void setIs_active(boolean is_active) {
        this.is_active = is_active;
        if (this.is_active = false){
            this.timeToLive.cancel();
        }
    }

    public GeoPoint getGp() {
        return gp;
    }

    public void setGp(GeoPoint gp) {
        this.gp = gp;
    }

    public ArrayList getNeighbourList() {
        return neighbourList;
    }

    public void setNeighbourList(ArrayList neighbourList) {
        this.neighbourList = neighbourList;
    }
}

package htw_berlin.ba_timsitte.network;

import android.os.CountDownTimer;
import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.NonNull;

public class Route implements Parcelable {

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

    protected Route(Parcel in) {
        destination = in.readString();
        destSequenceNumber = in.readInt();
        next = in.readString();
        hop_count = in.readInt();
        is_active = in.readByte() != 0;
        lifetime = in.readInt();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(destination);
        dest.writeInt(destSequenceNumber);
        dest.writeString(next);
        dest.writeInt(hop_count);
        dest.writeByte((byte) (is_active ? 1 : 0));
        dest.writeInt(lifetime);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<Route> CREATOR = new Creator<Route>() {
        @Override
        public Route createFromParcel(Parcel in) {
            return new Route(in);
        }

        @Override
        public Route[] newArray(int size) {
            return new Route[size];
        }
    };

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

package htw_berlin.ba_timsitte.network;

import android.os.Parcel;
import android.os.Parcelable;

public class RREQEntry implements Parcelable {
    private int id;
    private String originator;

    public RREQEntry(int id, String originator){
        this.id = id;
        this.originator = originator;

    }

    protected RREQEntry(Parcel in) {
        id = in.readInt();
        originator = in.readString();
    }

    public static final Creator<RREQEntry> CREATOR = new Creator<RREQEntry>() {
        @Override
        public RREQEntry createFromParcel(Parcel in) {
            return new RREQEntry(in);
        }

        @Override
        public RREQEntry[] newArray(int size) {
            return new RREQEntry[size];
        }
    };

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getOriginator() {
        return originator;
    }

    public void setOriginator(String originator) {
        this.originator = originator;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(id);
        dest.writeString(originator);
    }
}

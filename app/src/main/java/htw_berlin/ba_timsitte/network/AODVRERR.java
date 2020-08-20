package htw_berlin.ba_timsitte.network;

import android.os.Build;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;

public class AODVRERR extends AODVMessage{
    private int noDeleteFlag;
    private int reserved;
    private int destCount;
    private String unreachableDestination;
    private int unreachableDestSequenceNumber;

    public AODVRERR(int noDeleteFlag, int reserved, int destCount,
                    String unreachableDestination, int unreachableDestSequenceNumber){
        super(3);
        this.noDeleteFlag = noDeleteFlag;
        this.reserved = 0;
        this.destCount = destCount;
        this.unreachableDestination = unreachableDestination;
        this.unreachableDestSequenceNumber = unreachableDestSequenceNumber;
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    @NonNull
    @Override
    public String toString() {
        return String.join("|", Integer.toString(super.getType()), Integer.toString(noDeleteFlag),
                Integer.toString(reserved), Integer.toString(destCount), unreachableDestination,
                Integer.toString(unreachableDestSequenceNumber));
    }

    public String toInfoString(){
        return "RERR unreachDest: " + unreachableDestination;
    }

    public int getNoDeleteFlag() {
        return noDeleteFlag;
    }

    public void setNoDeleteFlag(int noDeleteFlag) {
        this.noDeleteFlag = noDeleteFlag;
    }

    public int getDestCount() {
        return destCount;
    }

    public void setDestCount(int destCount) {
        this.destCount = destCount;
    }

    public String getUnreachableDestination() {
        return unreachableDestination;
    }

    public void setUnreachableDestination(String unreachableDestination) {
        this.unreachableDestination = unreachableDestination;
    }

    public int getUnreachableDestSequenceNumber() {
        return unreachableDestSequenceNumber;
    }

    public void setUnreachableDestSequenceNumber(int unreachableDestSequenceNumber) {
        this.unreachableDestSequenceNumber = unreachableDestSequenceNumber;
    }

}

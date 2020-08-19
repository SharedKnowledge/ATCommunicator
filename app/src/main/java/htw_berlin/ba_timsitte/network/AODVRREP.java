package htw_berlin.ba_timsitte.network;

import android.os.Build;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;

public class AODVRREP extends AODVMessage{
    private int repairFlag;
    private int acknowledgmentFlag;
    private int reserved;
    private int prefixSize;
    private int hopCount;
    private String destination;
    private int destSequenceNumber;
    private String originator;
    private int lifetime;

    public AODVRREP(int repairFlag, int acknowledgmentFlag, int reserved, int prefixSize,
                    int hopCount, String destination, int destSequenceNumber, String originator,
                    int lifetime){
            super(2);
            this.repairFlag = repairFlag;
            this.acknowledgmentFlag = acknowledgmentFlag;
            this.reserved = 0;
            this.prefixSize = prefixSize;
            this.hopCount = hopCount;
            this.destination = destination;
            this.destSequenceNumber = destSequenceNumber;
            this.originator = originator;
            this.lifetime = lifetime;
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    @NonNull
    @Override
    public String toString() {
        return String.join("|", Integer.toString(super.getType()), Integer.toString(repairFlag),
                Integer.toString(acknowledgmentFlag), Integer.toString(reserved), Integer.toString(prefixSize),
                Integer.toString(hopCount), destination, Integer.toString(destSequenceNumber), originator,
                Integer.toString(lifetime));
    }

    public int getRepairFlag() {
        return repairFlag;
    }

    public void setRepairFlag(int repairFlag) {
        this.repairFlag = repairFlag;
    }

    public int getAcknowledgmentFlag() {
        return acknowledgmentFlag;
    }

    public void setAcknowledgmentFlag(int acknowledgmentFlag) {
        this.acknowledgmentFlag = acknowledgmentFlag;
    }

    public int getPrefixSize() {
        return prefixSize;
    }

    public void setPrefixSize(int prefixSize) {
        this.prefixSize = prefixSize;
    }

    public int getHopCount() {
        return hopCount;
    }

    public void setHopCount(int hopCount) {
        this.hopCount = hopCount;
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

    public String getOriginator() {
        return originator;
    }

    public void setOriginator(String originator) {
        this.originator = originator;
    }

    public int getLifetime() {
        return lifetime;
    }

    public void setLifetime(int lifetime) {
        this.lifetime = lifetime;
    }
}

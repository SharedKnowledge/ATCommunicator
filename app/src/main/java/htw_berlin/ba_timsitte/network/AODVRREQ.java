package htw_berlin.ba_timsitte.network;

import android.os.Build;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;

public class AODVRREQ extends AODVMessage{
    private int joinFlag;
    private int repairFlag;
    private int gratuitousRREPFlag;
    private int destinationOnlyFlag;
    private int unknownSeqNrFlag;
    private int reserved;
    private int timeToLive;
    private int hopCount;
    private int rreqId;
    private String destination;
    private int destSequenceNumber;
    private String originator;
    private int origSequenceNumber;


    public AODVRREQ(int joinFlag, int repairFlag, int gratuitousRREPFlag, int destinationOnlyFlag,
                    int unknownSeqNrFlag, int reserved, int timeToLive, int hopCount, int rreqId, String destination,
                    int destSequenceNumber, String originator, int origSequenceNumber){
        super(1);
        this.joinFlag = joinFlag;
        this.repairFlag = repairFlag;
        this.gratuitousRREPFlag = gratuitousRREPFlag;
        this.destinationOnlyFlag = destinationOnlyFlag;
        this.unknownSeqNrFlag = unknownSeqNrFlag;
        this.reserved = 0;
        this.timeToLive = timeToLive;
        this.hopCount = hopCount;
        this.rreqId = rreqId;
        this.destination = destination;
        this.destSequenceNumber = destSequenceNumber;
        this.originator = originator;
        this.origSequenceNumber = origSequenceNumber;
    }


    @RequiresApi(api = Build.VERSION_CODES.O)
    @NonNull
    @Override
    public String toString() {
        return String.join("|", Integer.toString(super.getType()), Integer.toString(joinFlag),
                Integer.toString(repairFlag), Integer.toString(gratuitousRREPFlag), Integer.toString(destinationOnlyFlag),
                Integer.toString(unknownSeqNrFlag), Integer.toString(reserved), Integer.toString(hopCount),
                Integer.toString(rreqId), destination, Integer.toString(destSequenceNumber),
                originator, Integer.toString(origSequenceNumber));
    }

    public String toInfoString(){
        return "RREQ dest: " +  destination + " orig: " +  originator + " hop: " + hopCount;
    }

    public int getJoinFlag() {
        return joinFlag;
    }

    public void setJoinFlag(int joinFlag) {
        this.joinFlag = joinFlag;
    }

    public int getRepairFlag() {
        return repairFlag;
    }

    public void setRepairFlag(int repairFlag) {
        this.repairFlag = repairFlag;
    }

    public int getGratuitousRREPFlag() {
        return gratuitousRREPFlag;
    }

    public void setGratuitousRREPFlag(int gratuitousRREPFlag) {
        this.gratuitousRREPFlag = gratuitousRREPFlag;
    }

    public int getDestinationOnlyFlag() {
        return destinationOnlyFlag;
    }

    public void setDestinationOnlyFlag(int destinationOnlyFlag) {
        this.destinationOnlyFlag = destinationOnlyFlag;
    }

    public int getUnknownSeqNrFlag() {
        return unknownSeqNrFlag;
    }

    public void setUnknownSeqNrFlag(int unknownSeqNrFlag) {
        this.unknownSeqNrFlag = unknownSeqNrFlag;
    }

    public int getHopCount() {
        return hopCount;
    }

    public void setHopCount(int hopCount) {
        this.hopCount = hopCount;
    }

    public int getRreqId() {
        return rreqId;
    }

    public void setRreqId(int rreqId) {
        this.rreqId = rreqId;
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

    public int getOrigSequenceNumber() {
        return origSequenceNumber;
    }

    public void setOrigSequenceNumber(int origSequenceNumber) {
        this.origSequenceNumber = origSequenceNumber;
    }

    public int getTimeToLive() {
        return timeToLive;
    }

    public void setTimeToLive(int timeToLive) {
        this.timeToLive = timeToLive;
    }
}

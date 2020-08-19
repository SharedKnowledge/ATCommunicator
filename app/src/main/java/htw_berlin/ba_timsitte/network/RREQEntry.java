package htw_berlin.ba_timsitte.network;

public class RREQEntry {
    private int id;
    private String originator;

    public RREQEntry(int id, String originator){
        this.id = id;
        this.originator = originator;

    }

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
}

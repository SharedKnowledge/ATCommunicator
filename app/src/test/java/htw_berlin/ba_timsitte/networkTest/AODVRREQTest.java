package htw_berlin.ba_timsitte.networkTest;

import org.junit.Test;

import htw_berlin.ba_timsitte.network.AODVRREQ;

import static org.junit.Assert.assertEquals;

public class AODVRREQTest {
    @Test
    public void testValidAODVRREQ(){
        AODVRREQ rerr = new AODVRREQ(0, 0, 0, 0,
                0, 0, 3, 1, 12, "destTest", 12, "origTest", 13);
        assertEquals("1|0|0|0|0|0|0|3|1|12|destTest|12|origTest|13", rerr.toString());
    }
}
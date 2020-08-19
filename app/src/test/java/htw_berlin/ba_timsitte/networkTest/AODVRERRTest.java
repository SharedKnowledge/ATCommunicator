package htw_berlin.ba_timsitte.networkTest;

import org.junit.Test;
import static org.junit.Assert.*;

import htw_berlin.ba_timsitte.network.AODVRERR;

public class AODVRERRTest {

    @Test
    public void testValidAODVRERR(){
        AODVRERR rerr = new AODVRERR(0, 0, 1, "test", 12);
        assertEquals("3|0|0|1|test|12", rerr.toString());
    }
}

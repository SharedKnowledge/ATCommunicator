package htw_berlin.ba_timsitte.networkTest;

import org.junit.Test;

import htw_berlin.ba_timsitte.network.AODVRREP;
import static org.junit.Assert.assertEquals;

public class AODVRREPTest {

    @Test
    public void testValidRREPTest(){
        AODVRREP rrep = new AODVRREP(0, 0, 0, 0, 0,
                "destTest", 1, "origTest", 12);
        assertEquals("2|0|0|0|0|0|destTest|1|origTest|12", rrep.toString());
    }
}

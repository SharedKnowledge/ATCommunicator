package htw_berlin.ba_timsitte.networkTest;

import org.junit.Test;
import static org.junit.Assert.*;

import htw_berlin.ba_timsitte.network.AODVNetworkProtocol;

public class AODVNetworkProtocolUnitTest {

    @Test
    public void testValidBroadcast(){

    }

    @Test
    public void testAODV(){
        String test = "AODV|Hi";
        String sub_test = test.substring(5);
        assertEquals(sub_test, "Hi");
    }
}

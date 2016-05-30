package pt.upa.broker.ws.it;

import org.junit.*;

import pt.upa.broker.ws.InvalidPriceFault_Exception;
import pt.upa.broker.ws.TransportView;
import pt.upa.broker.ws.UnavailableTransportFault_Exception;
import pt.upa.broker.ws.UnavailableTransportPriceFault_Exception;
import pt.upa.broker.ws.UnknownLocationFault_Exception;
import pt.upa.broker.ws.cli.BrokerClient;

import static org.junit.Assert.*;

import java.util.List;

import javax.xml.registry.JAXRException;

/**
 *  Integration Test example
 *  
 *  Invoked by Maven in the "verify" life-cycle phase
 *  Should invoke "live" remote servers 
 */
public class ExampleIT {
	
	/*private String url = "http://localhost:9090";
	private String broker = "UpaBroker";
	private BrokerClient bc;

    // static members


    // one-time initialization and clean-up

    @BeforeClass
    public static void oneTimeSetUp() {

    }

    @AfterClass
    public static void oneTimeTearDown() {

    }


    // members


    // initialization and clean-up for each test

    @Before
    public void setUp() {
    	BrokerClient bc = new BrokerClient(url, broker);
    	try {
			bc.connect();
		} catch (JAXRException e) {
			e.printStackTrace();
		}
    }

    @After
    public void tearDown() {
    }


    // tests

    @Test
    public void pingTest() {
    	String s = null;
    	s = bc.ping("ping");
    	assertTrue("ping unsuccessful", s != null);
    }
    
    @Test
    public void requestTransportTest() throws InvalidPriceFault_Exception, UnavailableTransportFault_Exception, UnavailableTransportPriceFault_Exception, UnknownLocationFault_Exception {
    	String s = null;
    	s = bc.requestTransport("Castelo Branco", "Lisboa", 50);
    	assertTrue("requestTransport unsuccessful", s != null);
    }
    
    @Test(expected = InvalidPriceFault_Exception.class)
    public void requestTransportInvalidPriceTest() throws InvalidPriceFault_Exception, UnavailableTransportFault_Exception, UnavailableTransportPriceFault_Exception, UnknownLocationFault_Exception {
    	bc.requestTransport("Castelo Branco", "Lisboa", -10);
    }
    
    @Test(expected = UnknownLocationFault_Exception.class)
    public void requestTransportInvalidLocationTest() throws InvalidPriceFault_Exception, UnavailableTransportFault_Exception, UnavailableTransportPriceFault_Exception, UnknownLocationFault_Exception {
    	bc.requestTransport("Castelo Branco", "Allgarve", -10);
    }
    
    @Test
    public void listTransportsTest() throws InvalidPriceFault_Exception, UnavailableTransportFault_Exception, UnavailableTransportPriceFault_Exception, UnknownLocationFault_Exception {
    	List<TransportView> list;
    	bc.requestTransport("Castelo Branco", "Lisboa", 50);
    	list = bc.listTransports();
    	assertTrue("listTransport unsuccessful", !list.isEmpty());
    }
    
    @Test
    public void clearTransportsTest() throws InvalidPriceFault_Exception, UnavailableTransportFault_Exception, UnavailableTransportPriceFault_Exception, UnknownLocationFault_Exception {
    	List<TransportView> list;
    	bc.requestTransport("Castelo Branco", "Lisboa", 50);
    	bc.clearTransports();
    	list = bc.listTransports();
    	assertTrue("clearTransport unsuccessful", list.isEmpty());
    }
    
    @Test
    public void viewTransportTest() throws InvalidPriceFault_Exception, UnavailableTransportFault_Exception, UnavailableTransportPriceFault_Exception, UnknownLocationFault_Exception {
    	List<TransportView> list;
    	bc.requestTransport("Castelo Branco", "Lisboa", 50);
    	list = bc.listTransports();
    	TransportView transport = list.get(0);
    	assertTrue("viewTransport unsuccessful", transport.getPrice() <= 50);
    }*/
}
package pt.upa.transporter.ws;

import org.junit.*;
import static org.junit.Assert.*;

import java.util.List;

/**
 *  Unit Test example
 *  
 *  Invoked by Maven in the "test" life-cycle phase
 *  If necessary, should invoke "mock" remote servers 
 */
public class ExampleTest {

    // static members
	private TransporterPort tp;
	private JobView job;

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
    	tp = new TransporterPort();
    }

    @After
    public void tearDown() {
    	tp = null;
    	job = null;
    }


    // tests

    @Test(expected = BadPriceFault_Exception.class)
    public void negativePriceTest() throws BadLocationFault_Exception, BadPriceFault_Exception {
        tp.setUpaNo(1);
        tp.requestJob("Castelo Branco", "Faro", -10);
    }
    
    @Test(expected = BadLocationFault_Exception.class)
    public void unknownOriginTest() throws BadLocationFault_Exception, BadPriceFault_Exception {
        tp.setUpaNo(1);
        tp.requestJob("Sintra", "Faro", 10);
    }
    
    @Test(expected = BadLocationFault_Exception.class)
    public void unknownDestinationTest() throws BadLocationFault_Exception, BadPriceFault_Exception {
        tp.setUpaNo(1);
        tp.requestJob("Castelo Branco", "Allgarve", 10);
    }
    
    @Test
    public void getTransporterNoTest() throws BadLocationFault_Exception, BadPriceFault_Exception {
        tp.setUpaNo(2);
        int upaNo = tp.getUpaNo(); 
        assertEquals("Invalid origin and/or destination", 2, upaNo);
    }
    
    @Test
    public void overPricedTest() throws BadLocationFault_Exception, BadPriceFault_Exception {
        tp.setUpaNo(1);
        job = tp.requestJob("Castelo Branco", "Faro", 105);
        assertEquals("Error price not over maximum (100)", null, job);
    }
    
    @Test
    public void wrongRegion1Test() throws BadLocationFault_Exception, BadPriceFault_Exception {
        tp.setUpaNo(1);
        job = tp.requestJob("Porto", "Faro", 105);
        
        
        assertEquals("Invalid origin and/or destination", null, job);
    }
    
    @Test
    public void wrongRegion2Test() throws BadLocationFault_Exception, BadPriceFault_Exception {
        tp.setUpaNo(2);
        job = tp.requestJob("Castelo Branco", "Faro", 105);
        assertEquals("Invalid origin and/or destination", null, job);
    }
    
    @Test
    public void priceUnder10Test1() throws BadLocationFault_Exception, BadPriceFault_Exception {
        tp.setUpaNo(1);
        job = tp.requestJob("Castelo Branco", "Faro", 5);
        assertTrue("Wrong price ", (job.getJobPrice()>=0 && job.getJobPrice()<=5));
    }
 
    @Test
    public void priceUnder10Test2() throws BadLocationFault_Exception, BadPriceFault_Exception {
        tp.setUpaNo(2);
        job = tp.requestJob("Castelo Branco", "Porto", 5);
        assertTrue("Wrong price ", (job.getJobPrice()>=0 && job.getJobPrice()<=5));
    }
    
    @Test
    public void betweenRangePriceTest1impar() throws BadLocationFault_Exception, BadPriceFault_Exception {
        tp.setUpaNo(1);
        job = tp.requestJob("Castelo Branco", "Faro", 21);
        assertTrue("Wrong price ", (job.getJobPrice()<=21));
    }
    
    @Test
    public void betweenRangePriceTest1par() throws BadLocationFault_Exception, BadPriceFault_Exception {
        tp.setUpaNo(1);
        job = tp.requestJob("Castelo Branco", "Faro", 20);
        assertTrue("Wrong price ", (job.getJobPrice()>=20));
    }
    
    @Test
    public void betweenRangePriceTest2impar() throws BadLocationFault_Exception, BadPriceFault_Exception {
        tp.setUpaNo(2);
        job = tp.requestJob("Castelo Branco", "Porto", 21);
        assertTrue("Wrong price ", (job.getJobPrice()>=21));
    }

    @Test
    public void betweenRangePriceTest2par() throws BadLocationFault_Exception, BadPriceFault_Exception {
        tp.setUpaNo(2);
        job = tp.requestJob("Castelo Branco", "Porto", 21);
        assertTrue("Wrong price ", (job.getJobPrice()>=21));
    }
    
    @Test
    public void wrongSameRegion1Test() throws BadLocationFault_Exception, BadPriceFault_Exception {
        tp.setUpaNo(1);
        job = tp.requestJob("Porto", "Braga", 50);
        assertEquals("Invalid origin and destination", null, job);
    }
    
    @Test
    public void wrongSameRegion2Test() throws BadLocationFault_Exception, BadPriceFault_Exception {
        tp.setUpaNo(2);
        job = tp.requestJob("Faro", "Évora", 50);
        assertEquals("Invalid origin and destination", null, job);
    }
    
    @Test
    public void pingTest() throws BadLocationFault_Exception, BadPriceFault_Exception {
        tp.setUpaNo(2);
        String ping = "UpaTransporter" + tp.getUpaNo() + " is alive and fully operational.";
        String test = tp.ping("test");	
        assertEquals("Invalid origin and destination", ping, test);
    }
    
    @Test
    public void betweenRangePriceTest2() throws BadLocationFault_Exception, BadPriceFault_Exception {
        tp.setUpaNo(2);
        job = tp.requestJob("Castelo Branco", "Porto", 20);
        assertTrue("Wrong price ", (job.getJobPrice()<=20));
    }
    
 /*   @Test
    public void decideJobAcceptTest() throws BadLocationFault_Exception, BadPriceFault_Exception, BadJobFault_Exception {
        JobView status;
        tp.setUpaNo(2);
        job = tp.requestJob("Castelo Branco", "Porto", 20);
        tp.decideJob(job.getJobIdentifier(), true);
        status = tp.jobStatus(job.getJobIdentifier());
        assertEquals("Not decided ", JobStateView.ACCEPTED, status.getJobState());
    }*/
    
    @Test
    public void decideJobRejectTest() throws BadLocationFault_Exception, BadPriceFault_Exception, BadJobFault_Exception {
        JobView status;
        tp.setUpaNo(2);
        job = tp.requestJob("Castelo Branco", "Porto", 20);
        tp.decideJob(job.getJobIdentifier(), false);
        status = tp.jobStatus(job.getJobIdentifier());
        assertEquals("Not decided ", JobStateView.REJECTED, status.getJobState());
    }
    
    @Test
    public void jobStatusNullTest() throws BadLocationFault_Exception, BadPriceFault_Exception, BadJobFault_Exception {
        JobView status;
        tp.setUpaNo(2);
        job = tp.requestJob("Castelo Branco", "Porto", 20);
        status = tp.jobStatus("Non valid id");
        assertEquals("JobStatus not null ", null, status);
    }
    
    @Test
    public void clearJobsTest() throws BadLocationFault_Exception, BadPriceFault_Exception {
        List<JobView> list;
        tp.setUpaNo(2);
        job = tp.requestJob("Castelo Branco", "Porto", 20);
        tp.clearJobs();
        list = tp.listJobs();
        assertTrue("List not clear ", list.isEmpty());
    }
    
}
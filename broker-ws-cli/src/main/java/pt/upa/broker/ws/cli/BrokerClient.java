package pt.upa.broker.ws.cli;

import static javax.xml.ws.BindingProvider.ENDPOINT_ADDRESS_PROPERTY;

import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.xml.registry.JAXRException;
import javax.xml.ws.BindingProvider;
import javax.xml.ws.WebServiceException;

import pt.ulisboa.tecnico.sdis.ws.uddi.UDDINaming;
import pt.upa.broker.ws.BrokerPortType;
import pt.upa.broker.ws.BrokerService;
import pt.upa.broker.ws.InvalidPriceFault_Exception;
import pt.upa.broker.ws.TransportView;
import pt.upa.broker.ws.UnavailableTransportFault_Exception;
import pt.upa.broker.ws.UnavailableTransportPriceFault_Exception;
import pt.upa.broker.ws.UnknownLocationFault_Exception;
import pt.upa.broker.ws.UnknownTransportFault_Exception;

public class BrokerClient {
	
	private String UDDIurl;
	private String brokerName;
	BrokerPortType port;
	
	public BrokerClient(String UDDIServer, String brokerServer) {
		UDDIurl = UDDIServer;
		brokerName = brokerServer;
	}
	

	public void connect() throws JAXRException {
		System.out.printf("Contacting UDDI at %s%n", UDDIurl);
        UDDINaming uddiNaming = new UDDINaming(UDDIurl);

        System.out.printf("Looking for '%s'%n", brokerName);
        String endpointAddress = uddiNaming.lookup(brokerName);

        if (endpointAddress == null) {
                System.out.println("Not found!");
                return;
        } else {
                System.out.printf("Found %s%n", endpointAddress);
        }

        System.out.println("Creating stub ...");
        BrokerService service = new BrokerService();
        BrokerPortType bport = service.getBrokerPort();

        System.out.println("Setting endpoint address ...");
        BindingProvider bindingProvider = (BindingProvider) bport;
        Map<String, Object> requestContext = bindingProvider.getRequestContext();
        requestContext.put(ENDPOINT_ADDRESS_PROPERTY, endpointAddress);
        port = bport;
	}
	
	private void setTimeOut(BrokerPortType bport){
		
		BindingProvider bindingProvider = (BindingProvider) bport;
		Map<String, Object> requestContext = bindingProvider.getRequestContext();
		
		 int connectionTimeout = 5000;
         // The connection timeout property has different names in different versions of JAX-WS
         // Set them all to avoid compatibility issues
         final List<String> CONN_TIME_PROPS = new ArrayList<String>();
         CONN_TIME_PROPS.add("com.sun.xml.ws.connect.timeout");
         CONN_TIME_PROPS.add("com.sun.xml.internal.ws.connect.timeout");
         CONN_TIME_PROPS.add("javax.xml.ws.client.connectionTimeout");
         // Set timeout until a connection is established (unit is milliseconds; 0 means infinite)
         for (String propName : CONN_TIME_PROPS)
             requestContext.put(propName, connectionTimeout);
         System.out.printf("Set connection timeout to %d milliseconds%n", connectionTimeout);

         int receiveTimeout = 10000;
         // The receive timeout property has alternative names
         // Again, set them all to avoid compability issues
         final List<String> RECV_TIME_PROPS = new ArrayList<String>();
         RECV_TIME_PROPS.add("com.sun.xml.ws.request.timeout");
         RECV_TIME_PROPS.add("com.sun.xml.internal.ws.request.timeout");
         RECV_TIME_PROPS.add("javax.xml.ws.client.receiveTimeout");
         // Set timeout until the response is received (unit is milliseconds; 0 means infinite)
         for (String propName : RECV_TIME_PROPS)
             requestContext.put(propName, receiveTimeout);
        
	}
	
	public String ping(String s){
		setTimeOut(port);
		try{
			String result = port.ping(s);
			return result;
		}
		catch(WebServiceException wse){
			 System.out.println("Caught: " + wse);
             Throwable cause = wse.getCause();
             if (cause != null && cause instanceof SocketTimeoutException) {
                 System.out.println("The cause was a timeout exception: " + cause);
             }
             return "Something went wrong with ping timeout.";
		}
	}
	
	public String requestTransport(String origin, String destination, int price) throws InvalidPriceFault_Exception, UnavailableTransportFault_Exception, UnavailableTransportPriceFault_Exception, UnknownLocationFault_Exception{
		setTimeOut(port);
		try{
			String result = port.requestTransport(origin, destination, price);
			return result;
		}
		catch(WebServiceException wse){
			 System.out.println("Caught: " + wse);
             Throwable cause = wse.getCause();
             if (cause != null && cause instanceof SocketTimeoutException) {
                 System.out.println("The cause was a timeout exception: " + cause);
             }
             return "Something went wrong with requestTransport timeout.";
		}
				
	}
	
	public void clearTransports(){
		setTimeOut(port);
		try{
			port.clearTransports();
		}
		catch(WebServiceException wse){
			 System.out.println("Caught: " + wse);
             Throwable cause = wse.getCause();
             if (cause != null && cause instanceof SocketTimeoutException) {
                 System.out.println("The cause was a timeout exception: " + cause);
             }
		}
	}
	
	public List<TransportView> listTransports(){
		setTimeOut(port);
		List<TransportView> result = null;
		try{
			result = port.listTransports();
			return result;
		}
		catch(WebServiceException wse){
			 System.out.println("Caught: " + wse);
             Throwable cause = wse.getCause();
             if (cause != null && cause instanceof SocketTimeoutException) {
                 System.out.println("The cause was a timeout exception: " + cause);
             }
             return result;
		}
		
	}
	
	public TransportView viewTransport(String s) throws UnknownTransportFault_Exception{
		setTimeOut(port);
		TransportView result = null;
		try{
			result = port.viewTransport(s);
			return result;
		}
		catch(WebServiceException wse){
			 System.out.println("Caught: " + wse);
             Throwable cause = wse.getCause();
             if (cause != null && cause instanceof SocketTimeoutException) {
                 System.out.println("The cause was a timeout exception: " + cause);
             }
             return result;
		}
	}
	
}
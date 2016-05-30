package pt.upa.broker.ws;

import java.io.IOException;

import javax.xml.registry.JAXRException;
import javax.xml.ws.Endpoint;

import pt.ulisboa.tecnico.sdis.ws.uddi.UDDINaming;
import pt.upa.transporter.ws.cli.TransporterClientException;

public class BrokerEndpointManager {
	
	private BrokerPort servicePort = new BrokerPort();

	private Endpoint endpoint;
	private UDDINaming uddiNaming;
	private String serviceName;
	private String serviceUrl;
	private String uddiUrl;
	private boolean secondary;
	private String primaryUrl = null;
	
	public BrokerEndpointManager(String name, String url, String uddi ) throws JAXRException{
		serviceName = name;
		serviceUrl = url;
		uddiUrl = uddi;
		secondary = false;
	}
	
	public void setServiceName(String name){
		serviceName = name;
	}
	
	public void setServiceUrl(String url){
		serviceUrl = url;
	}
	
	public void setSecondary(boolean sec){
		secondary = sec;
	}
	
	public boolean getSecondary(){
		return secondary;
	}
	
	public void start() throws TransporterClientException{		
		try{
			
				uddiNaming = new UDDINaming(uddiUrl);
				primaryUrl = uddiNaming.lookup("UpaBroker");
		}
		catch(Exception e){
			primaryUrl = null;
		}
		try{
				if(primaryUrl == null){
					
					setSecondary(false);
					servicePort.setSecondary(false);
					uddiPublish(serviceName, serviceUrl);
					
				}
				else{
					setSecondary(true);
					servicePort.setSecondary(true);
					setServiceName("UpaBackup");
					setServiceUrl("http://localhost:8079/broker-ws/endpoint");
					
					uddiPublish(serviceName, serviceUrl);
					//Connecting to primary
					servicePort.connectToPrimary(primaryUrl);
					servicePort.imAlive();
					replacePrimary();
				}
			} catch (Exception e) {
				stop();
			}
	}
	
	public void replacePrimary(){
		stop();
		setSecondary(false);
		
		setServiceName("UpaBroker");
		setServiceUrl(primaryUrl);
		uddiPublish(serviceName, serviceUrl);
	}
	
	public void uddiPublish(String name, String url){
		
		try{
			servicePort.connect(uddiUrl);
			endpoint = Endpoint.create(servicePort);
			
			// publish endpoint
			System.out.printf("Starting %s%n", url);
			endpoint.publish(url);
	
			// publish to UDDI
			System.out.printf("Publishing '%s' to UDDI at %s%n", name, uddiUrl);
		
			uddiNaming.rebind(name, url);
		}
		catch (Exception e) {
			e.printStackTrace();
			stop();
		}
	}
	
	
	public void awaitConnections() throws IOException{
		System.out.println("Awaiting connections");
		System.out.println("Press enter to shutdown");
		System.in.read();
	}
	
	public void stop(){
		try {
			if (endpoint != null) {
				// stop endpoint
				endpoint.stop();
				System.out.printf("Stopped %s%n", serviceUrl);
			}
		} catch (Exception e) {
			System.out.printf("Caught exception when stopping: %s%n", e);
		}
		try {
			if (uddiNaming != null) {
				// delete from UDDI
				uddiNaming.unbind(serviceName);
				System.out.printf("Deleted '%s' from UDDI%n", serviceName);
			}
		} catch (Exception e) {
			System.out.printf("Caught exception when deleting: %s%n", e);
		}
	}

}


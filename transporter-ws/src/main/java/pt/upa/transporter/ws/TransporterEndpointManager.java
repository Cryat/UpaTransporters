package pt.upa.transporter.ws;

import java.io.IOException;

import javax.xml.registry.JAXRException;
import javax.xml.ws.Endpoint;

import pt.ulisboa.tecnico.sdis.ws.uddi.UDDINaming;

public class TransporterEndpointManager {
	
	private TransporterPort servicePort = new TransporterPort();

	private Endpoint endpoint;
	private UDDINaming uddiNaming;
	private String serviceName;
	private String serviceUrl;
	private String uddiUrl;
	
	public TransporterEndpointManager(String name, String url, String uddi ) throws JAXRException{
		serviceName = name;
		serviceUrl = url;
		uddiUrl = uddi;
	}
	
	public void start(){
		int transporterNo = Integer.parseInt(serviceName.replace("UpaTransporter", ""));
		
			servicePort.setUpaNo(transporterNo);
			endpoint = Endpoint.create(servicePort);
			
			// publish endpoint
			System.out.printf("Starting %s%n", serviceUrl);
			endpoint.publish(serviceUrl);

			// publish to UDDI
			System.out.printf("Publishing '%s' to UDDI at %s%n", serviceName, uddiUrl);
			try {
				uddiNaming = new UDDINaming(uddiUrl);
				uddiNaming.rebind(serviceName, serviceUrl);

			} catch (JAXRException e) {
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
				servicePort.cancelTimer();
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


package pt.upa.broker.ws;

import static javax.xml.ws.BindingProvider.ENDPOINT_ADDRESS_PROPERTY;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

import javax.jws.HandlerChain;
import javax.jws.WebService;
import javax.xml.registry.JAXRException;
import javax.xml.ws.BindingProvider;

import pt.ulisboa.tecnico.sdis.ws.uddi.UDDINaming;
import pt.upa.transporter.ws.BadJobFault_Exception;
import pt.upa.transporter.ws.BadLocationFault_Exception;
import pt.upa.transporter.ws.BadPriceFault_Exception;
import pt.upa.transporter.ws.JobStateView;
import pt.upa.transporter.ws.JobView;
import pt.upa.transporter.ws.cli.TransporterClient;
import pt.upa.transporter.ws.cli.TransporterClientException;


@WebService(
	    endpointInterface="pt.upa.broker.ws.BrokerPortType",
	    wsdlLocation="broker.1_0.wsdl",
	    name="BrokerWebService",
	    portName="BrokerPort",
	    targetNamespace="http://ws.broker.upa.pt/",
	    serviceName="BrokerService"
	)
public class BrokerPort implements BrokerPortType{
	List<TransporterClient> unnamedClients = new ArrayList<TransporterClient>();
	SortedMap<String, TransporterClient> clients = new TreeMap<String, TransporterClient>();
	SortedMap<String, TransportView> transports = new TreeMap<String, TransportView>();
	BrokerPortType bPort = null;
	boolean secondary = false;
	
	UDDINaming uddiNaming;
	String uddiUrl;
	String secondaryUrl = null;
	
	@Override
	public String ping(String name) {
		
		String pong = "";
		pong = pong + "Broker is alive and functional\n";
		for (TransporterClient tc : unnamedClients){
			pong = pong + tc.ping(name);
		}
		return pong;
	}

	@Override
	public  String requestTransport(String origin, String destination, int price)
			throws InvalidPriceFault_Exception, UnavailableTransportFault_Exception,
			UnavailableTransportPriceFault_Exception, UnknownLocationFault_Exception {
		
		int cheapestPrice = 100;
		
		JobView cheapestJob = null;
		JobView job;
		
		List<JobView> jobsReceived = new ArrayList<JobView>();
		
		for(TransporterClient tc: unnamedClients){
			try {
				job = tc.requestJob(origin, destination, price);
				
				if(job != null){
					jobsReceived.add(job);
					clients.put(job.getCompanyName(), tc);
					if(job.getJobPrice() <= cheapestPrice){
						cheapestJob = job;
						cheapestPrice = job.getJobPrice();
					}
				}
				
			} catch (BadLocationFault_Exception e) {
				throw new UnknownLocationFault_Exception("Unknown location for the transporter", new UnknownLocationFault());
			} catch (BadPriceFault_Exception e) {
				throw new InvalidPriceFault_Exception("Unknown location for the transporter", new InvalidPriceFault());
			}
		}
		
		if(cheapestJob == null){
			throw new UnavailableTransportFault_Exception("No job was received meeting the requirements", 
					new UnavailableTransportFault());
		}
		else{
			if(cheapestJob.getJobPrice() <= price){
				for(JobView jv : jobsReceived){
					try{
						if(cheapestJob.equals(jv)){
							
							clients.get(jv.getCompanyName()).decideJob(jv.getJobIdentifier(), true);	
							transports.put(jv.getJobIdentifier(), jobToTransport(jv));
						}
						else{
							clients.get(jv.getCompanyName()).decideJob(jv.getJobIdentifier(), false);					
						}
						
					}catch(BadJobFault_Exception e){
						throw new UnavailableTransportFault_Exception("Previous receibed job could not be accepted or rejected.", 
								new UnavailableTransportFault()); 
					}
				}
			}
			else{
				throw new UnavailableTransportPriceFault_Exception("No job under the stated price.", 
						new UnavailableTransportPriceFault());
			}
			
			if(!getSecondary()){ update(getTransportViewsFromMap()); }
		
			return cheapestJob.getJobIdentifier();
		}
	}
	
	public List<TransportView> getTransportViewsFromMap(){
		
		return new ArrayList<>(transports.values());
	}

	@Override
	public TransportView viewTransport(String id) throws UnknownTransportFault_Exception {
		TransportView tv;
		JobView job;
		if(id != null){
			tv = transports.get(id);
			if(tv != null){
				System.out.println("ID FOUND BEFORE VIEWING WAS: " + tv.getId());
				job = clients.get(tv.getTransporterCompany()).jobStatus(id);
				if(job != null){
					tv = jobToTransport(job);
					return tv;
				}
				else{
					throw new UnknownTransportFault_Exception(tv.getTransporterCompany() + " failed to locate job.", 
							new UnknownTransportFault());
				}
			}
			else{
				throw new UnknownTransportFault_Exception("Broker failed to locate job.", new UnknownTransportFault());
			}
		}
		else{
			throw new UnknownTransportFault_Exception("Id given to view was null.", new UnknownTransportFault());
		}
	}

	@Override
	public List<TransportView> listTransports() {
		TransportView conversion;
		List<TransportView> tList = new ArrayList<TransportView>();
		List<JobView> jobList;
		
		for (TransporterClient tc : clients.values()){
			jobList = tc.listJobs();
			for (JobView job : jobList){
				conversion = jobToTransport(job);
				tList.add(conversion);
			}
		}
		if(!getSecondary()){ update(getTransportViewsFromMap()); }
		return tList;
	}

	@Override
	public void clearTransports() {
		transports.clear();
		for (TransporterClient tc : clients.values()){
			tc.clearJobs();
		}
		if(!getSecondary()){ update(getTransportViewsFromMap()); }
	}
	
	public void connectToPrimary(String url){
		BrokerService service = new BrokerService();
		bPort = service.getBrokerPort();

			System.out.println("Setting endpoint address ...");
			BindingProvider bindingProvider = (BindingProvider) bPort;
			Map<String, Object> requestContext = bindingProvider.getRequestContext();
			requestContext.put(ENDPOINT_ADDRESS_PROPERTY, url);

	}
	
	public void imAlive() throws InterruptedException {
		String print = null;
		
		do {
			try{
				print = bPort.ping("Im alive");
				System.out.println(print);
			}
			catch(Exception e){
				print = null;
				System.out.println("Primary broker is dead...");
		}
			
		
		Thread.sleep(4000);
		
		} while(print != null);
		
	}
	
	public void connect(String uddi) throws JAXRException, TransporterClientException{
		List<String> endpoints = new ArrayList<String>();
		TransporterClient tc;
		uddiUrl = uddi;
		UDDINaming uddiNaming = new UDDINaming(uddi);
        endpoints.addAll(uddiNaming.list("UpaTransporter%"));
        for(String endpoint : endpoints){
        	tc = new TransporterClient(endpoint);
        	unnamedClients.add(tc);
        }
	}
	
	private TransportView jobToTransport(JobView job){
		TransportView conversion = new TransportView();
		SortedMap<JobStateView, TransportStateView> maping = new TreeMap<JobStateView, TransportStateView>();
		maping.put(JobStateView.PROPOSED, TransportStateView.BUDGETED);
		maping.put(JobStateView.ACCEPTED, TransportStateView.BOOKED);
		maping.put(JobStateView.REJECTED, TransportStateView.FAILED);
		maping.put(JobStateView.HEADING, TransportStateView.HEADING);
		maping.put(JobStateView.ONGOING, TransportStateView.ONGOING);
		maping.put(JobStateView.COMPLETED, TransportStateView.COMPLETED);

		
		conversion.setOrigin(job.getJobOrigin());
		conversion.setDestination(job.getJobDestination());
		conversion.setId(job.getJobIdentifier());
		conversion.setPrice(job.getJobPrice());
		conversion.setState(maping.get(job.getJobState()));
		conversion.setTransporterCompany(job.getCompanyName());
		
		return conversion;
	}
	
	private JobView transportToJob(TransportView view){
		JobView conversion = new JobView();
		SortedMap<TransportStateView, JobStateView> maping = new TreeMap<TransportStateView, JobStateView>();
		maping.put(TransportStateView.HEADING, JobStateView.HEADING);
		maping.put(TransportStateView.ONGOING, JobStateView.ONGOING);
		maping.put(TransportStateView.COMPLETED, JobStateView.COMPLETED);
		
		conversion.setJobOrigin(view.getOrigin());
		conversion.setJobDestination(view.getDestination());
		conversion.setJobIdentifier(view.getId());
		conversion.setJobPrice(view.getPrice());
		conversion.setCompanyName(view.getTransporterCompany());
		
		return conversion;
	}
	
	public void setSecondary(boolean sec){
		secondary = sec;
	}
	
	public boolean getSecondary() {
		return secondary;
	}
	
	@Override
	public void update(List<TransportView> jobs) {
		
		if(!getSecondary()){
			try {
				
				uddiNaming = new UDDINaming(uddiUrl);
				secondaryUrl = uddiNaming.lookup("UpaBackup");
				connectToPrimary(secondaryUrl);
					
				bPort.update(jobs);
				
			} catch (Exception e) {
				bPort = null;
				System.out.println(">>>>BROKER---There is no upa backup online---");
			}
			
		}
		
		else{
			//set variables
			SortedMap<String, TransportView> map = new TreeMap<String, TransportView>();
			
			for(TransportView tv : jobs){
						map.put(tv.getId(), tv);
			}
			transports = map;
			System.out.println("Updated:");
			for(Map.Entry<String, TransportView> content : transports.entrySet()){
				System.out.println(content.getKey() + "------" + content.getValue());
			}
		}
	}

}
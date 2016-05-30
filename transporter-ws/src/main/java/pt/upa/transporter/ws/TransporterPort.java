package pt.upa.transporter.ws;


import java.security.PublicKey;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.SortedMap;
import java.util.Timer;
import java.util.TimerTask;
import java.util.TreeMap;

import javax.annotation.Resource;
import javax.jws.HandlerChain;
import javax.jws.WebService;
import javax.xml.ws.WebServiceContext;


@WebService(
	    endpointInterface="pt.upa.transporter.ws.TransporterPortType",
	    wsdlLocation="transporter.1_0.wsdl",
	    name="TransporterWebService",
	    portName="TransporterPort",
	    targetNamespace="http://ws.transporter.upa.pt/",
	    serviceName="TransporterService"
	)
@HandlerChain(file = "/transporter_handler_chain.xml")
public class TransporterPort implements TransporterPortType {
	
	@Resource
	private WebServiceContext webServiceContext;
	
	private int upaNo, requestID = 1;
	private PublicKey bpk;
	private SortedMap<String, JobView> jobs = new TreeMap<String, JobView>();
	private SortedMap<String, String> locations = new TreeMap<String, String>();
	private Timer statusUpdater= new Timer();
	TimerTask statusUpdate = new TimerTask() {
	    @Override
	    public void run() {
	        changeStates();
	    }
	};
	
	public TransporterPort(){
		
		//Norte
		locations.put("Porto", "N"); locations.put("Braga", "N"); locations.put("Viana do Castelo", "N");
		locations.put("Vila Real", "N"); locations.put("Bragança", "N");
		
		//Sul
		locations.put("Setúbal", "S"); locations.put("Évora", "S"); locations.put("Portalegre", "S");
		locations.put("Beja", "S"); locations.put("Faro", "S");	
		
		//Centro
		locations.put("Lisboa", "C"); locations.put("Leiria", "C");locations.put("Santarém", "C");
		locations.put("Castelo Branco", "C"); locations.put("Coimbra", "C"); locations.put("Aveiro", "C");
		locations.put("Viseu", "C"); locations.put("Guarda", "C");		
		
		statusUpdater.schedule(statusUpdate, 0, generateDelay());
	}
	
	public void setUpaNo(int upa){
		upaNo = upa;
	}
	
	public int getUpaNo(){
		return upaNo;
	}
	
	@Override
	public String ping(String name) {
		return "UpaTransporter" + getUpaNo() + " is alive and fully operational.";
	}

	
	private int randomPrice(int min, int max){
		 Random rand = new Random();
		 int randomNum = rand.nextInt((max - min) + 1) + min;
		 return randomNum;
	}
	
	private String genID(){
		String tempString = "UpaTransporter" + upaNo + "_ID=" + requestID;
		requestID += 1;
		return tempString;
	}

	@Override
	public JobView requestJob(String origin, String destination, int price)
			throws BadLocationFault_Exception, BadPriceFault_Exception {
		
		int priceOffer = 0;
		JobView jobOffer = new JobView();
		
		if (origin == null){
			throw new BadLocationFault_Exception("Null origin was received and detected", new BadLocationFault());
			}
		if (destination == null){
			throw new BadLocationFault_Exception("Null destination was received and detected", new BadLocationFault());
			}

		
		
		if(price < 0){
			throw new BadPriceFault_Exception("Negative prices should not be suggested.", new BadPriceFault());
		}
		if(price > 100){
			return null;
		}
		
		if(!locations.containsKey(origin)){
			throw new BadLocationFault_Exception("Unknown Origin.", new BadLocationFault());
		}
		
		if(!locations.containsKey(destination)){
			throw new BadLocationFault_Exception("Unknown destination.", new BadLocationFault());
		}
		
		if((upaNo % 2 == 0) && (((locations.get(origin).equals("S")) || (locations.get(destination).equals("S"))))){
			return null;
		}
		if((upaNo % 2 != 0) && (((locations.get(origin).equals("N")) || (locations.get(destination).equals("N"))))){
			return null;
		}
		
		if(price < 10){
			priceOffer = randomPrice(0, price);
		}
		
		if(price > 10 && price <= 100 ){
			if(price % 2 != 0){
				if(upaNo % 2 != 0){
					priceOffer = randomPrice(0, price);
				}
				else{
					priceOffer = randomPrice(price, 100);
				}
			}
			else{
				if(upaNo % 2 == 0){
					priceOffer = randomPrice(0, price);
				}
				else{
					priceOffer = randomPrice(price, 100);
				}
			}
		}
		
		jobOffer.setCompanyName("UpaTransporter" + upaNo);
		jobOffer.setJobDestination(destination);
		jobOffer.setJobIdentifier(genID());
		jobOffer.setJobOrigin(origin);
		jobOffer.setJobPrice(priceOffer);
		jobOffer.setJobState(JobStateView.PROPOSED);
		jobs.put(jobOffer.getJobIdentifier(),jobOffer);
		return jobOffer;
		
	}

	@Override
	public JobView decideJob(String id, boolean accept) throws BadJobFault_Exception {
		if(id == null){throw new BadJobFault_Exception("Job was previously accepted duplicate recived", new BadJobFault());}

		if(jobs.containsKey(id)){
			if(jobs.get(id).getJobState().equals(JobStateView.PROPOSED))
				if(accept){
					jobs.get(id).setJobState(JobStateView.ACCEPTED);
				}
				else{
					jobs.get(id).setJobState(JobStateView.REJECTED);
				}
			else{
				throw new BadJobFault_Exception("Job was previously accepted duplicate recived", new BadJobFault());
			}
		}
		else{
			throw new BadJobFault_Exception("Trying to accept a null detected", new BadJobFault());
		}
		return jobs.get(id);
	}

	@Override
	public JobView jobStatus(String id) {
		if(id == null){return null;}

		if(jobs.containsKey(id)){
			return jobs.get(id);
		}
		else{
			return null;
		}
	}

	@Override
	public List<JobView> listJobs() {
		List<JobView> jobsList = new ArrayList<JobView>();
		jobsList.addAll(jobs.values());
		return jobsList;
	}

	@Override
	public void clearJobs() {
		jobs.clear();
	}
	
	
	private void changeStates(){
		JobStateView state;
		for(JobView job: jobs.values()){
			state = job.getJobState();
			if(state.equals(JobStateView.ACCEPTED)){
				job.setJobState(JobStateView.HEADING);
			}
			if(state.equals(JobStateView.HEADING)){
				job.setJobState(JobStateView.ONGOING);
			}
			if(state.equals(JobStateView.ONGOING)){		
				job.setJobState(JobStateView.COMPLETED);
			}
		}
	}
	
	private int generateDelay(){
		Random random = new Random();
		int result = random.nextInt(4000) + 1000;
		return result;
	}
	
	
	public void cancelTimer(){
		statusUpdater.cancel();
	}
	
	

	// TODO

}
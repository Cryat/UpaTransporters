package pt.upa.ca.ws;

import javax.jws.WebService;

@WebService
public interface CAInterface {

	String ping(String name);
	
	String getCertificate(String entityName) throws Exception;

}

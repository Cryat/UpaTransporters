package pt.upa.ca.ws.cli;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.security.PublicKey;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.util.Map;

import static javax.xml.ws.BindingProvider.ENDPOINT_ADDRESS_PROPERTY;

import javax.xml.bind.DatatypeConverter;
import javax.xml.ws.BindingProvider;

import pt.ulisboa.tecnico.sdis.ws.uddi.UDDINaming;
import pt.upa.ca.ws.CAImplService;
import pt.upa.ca.ws.CAInterface;
import pt.upa.ca.ws.Exception_Exception;

public class CA {
	
	CAImplService service = null;

	/** WS port (port type is the interface, port is the implementation) */
	CAInterface port = null;

	/** UDDI server URL */
	private String uddiURL = null;

	/** WS name */
	private String wsName = null;

	/** WS endpoint address */
	private String wsURL = null; // default value is defined inside WSDL

	public String getWsURL() {
		return wsURL;
	}

	/** output option **/
	private boolean verbose = false;

	public boolean isVerbose() {
		return verbose;
	}

	public void setVerbose(boolean verbose) {
		this.verbose = verbose;
	}

	/** constructor with provided web service URL */
	public CA(String wsURL) throws CAException {
		this.wsURL = wsURL;
		createStub();
	}

	/** constructor with provided UDDI location and name */
	public CA(String uddiURL, String wsName) throws CAException {
		this.uddiURL = uddiURL;
		this.wsName = wsName;
		uddiLookup();
		createStub();
	}

	/** UDDI lookup */
	private void uddiLookup() throws CAException {
		try {
			if (verbose)
				System.out.printf("Contacting UDDI at %s%n", uddiURL);
			UDDINaming uddiNaming = new UDDINaming(uddiURL);

			if (verbose)
				System.out.printf("Looking for '%s'%n", wsName);
			wsURL = uddiNaming.lookup(wsName);

		} catch (Exception e) {
			String msg = String.format("Client failed lookup on UDDI at %s!",
					uddiURL);
			throw new CAException(msg, e);
		}

		if (wsURL == null) {
			String msg = String.format(
					"Service with name %s not found on UDDI at %s", wsName,
					uddiURL);
			throw new CAException(msg);
		}
	}

	/** Stub creation and configuration */
	private void createStub() {
		if (verbose)
			System.out.println("Creating stub ...");
		service = new CAImplService();
		port = service.getCAImplPort();

		if (wsURL != null) {
			if (verbose)
				System.out.println("Setting endpoint address ...");
			BindingProvider bindingProvider = (BindingProvider) port;
			Map<String, Object> requestContext = bindingProvider
					.getRequestContext();
			requestContext.put(ENDPOINT_ADDRESS_PROPERTY, wsURL);
		}
	}

	// remote invocation methods ----------------------------------------------

	public String ping(String name) {
		return port.ping(name);
	}
	
	public PublicKey getPublicKeyFromCA(String entity) throws Exception{
		String received = port.getCertificate(entity);
		byte[] bytes =DatatypeConverter.parseBase64Binary(received);
		try {
			CertificateFactory cf = CertificateFactory.getInstance("X.509");
			InputStream in = new ByteArrayInputStream(bytes);
			Certificate cert =cf.generateCertificate(in);
			return cert.getPublicKey();
			
		} catch (CertificateException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null; //Should not happen but should be tested
	}
	
	// main -------------------------------------------------------------------

	public static void main(String[] args) throws Exception {
		// Check arguments
		if (args.length == 0) {
			System.err.println("Argument(s) missing!");
			System.err.println("Usage: java " + CA.class.getName()
					+ " wsURL OR uddiURL wsName");
			return;
		}
		String uddiURL = null;
		String wsName = null;
		String wsURL = null;
		if (args.length == 1) {
			wsURL = args[0];
		} else if (args.length >= 2) {
			uddiURL = args[0];
			wsName = args[1];
		}

		// Create client
		CA client = null;

		if (wsURL != null) {
			System.out.printf("Creating client for server at %s%n", wsURL);
			client = new CA(wsURL);
		} else if (uddiURL != null) {
			System.out
			.printf("Creating client using UDDI at %s for server with name %s%n",
					uddiURL, wsName);
			client = new CA(uddiURL, wsName);
		}

		// the following remote invocations are just basic examples
		// the actual tests are made using JUnit

		System.out.println("Invoke ping()...");
		String result = client.ping("client");
		System.out.println(result);

	}
}

package pt.upa.ca;

import static javax.xml.ws.BindingProvider.ENDPOINT_ADDRESS_PROPERTY;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.security.cert.Certificate;
import java.security.cert.CertificateFactory;
import java.util.Map;

import javax.xml.ws.BindingProvider;

// classes generated from WSDL
import pt.ulisboa.tecnico.sdis.ws.uddi.UDDINaming;
import pt.upa.ca.ws.CAImplService;
import pt.upa.ca.ws.CAInterface;
import pt.upa.ca.ws.cli.CA;

public class CAClientApplication {


	public static void main(String[] args) throws Exception {
		// Check arguments
		if (args.length == 0) {
            System.err.println("Argument(s) missing!");
            System.err.println("Usage: java " + CAClientApplication.class.getName()
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
            System.out.printf("Creating client using UDDI at %s for server with name %s%n",
                uddiURL, wsName);
            client = new CA(uddiURL, wsName);
        }

        // the following remote invocations are just basic examples
        // the actual tests are made using JUnit

        System.out.println("Invoke ping()...");
        String result = client.ping("client");
        System.out.println(result);
        String result2 = client.getPublicKeyFromCA("Broker").toString();
        System.out.println(result2);
	}
	
	
	

}

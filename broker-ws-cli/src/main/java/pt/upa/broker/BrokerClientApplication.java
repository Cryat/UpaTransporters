package pt.upa.broker;

import static javax.xml.ws.BindingProvider.ENDPOINT_ADDRESS_PROPERTY;

import java.util.List;
import java.util.Map;
import java.util.Scanner;

import javax.xml.ws.BindingProvider;

import pt.ulisboa.tecnico.sdis.ws.uddi.UDDINaming;
import pt.upa.broker.BrokerClientApplication;
import pt.upa.broker.ws.BrokerPortType;
import pt.upa.broker.ws.BrokerService;
import pt.upa.broker.ws.TransportView;
import pt.upa.broker.ws.cli.BrokerClient;

public class BrokerClientApplication {

	public static void main(String[] args) throws Exception {
		int command, price;
		String origin, destination, transport;
		boolean flag = true;
		Scanner scanner = new Scanner(System.in);
        // Check arguments
        if (args.length < 2) {
                System.err.println("Argument(s) missing!");
                System.err.printf("Usage: java %s uddiURL name%n", BrokerClientApplication.class.getName());
                return;
        }

        String uddiURL = args[0];

        String name = args[1];

        BrokerClient bc = new BrokerClient(uddiURL, name);
        bc.connect();
        String result = null;
        List<TransportView> transportView;
        TransportView tp;
        //System.out.println(result);
        //bc.requestTransport("Lisboa", "Leiria", 31);
        System.out.println("Welcome to UPA!");
        System.out.println("Press 1 to request a transport");
        System.out.println("Press 2 to clear all transports");
        System.out.println("Press 3 to list all transports");
        System.out.println("Press 4 to view all transports");
        System.out.println("Press 5 for help");
        System.out.println("Press 6 to exit the application");
        //scanner.nextLine();        
        while(flag) {
            command = scanner.nextInt();
	    	switch(command) {
	    		case 1:
	    			System.out.println("Request Transport");
	    			System.out.println("Origin: ");
	    			origin = scanner.next();
	    			System.out.println("Destination: ");
	    			destination = scanner.next(); 
	    			System.out.println("Price: ");
	    			price = scanner.nextInt();
	    			result = bc.requestTransport(origin, destination, price);
	    			System.out.println(result);
	    			break;
	    		case 2:
	    			System.out.println("Clear Transports");
	    			bc.clearTransports();
	    			System.out.println("The transports are cleaned");
	    			break;
	    		case 3:
	    			System.out.println("List Transports");
	    			transportView = bc.listTransports();
	    			for(TransportView tp2 : transportView) {
	    				System.out.println("Id: " + tp2.getId() + " Company: " + tp2.getTransporterCompany());
	    			}
	    			break;
	    		case 4:
	    			System.out.println("View Transport");
	    			System.out.println("Transport: ");
	    			transport = scanner.next();
	    			tp = bc.viewTransport(transport);
	    			System.out.println("Id: " + tp.getId() + " Company: " + tp.getTransporterCompany());
	    			break;
	    		case 5:
	    	        System.out.println("Press 1 to request a transport");
	    	        System.out.println("Press 2 to clear all transports");
	    	        System.out.println("Press 3 to list all transports");
	    	        System.out.println("Press 4 to view all transports");
	    	        System.out.println("Press 5 for help");
	    	        System.out.println("Press 6 to exit the application");
	    	        break;
	    		case 6:
	    			System.out.println("Bye!");
	    			flag = false;
	    			break;
	    		default:
	    			System.out.println("Invalid Command");
	    			command = (scanner.nextInt());
	    			break;
	    	}
        }
    }
        

}

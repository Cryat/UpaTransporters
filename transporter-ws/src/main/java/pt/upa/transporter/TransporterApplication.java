package pt.upa.transporter;

import pt.upa.handler.utils.Context;
import pt.upa.transporter.ws.TransporterEndpointManager;

public class TransporterApplication {
		
	public static void main(String[] args) throws Exception {
		// Check arguments
				if (args.length < 3) {
					System.err.println("Argument(s) missing!");
					System.err.printf("Usage: java %s uddiURL wsName wsURL%n", TransporterApplication.class.getName());
					return;
				}

				String uddiURL = args[0];
				String name = args[1];
				String url = args[2];
				
				Context.entityName = name;
				TransporterEndpointManager manager= new TransporterEndpointManager(name, url, uddiURL);
				manager.start();
				manager.awaitConnections();
				manager.stop();
			}


}

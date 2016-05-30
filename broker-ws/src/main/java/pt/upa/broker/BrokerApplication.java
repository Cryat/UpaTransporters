package pt.upa.broker;



import pt.upa.broker.ws.BrokerEndpointManager;

public class BrokerApplication {

	public static void main(String[] args) throws Exception {
		// Check arguments
				if (args.length < 3) {
					System.err.println("Argument(s) missing!");
					System.err.printf("Usage: java %s uddiURL wsName wsURL%n", BrokerApplication.class.getName());
					return;
				}

				String uddiURL = args[0];
				String name = args[1];
				String url = args[2];

				BrokerEndpointManager manager = new BrokerEndpointManager(name, url, uddiURL);
				
				manager.start();
				manager.awaitConnections();
				manager.stop();
			}

}
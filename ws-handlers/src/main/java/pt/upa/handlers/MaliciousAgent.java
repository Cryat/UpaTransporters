package pt.upa.handlers;

import java.util.Iterator;
import java.util.Set;

import javax.xml.namespace.QName;
import javax.xml.soap.Name;
import javax.xml.soap.SOAPBody;
import javax.xml.soap.SOAPElement;
import javax.xml.soap.SOAPEnvelope;
import javax.xml.soap.SOAPException;
import javax.xml.soap.SOAPHeader;
import javax.xml.soap.SOAPHeaderElement;
import javax.xml.soap.SOAPMessage;
import javax.xml.soap.SOAPPart;
import javax.xml.ws.handler.MessageContext;
import javax.xml.ws.handler.soap.SOAPHandler;
import javax.xml.ws.handler.soap.SOAPMessageContext;

/**
 *  This SOAPHandler shows how to set/get values from headers in
 *  inbound/outbound SOAP messages.
 *
 *  A header is created in an outbound message and is read on an
 *  inbound message.
 *
 *  The value that is read from the header
 *  is placed in a SOAP message context property
 *  that can be accessed by other handlers or by the application.
 */
public class MaliciousAgent implements SOAPHandler<SOAPMessageContext> {

	public static final String SENDER_SIGNATURE = "senderSignature";
	public static final String SENDER_NS = "urn:upa";
	public static final String SENDER_ENTITY = "senderEntity";
	public static final String SENDER_HEADER = "senderHeader";
	public static final String SENDER_NOUNCE = "senderNounce";
	
	public static Boolean toRepeat = false;
	public static SOAPMessage repeaterMessage= null;

    //
    // Handler interface methods
    //
    public Set<QName> getHeaders() {
        return null;
    }

    public boolean handleMessage(SOAPMessageContext smc) {

        Boolean outboundElement = (Boolean) smc
                .get(MessageContext.MESSAGE_OUTBOUND_PROPERTY);

        try {
            if (!outboundElement.booleanValue()) {
            	if(toRepeat){
    				System.out.println(">>>>Malicious Agent---Injeting Repeated Message");
    				smc.setMessage(repeaterMessage);
    				return true;
    			}
            	toRepeat=true;
            	
                // get SOAP envelope header
                SOAPMessage msg = smc.getMessage();
                SOAPPart sp = msg.getSOAPPart();
                SOAPEnvelope se = sp.getEnvelope();
                
                SOAPHeader sh;
				
    			try {
    				sh = se.getHeader();
    			} catch (SOAPException e1) {
    				System.out.println(">>>>Malicious Agent---Failed to get header from soap envelope discarding message---");
    				throw new RuntimeException("Failed to verify Signature");
    			}
    			
    			// check header
    			if (sh == null) {
    				System.out.println(">>>>Malicious Agent---Header not Found discarding message---");
    				return true;
    			}
    			repeaterMessage = smc.getMessage();
            
    		/*
                SOAPBody sb = se.getBody();
                
                System.out.println(">>>>MaliciousAgent---Injecting Information into message body.---");
       			sb.addTextNode("tamperedMessage");
*/

                
               
            }
        } catch (Exception e) {
            System.out.print("Caught exception in handleMessage: ");
            System.out.println(e);
            System.out.println("Continue normal processing...");
        }

        return true;
    }

    public boolean handleFault(SOAPMessageContext smc) {
        System.out.println("Ignoring fault message...");
        return true;
    }

    public void close(MessageContext messageContext) {
    }
    
    private SOAPElement getElementFromHeader(SOAPEnvelope env,SOAPHeader h, String localName, String prefix, String uri) throws SOAPException{
		Name name = env.createName(localName, prefix, uri);
		
		Iterator it = h.getChildElements(name);
		
		if (!it.hasNext()) {
			System.out.printf("Header element %s not found.%n", SENDER_SIGNATURE);
			return null;
		}
		SOAPElement element = (SOAPElement) it.next();
		return element;
	}

}
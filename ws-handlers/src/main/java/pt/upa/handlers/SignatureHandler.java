package pt.upa.handlers;


import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.StringWriter;
import java.security.KeyStore;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.security.Signature;
import java.security.SignatureException;
import java.security.cert.Certificate;
import java.security.cert.CertificateFactory;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;

import javax.xml.bind.DatatypeConverter;
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
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.TransformerFactoryConfigurationError;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.ws.handler.MessageContext;
import javax.xml.ws.handler.soap.SOAPHandler;
import javax.xml.ws.handler.soap.SOAPMessageContext;

import pt.upa.ca.ws.cli.CA;
import pt.upa.handler.utils.Context;




public class SignatureHandler implements SOAPHandler<SOAPMessageContext> {
	
	public static final String SENDER_SIGNATURE = "senderSignature";
	public static final String SENDER_NS = "urn:upa";
	public static final String SENDER_ENTITY = "senderEntity";
	public static final String SENDER_HEADER = "senderHeader";
	public static final String SENDER_NOUNCE = "senderNounce";
	
	public static final String KSPassword = "ins3cur3";
	public static final String KeyPassword = "1nsecure";
	public static SortedMap<String, PublicKey> publicKeyCache = new TreeMap<String, PublicKey>();
	public static List<String> outboundNounces = new ArrayList<String>();
	public static List<String> inboundNounces = new ArrayList<String>();



	public boolean handleMessage(SOAPMessageContext smc) {
		Boolean outbound = (Boolean) smc.get(MessageContext.MESSAGE_OUTBOUND_PROPERTY);
				
		if (outbound) {
			
			//outbound message needs signing
			
			String senderEntity = Context.entityName;
			
			SecureRandom sr = new SecureRandom();
			Long nounceLong = sr.nextLong();
			String nounceString = nounceLong.toString(); 

			while(outboundNounces.contains(nounceString)){
				nounceLong = sr.nextLong();
				nounceLong.toString(); 
			}
			
			outboundNounces.add(nounceString);


			// get SOAP envelope
			SOAPMessage msg = smc.getMessage();
			SOAPPart sp = msg.getSOAPPart();
			
			SOAPEnvelope se;
			try {
				se = sp.getEnvelope();
			} catch (SOAPException e) {
				System.out.println(">>>>Handler---Failed to get envelope from soap part aborting---");
				throw new RuntimeException("Failed to generate Signature");
			}

			// add header
			SOAPHeader sh;
			try {
				
				sh = se.getHeader();
				
			} catch (SOAPException e) {
				System.out.println(">>>>Handler---Failed to get header from soap envelope aborting---");
				throw new RuntimeException("Failed to generate Signature");
			}
			
			
			//Make the digital signature of the body
			
			//Obtaining the body
			SOAPBody sb;
			try {
				
				sb = se.getBody();
				
			} catch (SOAPException e1) {
				System.out.println(">>>>Handler---Failed to get body soap envelope aborting---");
				throw new RuntimeException("Failed to generate Signature");
			}
			
			//Converting From SOAPBody into String
			DOMSource source = new DOMSource(sb);
			StringWriter stringResult = new StringWriter();
			try {
				
				TransformerFactory.newInstance().newTransformer().transform(source, new StreamResult(stringResult));
				
			} catch (TransformerException | TransformerFactoryConfigurationError e1) {
				System.out.println(">>>>Handler---Failed to transform soap body internal error aborting---");
				throw new RuntimeException("Failed to generate Signature");
			}
			
			String bodyString = stringResult.toString();
			
			String toSignString = senderEntity + nounceString + bodyString;
			
			
			//Converting the body String into byte[] needed for signature
			byte[] rawToSign= DatatypeConverter.parseBase64Binary(toSignString);

			//Generating Path to privateKey
			String pathToStore = "PrivateKey/"+senderEntity+".jks";
			
			//getting the corresponding PrivateKey from the file
			PrivateKey pk;
			try {
				pk = getPrivateKeyFromKeystore(pathToStore, KSPassword.toCharArray(),
														  senderEntity, KeyPassword.toCharArray());
			} catch (Exception e1) {
				System.out.println(">>>>Handler---Failed to read private key from file aborting---");
				throw new RuntimeException("Failed to generate Signature");
			}
			
			//signing the raw body bytes
			byte[] rawSignature;
			try {
				rawSignature = makeDigitalSignature(rawToSign, pk);
			} catch (Exception e1) {
				System.out.println(">>>>Handler---Failed while generating signature internal error aborting---");
				throw new RuntimeException("Failed to generate Signature");
			}
			String SignatureString = DatatypeConverter.printBase64Binary(rawSignature);
			
			// add header signature element its value -- signature value
			
			
			PublicKey pbk;
			try {
				pbk = getPublicKeyFromKeystore(pathToStore, KSPassword.toCharArray(), senderEntity);
			} catch (Exception e1) {
				System.out.println(">>>>Handler---Failed to get public key from file aborting---");
				throw new RuntimeException("Failed to generate Signature");
			}
			Boolean isvalid;
			try {
				isvalid = verifyDigitalSignature(rawSignature, rawToSign, pbk);
			} catch (Exception e1) {
				System.out.println(">>>>Handler---Failed to verify own signature aborting---");
				throw new RuntimeException("Failed to generate Signature");
			}
			
			if(isvalid){
				System.out.println(">>>>Handler---Signature Successfully Generated---");
			}
			else{
				System.out.println(">>>>Handler---Bad Signature Generation---");
				throw new RuntimeException("Failed to generate Signature");
			}
			
			
			if (sh == null)
				try {
					
					sh = se.addHeader();
					
				} catch (SOAPException e) {
					System.out.println(">>>>Handler---Failed to add header to soap envelope aborting---");
					throw new RuntimeException("Failed to generate Signature");
				}

			// add header element (name, namespace prefix, namespace) - signature element
			try{
				
				Name senderEntityName = se.createName(SENDER_ENTITY, "e", SENDER_NS);
				SOAPHeaderElement senderEntityElement = sh.addHeaderElement(senderEntityName);
				senderEntityElement.addTextNode(senderEntity);
				
				Name nounceName = se.createName(SENDER_NOUNCE, "e", SENDER_NS);
				SOAPHeaderElement senderNounceElement = sh.addHeaderElement(nounceName);
				senderNounceElement.addTextNode(nounceString);
				
				Name signatureName = se.createName(SENDER_SIGNATURE, "e", SENDER_NS);
				SOAPHeaderElement senderSignatureElement = sh.addHeaderElement(signatureName);
				senderSignatureElement.addTextNode(SignatureString);
				
			}catch(SOAPException e){
				System.out.println(">>>>Handler---Failed to add header elements to header---");
				throw new RuntimeException("Failed to generate Signature");
			}

			
		} 
		else {
		// inbound message needs signature verification

		// get token from response SOAP header
			// get SOAP envelope header
			SOAPMessage msg = smc.getMessage();
			SOAPPart sp = msg.getSOAPPart();
			
			SOAPEnvelope se;
			try {
				se = sp.getEnvelope();
			} catch (SOAPException e1) {
				System.out.println(">>>>Handler---Failed to get envelope from soap part discarding message---");
				throw new RuntimeException("Failed to verify Signature");
			}
			
			
			SOAPHeader sh;
			try {
				sh = se.getHeader();
			} catch (SOAPException e1) {
				System.out.println(">>>>Handler---Failed to get header from soap envelope discarding message---");
				throw new RuntimeException("Failed to verify Signature");
			}

			// check header
			if (sh == null) {
				System.out.println(">>>>Handler---Header not Found discarding message---");
				return true;
			}
			
			
			SOAPElement senderEntityElement = null;
			try {
				senderEntityElement = getElementFromHeader(se, sh, SENDER_ENTITY, "e", SENDER_NS);
			} catch (SOAPException e) {
				System.out.println(">>>>Handler---Failed to get sender element from header discarding message---");
				throw new RuntimeException("Failed to verify Signature");

			}
			String senderEntityString = senderEntityElement.getValue();

			
			SOAPElement senderNounceElement = null;
			try {
				senderNounceElement = getElementFromHeader(se, sh, SENDER_NOUNCE, "e", SENDER_NS);
			} catch (SOAPException e) {
				System.out.println(">>>>Handler---Failed to get nounce element from header discarding message---");
				throw new RuntimeException("Failed to verify Signature");
			}
			String nounceString = senderNounceElement.getValue();
			
			if(inboundNounces.contains(nounceString)){
				throw new RuntimeException("Repeated Nounce Aborting");
			}
			else{
				inboundNounces.add(nounceString);
			}

			
			SOAPElement senderSignatureElement = null;
			try {
				senderSignatureElement = getElementFromHeader(se, sh, SENDER_SIGNATURE, "e", SENDER_NS);
			} catch (SOAPException e) {
				System.out.println(">>>>Handler---Failed to get signature element from header discarding message---");
				throw new RuntimeException("Failed to verify Signature");
			}
			String signatureString = senderSignatureElement.getValue();

			
			SOAPBody soapBody = null;
			try {
				soapBody = se.getBody();
			} catch (SOAPException e) {
				System.out.println(">>>>Handler---Failed to get body element from envelope discarding message---");
				throw new RuntimeException("Failed to verify Signature");
			}
			
			//not sure if it works
			DOMSource source = new DOMSource(soapBody);
			StringWriter stringResult = new StringWriter();
			try {
				TransformerFactory.newInstance().newTransformer().transform(source, new StreamResult(stringResult));
			} catch (TransformerException | TransformerFactoryConfigurationError e) {
				System.out.println(">>>>HandlerFailed to transform message body to string internal error");
				throw new RuntimeException("Failed to verify Signature");
			}
			String bodyString = stringResult.toString(); 
			
			String contentString = senderEntityString + nounceString + bodyString;
			
			byte[] rawContent= DatatypeConverter.parseBase64Binary(contentString);
			byte[] rawSignature = DatatypeConverter.parseBase64Binary(signatureString);

			PublicKey responseEntityPbk = null;
			if(publicKeyCache.containsKey(senderEntityString)){
				responseEntityPbk = publicKeyCache.get(senderEntityString);
			}
			else{
				System.out.println(">>>>Handler---Public Key to entity not found on Context contacting CA---");
				try{
					CA caClient = new CA("http://localhost:9090", "UpaCA");
					responseEntityPbk = caClient.getPublicKeyFromCA(senderEntityString);
				}catch (Exception e) {
					System.out.println(">>>>Handler---Failed to get public Key from CA discarding message---");
				}
				publicKeyCache.put(senderEntityString, responseEntityPbk);
			}
			
			Boolean validMessage = null;
			try {
				validMessage = verifyDigitalSignature(rawSignature, rawContent, responseEntityPbk);
			} catch (Exception e) {
				System.out.println(">>>>Handler---Internal Error while verifying signature discarding message---");
			}
			System.out.println(">>>>Handler---Verifying Digital Signature---");
			
			if(validMessage){
				System.out.println(">>>>Handler---Valid Signature. Processing message---");
			}
			else{
				System.out.println(">>>>Handler---Invalid Signature. Discarding message---");
				throw new RuntimeException("Signature could not be validated aborting connection.");
			}
				
		}

		return true;
	}

	public boolean handleFault(SOAPMessageContext smc) {
		return true;
	}

	public Set<QName> getHeaders() {
		return null;
	}

	public void close(MessageContext messageContext) {
	}
	
	
	private static PrivateKey getPrivateKeyFromKeystore(String keyStoreFilePath, char[] keyStorePassword,
			String keyAlias, char[] keyPassword) throws Exception {

		KeyStore keystore = readKeystoreFile(keyStoreFilePath, keyStorePassword);
		PrivateKey key = (PrivateKey) keystore.getKey(keyAlias, keyPassword);

		return key;
	}
	
	private static PublicKey getPublicKeyFromKeystore(String keyStoreFilePath, char[] keyStorePassword,
			String certAlias) throws Exception {

		KeyStore keystore = readKeystoreFile(keyStoreFilePath, keyStorePassword);
		PublicKey key = keystore.getCertificate(certAlias).getPublicKey();

		return key;
	}
	
	private static KeyStore readKeystoreFile(String keyStoreFilePath, char[] keyStorePassword) throws Exception {
		FileInputStream fis;
		try {
			fis = new FileInputStream(keyStoreFilePath);
		} catch (FileNotFoundException e) {
			System.err.println("Keystore file <" + keyStoreFilePath + "> not fount.");
			return null;
		}
		KeyStore keystore = KeyStore.getInstance(KeyStore.getDefaultType());
		keystore.load(fis, keyStorePassword);
		return keystore;
	}
	
	
	/** auxiliary method to calculate digest from text and cipher it */
	private static byte[] makeDigitalSignature(byte[] bytes, PrivateKey privateKey) throws Exception {

		// get a signature object using the SHA-1 and RSA combo
		// and sign the plain-text with the private key
		Signature sig = Signature.getInstance("SHA1WithRSA");
		sig.initSign(privateKey);
		sig.update(bytes);
		byte[] signature = sig.sign();

		return signature;
	}
	
	
	public static Certificate readCertificateFile(String certificateFilePath) throws Exception {
		FileInputStream fis;

		try {
			fis = new FileInputStream(certificateFilePath);
		} catch (FileNotFoundException e) {
			System.err.println("Certificate file <" + certificateFilePath + "> not fount.");
			return null;
		}
		BufferedInputStream bis = new BufferedInputStream(fis);

		CertificateFactory cf = CertificateFactory.getInstance("X.509");

		if (bis.available() > 0) {
			Certificate cert = cf.generateCertificate(bis);
			return cert;
			// It is possible to print the content of the certificate file:
			// System.out.println(cert.toString());
		}
		bis.close();
		fis.close();
		return null;
	}
	
	
	public static boolean verifyDigitalSignature(byte[] cipherDigest, byte[] bytes, PublicKey publicKey)
			throws Exception {

		// verify the signature with the public key
		Signature sig = Signature.getInstance("SHA1WithRSA");
		sig.initVerify(publicKey);
		sig.update(bytes);
		try {
			return sig.verify(cipherDigest);
		} catch (SignatureException se) {
			System.err.println("Caught exception while verifying signature " + se);
			return false;
		}
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



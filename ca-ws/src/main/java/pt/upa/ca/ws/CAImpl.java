package pt.upa.ca.ws;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.security.cert.Certificate;
import java.security.cert.CertificateFactory;
import java.util.SortedMap;
import java.util.TreeMap;

import javax.jws.WebService;
import javax.xml.bind.DatatypeConverter;

@WebService(endpointInterface = "pt.upa.ca.ws.CAInterface")
public class CAImpl implements CAInterface {

	SortedMap<String, String> entityCerts= new TreeMap<String, String>();
	
	public CAImpl(){
		entityCerts.put("Broker", "certs/Broker.cer");
		entityCerts.put("UpaTransporter1", "certs/UpaTransporter1.cer");
		entityCerts.put("UpaTransporter2", "certs/UpaTransporter2.cer");
	}
	
	public String ping(String name) {
		return "Hello " + name + "!";
	}

	
	public String getCertificate(String entityName) throws Exception {
		Certificate certificate = readCertificateFile(entityCerts.get(entityName));
		byte[] bytes = certificate.getEncoded();
		String plainText = DatatypeConverter.printBase64Binary(bytes);
		return plainText;
		
	}
	
	
	private static Certificate readCertificateFile(String certificateFilePath) throws Exception {
		FileInputStream fis;

		try {
			fis = new FileInputStream(certificateFilePath);
		} catch (FileNotFoundException e) {
			System.err.println("Certificate file <" + certificateFilePath + "> not found.");
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

}
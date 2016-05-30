package pt.upa.ca.ws.cli;

public class CAException extends Exception {
	
	private static final long serialVersionUID = 1L;

	public CAException() {
    }

    public CAException(String message) {
        super(message);
    }

    public CAException(Throwable cause) {
        super(cause);
    }

    public CAException(String message, Throwable cause) {
        super(message, cause);
    }
}

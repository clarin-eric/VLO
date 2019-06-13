package eu.clarin.cmdi.vlo.exposure.postgresql;

public class VloExposureException extends Exception {
	  
	public VloExposureException(String message) {
	        super(message);
	    }

    public VloExposureException(String message, Throwable cause) {
        super(message, cause);
    }

    public VloExposureException(Throwable cause) {
        super(cause);
    }
}

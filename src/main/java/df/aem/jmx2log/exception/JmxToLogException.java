package df.aem.jmx2log.exception;

public class JmxToLogException extends RuntimeException {

    public JmxToLogException(String message) {
        super(message);
    }

    public JmxToLogException(String message, Throwable cause) {
        super(message, cause);
    }
}

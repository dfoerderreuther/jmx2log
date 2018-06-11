package df.aem.jmx2log.exception;

public class CouldNotReadJmxValueException extends JmxToLogException {

    public CouldNotReadJmxValueException(Throwable e) {
        super(e.getMessage(), e);
    }

}

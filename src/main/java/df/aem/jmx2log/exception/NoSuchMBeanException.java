package df.aem.jmx2log.exception;

public class NoSuchMBeanException extends JmxToLogException {

    public NoSuchMBeanException(String pattern) {
        super("Unable to find bean matching name pattern \"" + pattern + "\"");
    }

}

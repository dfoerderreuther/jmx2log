package df.aem.jmx2log;

import df.aem.jmx2log.exception.CouldNotReadJmxValueException;
import df.aem.jmx2log.exception.NoSuchAttributeException;
import df.aem.jmx2log.exception.NoSuchMBeanException;

import javax.management.ObjectName;

/**
 * Created by Dominik Foerderreuther df@adobe.com on 26.12.17.
 */
public interface ReadJmxService {

    /**
     * List all mBeans on local mBean server.
     *
     * @return all MBeans on local MBean server
     * @throws NoSuchMBeanException if no MBean exists.
     */
    Iterable<ObjectName> mBeans() throws NoSuchMBeanException;

    /**
     * Looks up all mBeans on local mBean server that match pattern.
     *
     * If no such mBean exists, a NoSuchMBeanException will be thrown.
     * @param pattern The pattern to match names of mbeans against
     * @return Set of matching {@link ObjectName}s
     * @throws NoSuchMBeanException if no matching MBean exists.
     */
    Iterable<ObjectName> mBeans(String pattern) throws NoSuchMBeanException;

    /**
     * Evaluates all attributes of mbean.
     *
     * @param mBean The mbean to evaluate
     * @return an iterable over all the values that have been extracted during run
     * @throws CouldNotReadJmxValueException if something goes wrong while reading any value
     * @throws NoSuchAttributeException if mbean does not contain any attribute
     */
    Iterable<MBeanAttribute> attributes(ObjectName mBean) throws CouldNotReadJmxValueException;

    /**
     * Evaluates all attributes on mbean that match namePattern.
     *
     * @param mBean The MBean to evaluate
     * @param namePattern The pattern attributes of mbean will be matched against
     * @return an iterable over all the values that have been extracted during run
     * @throws CouldNotReadJmxValueException if something goes wrong while reading any value
     * @throws NoSuchAttributeException if something is wrong with the configuration -> no matching attribute on mbean
     */
    Iterable<MBeanAttribute> attributes(ObjectName mBean, String namePattern)
            throws CouldNotReadJmxValueException, NoSuchAttributeException;

    interface MBeanAttribute {
        String name();

        String type();

        Object value();
    }

}

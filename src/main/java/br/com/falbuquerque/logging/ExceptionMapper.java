package br.com.falbuquerque.logging;

import java.util.HashMap;
import java.util.Map;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.Logger;

/**
 * Maps exceptions of different types to loggers.
 * 
 * @author Felipe Albuquerque
 */
public class ExceptionMapper {

    private final Map<Class<? extends Exception>, LoggerConfiguration> mapping = new HashMap<Class<? extends Exception>, LoggerConfiguration>();

    /**
     * Adds an &lt;exception type, logger&gt; tuple.
     * 
     * @param exceptionClass
     *            the class
     * @param mappedLogger
     *            the logger to which the exception type will be mapped
     * @return the current instance
     */
    public ExceptionMapper map(final Class<? extends Exception> exceptionClass, final Logger mappedLogger) {
        mapping.put(exceptionClass, new LoggerConfiguration(mappedLogger));
        return this;
    }

    /**
     * Adds an &lt;exception type, logger&gt; tuple.
     * 
     * @param exceptionClass
     *            the class
     * @param mappedLogger
     *            the logger to which the exception type will be mapped
     * @param logLevel
     *            the level of the log
     * @return the current instance
     */
    public ExceptionMapper map(final Class<? extends Exception> exceptionClass, final Logger mappedLogger, final Level logLevel) {
        mapping.put(exceptionClass, new LoggerConfiguration(mappedLogger, logLevel));
        return this;
    }

    /**
     * Gets the logger mapped to the given exception type.
     * 
     * @param exceptionClass
     *            the exception type
     * @return the logger mapped to the exception type or <code>null</code> if
     *         the specified type does not have any mapping
     */
    public LoggerConfiguration getLogger(final Class<? extends Exception> exceptionClass) {
        return mapping.get(exceptionClass);
    }

}

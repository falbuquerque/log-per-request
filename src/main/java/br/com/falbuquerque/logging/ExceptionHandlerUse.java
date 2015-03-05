package br.com.falbuquerque.logging;

import org.apache.logging.log4j.Logger;

import com.google.gson.annotations.Expose;

/**
 * Encapsulates the usage of exception handlers.
 * 
 * @author Felipe Albuquerque
 */
class ExceptionHandlerUse {

    private final ExceptionHandler exceptionHandler;
    private final BufferedLogger owner;
    
    @Expose
    private Boolean exceptionsLogged;

    /**
     * Creates an encapsulated exception handler user.
     * 
     * @param owner
     *            the logger that owns this exception handler user
     * @param defaultExceptionLogger
     *            the default exception logger
     * @param exceptionMapper
     *            the exception mapper
     */
    ExceptionHandlerUse(final BufferedLogger owner, final Logger defaultExceptionLogger, final ExceptionMapper exceptionMapper) {
        this.owner = owner;
        exceptionHandler = new ExceptionHandler(defaultExceptionLogger, exceptionMapper);
    }
    
    /**
     * Creates an encapsulated exception handler user.
     * 
     * @param owner
     *            the logger that owns this exception handler user
     * @param defaultExceptionLogger
     *            the default exception logger
     */
    ExceptionHandlerUse(final BufferedLogger owner, final Logger defaultExceptionLogger) {
        this(owner, defaultExceptionLogger, null);
    }

    /**
     * Appends an exception to the logger.
     * 
     * @param exception
     *            the exception to be appended
     * @return the current instance
     */
    void appendException(final Exception exception) {
        exceptionHandler.appendException(exception);
        exceptionsLogged = true;
    }

    /**
     * Gets the exception mapper.
     * 
     * @return the exception mapper
     */
    ExceptionMapper getExceptionMapper() {
        return exceptionHandler.getExceptionMapper();
    }

    /**
     * Invokes the log of the exception handler if there is anything to log.
     */
    void log() {
        exceptionHandler.acknowledgeLogger(owner);

        if (exceptionsLogged) {
            exceptionHandler.log();
        }

    }

}

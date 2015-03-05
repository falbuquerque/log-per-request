package br.com.falbuquerque.logging;

import java.util.Collection;
import java.util.LinkedList;

import org.apache.logging.log4j.Logger;

import br.com.falbuquerque.logging.request.Request;
import br.com.falbuquerque.logging.util.JsonParser;

import com.google.gson.annotations.Expose;

/**
 * Buffers the messages before logging them and the request. The information is
 * logged in a JSON format.
 * 
 * @author Felipe Albuquerque
 */
public class BufferedLogger {

    @Expose
    private final Request request;

    @Expose
    private final Collection<String> messages;

    private final Logger mainLogger;

    @Expose
    private ExceptionHandlerUse internalExceptions;

    @Expose
    private ExceptionHandlerUse businessExceptions;

    /**
     * Creates a buffered logger.
     * 
     * @param request
     *            the request that will be logged
     * @param mainLogger
     *            the main logger. Logs INFO, i.e., process step messages
     */
    public BufferedLogger(final Request request, final Logger mainLogger) {
        this(request, mainLogger, null, null);
    }

    /**
     * Creates a buffered logger.
     * 
     * @param request
     *            the request that will be logged
     * @param mainLogger
     *            the main logger. Logs INFO, i.e., process step messages
     * @param internalExceptionsLogger
     *            the internal exceptions logger. All appended internal
     *            exceptions are logged by this object
     */
    public BufferedLogger(final Request request, final Logger mainLogger, final Logger internalExceptionsLogger) {
        this(request, mainLogger, internalExceptionsLogger, null);
    }

    /**
     * Creates a buffered logger.
     * 
     * @param request
     *            the request that will be logged
     * @param mainLogger
     *            the main logger. Logs INFO, i.e., process step messages
     * @param internalExceptionsLogger
     *            the internal exceptions logger. All appended internal
     *            exceptions are logged by this object
     * @param businessExceptionsLogger
     *            the business exceptions logger. All appended business
     *            exceptions are logged by this object
     */
    public BufferedLogger(final Request request, final Logger mainLogger, final Logger internalExceptionsLogger,
            final Logger businessExceptionsLogger) {
        this.request = request;
        this.mainLogger = mainLogger;

        if (internalExceptionsLogger != null) {
            internalExceptions = new ExceptionHandlerUse(this, internalExceptionsLogger);
        }

        if (businessExceptionsLogger != null) {
            businessExceptions = new ExceptionHandlerUse(this, businessExceptionsLogger);
        }

        messages = new LinkedList<>();
    }

    /**
     * Appends a message to the logger.
     * 
     * @param message
     *            the message to be appended
     * @return the current instance
     */
    public BufferedLogger append(final String message) {
        messages.add(message);
        return this;
    }

    /**
     * Appends an internal exception to the logger.
     * 
     * @param exception
     *            the exception to be appended
     * @return the current instance
     */
    public BufferedLogger appendInternalException(final Exception exception) {
        initializeExceptionHandlerUses();
        internalExceptions.appendException(exception);
        return this;
    }

    /**
     * Appends a business exception to the logger.
     * 
     * @param exception
     *            the exception to be appended
     * @return the current instance
     */
    public BufferedLogger appendBusinessException(final Exception exception) {
        initializeExceptionHandlerUses();
        businessExceptions.appendException(exception);
        return this;
    }

    /**
     * Logs the content of the current buffered logger.
     */
    public void log() {
        initializeExceptionHandlerUses();

        if (mainLogger.isInfoEnabled()) {
            mainLogger.info(new JsonParser().toJson(this));
        }

        internalExceptions.log();
        businessExceptions.log();
    }

    /**
     * Creates a handler for internal exceptions for the current logger.
     * 
     * @param defaultExceptionLogger
     *            the default internal exceptions logger
     * @return the current instance
     */
    public BufferedLogger createInternalExceptionHandler(final Logger defaultExceptionLogger) {
        internalExceptions = new ExceptionHandlerUse(this, defaultExceptionLogger);
        return this;
    }

    /**
     * Creates a handler for internal exceptions for the current logger.
     * 
     * @param defaultExceptionLogger
     *            the default internal exceptions logger
     * @param exceptionMapper
     *            the exception mapper
     * @return the current instance
     */
    public BufferedLogger createInternalExceptionHandler(final Logger defaultExceptionLogger,
            final ExceptionMapper exceptionMapper) {
        internalExceptions = new ExceptionHandlerUse(this, defaultExceptionLogger, exceptionMapper);
        return this;
    }

    /**
     * Creates a handler for business exceptions for the current logger.
     * 
     * @param defaultExceptionLogger
     *            the default business exceptions logger
     * @return the current instance
     */
    public BufferedLogger createBusinessExceptionHandler(final Logger defaultBusinessExceptionLogger) {
        businessExceptions = new ExceptionHandlerUse(this, defaultBusinessExceptionLogger);
        return this;
    }

    /**
     * Creates a handler for business exceptions for the current logger.
     * 
     * @param defaultExceptionLogger
     *            the default business exceptions logger
     * @return the current instance
     */
    public BufferedLogger createBusinessExceptionHandler(final Logger defaultBusinessExceptionLogger,
            final ExceptionMapper exceptionMapper) {
        businessExceptions = new ExceptionHandlerUse(this, defaultBusinessExceptionLogger, exceptionMapper);
        return this;
    }

    /**
     * Gets the internal exceptions mapper.
     * 
     * @return the internal exceptions mapper
     */
    public ExceptionMapper getInternalExceptionsMapper() {

        if (internalExceptions != null) {
            return internalExceptions.getExceptionMapper();
        } else {
            return null;
        }

    }

    /**
     * Gets the business exceptions mapper.
     * 
     * @return the business exceptions mapper
     */
    public ExceptionMapper getBusinessExceptionsMapper() {

        if (businessExceptions != null) {
            return businessExceptions.getExceptionMapper();
        } else {
            return null;
        }

    }

    /**
     * Gets the request.
     * 
     * @return the request
     */
    Request getRequest() {
        return request;
    }

    /**
     * Initializes the exception handler uses if they were not initiated yet.
     */
    private void initializeExceptionHandlerUses() {

        if (internalExceptions == null) {
            internalExceptions = new ExceptionHandlerUse(this, mainLogger);
        }

        if (businessExceptions == null) {
            businessExceptions = new ExceptionHandlerUse(this, mainLogger);
        }

    }

}

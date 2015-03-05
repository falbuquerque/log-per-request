package br.com.falbuquerque.logging;

import java.util.Collection;
import java.util.LinkedList;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.Logger;

/**
 * Handles exceptions and directs them to the appropriate loggers.
 * 
 * @author Felipe Albuquerque
 */
class ExceptionHandler {

    private final Logger defaultExceptionLogger;
    private final ExceptionMapper exceptionMapper;
    private final Level defaultLogLevel;
    private final Collection<Exception> exceptions;
    private BufferedLogger bufferedLogger;

    /**
     * Creates an exception handler.
     * 
     * @param defaultExceptionLogger
     *            the logger that will log the exceptions
     */
    ExceptionHandler(final Logger defaultExceptionLogger) {
        this(defaultExceptionLogger, null, null);
    }

    /**
     * Creates an exception handler.
     * 
     * @param defaultExceptionLogger
     *            the logger that will log the exceptions
     * @param exceptionMapper
     *            the object responsible for mapping exceptions to different
     *            loggers
     */
    ExceptionHandler(final Logger defaultExceptionLogger, final ExceptionMapper exceptionMapper) {
        this(defaultExceptionLogger, exceptionMapper, null);
    }

    /**
     * Creates an exception handler.
     * 
     * @param defaultExceptionLogger
     *            the logger that will log the exceptions
     * @param defaultLogLevel
     *            the default log level to exceptions logger by the current
     *            object
     */
    ExceptionHandler(final Logger defaultExceptionLogger, final Level defaultLogLevel) {
        this(defaultExceptionLogger, null, defaultLogLevel);
    }

    private ExceptionHandler(final Logger defaultExceptionLogger, final ExceptionMapper exceptionMapper,
            final Level defaultLogLevel) {
        this.defaultExceptionLogger = defaultExceptionLogger;
        this.exceptionMapper = exceptionMapper;

        if (defaultLogLevel == null) {
            this.defaultLogLevel = Level.ERROR;
        } else {
            this.defaultLogLevel = defaultLogLevel;
        }

        exceptions = new LinkedList<>();
    }

    /**
     * Appends an exception to the log.
     * 
     * @param thrownException
     *            the thrown exception to be appended
     * @return the current instance
     */
    ExceptionHandler appendException(final Exception thrownException) {
        exceptions.add(thrownException);
        return this;
    }

    /**
     * Logs the exceptions.
     */
    void log() {
        String token = "undefined";

        if (bufferedLogger != null) {
            token = bufferedLogger.getRequest().getToken();
        }

        final String logMessage = "Exception in request [" + token + "]";

        for (final Exception exception : exceptions) {
            Logger logger = null;
            Level level = null;

            if (exceptionMapper != null) {
                final LoggerConfiguration loggerToException = exceptionMapper.getLogger(exception.getClass());

                if (loggerToException != null) {
                    logger = loggerToException.getLogger();
                    level = loggerToException.getLevel();
                }

            }

            if (logger == null) {
                logger = defaultExceptionLogger;
            }

            if (level == null) {
                level = defaultLogLevel;
            }

            if (logger.isEnabled(defaultLogLevel)) {
                logger.log(level, logMessage, exception);
            }

            logger.error(logMessage, exception);
        }

    }

    /**
     * Cleans up the exceptions from the handler.
     * 
     * @return the current instance
     */
    ExceptionHandler cleanupExceptions() {
        exceptions.clear();
        return this;
    }

    /**
     * Acknowledges the logger that is using the current exception handler.
     * 
     * @param logger
     *            the logger that is using the current exception handler
     */
    void acknowledgeLogger(final BufferedLogger logger) {
        this.bufferedLogger = logger;
    }

    /**
     * Gets the exception mapper.
     * 
     * @return the exception mapper
     */
    ExceptionMapper getExceptionMapper() {
        return exceptionMapper;
    }

}

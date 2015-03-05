package br.com.falbuquerque.logging;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.Logger;

/**
 * Represents a configuration of a logger.
 * 
 * @author Felipe Albuquerque
 */
public class LoggerConfiguration {

    private final Logger logger;
    private final Level level;

    /**
     * Creates a configuration of a logger.
     * 
     * @param logger
     *            the logger
     * @param level
     *            the log level
     */
    public LoggerConfiguration(final Logger logger, final Level level) {
        this.logger = logger;
        this.level = level;
    }

    /**
     * Creates a configuration of a logger.
     * 
     * @param logger
     *            the logger
     */
    public LoggerConfiguration(final Logger logger) {
        this(logger, null);
    }

    /**
     * Gets the logger.
     * 
     * @return the logger
     */
    public Logger getLogger() {
        return logger;
    }

    /**
     * Gets the log level.
     * 
     * @return the log level
     */
    public Level getLevel() {
        return level;
    }

}

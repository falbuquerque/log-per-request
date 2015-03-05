package br.com.falbuquerque.logging;

import static br.com.falbuquerque.logging.CommonConstants.TOKEN;
import static org.junit.Assert.assertNull;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;
import static org.powermock.api.mockito.PowerMockito.whenNew;

import java.text.MessageFormat;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.Logger;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import br.com.falbuquerque.logging.request.Parameter;
import br.com.falbuquerque.logging.request.Request;

/**
 * Tests the behavior of the {@link BufferedLogger} class.
 * 
 * @author Felipe Albuquerque
 */
@RunWith(PowerMockRunner.class)
@PrepareForTest({ BufferedLogger.class, ExceptionHandler.class, ExceptionHandlerUse.class })
public class BufferedLoggerTest {

    private static final String LOG_TEMPLATE = "'{'\"request\":'{'\"token\":\"{0}\","
            + "\"parameters\":['{'\"name\":\"{1}\",\"value\":\"{2}\"'}','{'\"name\":\"{3}\",\"value\":{4}'}',"
            + "'{'\"name\":\"{5}\",\"value\":{6}'}']},\"messages\":[{7}],"
            + "\"internalExceptions\":'{'\"exceptionsLogged\":{8}'}',"
            + "\"businessExceptions\":'{'\"exceptionsLogged\":{9}'}'}'";
    private static final String LOG_MESSAGE1 = "Log message 1";
    private static final String LOG_MESSAGE2 = "Log message 2";

    private static final Parameter PARAM1 = new Parameter("param1", "a");
    private static final Parameter PARAM2 = new Parameter("param2", 1);
    private static final Parameter PARAM3 = new Parameter("param3", 2);
    private static final Request DEFAULT_REQUEST = new Request(TOKEN, PARAM1, PARAM2, PARAM3);

    /**
     * Object of the class under test.
     */
    private BufferedLogger bufferedLogger;

    @Mock
    private Logger mainLogger;

    @Mock
    private Logger internalExceptionLogger;

    @Mock
    private Logger businessExceptionLogger;

    @Mock
    private ExceptionHandler internalExceptionHandler;

    @Mock
    private ExceptionHandler businessExceptionHandler;

    @Mock
    private ExceptionMapper exceptionMapper;

    @Before
    public void initialize() {
        bufferedLogger = new BufferedLogger(DEFAULT_REQUEST, mainLogger);

        when(mainLogger.isInfoEnabled()).thenReturn(true);
        when(internalExceptionHandler.appendException(any(Exception.class))).thenReturn(internalExceptionHandler);
        doNothing().when(internalExceptionHandler).log();
        when(internalExceptionHandler.cleanupExceptions()).thenReturn(internalExceptionHandler);
        doNothing().when(internalExceptionHandler).acknowledgeLogger(bufferedLogger);
    }

    @Test
    public void loggerShouldLetMessageBeAppendedWithoutLogging() {
        bufferedLogger.append("Log message");

        verifyZeroInteractions(mainLogger);
    }

    @Test
    public void loggerShouldLogDefaultMessage() {
        bufferedLogger.log();

        verify(mainLogger).info(createDefaultLog());
    }

    @Test
    public void loggerShouldNotLogIfInfoIsNotEnabled() {
        when(mainLogger.isInfoEnabled()).thenReturn(false);

        bufferedLogger.log();

        verify(mainLogger, times(1)).isInfoEnabled();
        verifyNoMoreInteractions(mainLogger);
    }

    @Test
    public void loggerShouldLogMoreThanOneMessage() {
        bufferedLogger.append(LOG_MESSAGE1).append(LOG_MESSAGE2).log();

        verify(mainLogger).info(createCustomLogWithDefaultRequest(LOG_MESSAGE1, LOG_MESSAGE2));
    }

    @Test
    public void loggerShouldAcceptAnInternalExceptionsLogger() throws Exception {
        Exception thrownException = new Exception();

        whenNew(ExceptionHandler.class).withArguments(internalExceptionLogger).thenReturn(internalExceptionHandler);
        whenNew(ExceptionHandler.class).withArguments(mainLogger).thenReturn(businessExceptionHandler);

        bufferedLogger.createInternalExceptionHandler(internalExceptionLogger).append(LOG_MESSAGE1).append(LOG_MESSAGE2)
                .appendInternalException(thrownException).log();

        verify(mainLogger).info(createCustomLogWithDefaultRequest(true, LOG_MESSAGE1, LOG_MESSAGE2));
        verify(internalExceptionHandler).appendException(thrownException);
        verify(internalExceptionHandler).log();
    }

    @Test
    public void loggerShouldAcceptAnInternalExceptionsLoggerWithMapper() throws Exception {
        Exception thrownException = new Exception();

        whenNew(ExceptionHandler.class).withArguments(internalExceptionLogger, exceptionMapper).thenReturn(
                internalExceptionHandler);
        whenNew(ExceptionHandler.class).withArguments(mainLogger).thenReturn(businessExceptionHandler);

        bufferedLogger.createInternalExceptionHandler(internalExceptionLogger, exceptionMapper).append(LOG_MESSAGE1)
                .append(LOG_MESSAGE2).appendInternalException(thrownException).log();

        verify(mainLogger).info(createCustomLogWithDefaultRequest(true, LOG_MESSAGE1, LOG_MESSAGE2));
        verify(internalExceptionHandler).appendException(thrownException);
        verify(internalExceptionHandler).log();
    }

    @Test
    public void loggerShouldAcceptAnInternalExceptionsLoggerInTheConstructor() throws Exception {
        Exception thrownException = new Exception();

        whenNew(ExceptionHandler.class).withArguments(internalExceptionLogger).thenReturn(internalExceptionHandler);
        whenNew(ExceptionHandler.class).withArguments(mainLogger).thenReturn(businessExceptionHandler);

        bufferedLogger = new BufferedLogger(DEFAULT_REQUEST, mainLogger, internalExceptionLogger);

        bufferedLogger.append(LOG_MESSAGE1).append(LOG_MESSAGE2).appendInternalException(thrownException).log();

        verify(mainLogger).info(createCustomLogWithDefaultRequest(true, LOG_MESSAGE1, LOG_MESSAGE2));
        verify(internalExceptionHandler).appendException(thrownException);
        verify(internalExceptionHandler).log();
    }

    @Test
    public void loggerShouldUseMainLoggerToLogInternalExceptionsIfNoExceptionLoggerWasSpecified() throws Exception {
        Exception thrownException = new Exception();

        whenNew(ExceptionHandler.class).withArguments(mainLogger).thenReturn(internalExceptionHandler);

        bufferedLogger.append(LOG_MESSAGE1).append(LOG_MESSAGE2).appendInternalException(thrownException).log();

        verify(mainLogger).info(createCustomLogWithDefaultRequest(true, LOG_MESSAGE1, LOG_MESSAGE2));
        verify(internalExceptionHandler).appendException(thrownException);
        verify(internalExceptionHandler).log();
    }

    @Test
    public void loggerShouldAllowCustomInternalExceptionsMappingAfterConstructor() throws Exception {
        Exception thrownException = new Exception();
        ExceptionMapper retrievedMapper;

        whenNew(ExceptionHandler.class).withArguments(internalExceptionLogger, exceptionMapper).thenReturn(
                internalExceptionHandler);
        whenNew(ExceptionHandler.class).withArguments(mainLogger).thenReturn(businessExceptionHandler);
        when(internalExceptionHandler.getExceptionMapper()).thenReturn(exceptionMapper);
        when(exceptionMapper.map(Exception.class, internalExceptionLogger)).thenReturn(exceptionMapper);

        bufferedLogger.createInternalExceptionHandler(internalExceptionLogger, exceptionMapper);
        retrievedMapper = bufferedLogger.getInternalExceptionsMapper();
        retrievedMapper.map(Exception.class, internalExceptionLogger);

        bufferedLogger.append(LOG_MESSAGE1).append(LOG_MESSAGE2).appendInternalException(thrownException).log();

        verify(exceptionMapper).map(Exception.class, internalExceptionLogger);
        verify(mainLogger).info(createCustomLogWithDefaultRequest(true, LOG_MESSAGE1, LOG_MESSAGE2));
        verify(internalExceptionHandler).appendException(thrownException);
        verify(internalExceptionHandler).log();
    }

    @Test
    public void loggerShouldReturnNothingWhenGettingAnUnspecifiedInternalExceptionMapper() {
        assertNull(bufferedLogger.getInternalExceptionsMapper());
    }

    @Test
    public void loggerShouldAcceptABusinessExceptionsLoggerInTheConstructor() throws Exception {
        Exception thrownException = new Exception();

        whenNew(ExceptionHandler.class).withArguments(businessExceptionLogger).thenReturn(businessExceptionHandler);
        whenNew(ExceptionHandler.class).withArguments(internalExceptionLogger).thenReturn(internalExceptionHandler);

        bufferedLogger = new BufferedLogger(DEFAULT_REQUEST, mainLogger, internalExceptionLogger, businessExceptionLogger);

        bufferedLogger.append(LOG_MESSAGE1).append(LOG_MESSAGE2).appendBusinessException(thrownException).log();

        verify(mainLogger).info(createCustomLogWithDefaultRequest(false, true, LOG_MESSAGE1, LOG_MESSAGE2));
        verify(businessExceptionHandler).appendException(thrownException);
        verify(businessExceptionHandler).log();
    }

    @Test
    public void loggerShouldAllowSeparatedBusinessExceptionsLoggingAsAcceptsInternalExceptions() throws Exception {
        Exception thrownException = new Exception();

        whenNew(ExceptionHandler.class).withArguments(businessExceptionLogger).thenReturn(businessExceptionHandler);
        whenNew(ExceptionHandler.class).withArguments(mainLogger).thenReturn(internalExceptionHandler);

        bufferedLogger.createBusinessExceptionHandler(businessExceptionLogger).append(LOG_MESSAGE1).append(LOG_MESSAGE2)
                .appendBusinessException(thrownException).log();

        verify(mainLogger).info(createCustomLogWithDefaultRequest(false, true, LOG_MESSAGE1, LOG_MESSAGE2));
        verify(businessExceptionHandler).appendException(thrownException);
        verify(businessExceptionHandler).log();
    }

    @Test
    public void loggerShouldAllowCustomBusinessExceptionsMappingAfterConstructor() throws Exception {
        Exception thrownException = new Exception();
        ExceptionMapper retrievedMapper;

        whenNew(ExceptionHandler.class).withArguments(businessExceptionLogger, exceptionMapper).thenReturn(
                businessExceptionHandler);
        whenNew(ExceptionHandler.class).withArguments(mainLogger).thenReturn(internalExceptionHandler);
        when(businessExceptionHandler.getExceptionMapper()).thenReturn(exceptionMapper);
        when(exceptionMapper.map(Exception.class, businessExceptionLogger)).thenReturn(exceptionMapper);

        bufferedLogger.createBusinessExceptionHandler(businessExceptionLogger, exceptionMapper);
        retrievedMapper = bufferedLogger.getBusinessExceptionsMapper();
        retrievedMapper.map(Exception.class, businessExceptionLogger);

        bufferedLogger.append(LOG_MESSAGE1).append(LOG_MESSAGE2).appendBusinessException(thrownException).log();

        verify(exceptionMapper).map(Exception.class, businessExceptionLogger);
        verify(mainLogger).info(createCustomLogWithDefaultRequest(false, true, LOG_MESSAGE1, LOG_MESSAGE2));
        verify(businessExceptionHandler).appendException(thrownException);
        verify(businessExceptionHandler).log();
    }

    @Test
    public void loggerShouldReturnNothingWhenGettingAnUnspecifiedBusinessExceptionMapper() {
        assertNull(bufferedLogger.getBusinessExceptionsMapper());
    }

    @Test
    // TODO novos construtores e creates()
    public void loggerShouldAllowCustomLogLevelToInternalExceptions() throws Exception {
        Exception thrownException = new Exception();
        Level logLevel = Level.ERROR;

        whenNew(ExceptionHandler.class).withArguments(internalExceptionLogger, logLevel).thenReturn(internalExceptionHandler);
        whenNew(ExceptionHandler.class).withArguments(mainLogger).thenReturn(businessExceptionHandler);

        bufferedLogger.createInternalExceptionHandler(internalExceptionLogger).append(LOG_MESSAGE1).append(LOG_MESSAGE2)
                .appendInternalException(thrownException).log();

        verify(mainLogger).info(createCustomLogWithDefaultRequest(true, LOG_MESSAGE1, LOG_MESSAGE2));
        verify(internalExceptionHandler).appendException(thrownException);
        verify(internalExceptionHandler).log();
    }

    /**
     * Creates the default log with the default request.
     * 
     * @return the default log with the default request
     */
    private String createDefaultLog() {
        return createCustomLog(DEFAULT_REQUEST);
    }

    /**
     * Creates a custom log with the default request.
     * 
     * @param logMessages
     *            the custom log messages
     * @return the custom log with the custom messages
     */
    private String createCustomLogWithDefaultRequest(String... logMessages) {
        return createCustomLogWithDefaultRequest(false, logMessages);
    }

    /**
     * Creates a custom log with the default request.
     * 
     * @param internalExceptionsLogged
     *            flag that indicates if whether there were internal exceptions
     *            to be logged
     * @param logMessages
     *            the custom log messages
     * @return the custom log with the custom messages
     */
    private String createCustomLogWithDefaultRequest(boolean exceptionsLogged, String... logMessages) {
        return createCustomLogWithDefaultRequest(exceptionsLogged, false, logMessages);
    }

    /**
     * Creates a custom log with the default request.
     * 
     * @param internalExceptionsLogged
     *            flag that indicates if whether there were internal exceptions
     *            to be logged
     * @param businessExceptionsLogged
     *            flag that indicates if whether there were business exceptions
     *            to be logged
     * @param logMessages
     *            the custom log messages
     * @return the custom log with the custom messages
     */
    private String createCustomLogWithDefaultRequest(boolean exceptionsLogged, boolean businessExceptionsLogged,
            String... logMessages) {
        return createCustomLog(DEFAULT_REQUEST, exceptionsLogged, businessExceptionsLogged, logMessages);
    }

    /**
     * Creates a custom log with a custom request and custom messages.
     * 
     * @param request
     *            the custom request
     * @param logMessages
     *            the custom messages
     * @return the custom log
     */
    private String createCustomLog(Request request, String... logMessages) {
        return createCustomLog(request, false, false, logMessages);
    }

    /**
     * Creates a custom log with a custom request and custom messages.
     * 
     * @param request
     *            the custom request
     * @param internalExceptionsLogged
     *            flag that indicates if whether there were internal exceptions
     *            to be logged
     * @param businessExceptionsLogged
     *            flag that indicates if whether there were business exceptions
     *            to be logged
     * @param logMessages
     *            the custom messages
     * @return the custom log
     */
    private String createCustomLog(Request request, boolean internalExceptionsLogged, boolean businessExceptionsLogged,
            String... logMessages) {
        Parameter parameter1 = null;
        Parameter parameter2 = null;
        Parameter parameter3 = null;

        int i = 0;

        outer: for (Parameter parameter : request.getParameters()) {

            switch (i) {

            case 0: {
                parameter1 = parameter;
                break;
            }
            case 1: {
                parameter2 = parameter;
                break;
            }
            case 2: {
                parameter3 = parameter;
                break outer;
            }

            }

            i++;
        }

        String logMessage = "";

        if (logMessages.length > 0) {
            logMessage = buildLogMessages(logMessages);
        }

        return MessageFormat.format(LOG_TEMPLATE, request.getToken(), parameter1.getName(), parameter1.getValue(),
                parameter2.getName(), parameter2.getValue(), parameter3.getName(), parameter3.getValue(), logMessage,
                internalExceptionsLogged, businessExceptionsLogged);
    }

    /**
     * Build an String representing the log messages.
     * 
     * @param logMessages
     *            the log messages
     * @return the String representing the log messages
     */
    private String buildLogMessages(String... logMessages) {
        StringBuilder additionalLog = new StringBuilder();

        for (String message : logMessages) {
            additionalLog.append("\"").append(message).append("\",");
        }

        additionalLog.delete(additionalLog.length() - 1, additionalLog.length());

        return additionalLog.toString();
    }

}

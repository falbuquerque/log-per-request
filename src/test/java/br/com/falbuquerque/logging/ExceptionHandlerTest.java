package br.com.falbuquerque.logging;

import static br.com.falbuquerque.logging.CommonConstants.TOKEN;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.Logger;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import br.com.falbuquerque.logging.request.Request;

/**
 * Tests the behavior of the {@link ExceptionHandler} class.
 * 
 * @author Felipe Albuquerque
 */
@RunWith(MockitoJUnitRunner.class)
public class ExceptionHandlerTest {

    private final Level defaultExceptionLogLevel = Level.ERROR;

    @Mock
    private Logger defaultExceptionLogger;

    @Mock
    private Logger runtimeExceptionLogger;

    @Mock
    private BufferedLogger bufferedLogger;

    @Mock
    private Request request;

    @Before
    public void initialize() {
        when(bufferedLogger.getRequest()).thenReturn(request);
        when(request.getToken()).thenReturn(TOKEN);
        when(defaultExceptionLogger.isEnabled(any(Level.class))).thenReturn(true);
        when(runtimeExceptionLogger.isEnabled(any(Level.class))).thenReturn(true);
    }

    @Test
    public void exceptionHandlerShouldBeAbleToMapAnExceptionToASingleLogger() {
        Exception thrownException = new Exception();

        new ExceptionHandler(defaultExceptionLogger).appendException(thrownException).log();

        verify(defaultExceptionLogger).log(defaultExceptionLogLevel, buildExceptionLogMessage(), thrownException);
    }

    @Test
    public void exceptionHandlerShouldBeAbleToMapMoreThanOneExceptionToASingleLogger() {
        Exception thrownException1 = new Exception();
        Exception thrownException2 = new Exception();
        Exception thrownException3 = new Exception();

        new ExceptionHandler(defaultExceptionLogger).appendException(thrownException1).appendException(thrownException2)
                .appendException(thrownException3).log();

        String log = buildExceptionLogMessage();

        verify(defaultExceptionLogger).log(defaultExceptionLogLevel, log, thrownException1);
        verify(defaultExceptionLogger).log(defaultExceptionLogLevel, log, thrownException2);
        verify(defaultExceptionLogger).log(defaultExceptionLogLevel, log, thrownException3);
    }

    @Test
    public void exceptionHandlerShouldUseDefaultLogWhenExceptionClassHasNoMapping() {
        Exception thrownException1 = new Exception();
        RuntimeException thrownException2 = new RuntimeException();
        Exception thrownException3 = new Exception();
        NullPointerException thrownException4 = new NullPointerException();

        new ExceptionHandler(defaultExceptionLogger, new ExceptionMapper().map(Exception.class, defaultExceptionLogger).map(
                RuntimeException.class, runtimeExceptionLogger)).appendException(thrownException1)
                .appendException(thrownException2).appendException(thrownException3).appendException(thrownException4).log();

        String log = buildExceptionLogMessage();

        verify(defaultExceptionLogger).log(defaultExceptionLogLevel, log, thrownException1);
        verify(runtimeExceptionLogger).log(defaultExceptionLogLevel, log, thrownException2);
        verify(defaultExceptionLogger).log(defaultExceptionLogLevel, log, thrownException3);
        verify(defaultExceptionLogger).log(defaultExceptionLogLevel, log, thrownException4);
    }

    @Test
    public void nothingShouldBeLoggedWhenExceptionsAreCleanedUp() {
        Exception thrownException1 = new Exception();

        new ExceptionHandler(defaultExceptionLogger).appendException(thrownException1).cleanupExceptions().log();

        verifyNoMoreInteractions(defaultExceptionLogger);
    }

    @Test
    public void exceptionHandlerShouldAllowDefinitionOfDefaultLoggerLevel() {
        Exception thrownException1 = new Exception();
        Level logLevel = Level.INFO;

        new ExceptionHandler(defaultExceptionLogger, logLevel).appendException(thrownException1).log();

        String log = buildExceptionLogMessage();

        verify(defaultExceptionLogger).log(logLevel, log, thrownException1);
    }

    @Test
    public void exceptionHandlerShouldAllowLoggerLevelOtherThanTheDefaultToExceptionMappings() {
        Exception thrownException1 = new Exception();
        RuntimeException thrownException2 = new RuntimeException();
        Level runtimeLogLevel = Level.WARN;

        new ExceptionHandler(defaultExceptionLogger, new ExceptionMapper().map(Exception.class, defaultExceptionLogger).map(
                RuntimeException.class, runtimeExceptionLogger, runtimeLogLevel)).appendException(thrownException1)
                .appendException(thrownException2).log();

        String log = buildExceptionLogMessage();

        verify(defaultExceptionLogger).log(defaultExceptionLogLevel, log, thrownException1);
        verify(runtimeExceptionLogger).log(runtimeLogLevel, log, thrownException2);
    }

    /**
     * Builds the exception log message.
     * 
     * @return the exception log message
     */
    private String buildExceptionLogMessage() {
        return "Exception in request [undefined]";
    }

}

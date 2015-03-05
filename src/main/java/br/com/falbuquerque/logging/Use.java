package br.com.falbuquerque.logging;
import java.sql.SQLException;
import java.text.ParseException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import br.com.falbuquerque.logging.request.Parameter;
import br.com.falbuquerque.logging.request.Request;

public class Use {

    static Logger mainLogger = LogManager.getLogger("main");
    static Logger errorLogger = LogManager.getLogger("error");
    static Logger runtimeLogger = LogManager.getLogger("runtime");
    static Logger businessLogger = LogManager.getLogger("business");

    public static void main(String[] args) {
        new BufferedLogger(new Request("WWED033A", new Parameter("param", "val")), mainLogger)
                .createInternalExceptionHandler(
                        errorLogger,
                        new ExceptionMapper().map(ParseException.class, errorLogger).map(IllegalArgumentException.class,
                                runtimeLogger))
                .createBusinessExceptionHandler(businessLogger, new ExceptionMapper().map(MyException.class, businessLogger))
                .append("Log message 1").append("Log message 2").append("Log message 3")
                .appendInternalException(new ParseException("Bla bla", 1)) // error
                .appendInternalException(new IllegalArgumentException("Param is invalid")) // runtime
                .appendInternalException(new SQLException("SQL Exception")) // error
                .appendBusinessException(new MyException()) // business
                .log();
    }

    static class MyException extends Exception {

        private static final long serialVersionUID = 1906009163468611513L;
    }

}

package LOG;


import control.Controller;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.logging.*;

/**
 * Logger
 * @author Karam
 */
public class Logging {
//    private static final Logger loggerClient = Logger.getLogger(Controller.class.getClass().getName() + "2");
    private static final Logger loggerServer = Logger.getLogger(Controller.class.getClass().getName() + "1");
    private static FileHandler fileHandlerClient;
    private static FileHandler fileHandlerServer;
    private static byte clientLoggerCounter = 1;

    public static Logger getLoggerClient(String name) {
        clientLoggerCounter++;
        Logger tmpLogger = Logger.getLogger(Controller.class.getClass().getName() + "_" + name + "" + clientLoggerCounter);
        initFileHandlerClient(tmpLogger, name);
        return tmpLogger;

    }

    public static Logger getLoggerServer() {
        return loggerServer;
    }

    /**
     * client log txt in a specific form
     */
    public static void initFileHandlerClient(Logger loggerClient, String name) {
        try {
            fileHandlerClient = new FileHandler("Log" + "_" + name + "_" + (clientLoggerCounter -1) + ".txt");
        } catch (IOException e) {
            e.printStackTrace();
        }

        fileHandlerClient.setFormatter(new Formatter() {

            @Override
            public String format(LogRecord record) {
                SimpleDateFormat logTime = new SimpleDateFormat(" MM-dd-yyyy HH:mm:ss");
                Calendar cal = new GregorianCalendar();
                cal.setTimeInMillis(record.getMillis());
                return record.getLevel()
                        + logTime.format(cal.getTime())
                        + " || "
                        + record.getSourceClassName().substring(
                        record.getSourceClassName().lastIndexOf(".") + 1,
                        record.getSourceClassName().length())
                        + "."
                        + record.getSourceMethodName()
                        + "() : "
                        + record.getMessage() + "\n";
            }
        });

        loggerClient.addHandler(fileHandlerClient);

        appendSystemInfo(loggerClient);
    }

    /**
     * server log txt in a specific form
     */
    public static void initFileHandlerServer() {
        try {
            fileHandlerServer = new FileHandler("LogServer.txt");
        } catch (IOException e) {
            e.printStackTrace();
        }

        fileHandlerServer.setFormatter(new Formatter() {
            @Override
            public String format(LogRecord record) {
                SimpleDateFormat logTime = new SimpleDateFormat("MM-dd-yyyy HH:mm:ss");
                Calendar cal = new GregorianCalendar();
                cal.setTimeInMillis(record.getMillis());
                return record.getLevel()
                        + logTime.format(cal.getTime())
                        + " || "
                        + record.getSourceClassName().substring(
                        record.getSourceClassName().lastIndexOf(".") + 1,
                        record.getSourceClassName().length())
                        + "."
                        + record.getSourceMethodName()
                        + "() : "
                        + record.getMessage() + "\n";
            }
        });

        loggerServer.addHandler(fileHandlerServer);

        appendSystemInfo(loggerServer);
    }

    /**
     * ! stops all loggers except one !
     * This method deletes all default logger handlers (not needed).
     */
    public static void suppressConsoleOutput() {
        LogManager.getLogManager().reset();
    }

    /**
     * this methode shows the current operation system and java version
     * @param logger
     */
    public static void appendSystemInfo(Logger logger) {
        String arch = System.getProperty("os.arch");
        String name = System.getProperty("os.name");
        String vers = System.getProperty("os.version");
        String jaVe = System.getProperty("java.version");
        logger.info("OS   information: " + name + " (" + arch + " version: " + vers + ")");
        logger.info("Java information: " + jaVe);
    }
}

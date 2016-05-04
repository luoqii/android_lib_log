package org.bbs.android.log;


import android.app.Application;
import android.os.Environment;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.UnknownHostException;
import java.util.Date;
import java.util.logging.FileHandler;
import java.util.logging.Formatter;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

/**
 * just a wrapper of android.util.Log with identical name for easy code replacing.
 *
 * if set a {@link Logger} by {@link #setLogger(Logger)}, all log will publish to
 * it with a {@link Record}.
 *
 * @see #setLogger(Logger)
 * @see SimpleFormatter
 */
public final class Log {

    /**
     * Priority constant for the println method; use Log.v.
     */
    public static final int VERBOSE = android.util.Log.VERBOSE;

    /**
     * Priority constant for the println method; use Log.d.
     */
    public static final int DEBUG = android.util.Log.DEBUG;

    /**
     * Priority constant for the println method; use Log.i.
     */
    public static final int INFO = android.util.Log.INFO;

    /**
     * Priority constant for the println method; use Log.w.
     */
    public static final int WARN = android.util.Log.WARN;

    /**
     * Priority constant for the println method; use Log.e.
     */
    public static final int ERROR = android.util.Log.ERROR;

    /**
     * Priority constant for the println method.
     */
    public static final int ASSERT = android.util.Log.ASSERT;

//    /**
//     * Exception class used to capture a stack trace in {@link #wtf}.
//     */
//    private static class TerribleFailure extends Exception {
//        TerribleFailure(String msg, Throwable cause) { super(msg, cause); }
//    }
//
//    /**
//     * Interface to handle terrible failures from {@link #wtf}.
//     *
//     * @hide
//     */
//    public interface TerribleFailureHandler {
//        void onTerribleFailure(String tag, TerribleFailure what, boolean system);
//    }
//
//    private static TerribleFailureHandler sWtfHandler = new TerribleFailureHandler() {
//        public void onTerribleFailure(String tag, TerribleFailure what, boolean system) {
//            RuntimeInit.wtf(tag, what, system);
//        }
//    };

    private Log() {
    }

    /**
     * Send a {@link #VERBOSE} log message.
     * @param tag Used to identify the source of a log message.  It usually identifies
     *        the class or activity where the log call occurs.
     * @param msg The message you would like logged.
     */
    public static int v(String tag, String msg) {
        return println_native(LOG_ID_MAIN, VERBOSE, tag, msg);
    }

    /**
     * Send a {@link #VERBOSE} log message and log the exception.
     * @param tag Used to identify the source of a log message.  It usually identifies
     *        the class or activity where the log call occurs.
     * @param msg The message you would like logged.
     * @param tr An exception to log
     */
    public static int v(String tag, String msg, Throwable tr) {
        return println_native(LOG_ID_MAIN, VERBOSE, tag, msg + '\n' + getStackTraceString(tr));
    }

    /**
     * Send a {@link #DEBUG} log message.
     * @param tag Used to identify the source of a log message.  It usually identifies
     *        the class or activity where the log call occurs.
     * @param msg The message you would like logged.
     */
    public static int d(String tag, String msg) {
        return println_native(LOG_ID_MAIN, DEBUG, tag, msg);
    }

    /**
     * Send a {@link #DEBUG} log message and log the exception.
     * @param tag Used to identify the source of a log message.  It usually identifies
     *        the class or activity where the log call occurs.
     * @param msg The message you would like logged.
     * @param tr An exception to log
     */
    public static int d(String tag, String msg, Throwable tr) {
        return println_native(LOG_ID_MAIN, DEBUG, tag, msg + '\n' + getStackTraceString(tr));
    }

    /**
     * Send an {@link #INFO} log message.
     * @param tag Used to identify the source of a log message.  It usually identifies
     *        the class or activity where the log call occurs.
     * @param msg The message you would like logged.
     */
    public static int i(String tag, String msg) {
        return println_native(LOG_ID_MAIN, INFO, tag, msg);
    }

    /**
     * Send a {@link #INFO} log message and log the exception.
     * @param tag Used to identify the source of a log message.  It usually identifies
     *        the class or activity where the log call occurs.
     * @param msg The message you would like logged.
     * @param tr An exception to log
     */
    public static int i(String tag, String msg, Throwable tr) {
        return println_native(LOG_ID_MAIN, INFO, tag, msg + '\n' + getStackTraceString(tr));
    }

    /**
     * Send a {@link #WARN} log message.
     * @param tag Used to identify the source of a log message.  It usually identifies
     *        the class or activity where the log call occurs.
     * @param msg The message you would like logged.
     */
    public static int w(String tag, String msg) {
        return println_native(LOG_ID_MAIN, WARN, tag, msg);
    }

    /**
     * Send a {@link #WARN} log message and log the exception.
     * @param tag Used to identify the source of a log message.  It usually identifies
     *        the class or activity where the log call occurs.
     * @param msg The message you would like logged.
     * @param tr An exception to log
     */
    public static int w(String tag, String msg, Throwable tr) {
        return println_native(LOG_ID_MAIN, WARN, tag, msg + '\n' + getStackTraceString(tr));
    }

//    /**
//     * Checks to see whether or not a log for the specified tag is loggable at the specified level.
//     *
//     *  The default level of any tag is set to INFO. This means that any level above and including
//     *  INFO will be logged. Before you make any calls to a logging method you should check to see
//     *  if your tag should be logged. You can change the default level by setting a system property:
//     *      'setprop log.tag.&lt;YOUR_LOG_TAG> &lt;LEVEL>'
//     *  Where level is either VERBOSE, DEBUG, INFO, WARN, ERROR, ASSERT, or SUPPRESS. SUPPRESS will
//     *  turn off all logging for your tag. You can also create a local.prop file that with the
//     *  following in it:
//     *      'log.tag.&lt;YOUR_LOG_TAG>=&lt;LEVEL>'
//     *  and place that in /data/local.prop.
//     *
//     * @param tag The tag to check.
//     * @param level The level to check.
//     * @return Whether or not that this is allowed to be logged.
//     * @throws IllegalArgumentException is thrown if the tag.length() > 23.
//     */
//    public static native boolean isLoggable(String tag, int level);

    /*
     * Send a {@link #WARN} log message and log the exception.
     * @param tag Used to identify the source of a log message.  It usually identifies
     *        the class or activity where the log call occurs.
     * @param tr An exception to log
     */
    public static int w(String tag, Throwable tr) {
        return println_native(LOG_ID_MAIN, WARN, tag, getStackTraceString(tr));
    }

    /**
     * Send an {@link #ERROR} log message.
     * @param tag Used to identify the source of a log message.  It usually identifies
     *        the class or activity where the log call occurs.
     * @param msg The message you would like logged.
     */
    public static int e(String tag, String msg) {
        return println_native(LOG_ID_MAIN, ERROR, tag, msg);
    }

    /**
     * Send a {@link #ERROR} log message and log the exception.
     * @param tag Used to identify the source of a log message.  It usually identifies
     *        the class or activity where the log call occurs.
     * @param msg The message you would like logged.
     * @param tr An exception to log
     */
    public static int e(String tag, String msg, Throwable tr) {
        return println_native(LOG_ID_MAIN, ERROR, tag, msg + '\n' + getStackTraceString(tr));
    }

//    /**
//     * What a Terrible Failure: Report a condition that should never happen.
//     * The error will always be logged at level ASSERT with the call stack.
//     * Depending on system configuration, a report may be added to the
//     * {@link android.os.DropBoxManager} and/or the process may be terminated
//     * immediately with an error dialog.
//     * @param tag Used to identify the source of a log message.
//     * @param msg The message you would like logged.
//     */
//    public static int wtf(String tag, String msg) {
//        return wtf(LOG_ID_MAIN, tag, msg, null, false, false);
//    }
//
//    /**
//     * Like {@link #wtf(String, String)}, but also writes to the log the full
//     * call stack.
//     * @hide
//     */
//    public static int wtfStack(String tag, String msg) {
//        return wtf(LOG_ID_MAIN, tag, msg, null, true, false);
//    }
//
//    /**
//     * What a Terrible Failure: Report an exception that should never happen.
//     * Similar to {@link #wtf(String, String)}, with an exception to log.
//     * @param tag Used to identify the source of a log message.
//     * @param tr An exception to log.
//     */
//    public static int wtf(String tag, Throwable tr) {
//        return wtf(LOG_ID_MAIN, tag, tr.getMessage(), tr, false, false);
//    }
//
//    /**
//     * What a Terrible Failure: Report an exception that should never happen.
//     * Similar to {@link #wtf(String, Throwable)}, with a message as well.
//     * @param tag Used to identify the source of a log message.
//     * @param msg The message you would like logged.
//     * @param tr An exception to log.  May be null.
//     */
//    public static int wtf(String tag, String msg, Throwable tr) {
//        return wtf(LOG_ID_MAIN, tag, msg, tr, false, false);
//    }

//    static int wtf(int logId, String tag, String msg, Throwable tr, boolean localStack,
//                   boolean system) {
//        TerribleFailure what = new TerribleFailure(msg, tr);
//        // Only mark this as ERROR, do not use ASSERT since that should be
//        // reserved for cases where the system is guaranteed to abort.
//        // The onTerribleFailure call does not always cause a crash.
//        int bytes = println_native(logId, ERROR, tag, msg + '\n'
//                + getStackTraceString(localStack ? what : tr));
//        sWtfHandler.onTerribleFailure(tag, what, system);
//        return bytes;
//    }
//
//    static void wtfQuiet(int logId, String tag, String msg, boolean system) {
//        TerribleFailure what = new TerribleFailure(msg, null);
//        sWtfHandler.onTerribleFailure(tag, what, system);
//    }
//
//    /**
//     * Sets the terrible failure handler, for testing.
//     *
//     * @return the old handler
//     *
//     * @hide
//     */
//    public static TerribleFailureHandler setWtfHandler(TerribleFailureHandler handler) {
//        if (handler == null) {
//            throw new NullPointerException("handler == null");
//        }
//        TerribleFailureHandler oldHandler = sWtfHandler;
//        sWtfHandler = handler;
//        return oldHandler;
//    }

    /**
     * Handy function to get a loggable stack trace from a Throwable
     * @param tr An exception to log
     */
    public static String getStackTraceString(Throwable tr) {
//        if (tr == null) {
//            return "";
//        }
//
//        // This is to reduce the amount of log spew that apps do in the non-error
//        // condition of the network being unavailable.
//        Throwable t = tr;
//        while (t != null) {
//            if (t instanceof UnknownHostException) {
//                return "";
//            }
//            t = t.getCause();
//        }
//
//        StringWriter sw = new StringWriter();
//        PrintWriter pw = new FastPrintWriter(sw, false, 256);
//        tr.printStackTrace(pw);
//        pw.flush();
//        return sw.toString();
        return android.util.Log.getStackTraceString(tr);
    }

    /**
     * Low-level logging call.
     * @param priority The priority/type of this log message
     * @param tag Used to identify the source of a log message.  It usually identifies
     *        the class or activity where the log call occurs.
     * @param msg The message you would like logged.
     * @return The number of bytes written.
     */
    public static int println(int priority, String tag, String msg) {
        return println_native(LOG_ID_MAIN, priority, tag, msg);
    }

    /** @hide */ public static final int LOG_ID_MAIN = 0;
    /** @hide */ public static final int LOG_ID_RADIO = 1;
    /** @hide */ public static final int LOG_ID_EVENTS = 2;
    /** @hide */ public static final int LOG_ID_SYSTEM = 3;
    /** @hide */ public static final int LOG_ID_CRASH = 4;

    /** @hide */ public static int println_native(int bufID,
                                                         int priority, String tag, String msg){
        android.util.Log.println(priority, tag, msg);
        log2Logger(priority, tag, msg);
        return 0;
    }

    private static void log2Logger(int priority, String tag, String msg) {
        if (null != sLogger){
            sLogger.log(new Record(toLevel(priority), tag, msg));
        }
    }

    private static Level toLevel(int priority) {
        Level level = Level.ALL;
        switch (priority){
            case VERBOSE:
                level = Level.FINEST;
                break;
            case INFO:
                level = Level.INFO;
                break;
            case WARN:
                level = Level.WARNING;
                break;
            case DEBUG:
                level = Level.FINE;
                break;
            case ERROR:
                level = Level.SEVERE;
                break;
            case ASSERT:
                level = Level.FINE;//???
                break;
        }

        // FIXME remove duplicated log item in logcat output
        level = Level.ALL;

        return level;
    }

    private static Logger sLogger;
    public static void setLogger(Logger logger){
        sLogger = logger;
    }

    public static class Record extends LogRecord{

        private final String mTag;

        public Record(Level level, String tag, String msg) {
            super(level, msg);
            mTag = tag;
        }
    }

    public static class SimpleFormatter extends Formatter {

        @Override
        public String format(LogRecord r) {
            Record record = (Record) r;
            final Date d = new Date(r.getMillis());
            return "[" + (d.getYear() + 1900) + "/" + d.getMonth() + "/" + d.getDate()
                    + " " + d.getHours()
                    + ":" + d.getMinutes() + ":" + d.getSeconds() + "]"
                    + record.mTag + ":"
                    + r.getMessage()
                    + "\n";
        }
    }

}

package TeamControlium.Utilities;

import org.apache.commons.io.FilenameUtils;
import java.io.File;
import java.nio.charset.Charset;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.Files;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

/// Enables test scripts to log data that assists in debugging of test scripts and/or framework.  Library and
/// helper classes write to the debug log using log levels and to ensure detailed analysis
/// is possible.<br/>
/// Debug text redirection is possible if underlying tool supplies its own logging and/or debug output, wired up
/// to and is set to false.
/// The timestamp, shown on every line of the log output, is reset on the first call to the TeamControlium.Utilities.Logger.
/// </summary>
public final class Logger {
    private boolean errorWrittenToEventLog;
    private Date testTimer;         // Used to keep track of time since first call to TeamControlium.Utilities.Logger class made.
    private HashMap<Long, String> testToolStrings;       // Used to build string-per-thread as logger Write calls made
    static private Logger _Logger;


    private Logger() {
        errorWrittenToEventLog = false;
        testToolStrings = new HashMap<>();
        _LoggingLevel = LogLevels.TestInformation; // Default logging level
        ResetTimer();
    }

    /// <summary>
    /// Logging level. Lowest is Error (least amount of log data written - only writes at
    /// level Error are written to the log). Most data is written to
    /// the log if level set is FrameworkDebug
    /// </summary>
    private LogLevels _LoggingLevel;
    static public LogLevels getLoggingLevel() { if (_Logger==null) { _Logger = new Logger(); } return _Logger._LoggingLevel;}
    static public void setLoggingLevel(LogLevels loggingLevel) { if (_Logger==null) { _Logger = new Logger(); } _Logger._LoggingLevel = loggingLevel;}

    /// <summary>
    /// Defines where log lines are written to.
    /// If true (or has not been defined), debug data is written to the Console (stdout)
    /// If false, debug data logging is written to the delegate (if wired up)
    /// </summary>
    /// <remarks>
    /// The default is for log data to be written to the console
    /// </remarks>
    private boolean _WriteToConsole;
    static public boolean getWriteToConsole() { if (_Logger==null) { _Logger = new Logger(); } return _Logger._WriteToConsole;}
    static public void setWriteToConsole(boolean writeToConsole) { if (_Logger==null) { _Logger = new Logger(); } _Logger._WriteToConsole = writeToConsole;}

    /// <summary>
    /// System delegate to write debug data to if WriteToConsole is false.
    /// </summary>
    public Consumer<String> _TestToolLog;
    static public Consumer<String> getTestToolLog() { if (_Logger==null) { _Logger = new Logger(); } return _Logger._TestToolLog;}
    static public void setTestToolLog(Consumer<String> testToolLog) { if (_Logger==null) { _Logger = new Logger(); } _Logger._TestToolLog = testToolLog;}

    /// <summary>
    /// Levels of logging - Verbose (Maximum) to Exception (Minimum).  If level of text being written to
    /// logging is equal to, or higher than the current LoggingLevel the text is written.<br/>
    /// This is used to filter logging so that only entries to log are made if the level of the write is equal
    /// or greater than the logging level set by <see cref="LoggingLevel">LoggingLevel</see>.
    /// </summary>
    public enum LogLevels {
        /// <summary>
        /// Data written to log if LoggingLevel is FrameworkDebug and Write is FrameworkDebug or higher
        /// </summary>
        FrameworkDebug (0),
        /// <summary>
        /// Data written to log if LoggingLevel is FrameworkInformation and Write is FrameworkInformation or higher
        /// </summary>
        FrameworkInformation (1),
        /// <summary>
        /// Data written to log if LoggingLevel is TestDebug and Write is TestDebug or higher
        /// </summary>
        TestDebug (2),
        /// <summary>
        /// Data written to log if LoggingLevel is TestInformation and Write is TestInformation or Error
        /// </summary>
        TestInformation (3),
        /// <summary>
        /// Data always written to results
        /// </summary>
        Error (4);

        private int numVal;
        public int getVal() { return this.numVal;}

        LogLevels(int numVal) {
            this.numVal = numVal;
        }

        public int getNumVal() {
            return numVal;
        }
    };


    /// <summary>
    /// Appends text to currently active line and writes line to active log.  If new line, text is pre-pended with Line header information
    /// </summary>
    /// MethodBase of class calling TeamControlium.Utilities.Logger class
    /// Level of debug text to be written
    /// Text string to be written
    /// <remarks>Text is written if TypeOfWrite is equal to, or higher the current Logging Level</remarks>
    private void DoWriteLine(StackTraceElement methodBase, LogLevels TypeOfWrite, String textString)
    {
        if (TypeOfWrite.getVal() >= getLoggingLevel().getVal())
        {
            String textToWrite = textString;
            //
            // Ensure only one thread can do the actual writing to the log.  Prevents line corruption and allows us to direct writes to correct thread
            //
            //synchronized (this)
            {
                long threadID = Thread.currentThread().getId();
                if (testToolStrings.containsKey(threadID))
                {
                    try
                    {
                        testToolStrings.put(threadID,testToolStrings.get(threadID).endsWith(" ") ? "" : " " + textToWrite);
                        textToWrite = testToolStrings.get(threadID);
                    }
                    finally
                    {
                        testToolStrings.remove(threadID);
                    }
                }
                else
                {
                    String preAmble = GetPreAmble(methodBase, TypeOfWrite);
                    textToWrite = preAmble + ((textString==null)?"":textString);
                }

                try
                {
                    Consumer<String> d = getTestToolLog();
                    if (getWriteToConsole() || getTestToolLog() == null)
                        // If we are writing to the console or test tool has not wired up their logger...
                        System.out.println(textToWrite);
                    else
                        _TestToolLog.accept(textToWrite);
                }
                catch (Exception ex)
                {
                    //
                    // Hmmm, dunno how yet..
                    //
//                    String details;
//                    if (!errorWrittenToEventLog)
//                    {
//                        using (EventLog appLog = new EventLog("Application"))
//                        {
//                            if (WriteToConsole)
//                            {
//                                details = "console (STDOUT)";
//                            }
//                            else
//                            {
//                                details = string.Format("delegate provide by tool{0}.", (TestToolLog == null) ?
//                                        " (Is null! - Has not been implemented!)" :
//                                        "");
//                            }
//                            appLog.Source = "Application";
//                            appLog.WriteEntry(string.Format("AppServiceInterfaceMock - TeamControlium.Utilities.Logger error writing to {0}.\r\n\r\n" +
//                                    "Attempt to write line;\r\n" +
//                                    "{1}\r\n\r\n" +
//                                    "No further log writes to event log will happen in this session", details, textToWrite, ex), EventLogEntryType.Warning, 12791, 1);
//                        }
//                        errorWrittenToEventLog = true;
//                    }
                }
            }
        }
    }

    /// <summary>
    /// Appends text to currently active line.  If the start of line, text is pre-pended with Line header information
    /// </summary>
    /// <remarks>Text is written if TypeOfWrite is equal to, or higher the current Logging Level</remarks>
    private void DoWrite(StackTraceElement methodBase, LogLevels TypeOfWrite, String textString)
    {
        // Only do write if level of this write is equal to or greater than the current logging level
        if (TypeOfWrite.getVal() >= getLoggingLevel().getVal())
        {            // Ensure thread safety by locking code around the write
            synchronized(this)
            {
                //
                // Get the id of the current thread and append text to end of the dictionary item for that
                // thread (create new item if doesnt already exist).  If this is
                // first time this thread is doing a write, prepend the PreAmble text first.
                //
                long threadID = Thread.currentThread().getId();
                if (testToolStrings.containsKey(threadID)) testToolStrings.put(threadID,GetPreAmble(methodBase, TypeOfWrite));
                testToolStrings.put(threadID,testToolStrings.get(threadID).endsWith(" ") ? "" : " " + textString);
            }
        }
    }

    /// <summary>
    /// Gets class-type and Method name of passed MethodBase class.
    /// </summary>
    private String CallingMethodDetails(StackTraceElement methodBase)
    {
        String methodName="";
        String typeName="";
        if (methodBase != null)
        {
            methodName = methodBase.getMethodName();
            if (methodName==null) methodName = "<Unknown>";
            typeName = methodBase.getClassName();
            if (typeName==null) typeName = "<Unknown>";
        }
        return String.format("%s.%s", typeName, methodName);
    }

    /// <summary>
    /// Constructs and returns a log-file pre-amble.  Preamble is {Log Type} {Time} [Calling Type.Method]:
    /// </summary>
    private String GetPreAmble(StackTraceElement methodBase, LogLevels TypeOfWrite)
    {
        SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm:ss.SS");

        String time = String.format("[%s}][%s]", timeFormat.format(Calendar.getInstance().getTime()), elapsedTime());
        int totalTimeLength = time.length() + (8 - TypeOfWrite.getNumVal());
        String preAmble = String.format("%s %s [%s]: ", TypeOfWrite.toString(),
                time,
                CallingMethodDetails(methodBase));
        return preAmble;
    }

    private boolean FileExists(String fullPathAndFilename) {
        File f = new File(fullPathAndFilename);
        return (f.exists() && !f.isDirectory());
    }

    public String elapsedTime() {
        long diffInMilliSeconds = Calendar.getInstance().getTime().getTime() - testTimer.getTime();
        List<TimeUnit> units = Arrays.asList(TimeUnit.HOURS,TimeUnit.MINUTES,TimeUnit.SECONDS,TimeUnit.MILLISECONDS); //new Arrays.asList()//Arrays.asList(TimeUnit.HOURS,TimeUnit.MINUTES);
      //  Collections.reverse(units);
        String result ="";
        long milliSecondsRest = diffInMilliSeconds;
        for (TimeUnit unit : units) {
            long diff = unit.convert(milliSecondsRest,TimeUnit.MILLISECONDS);
            milliSecondsRest = milliSecondsRest - unit.toMillis(diff);
            switch (unit) {
                case HOURS: result += String.format("%02d:",diff); break;
                case MINUTES: result += String.format("%02d:",diff); break;
                case SECONDS: result += String.format("%02d:",diff); break;
                case MILLISECONDS: result += String.format("%03d",diff); break;
            }
        }
        return result;
    }
    /// <summary>
    /// Resets the logger elapsed timer to zero
    /// </summary>
    public void ResetTimer()
    {
        testTimer = Calendar.getInstance().getTime();
    }

    /// <summary>
    /// Writes details of a caught exception to the active debug log at level Error
    /// </summary>
    /// <remarks>
    /// If current error logging level is FrameworkDebug the full
    /// exception is written, including stacktrace etc.<br/>
    /// With any other Log Level only the exception message is written. If an exception is thrown during write, TeamControlium.Utilities.Logger
    /// attempts to write the error details if able.
    /// </remarks>
    static public void LogException(Exception ex)
    {
        StackTraceElement[] stackTraceElements = Thread.currentThread().getStackTrace();
        StackTraceElement caller = stackTraceElements[2];
        if (_Logger==null) { _Logger = new Logger(); }

        if (_Logger.getLoggingLevel() == LogLevels.FrameworkDebug) {
            _Logger.DoWriteLine(caller,LogLevels.Error,String.format("Exception thrown: %s",ex.toString()));
        }
        else {
            _Logger.DoWriteLine(caller, LogLevels.Error, String.format("Exception thrown: %s", ex.getMessage()));
        }
    }

    /// <summary>
    /// Writes details of a caught exception to the active debug log at levelError
    /// </summary>
    /// <remarks>
    /// If current error logging level is FrameworkDebug the full
    /// exception is written, including stacktrace etc.<br/>
    /// With any other Log Level only the exception message is writteIf an exception is thrown during write, TeamControlium.Utilities.Logger
    /// attempts to write the error details if able.
    /// </remarks>
    static public void LogException(Exception ex, String text, Object... args)
    {
        StackTraceElement[] stackTraceElements = Thread.currentThread().getStackTrace();
        StackTraceElement caller = stackTraceElements[2];
        if (_Logger==null) { _Logger = new Logger(); }
        _Logger.DoWrite(caller, LogLevels.Error, String.format(text, args));
        if (_Logger.getLoggingLevel() == LogLevels.FrameworkDebug)
        {
            _Logger.DoWriteLine(caller, LogLevels.Error,
                    String.format("Exception thrown: %s", ex.toString()));
        }
        else
        {
            _Logger.DoWriteLine(caller, LogLevels.Error,
                    String.format("Exception thrown: %s", ex.getMessage()));
        }
    }

    /// <summary>
    /// Writes a line of data to the active debug log with no line termination
    /// </summary>
    static public void Write(LogLevels logLevel, String textString, Object... args)
    {
        StackTraceElement[] stackTraceElements = Thread.currentThread().getStackTrace();
        StackTraceElement caller = stackTraceElements[2];
        if (_Logger==null) { _Logger = new Logger(); }
        _Logger.DoWrite(caller, logLevel, String.format(textString, args));
    }

    /// <summary>
    /// Writes a line of data to the active debug log.
    /// Data can be formatted in the standard string.format syntax.  If an exception is thrown during write, TeamControlium.Utilities.Logger
    /// attempts to write the error deatils if able.
    /// </summary>
    static public void WriteLine(LogLevels logLevel, String textString, Object... args)
    {
        StackTraceElement[] stackTraceElements = Thread.currentThread().getStackTrace();
        StackTraceElement caller = stackTraceElements[2];
        if (_Logger==null) { _Logger = new Logger(); }
        _Logger.DoWriteLine(caller, logLevel,
                String.format(textString, args));
    }

    /// <summary>
    /// Writes given Text to a text file, optionally auto versioning (adding (n) to filename) OR
    /// overwriting.
    /// </summary>
    /// <remarks>
    /// No exception is raised if there is any problem, but details of error is written to TeamControlium.Utilities.Logger log
    /// </remarks>
    static public void WriteTextToFile(String Filename, boolean AutoVersion, String Text)
    {
        if (_Logger==null) { _Logger = new Logger(); }
        try
        {
            String FilenameToUse = Filename;
            if (AutoVersion)
            {
                int count = 1;
                String fileNameOnly = FilenameUtils.removeExtension(Filename);
                String extension = FilenameUtils.getExtension(Filename);
                String path = Paths.get(Filename).getParent().toString();
                FilenameToUse = Filename;

                while (_Logger.FileExists(FilenameToUse)) {
                    String tempFileName = String.format("%s(%d)", fileNameOnly, count++);
                    File preAmble = new File(path);
                    File combined = new File(preAmble,tempFileName + extension);
                    FilenameToUse = combined.getPath();
                }
            }
            List<String> lines = Arrays.asList(Text.split("\\r?\\n"));
            Path file = Paths.get(FilenameToUse);
            Files.write(file,lines, Charset.forName("UTF-8"));

        }
        catch (Exception ex)
        {
            LogException(ex, String.format("Cannot write data to file [%s] (AutoVersion=[%s])",(Filename==null)? "Null Filename!":Filename, (AutoVersion) ? "Yes" : "No"));
        }
    }



}


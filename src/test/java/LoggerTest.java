import java.io.ByteArrayOutputStream;
import java.io.FileDescriptor;
import java.io.FileOutputStream;
import java.io.PrintStream;

import static org.junit.jupiter.api.Assertions.*;

class LoggerTest {

    private String testOutput="";

    @org.junit.jupiter.api.BeforeEach
    void setUp() {
    }

    @org.junit.jupiter.api.AfterEach
    void tearDown() {
    }

    @org.junit.jupiter.api.Test
    void VerifyLoggerOutputText() {
        String testText = "Testing 123";
        System.out.println("Verify WriteLine method correctly writes message text to TestToolLog when wired up");
        Logger.setTestToolLog(this::OutputReceiver);                         // Wire-up consumer (Delegate in C# speak)
        Logger.setLoggingLevel(Logger.LogLevels.TestInformation);                   // Set the logging level
        Logger.WriteLine(Logger.LogLevels.TestInformation,testText); //  and write a line to the log...

        assertTrue(testOutput.endsWith(testText),String.format("Verify Logger output line <%s> ends with test-text <%s>",testOutput,testText));
    }

    @org.junit.jupiter.api.Test
    void VerifyCaller() {
        StackTraceElement[] stackTraceElements = Thread.currentThread().getStackTrace();
        StackTraceElement caller = stackTraceElements[1];
        String testText = String.format("[%s.%s]", caller.getClassName(), caller.getMethodName());
        System.out.println("Verify WriteLine correctly indicates the name/class of the caller");
        Logger.setTestToolLog(this::OutputReceiver);                           // Wire-up consumer (Delegate in C# speak)
        Logger.setLoggingLevel(Logger.LogLevels.TestInformation);                     // Set the logging level
        Logger.WriteLine(Logger.LogLevels.TestInformation,"Arbitary text"); //  and write a line to the log...

        assertTrue(testOutput.contains(testText),String.format("Verify Logger output line <%s> contains caller details <%s>",testOutput,testText));
    }

    @org.junit.jupiter.api.Test
    void VerifyLoggerOutputStdout() {
        String testText = "Testing stdout";
        System.out.println("Verify WriteLine method correctly writes message text to STDOUT when TestToolLog not wired up");
        Logger.setTestToolLog(null);                                                // Ensure TestToolLog not wired uo
        ByteArrayOutputStream baos = new ByteArrayOutputStream();                   // Intercept stdout messages
        System.setOut(new PrintStream(baos));
        Logger.setLoggingLevel(Logger.LogLevels.TestInformation);                   // Set the logging level
        Logger.WriteLine(Logger.LogLevels.TestInformation,testText);                //  and write a line to the log...
        String stdoutReceivedMessages = baos.toString();
        System.setOut(new PrintStream(new FileOutputStream(FileDescriptor.out)));
        assertTrue(stdoutReceivedMessages.endsWith(testText+"\r\n"),String.format("Verify Logger output line to stdout <%s> ends with test-text <%s>",stdoutReceivedMessages,testText));

    }

    ///
    /// When wired up to Logger TestToolLog delegate receives logger output.
    ///
    private void OutputReceiver(String line) {
        testOutput = line;
    }

}
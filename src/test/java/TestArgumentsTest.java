import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;

import static org.junit.jupiter.api.Assertions.*;

class TestArgumentsTest {

    @BeforeEach
    void setUp() {
    }

    @AfterEach
    void tearDown() {
    }

    @org.junit.jupiter.api.Test
    void VerifySingleFlagArgumentDash() {
        String[] args = {"-flag"};
        TestArguments testArgs = new TestArguments(args);

        String argument = testArgs.getParameter("flag");
        assertEquals("true",(argument==null)?"null":argument,"Verify string true returned if we ask if a command-line flag using - has been set");
    }

    @org.junit.jupiter.api.Test
    void VerifySingleFlagArgumentDoubleDash() {
        String[] args = {"--flag"};
        TestArguments testArgs = new TestArguments(args);

        String argument = testArgs.getParameter("flag");
        assertEquals("true",(argument==null)?"null":argument,"Verify string true returned if we ask if a command-line flag using -- has been set");
    }

    @org.junit.jupiter.api.Test
    void VerifySingleFlagArgumentSlash() {
        String[] args = {"/flag"};
        TestArguments testArgs = new TestArguments(args);

        String argument = testArgs.getParameter("flag");
        assertEquals("true",(argument==null)?"null":argument,"Verify string true returned if we ask if a command-line flag using / has been set");
    }

    @org.junit.jupiter.api.Test
    void VerifyNoArgument() {
        String[] args = {"-flag"};
        TestArguments testArgs = new TestArguments(args);

        String argument = testArgs.getParameter("notflag");
        assertEquals(null,argument,"Verify null returned if we ask if a command-line flag has been set and it has not");
    }

    @org.junit.jupiter.api.Test
    void VerifySingleDoubleQuotedArgument() {
        String[] args = {"-quoted=\"hello i am quoted\""};
        TestArguments testArgs = new TestArguments(args);

        String argument = testArgs.getParameter("quoted");
        assertEquals("hello i am quoted",(argument==null)?"null":argument,"Verify double-quoted single argument");
    }

    @org.junit.jupiter.api.Test
    void VerifySingleSingleQuotedArgument() {
        String[] args = {"-quoted='hello i am quoted'"};
        TestArguments testArgs = new TestArguments(args);

        String argument = testArgs.getParameter("quoted");
        assertEquals("hello i am quoted",(argument==null)?"null":argument,"Verify single-quoted single argument");
    }

    @org.junit.jupiter.api.Test
    void VerifySingleSingleQuotedArgumentWithInternalDash() {
        String[] args = {"-quoted='hello -i am quoted'"};
        TestArguments testArgs = new TestArguments(args);

        String argument = testArgs.getParameter("quoted");
        assertEquals("hello -i am quoted",(argument==null)?"null":argument,"Verify single-quoted single argument");
    }

    @org.junit.jupiter.api.Test
    void VerifySingleSingleQuotedArgumentWithInternalSlash() {
        String[] args = {"-quoted='hello /i am quoted'"};
        TestArguments testArgs = new TestArguments(args);

        String argument = testArgs.getParameter("quoted");
        assertEquals("hello /i am quoted",(argument==null)?"null":argument,"Verify single-quoted single argument");
    }

    @org.junit.jupiter.api.Test
    void VerifySingleSingleQuotedArgumentColon() {
        String[] args = {"-quoted:'hello i am quoted'"};
        TestArguments testArgs = new TestArguments(args);

        String argument = testArgs.getParameter("quoted");
        assertEquals("hello i am quoted",(argument==null)?"null":argument,"Verify single-quoted single argument with colon delimiter");
    }

    @org.junit.jupiter.api.Test
    void VerifySingleUnquotedArgument() {
        String[] args = {"-unquoted:hello i am quoted"};
        TestArguments testArgs = new TestArguments(args);

        String argument = testArgs.getParameter("unquoted");
        assertEquals("hello i am quoted",(argument==null)?"null":argument,"Verify single-quoted single argument with colon delimiter");
    }

    @org.junit.jupiter.api.Test
    void VerifyNumericArgument() {
        String[] args = {"-numeric=7"};
        TestArguments testArgs = new TestArguments(args);

        String argument = testArgs.getParameter("numeric");
        assertEquals("7",(argument==null)?"null":argument,"Verify numeric argument");
    }

    @org.junit.jupiter.api.Test
    void VerifyNumericArgumentColon() {
        String[] args = {"-numeric:7"};
        TestArguments testArgs = new TestArguments(args);

        String argument = testArgs.getParameter("numeric");
        assertEquals("7",(argument==null)?"null":argument,"Verify numeric argument with colon delimiter");
    }

    @org.junit.jupiter.api.Test
    void VerifyNoArguments() {
        String[] args = {""};
        TestArguments testArgs = new TestArguments(args);

        String argument = testArgs.getParameter("noargs");
        assertEquals("null",(argument==null)?"null":argument,"Verify no arguments");
    }

    @org.junit.jupiter.api.Test
    void VerifyMultipleMixedArguments() {
        String[] args = {"-no_value","/more_no_value","-num:7","/num2=8","-text:some","nomarker","-moretext=\"more text\"","-finalarg"};
        TestArguments testArgs = new TestArguments(args);

        String noValueArgument = testArgs.getParameter("no_value");
        assertEquals("true",(noValueArgument==null)?"null":noValueArgument,"Verify first flag");

        String moreNoValueArgument = testArgs.getParameter("more_no_value");
        assertEquals("true",(moreNoValueArgument==null)?"null":moreNoValueArgument,"Verify second flag");

        String numArgument = testArgs.getParameter("num");
        assertEquals("7",(numArgument==null)?"null":numArgument,"Verify first numeric");

        String num2Argument = testArgs.getParameter("num2");
        assertEquals("8",(num2Argument==null)?"null":num2Argument,"Verify second numeric");

        String textArgument = testArgs.getParameter("text");
        assertEquals("some",(textArgument==null)?"null":textArgument,"Verify first text");

        String testArgument = testArgs.getParameter("nomarker");
        assertEquals("null",(testArgument==null)?"null":testArgument,"Verify non-marked parameter ignored");

        String moreText = testArgs.getParameter("moretext");
        assertEquals("more text",(moreText==null)?"null":moreText,"Verify final quoted text");


    }



}
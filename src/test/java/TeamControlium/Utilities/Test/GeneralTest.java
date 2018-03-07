package TeamControlium.Utilities.Test;

import TeamControlium.Utilities.General;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;

import static org.junit.jupiter.api.Assertions.*;

class GeneralTest {

    @BeforeEach
    void setUp() {
    }

    @AfterEach
    void tearDown() {
    }

    @org.junit.jupiter.api.Test
    void DetokeniseStringObject() {
        String stringObject = "I have a {random;from(TTTT);1}oken";
        Object objectString = (Object) stringObject;

        Object resultObject = null;
        String result = "<Not Set>";

        try {
            resultObject = General.DetokeniseString(objectString);
            result = (String) resultObject;
        } catch (Exception ex) {
            assertFalse(true, "Exception must not be thrown!");
        }
        assertEquals("I have a Token", result, "The token should be resolved");
    }

    @org.junit.jupiter.api.Test
    void DetokeniseNonStringObject() {
        int nonStringObject = 7;
        Object objectString = (Object) nonStringObject;

        Object resultObject = null;
        int result = -1;

        try {
            resultObject = General.DetokeniseString(objectString);
            result = (int) resultObject;
        } catch (Exception ex) {
            assertFalse(true, "Exception must not be thrown!");
        }
        assertEquals(7, result, "The returned object should be the same unchanged");
    }

    @org.junit.jupiter.api.Test
    void IsValueTrueAllTests() {

        assertTrue(General.IsValueTrue("yes"), "yes must return true");
        assertTrue(General.IsValueTrue("Yes"), "Yes must return true");
        assertTrue(General.IsValueTrue("true"), "true must return true");
        assertTrue(General.IsValueTrue("True"), "True must return true");
        assertTrue(General.IsValueTrue("1"), "1 must return true");
        assertTrue(General.IsValueTrue("on"), "on must return true");
        assertTrue(General.IsValueTrue("On"), "On must return true");
        assertTrue(General.IsValueTrue("ON"), "ON must return true");

        assertFalse(General.IsValueTrue("no"), "no must return false");
        assertFalse(General.IsValueTrue("No"), "No must return false");
        assertFalse(General.IsValueTrue("false"), "false must return false");
        assertFalse(General.IsValueTrue("False"), "False must return false");
        assertFalse(General.IsValueTrue("0"), "o must return false");
        assertFalse(General.IsValueTrue("off"), "off must return false");
        assertFalse(General.IsValueTrue("Off"), "Off must return false");
        assertFalse(General.IsValueTrue("OFF"), "OFF must return false");

    }

    @org.junit.jupiter.api.Test
    void VerifyXPathWithDoubleQuotes() {

        String quotedXPath = "//*[@id=\"ext-gen1035\"]/div/div[3]/i";
        String response = General.CleanStringForXPath(quotedXPath);
        assertEquals("'"+quotedXPath+"'",response,"XPath must be enclosed in single quotes");
    }

    @org.junit.jupiter.api.Test
    void VerifyXPathWithSingleQuotes() {

        String quotedXPath = "//*[@id='ext-gen1035']/div/div[3]/i";
        String response = General.CleanStringForXPath(quotedXPath);
        assertEquals("\""+quotedXPath+"\"",response,"XPath must be enclosed in double quotes");
    }


    @org.junit.jupiter.api.Test
    void VerifyXPathWithSingleQuotesAndDouble() {

        String quotedXPath = "//*[@id='ext\"-\"gen1035']/div/div[3]/i";
        String response = General.CleanStringForXPath(quotedXPath);
        assertEquals("concat('"+quotedXPath+"')",response,"XPath must be wrapped in a concat");
    }

    @org.junit.jupiter.api.Test
    void VerifyXPathWithSingleAndDouble() {

        String quotedXPath = "//*[@id=\"ext'-'gen1035\"]/div/div[3]/i";
        String response = General.CleanStringForXPath(quotedXPath);
        assertEquals("concat('"+quotedXPath+"')",response,"XPath must be wrapped in a concat");
    }

    @org.junit.jupiter.api.Test
    void CleanStringForFilenameWithGoodString() {

        String goodPath = "C:\\First\\My.txt";
        String response = General.CleanStringForFilename(goodPath);
        assertEquals(goodPath,response,"File path is not changed");
    }

    @org.junit.jupiter.api.Test
    void CleanStringForFilenameWithBadString() {

        String goodPath = "C:\\First\\Date-26/7/2089\\Bads!@#$%^&*()\\name.txt";
        String response = General.CleanStringForFilename(goodPath);
        assertEquals("C:\\First\\Date-26_7_2089\\Bads_\\name.txt",response,"File path is clean");
    }


    @org.junit.jupiter.api.Test
    void GetTextFromHTML() {

        String html = "Hello";
        String response = "<Not set>";
        try {
            response = General.GetTextFromHTML(html);
        }
        catch (Exception ex) {
            assertFalse(true,"Exception must not be thrown");
        }
        assertEquals("Hello",response,"Text in no HTML is returned correctly");
    }

    @org.junit.jupiter.api.Test
    void GetTextFromHTMLSimpleDiv() {

        String html = "<div>Hello<div>";
        String response = "<Not set>";
        try {
            response = General.GetTextFromHTML(html);
        }
        catch (Exception ex) {
            assertFalse(true,"Exception must not be thrown");
        }
        assertEquals("Hello",response,"Text in a simple Div is returned correctly");
    }

    @org.junit.jupiter.api.Test
    void GetTextFromHTMLAnchor() {

        String html = "<a href=\"http:\\mysite\">Hello<div>";
        String response = "<Not set>";
        try {
            response = General.GetTextFromHTML(html);
        }
        catch (Exception ex) {
            assertFalse(true,"Exception must not be thrown");
        }
        assertEquals("Hello",response,"Text in a simple Anchor is returned correctly");
    }

    @org.junit.jupiter.api.Test
    void GetTextFromHTMLAnchorAndEmbeddedControl() {

        String html = "<a class=\"js-show-link comments-link \" title=\"expand to show all comments on this post\" href=\"#\" onclick=\"\">show <b>1</b> more comment</a>";
        String response = "<Not set>";
        try {
            response = General.GetTextFromHTML(html);
        }
        catch (Exception ex) {
            assertFalse(true,"Exception must not be thrown");
        }
        assertEquals("show 1 more comment",response,"Text in an anchor and formatting is returned correctly");
    }

    @org.junit.jupiter.api.Test
    void GetTextFromHTMLComplex() {

        String html = "<div><p>Start <strong>bold</strong> more <code>isCode()</code>. More <strong>bold</strong> stuff <strong>bold</strong> with' apostrophe <a href=\"https://a/link.html#intern--\" rel=\"noreferrer\">mylink</a> then more <em>text</em> is <code>==</code> used.</p><p>And more <a href=\"https://complex.com/nest/html/doc.html#anchor\" rel=\"noreferrer\">S.t.u.ff. <em>And</em></a> final</p></div>";
        String response = "<Not set>";
        try {
            response = General.GetTextFromHTML(html);
        }
        catch (Exception ex) {
            assertFalse(true,"Exception must not be thrown");
        }
        assertEquals("Start bold more isCode(). More bold stuff bold with' apostrophe mylink then more text is == used. And more S.t.u.ff. And final",response,"Text in complex HTML returned correctly");
    }

    @org.junit.jupiter.api.Test
    void GetTextFromHTMLEmpty() {

        String html = "";
        String response = "<Not set>";
        try {
            response = General.GetTextFromHTML(html);
        }
        catch (Exception ex) {
            assertFalse(true,"Exception must not be thrown");
        }
        assertEquals("",response,"Text in empty HTML returned correctly");
    }

    @org.junit.jupiter.api.Test
    void GetTextFromHTMLNull() {

        String html = null;
        String response = "<Not set>";
        try {
            response = General.GetTextFromHTML(html);
        }
        catch (Exception ex) {
            assertFalse(true,"Exception must not be thrown");
        }
        assertNull(response,"Null text returned correctly");
    }
}
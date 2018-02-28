import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.regex.Pattern;

import static org.junit.jupiter.api.Assertions.*;

class DetokenizerTest {

    @BeforeEach
    void setUp() {
    }

    @AfterEach
    void tearDown() {
    }

    @org.junit.jupiter.api.Test
    void VerifyTodaysDateToken() {
        String detokenised="<Not Set>";
        try {
             detokenised = Detokenizer.ProcessTokensInString("{date;today;dd/MM/yyyy}");
        }
        catch (Exception ex) {
            assertTrue(false,"Exception calling [Detokenizer.ProcessTokensInString]: " + ex);
        }
        Date dt = new Date();
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");
        assertEquals(dateFormat.format(dt),detokenised,"Date returned by Detokeniser matches todays date and in correct format");
    }

    @org.junit.jupiter.api.Test
    void VerifyYesterdaysDateToken() {
        String detokenised="<Not Set>";
        try {
            detokenised = Detokenizer.ProcessTokensInString("{date;yesterday;dd/MM/yyyy}");
        }
        catch (Exception ex) {
            assertTrue(false,"Exception calling [Detokenizer.ProcessTokensInString]: " + ex);
        }

        final Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DATE,-1);
        Date dt =  cal.getTime();

        SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");
        assertEquals(dateFormat.format(dt),detokenised,"Date returned by Detokeniser matches yesterdays date and in correct format");
    }

    @org.junit.jupiter.api.Test
    void VerifyRandomDateToken() {
        String detokenised="<Not Set>";
        String startDate = "01-01-1975";
        String endDate = "31-12-1975";
        SimpleDateFormat returnedDateFormat = new SimpleDateFormat("dd/MM/yyyy");
        SimpleDateFormat inputDateFormat = new SimpleDateFormat("dd-MM-yyyy");

        try {
            detokenised = Detokenizer.ProcessTokensInString("{random;date(" + startDate + "," + endDate + ");dd/MM/yyyy}");
        }
        catch (Exception ex) {
            assertTrue(false,"Exception calling [Detokenizer.ProcessTokensInString]: " + ex);
        }

        Date returnedDate=null;
        try {
             returnedDate = returnedDateFormat.parse(detokenised);
        }
        catch (Exception ex) {
            assertTrue(false,"Exception parsing [Detokenizer.ProcessTokensInString] returned date [" + detokenised + "]: " + ex);
        }

        Date startDateDate=null;
        Date endDateDate=null;
        try {
            startDateDate = inputDateFormat.parse(startDate);
            endDateDate = inputDateFormat.parse(endDate);
        }
        catch (Exception ex) {
            assertTrue(false, "Test error parsing start or end dates!");
        }

        assertTrue((!returnedDate.before(startDateDate)) && !returnedDate.after(endDateDate),"Verify returned date from token {random;date("+startDate+","+endDate+");dd/MM/yyyy} is not outside the minimum and maximum dates");

    }

    @org.junit.jupiter.api.Test
    void VerifyRandomDigits() {
        String detokenised="<Not Set>";
        int requiredLength = 5;
        try {
            detokenised = Detokenizer.ProcessTokensInString("{random;digits;"+ Integer.toString(requiredLength)+"}");
        }
        catch (Exception ex) {
            assertTrue(false,"Exception calling [Detokenizer.ProcessTokensInString]: " + ex);
        }

        boolean canParseInteger=false;
        try {
            int dummy = Integer.parseInt(detokenised);
            canParseInteger=true;
        }
        catch (Exception ex) {}

        assertTrue(canParseInteger,"Verify only digits returned");
        assertEquals(requiredLength,detokenised.length(),"Verify length of returned value is as required");
    }

    @org.junit.jupiter.api.Test
    void VerifyRandomLetters() {
        String detokenised="<Not Set>";
        int requiredLength = 20;
        try {
            detokenised = Detokenizer.ProcessTokensInString("{random;letters;"+ Integer.toString(requiredLength)+"}");
        }
        catch (Exception ex) {
            assertTrue(false,"Exception calling [Detokenizer.ProcessTokensInString]: " + ex);
        }


        assertTrue(detokenised.matches("[a-zA-Z]+"),"Verify only letters returned");
        assertEquals(requiredLength,detokenised.length(),"Verify length of returned value is as required");
    }

    @org.junit.jupiter.api.Test
    void VerifyRandomUppercaseLetters() {
        String detokenised="<Not Set>";
        int requiredLength = 20;
        try {
            detokenised = Detokenizer.ProcessTokensInString("{random;UpperCaseLetters;"+ Integer.toString(requiredLength)+"}");
        }
        catch (Exception ex) {
            assertTrue(false,"Exception calling [Detokenizer.ProcessTokensInString]: " + ex);
        }


        assertTrue(detokenised.matches("[A-Z]+"),"Verify only uppercase letters returned");
        assertEquals(requiredLength,detokenised.length(),"Verify length of returned value is as required");
    }

    @org.junit.jupiter.api.Test
    void VerifyRandomLowercaseLetters() {
        String detokenised="<Not Set>";
        int requiredLength = 20;
        try {
            detokenised = Detokenizer.ProcessTokensInString("{random;LowerCaseLetters;"+ Integer.toString(requiredLength)+"}");
        }
        catch (Exception ex) {
            assertTrue(false,"Exception calling [Detokenizer.ProcessTokensInString]: " + ex);
        }


        assertTrue(detokenised.matches("[a-z]+"),"Verify only lowercase letters returned");
        assertEquals(requiredLength,detokenised.length(),"Verify length of returned value is as required");
    }

    @org.junit.jupiter.api.Test
    void VerifyRandomSpecificCharacters() {
        String detokenised="<Not Set>";
        String characterSet = "a1\\b2c3d4!@#$%^&*()";  // Make sure we include characters that may cause issues!
        int requiredLength = 100;
        try {
            detokenised = Detokenizer.ProcessTokensInString("{random;from("+characterSet+");"+ Integer.toString(requiredLength)+"}");
        }
        catch (Exception ex) {
            assertTrue(false,"Exception calling [Detokenizer.ProcessTokensInString]: " + ex);
        }


        assertTrue(detokenised.matches("["+ Pattern.quote(characterSet)+"]+"),"Verify only characters from character set returned");
        assertEquals(requiredLength,detokenised.length(),"Verify length of returned value is as required");
    }


    @org.junit.jupiter.api.Test
    void VerifyNestedTokens() {
        String detokenised="<Not Set>";
        try {
            detokenised = Detokenizer.ProcessTokensInString("{random;digits;{random;from(456);1} }");
        }
        catch (Exception ex) {
            assertTrue(false,"Exception calling [Detokenizer.ProcessTokensInString]: " + ex);
        }

        boolean canParseInteger=false;
        try {
            int dummy = Integer.parseInt(detokenised);
            canParseInteger=true;
        }
        catch (Exception ex) {}

        assertTrue(!(detokenised.length()<4) && !(detokenised.length()>6) && canParseInteger,"Verify digits of length between 4 and 6");
    }

}
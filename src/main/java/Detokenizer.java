import org.apache.commons.lang.StringUtils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.regex.Pattern;

public class Detokenizer {

    private static final char tokenStartChar = '{';
    private static final char tokenEndChar = '}';

    private static Random RandomGenerator = new Random();

    public static String ProcessTokensInString(String stringWithTokens) throws Exception {
        String detokenizedString = "";
        int startIndex = 0;
        boolean foundTokenStart = false;

        //
        // Find the start of a token, ignoring doubles {{'s as they are litterals)
        //
        while (!foundTokenStart && startIndex < stringWithTokens.length())
        {
            if (stringWithTokens.charAt(startIndex) == tokenStartChar)
            {
                // We are looking at a token start char...
                if ((startIndex < stringWithTokens.length() - 1) && (stringWithTokens.charAt(startIndex + 1) == tokenStartChar))
                {
                    // Next char is also a start char, so ignore and skip past
                    startIndex += 1;
                }
                else
                {
                    // Next char not a start char so we have found a token!
                    foundTokenStart = true;
                }
            }
            startIndex += 1;
        }

        //
        // startIndex is now pointing to first char of a token
        //

        if (foundTokenStart)
        {
            boolean foundTokenEnd = false;
            int endIndex = startIndex; // We start searching for the end of the token from the first character of the
            //
            // Find the end of the token.
            //
            while (!foundTokenEnd && endIndex < stringWithTokens.length())
            {
                if ((stringWithTokens.charAt(endIndex) == tokenStartChar) &&
                        !((startIndex < stringWithTokens.length() - 1) && (stringWithTokens.charAt(startIndex + 1) == tokenStartChar)))
                {
                    //
                    // Another start token (and it is NOT a dounble!!!!)  We have nested tokens by golly.
                    // So, start the process again, but from the new start of the nested token. Hah, this
                    // is a quick easy way of dealing with nested tokens!
                    //
                    startIndex = endIndex + 1;
                }
                else if (stringWithTokens.charAt(endIndex) == tokenEndChar)
                {
                    if ((endIndex < stringWithTokens.length() - 1) && (stringWithTokens.charAt(endIndex + 1) == tokenEndChar))
                    {
                        // Next char is also an end char, so ignore and skip past
                        endIndex += 1;
                    }
                    else
                    {
                        // Next char not a start char so we have found a token!
                        foundTokenEnd = true;
                    }
                }
                endIndex += 1;
            }
            if (foundTokenEnd)
            {
                detokenizedString += stringWithTokens.substring(0, startIndex - 1);
                String token = stringWithTokens.substring(startIndex, endIndex - 1);
                try
                {
                    detokenizedString += ProcessToken(token);
                }
                catch (Exception ex)
                {
                    throw new Exception(String.format("Error processing token {%s}: %s",token, ex));
                }
                detokenizedString += stringWithTokens.substring(endIndex, stringWithTokens.length());
            }
            else
            {
                throw new Exception(String.format("Found token start {{ found at index {%d} but no closing }} found: [%s]",startIndex,stringWithTokens));
            }
            // Now, we call ourself again to process any more tokens....
            detokenizedString = ProcessTokensInString(detokenizedString);
        }
        else
        {
            // So no token found. We will convert all doubles back to singles and return the string...
            detokenizedString += stringWithTokens.replace("{{", "{").replace("}}", "}");
        }
        return detokenizedString;
    }

    /// <summary>
    /// System delegate to do custom token processing
    /// </summary>
    private static BiFunction<String,String[],String> _CustomTokenProcessor = null;
    public static void setTestToolLog(BiFunction<String,String[],String> testToolLog) { _CustomTokenProcessor = testToolLog;}


    private static String ProcessToken(String token) throws Exception {
        String delimiter = ";";
        String processedToken = "";
        if (StringUtils.isEmpty(token)) throw new Exception("Empty token!");
        String[] splitToken = token.split(delimiter, 2);
        switch (splitToken[0].toLowerCase().trim())
        {
            case "random":
                if (splitToken.length < 2) throw new Exception("Random token [" + token + "] needs 3 parts {{random;<type>;<length>}}");
                processedToken = DoRandomToken(delimiter, splitToken[1]);
                break;
            case "date":
                if (splitToken.length < 2) throw new Exception("Date token [" + token + "] needs 3 parts {{date;<offset>;<format>}}");
                processedToken = DoDateToken(delimiter, splitToken[1]);
                break;
            case "financialyearstart":
                if (splitToken.length < 2) throw new Exception("FinancialYearStart token [" + token + "] needs 3 parts {{FinancialYearStart;<date>;<format>}}");
                processedToken = DoFinancialYearToken(delimiter, splitToken[1], true);
                break;
            case "financialyearend":
                if (splitToken.length < 2) throw new Exception("FinancialYearEnd token [" + token + "] needs 3 parts {{FinancialYearEnd;<date>;<format>}}");
                processedToken = DoFinancialYearToken(delimiter, splitToken[1], false);
                break;
 //           case "seleniumkeys":       // We dont want to have to reference Selenium from the Utilities classes
 //           case "seleniumkey":
 //               if (splitToken.length < 2) throw new Exception("SeleniumKey token [" + token + "] needs 2 parts {{SeleniumKey;<Name>}}");
 //               processedToken = DoSeleniumKey(splitToken[1]);
 //               break;
            default: {
                if (_CustomTokenProcessor == null) {
                    throw new Exception("Unsupported token [" + splitToken[0] + "] in [" + token + "]");
                }
                else {
                    try {
                        processedToken = _CustomTokenProcessor.apply(delimiter, splitToken);
                    }
                    catch (Exception ex) {
                        throw new Exception("Error thrown by custom token processor.",ex);
                    }
                    if (processedToken==null) {
                        //
                        // Custom token processor returns null if token not processed
                        //
                        throw new Exception("Unsupported token [" + splitToken[0] + "] in [" + token + "]");
                    }
                }
            }
        }
        return processedToken;
    }

    private static String DoRandomToken(String delimiter, String TypeAndLength) throws Exception {
        String[] typeAndLengthOrFormat = TypeAndLength.split(delimiter, 2);
        String result ="";
        String select = "";
        String verb = typeAndLengthOrFormat[0].toLowerCase().trim();

        if (verb.startsWith("date("))
        {
            result = new SimpleDateFormat(typeAndLengthOrFormat[1]).format(DoRandomDate(verb.substring(verb.indexOf('(') + 1, verb.length() - 1)));
        }
        else if (verb.startsWith("float("))
        {
            result = String.format(typeAndLengthOrFormat[1],DoRandomFloat(verb.substring(verb.indexOf('(') + 1, verb.length() - 1)));
        }
        else
        {
            // {random,from(ASDF),5} - 5 characters selected from ASDF
            if (verb.startsWith("from("))
            {
                select = typeAndLengthOrFormat[0].trim().substring(verb.indexOf('(') + 1, verb.length() - 2);
            }
            else
            {
                switch (verb)
                {
                    case "letters":
                        select = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ";
                        break;
                    case "lowercaseletters":
                        select = "abcdefghijklmnopqrstuvwxyz";
                        break;
                    case "uppercaseletters":
                        select = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
                        break;
                    case "digits":
                        select = "01234567890";
                        break;
                    case "alphanumerics":
                        select = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ01234567890";
                        break;
                    case "acn":
                    {
                        String acn = ProcessTokensInString("{random;digits;9}");
                        return acn;
                    }
                    case "abn":
                    {
                        String acn = ProcessTokensInString("{random;acn}");
                        result = ProcessTokensInString("{{ABNFromACN;{acn}}}");
                        return result;
                    }
                    default:
                        throw new Exception("Unrecognised random Type [" +typeAndLengthOrFormat[0] +"] - Expect letters, lowercaseletters, uppercaseletters digits or alphanumerics");
                }
            }
            int number;
            try {
                int lastDigit=0;
                while (lastDigit < typeAndLengthOrFormat[1].length() && Character.isDigit(typeAndLengthOrFormat[1].charAt(lastDigit))) lastDigit++;
                number = Integer.parseInt(typeAndLengthOrFormat[1].substring(0,lastDigit));
            }
            catch (Exception ex) {
                throw new Exception("Invalid number of characters in Random token {{random;<type>;<length>}}");
            }
            for (int index = 0; index < number; index++) {
                int selectIndex = ThreadLocalRandom.current().nextInt(0,select.length());
                result += select.charAt(selectIndex);
            }
       }
        return result;
    }

    static private float DoRandomFloat(String MaxAndMinFloats) throws Exception {
        String delimiter = ",";
        String[] MaxAndMin = MaxAndMinFloats.split(delimiter);
        if (MaxAndMin.length != 2)
            throw new Exception("Invalid Maximum and Minimum floats. Expect {{random.float(min;max),<format>}}. Max/min was: [" + MaxAndMinFloats + "]");
        float Min;
        float Max;

        try {
            Min = Float.parseFloat(MaxAndMin[0]);
        }
        catch (Exception ex) {
            throw new Exception("Invalid Minimum float. Expect {{random.float(min;max),<format>}}. Max/min was: [" + MaxAndMinFloats +"]");
        }
        try {
            Max = Float.parseFloat(MaxAndMin[1]);
        }
        catch (Exception ex) {
            throw new Exception("Invalid Maximum float. Expect {{random.float(min;max),<format>}}. Max/min was: [" + MaxAndMinFloats +"]");
        }
        return DoRandomFloat(Min, Max);
    }

    static public float DoRandomFloat(float MinFloat, float MaxFloat) throws Exception {
        if (MinFloat >= MaxFloat)
            throw new Exception("Maximum float less than Minimum float! Expect {{random.float(min,max),<format>}} Min = "+ Float.toString(MinFloat) + ", Max = "+ Float.toString(MaxFloat));
        return ThreadLocalRandom.current().nextFloat() * (MaxFloat - MinFloat) + MinFloat;
    }

    static private Date DoRandomDate(String MaxAndMinDates) throws Exception {
        String delimiter = ",";
        String[] MaxAndMin = MaxAndMinDates.split(delimiter);
        if (MaxAndMin.length != 2)
            throw new Exception("Invalid Maximum and Minimum dates. Expect {{random;date(dd-MM-yyyy,dd-MM-yyyy);<format>}}. Max/min was: [" + MaxAndMinDates + "]");
        Date Min;
        Date Max;
        SimpleDateFormat dateFormat = new SimpleDateFormat("d-M-yyyy");

        try {
            Min = dateFormat.parse(MaxAndMin[0]);
        }
        catch (Exception ex) {
            throw new Exception("Invalid Minimum date. Expect {{random;date(dd-MM-yyyy,dd-MM-yyyy);<format>}}. Max/min was: [" + MaxAndMinDates + "]");
        }
        try {
            Max = dateFormat.parse(MaxAndMin[1]);
        }
        catch (Exception ex) {
            throw new Exception("Invalid Maximum date. Expect {{random;date(dd-MM-yyyy,dd-MM-yyyy);<format>}}. Max/min was: [" + MaxAndMinDates + "]");
        }

        return DoRandomDate(Min, Max);
    }
    static public Date DoRandomDate(Date MinDate, Date MaxDate) throws Exception {
        if (MinDate.after(MaxDate)) {
            SimpleDateFormat dateFormat = new SimpleDateFormat("d-M-yyyy");
            throw new Exception("Maximum date earlier than Maximum date! Expect {{random;date(dd-MM-yyyy,dd-MM-yyyy);<format>}} Mindate = " + dateFormat.format(MinDate) + ", Maxdate = " + dateFormat.format(MaxDate));
        }
        return new Date(ThreadLocalRandom.current().nextLong(MinDate.getTime(),MaxDate.getTime()));
    }

    private static String DoDateToken(String delimiter, String OffsetAndFormat) throws Exception {
        String[] offsetAndFormat = OffsetAndFormat.split(delimiter, 2);

        if (offsetAndFormat.length != 2)
        {
            throw new Exception("Date token does not have a format parameter; example: {date" + delimiter + "today" + delimiter + "dd-MM-yyyy}");
        }
        Date dt;
        String verb = offsetAndFormat[0].toLowerCase().trim();
        if (verb.startsWith("random("))
        {
            dt = DoRandomDate(verb.substring(verb.indexOf('(') + 1, verb.length() - 2 - verb.indexOf('(')));
        }
        else
        {
            switch (verb)
            {
                case "today":
                    dt = new Date();

                    break;
                case "yesterday":
                    dt = getDateOffset(Calendar.DATE,-1);
                    break;
                case "tomorrow":
                    dt = getDateOffset(Calendar.DATE,+1);
                    break;
                default:
                {
                    String[] activeOffset = verb.substring(0, verb.length() - 1).split(Pattern.quote("("),2);
                    if (offsetAndFormat[0].contains("(") && offsetAndFormat[0].endsWith(")"))
                    {
                        int offset;
                        try {
                            offset = Integer.parseInt(activeOffset[1]);
                        }
                         catch (Exception ex) {
                             throw new Exception("Invalid Active Date offset.  Expect AddYears(n) AddMonths(n) or AddDays(n). Got [" + activeOffset[0].trim() + "]");
                         }


                        switch (activeOffset[0].trim())
                        {
                            case "addyears":
                                dt = getDateOffset(Calendar.YEAR,offset);
                                break;
                            case "addmonths":
                                dt = getDateOffset(Calendar.MONTH,offset);
                                break;
                            case "adddays":
                                dt = getDateOffset(Calendar.DATE,offset);
                                break;
                            default:
                                throw new Exception("Invalid Active Date offset.  Expect AddYears(n) AddMonths(n) or AddDays(n). Got [" + activeOffset[0].trim() + "]");
                        }
                    }
                    else
                    {
                        throw new Exception("Invalid Active Date offset.  Expect AddYears(n) AddMonths(n) or AddDays(n). Got [" + activeOffset[0].trim() + "]");
                    }
                    break;
                }
            }
        }
        SimpleDateFormat dateFormat = new SimpleDateFormat(offsetAndFormat[1]);
        return dateFormat.format(dt);
    }

    private static String DoFinancialYearToken(String delimiter, String DateToWorkFromAndFormat, boolean Start) throws Exception {
        String financialYearStart = "01/07";
        String financialYearEnd = "01/07";

        String[] dateToWorkFromAndFormat = DateToWorkFromAndFormat.split(delimiter, 2);

        SimpleDateFormat dateFormat = new SimpleDateFormat();

        Date dateToWorkFrom = tryParseDate(dateToWorkFromAndFormat[0], Arrays.asList("dd/MM/yyyy", "d/MM/yyyy", "dd/M/yyyy", "d/M/yyyy", "dd/MM/yy", "d/MM/yy", "dd/M/yy", "d/M/yy"));

        if (dateToWorkFrom==null)
        {
            throw new Exception("Cannot parse date [" + dateToWorkFromAndFormat[0] + "].  Must be in format d/M/y.");
        }

        String year;
        if (dateToCalendar(dateToWorkFrom).get(Calendar.MONTH)+1 >= 7)
            year = Integer.toString(Start ? dateToCalendar(dateToWorkFrom).get(Calendar.YEAR) : dateToCalendar(dateToWorkFrom).get(Calendar.YEAR));
        else
            year = Integer.toString(Start ? (dateToCalendar(dateToWorkFrom).get(Calendar.YEAR) - 1) : dateToCalendar(dateToWorkFrom).get(Calendar.YEAR));

        Date returnDate = tryParseDate((Start ? financialYearStart : financialYearEnd) + "/" + year,Arrays.asList("dd/MM/yyyy"));

       return new SimpleDateFormat(dateToWorkFromAndFormat[1]).format(returnDate);
    }

    private static Date getDateOffset(int offsetType,int offset) {
        final Calendar cal = Calendar.getInstance();
        cal.add(offsetType,offset);
        return cal.getTime();
    }

    private static Calendar dateToCalendar(Date date) {
        final Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        return cal;
    }


    private static Date tryParseDate(String dateString, List<String> validFormats) {
        for (String formatString : validFormats) {
            try {
                return new SimpleDateFormat(formatString).parse(dateString);
            }
            catch (ParseException ex) {
            }
        }
        return null;
    }


}

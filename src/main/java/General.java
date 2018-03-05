import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import javax.swing.text.html.HTMLDocument;
import javax.swing.text.html.HTMLEditorKit;
import javax.swing.text.html.parser.ParserDelegator;
import java.io.Reader;
import java.io.StringReader;
import java.util.Objects;
import java.util.regex.Pattern;

public class General {
    /// <summary>
    /// If object is a string and <see cref="Settings.TokenProcessor"/> has been set, tokens with the string are processed
    /// </summary>
    /// <remarks><see cref="Settings.TokenProcessor"/> is called recursively until string returned matches string sent.  Ensures all tokens and nested tokens removed.</remarks>
    /// <typeparam name="T">Returns same object type as submitted</typeparam>
    /// <param name="ObjectToProcess">Object to process</param>
    /// <returns>Processed object</returns>
    public static Object DetokeniseString(Object ObjectToProcess) throws Exception {
        // Only do any processing if the object is a string.
        if (ObjectToProcess instanceof String)
        {
            //
            // Call the TokenProcessor in a loop until the string returned is the same as the string passed in.  This indicates any processing has been
            // completed.  Doing this allows token values to themselves contain tokens)
            //
            String StringToProcess = "";
            String ProcessedString = (String)ObjectToProcess;
            Logger.Write(Logger.LogLevels.FrameworkDebug, "Object is a string [{0}]. ", (ProcessedString==null) ? "":ProcessedString);
            while (!Objects.equals(StringToProcess,ProcessedString))
            {
                StringToProcess = ProcessedString; // This is safe in Java due to String immutability

                ProcessedString = Detokenizer.ProcessTokensInString(StringToProcess);
                Logger.WriteLine(Logger.LogLevels.FrameworkDebug, "Processed [{0}] to [{1}]", StringToProcess, (ProcessedString==null) ? "":ProcessedString);
            }
            return (Object)ProcessedString;
        }
        else
        {
            Logger.WriteLine(Logger.LogLevels.FrameworkDebug, "Object [{0}] not a String. Not processed", ObjectToProcess.getClass().getTypeName());
            return ObjectToProcess;
        }
    }

    /// <summary>
    /// Returns true if string does not start with 0 or starts with t(rue), y(es) or o(n)
    /// </summary>
    /// <param name="Value">value to check</param>
    /// <returns>true if string first digit is not 0 or is true, yes or on</returns>
    public static boolean IsValueTrue(String Value)
    {
        if (Value==null || Value == "") return false;
        int i;
        try {
            i = Integer.parseInt(Value);
            return i>0;
        }
        catch (Exception ex) {
            String lower = Value.toLowerCase();
            return (lower.charAt(0)=='t' || lower.charAt(0)=='y' || lower.startsWith("on"));
        }
    }

    /// <summary>
    /// Normalises single and double quotes for XPath use
    /// </summary>
    /// <param name="original">String containing single and double quotes</param>
    /// <returns>String for XPath use</returns>
    public static String CleanStringForXPath(String original)
    {
        if (!original.contains("'"))
            return '\'' + original + '\'';

        else if (!original.contains("\""))
            return '"' + original + '"';

        else
            return "concat('" + original.replace(Pattern.quote("'"), "',\"'\",'") + "')";
    }

    /// <summary>
    /// Makes string filename friendly
    /// </summary>
    /// <param name="original">Possible unfriendly filename string</param>
    /// <returns>String that can be used in a filename</returns>
    public static String CleanStringForFilename(String original)
    {
        String processed=  original.replaceAll("[^a-zA-Z0-9\\.\\-\\\\:]", "_");
        while (processed.contains("__")) processed = processed.replaceAll("__","_");
        return processed;
    }

    /// <summary>
    /// Extracts displayed text from an HTML node and desendants
    /// </summary>
    /// <param name="HtmlData">HTML containing text</param>
    /// <returns>Text with HTML stripped out</returns>
    public static String GetTextFromHTML(String HtmlData) throws Exception {
        Document htmlDoc =  Jsoup.parse(HtmlData);

        return htmlDoc.body().text();
    }

}

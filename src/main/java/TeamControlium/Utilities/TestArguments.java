package TeamControlium.Utilities;

import java.util.HashMap;
import java.util.regex.Pattern;

/// <summary>
/// Processes test command-line arguments and presents them to the test script as a string array
/// </summary>
//
// Note: Thanks to Mike Burns (https://www.linkedin.com/in/maddogmikeb) for original C# code
//
 public class TestArguments {
    // Variables
    private HashMap<String, String> processedParameters;

    /// <summary>
    /// Process the test arguments and make available for the test to use.
    /// </summary>
    /// <remarks>
    /// Arguments are space delimited and handle various common parameter preambles<br/><br/>
    /// EG. Test.exe -param1 value1 --param2 /param3:"Test-:-work /param4=happy -param5 '--=nice=--'
    /// </remarks>
    /// <param name="argumentsToProcess">String array of arguments for the test to use.</param>
    public TestArguments(String[] argumentsToProcess) {
        processedParameters = new HashMap<>();
        Pattern Spliter = Pattern.compile("^-{1,2}|^/|=|:", Pattern.CASE_INSENSITIVE);
        Pattern Remover = Pattern.compile("^['\"]?(.*?)['\"]?$", Pattern.CASE_INSENSITIVE);

        String currentParameterBeingBuilt = null;
        String[] argumentParts;

        // Valid parameters forms:
        // {-,/,--}param{ ,=,:}((",')value(",'))
        // Examples:
        // -param1 value1 --param2 /param3:"Test-:-work"
        //   /param4=happy -param5 '--=nice=--'
        for (String currentArgument : argumentsToProcess) {
            // Look for new parameters (-,/ or --) and a
            // possible enclosed value (=,:)
            argumentParts = Spliter.split(currentArgument, 3);

            switch (argumentParts.length) {
                // Found a value (for the last parameter
                // found (space separator))
                case 1:
                    if (currentParameterBeingBuilt != null) {
                        if (!processedParameters.containsKey(currentParameterBeingBuilt)) {
                            argumentParts[0] = Remover.matcher(argumentParts[0]).replaceAll("$1");

                            processedParameters.put(currentParameterBeingBuilt, argumentParts[0]);
                        }
                        currentParameterBeingBuilt = null;
                    }
                    // else Error: no parameter waiting for a value (skipped)
                    break;

                // Found just a parameter
                case 2:
                    // The last parameter is still waiting.
                    // With no value, set it to true.
                    if (currentParameterBeingBuilt != null) {
                        if (!processedParameters.containsKey(currentParameterBeingBuilt))
                            processedParameters.put(currentParameterBeingBuilt, "true");
                    }
                    currentParameterBeingBuilt = argumentParts[1];
                    break;

                // Parameter with enclosed value
                case 3:
                    // The last parameter is still waiting.
                    // With no value, set it to true.
                    if (currentParameterBeingBuilt != null) {
                        if (!processedParameters.containsKey(currentParameterBeingBuilt))
                            processedParameters.put(currentParameterBeingBuilt, "true");
                    }

                    currentParameterBeingBuilt = argumentParts[1];

                    // Remove possible enclosing characters (",')
                    if (!processedParameters.containsKey(currentParameterBeingBuilt)) {
                        argumentParts[2] = Remover.matcher(argumentParts[2]).replaceAll("$1");
                        processedParameters.put(currentParameterBeingBuilt, argumentParts[2]);
                    }

                    currentParameterBeingBuilt = null;
                    break;
            }
        }
        // In case a parameter is still waiting
        if (currentParameterBeingBuilt != null) {
            if (!processedParameters.containsKey(currentParameterBeingBuilt))
                processedParameters.put(currentParameterBeingBuilt, "true");
        }
    }

    /// <summary>
    /// Return a named parameter value if it exists
    /// </summary>
    /// <param name="Param">Parameter to obtain</param>
    /// <returns>Value of named parameter.  If named parameter does not exist null is returned</returns>
    public String getParameter(String Param) {
        try {
            return (processedParameters.get(Param));
        } catch (Exception ex) {
            return null;
        }
    }
}

import java.io.File;
import java.io.FileReader;
import java.io.BufferedReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * The JackTokenizer class tokenizes a Jack source file.
 */
public class JackTokenizer {
     // Regular expressions for different token types
    private static final Pattern KEYWORD_REGEX = Pattern.compile("^\\s*(class|constructor|function|method|static|field"
            + "|var|int|char|boolean|void|true|false|null|this|let|do|if|else|while|return)\\s*");

    private static final Pattern SYMBOL_REGEX = Pattern.compile("^\\s*([{}()\\[\\].,;+\\-*/&|<>=~])\\s*");

    private static final Pattern DIGIT_REGEX = Pattern.compile("^\\s*(\\d+)\\s*");

    private static final Pattern STRING_REGEX = Pattern.compile("^\\s*\"(.*?)\"\\s*");

    private static final Pattern IDENTIFIER_REGEX = Pattern.compile("^\\s*([a-zA-Z_][a-zA-Z1-9_]*)\\s*");

    private File sourceFile;
    private final ArrayList<String> tokens;
    private final ArrayList<TokenType> tokenTypes;
    private int count;
    private int len;

    /**
     * Constructs a JackTokenizer object with the specified source file.
     * @param sourceFile the source file to tokenize
     */
    public JackTokenizer(File sourceFile) {
        this.sourceFile = sourceFile;
        this.tokens = new ArrayList<>();
        this.tokenTypes = new ArrayList<>();
        this.count = -1;
        this.len = 0;
        tokenize();
    }

     /**
     * Tokenizes the input Jack source file.
     */
    private void tokenize() {
        try (BufferedReader reader = new BufferedReader(new FileReader(sourceFile))) {
            StringBuilder jackFileBuilder = new StringBuilder();
            String line;
            // Read the input Jack file line by line and remove comments
            while ((line = reader.readLine()) != null) {
                line = line.split("//")[0].trim();  // Remove comments
                if (!line.isEmpty() && !line.startsWith("*") && !line.startsWith("/**")) {
                    jackFileBuilder.append(line);
                }
            }
            // Convert the StringBuilder to a String
            String jackFile = jackFileBuilder.toString();
            Matcher matcher;
            // Tokenize the Jack file
            while (!jackFile.isEmpty()) {
                count++;  // Increment the token counter
            
                // Try to match the current string with each regular expression
                if ((matcher = KEYWORD_REGEX.matcher(jackFile)).find()) {
                    jackFile = matcher.replaceFirst("");  // Remove the matched token from the string
                    tokenTypes.add(TokenType.Keyword);  // Add the token type to the list
                    tokens.add(matcher.group(1));  // Add the token value to the list
                } else if ((matcher = SYMBOL_REGEX.matcher(jackFile)).find()) {
                    jackFile = matcher.replaceFirst("");
                    tokenTypes.add(TokenType.Symbol);
                    tokens.add(matcher.group(1));
                } else if ((matcher = DIGIT_REGEX.matcher(jackFile)).find()) {
                    jackFile = matcher.replaceFirst("");
                    tokenTypes.add(TokenType.IntegerConstant);
                    tokens.add(matcher.group(1));
                } else if ((matcher = STRING_REGEX.matcher(jackFile)).find()) {
                    jackFile = matcher.replaceFirst("");
                    tokenTypes.add(TokenType.StringConstant);
                    tokens.add(matcher.group(1));
                } else if ((matcher = IDENTIFIER_REGEX.matcher(jackFile)).find()) {
                    jackFile = matcher.replaceFirst("");
                    tokenTypes.add(TokenType.Identifier);
                    tokens.add(matcher.group(1));
                }
            }   
        
            // Set the length of the token list and reset the counter
            len = count;
            count = -1;
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Checks if there are more tokens in the input stream.
     * @return true if there are more tokens, false otherwise
     */
    public boolean hasMoreTokens() {
        return count < len - 1;
    }

    /**
     * Advances to the next token in the input stream.
     */
    public void advance() {
        if (hasMoreTokens()) {
            count++;
        }
    }

    public String getToken() {
        return tokens.get(count);
    }

    /**
     * Returns the type of the current token.
     * @return the type of the current token
     */
    public TokenType tokenType() {
        return tokenTypes.get(count);
    }

    /**
     * Returns the keyword of the current token.
     * Called only if the current token is of type keyword.
     * @return the keyword of the current token
     */
    public String Keyword() {
        return tokens.get(count);
    }

    /**
     * Returns the symbol of the current token.
     * Called only if the current token is of type symbol.
     * @return the symbol of the current token
     */
    public char Symbol() {
        return tokens.get(count).charAt(0);
    }

    /**
     * Returns the identifier of the current token.
     * Called only if the current token is of type identifier.
     * @return the identifier of the current token
     */
    public String Identifier() {
        return tokens.get(count);
    }

    /**
     * Returns the integer value of the current token.
     * Called only if the current token is of type integer constant.
     * @return the integer value of the current token
     */
    public int IntVal() {
        return Integer.parseInt(tokens.get(count));
    }

    /**
     * Returns the string value of the current token.
     * Called only if the current token is of type string constant.
     * @return the string value of the current token
     */
    public String StringVal() {
        return tokens.get(count);
    }

}

 

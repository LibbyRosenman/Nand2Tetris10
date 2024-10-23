import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

public class CompilationEngine {

    private BufferedWriter writer;
    private JackTokenizer token;
    private int indentation;
    private List<String> op = Arrays.asList("+", "-", "*", "/", "&", "|", "<", ">", "=");

    /**
     * constructor of the CompilationEngine.
     * Creates a new compilation engine with the given input and ouput
     * The next routine callesd (by the JackAnalyzer module) must be compileClass.
     * 
     * @param InputFile  / stream
     * @param outputfile / stream
     */
    public CompilationEngine(File InputFile, File outputFile) {
        try {
            // Create a FileWriter that writes to the specified file
            FileWriter fileWriter = new FileWriter(outputFile, true);
            // Wrap the FileWriter in a BufferedWriter for efficient writing
            writer = new BufferedWriter(fileWriter);
            // construct a tokenizer
            token = new JackTokenizer(InputFile);
            indentation = 0;
        } catch (IOException e) {
            // Handle the exception (e.g., print an error message or throw a custom
            // exception)
            e.printStackTrace();
        }
    }

    /**
     * This method compiles a complete class
     */
    public void compileClass() {
        if (this.token.hasMoreTokens()) {
            this.token.advance();
            this.write_start("class");
            this.process(this.token.Keyword()); // class
            this.process(this.token.Identifier()); // name of class
            this.process("{"); // symbol

            // handling zero or more classVarDec elements
            while ((this.token.Keyword().equals("static")) || (this.token.Keyword().equals("field"))) {

                this.compileClassVarDec();
            }
            // handling zero or more subroutineDec elements
            while ((this.token.Keyword().equals("constructor")) || (this.token.Keyword().equals("function"))
                    || (this.token.Keyword().equals("method"))) {
                this.compileSubroutine();
            }
            this.process("}");// symbol
            this.write_end("class");

        }
    }

    /**
     * This method compiles a static variable delareation, or a field declaration.
     */
    public void compileClassVarDec() {
        this.write_start("classVarDec");
        this.process(this.token.Keyword()); // static ot field
        // handling type and varName:
        if (this.token.tokenType().equals(TokenType.Keyword)) {
            this.process(this.token.Keyword());
        } else if (this.token.tokenType().equals(TokenType.Identifier)) {
            this.process(this.token.Identifier());
        }
        this.process(this.token.Identifier());
        while (this.token.Symbol() == ',') {
            this.process(",");
            this.process(this.token.Identifier());
        }
        this.process(";");
        this.write_end("classVarDec");
    }

    /**
     * This method compiles a complete method, function or constructor.
     */
    public void compileSubroutine() {
        this.write_start("subroutineDec");
        this.process(this.token.Keyword()); // costructor or function or method
        if (this.token.tokenType().equals(TokenType.Keyword)) {
            this.process(this.token.Keyword()); // void or int or char or boolean
        } else if (this.token.tokenType().equals(TokenType.Identifier)) {
            this.process(this.token.Identifier()); // className
        }
        this.process(this.token.Identifier()); // subroutinName
        this.process("(");
        this.compileParaeterList();
        this.process(")");
        this.compileSubroutineBody();
        this.write_end("subroutineDec");

    }

    /**
     * This method compiles a (possibly empty) parameter list.
     * Does not handle the enclosing parentheses tokens ( and ).
     */
    public void compileParaeterList() {
        this.write_start("parameterList");
        while (!this.token.tokenType().equals(TokenType.Symbol)) {

            // handling type and varName:
            if (this.token.tokenType().equals(TokenType.Keyword)) {
                this.process(this.token.Keyword());
            } else if (this.token.tokenType().equals(TokenType.Identifier)) {
                this.process(this.token.Identifier());
            }
            this.process(this.token.Identifier());
            if (this.token.Symbol() == ',') {
                this.process(",");
            }
        }
        this.write_end("parameterList");
    }

    /**
     * This method compiles a subroutines body.
     */
    public void compileSubroutineBody() {
        this.write_start("subroutineBody");
        this.process("{");
        // handling 0 or more varDec elements
        while (this.token.Keyword().equals("var")) {
            this.compileVarDec();
        }
        this.compileStatements();
        this.process("}");
        this.write_end("subroutineBody");
    }

    /**
     * This method compiles a var declaration.
     */
    public void compileVarDec() {
        this.write_start("varDec");
        this.process(this.token.Keyword()); // var
        // handling type and varName:
        if (this.token.tokenType().equals(TokenType.Keyword)) {
            this.process(this.token.Keyword());
        } else if (this.token.tokenType().equals(TokenType.Identifier)) {
            this.process(this.token.Identifier());
        }
        this.process(this.token.Identifier());
        while (this.token.Symbol() == ',') {
            this.process(",");
            this.process(this.token.Identifier());
        }
        this.process(";");
        this.write_end("varDec");
    }

    /**
     * This method compiles a sequece of statements.
     * Does not handle the enclosing curly bracket tokens { and }.
     */
    public void compileStatements() {
        this.write_start("statements");
        while (this.token.tokenType().equals(TokenType.Keyword)) {
            if (this.token.Keyword().equals("let")) {
                this.compileLet();
            } else if (this.token.Keyword().equals("if")) {
                this.compileIf();
            } else if (this.token.Keyword().equals("do")) {
                this.compileDo();
            } else if (this.token.Keyword().equals("while")) {
                this.compileWhile();
            } else if (this.token.Keyword().equals("return")) {
                this.compileReturn();
            }
        }
        this.write_end("statements");
    }

    /**
     * This method compiles a let statment.
     */
    public void compileLet() {
        this.write_start("letStatement");
        this.process(this.token.Keyword()); // let
        this.process(this.token.Identifier());// varName
        // ([expression])?
        if (this.token.Symbol() == '[') {
            this.process("[");
            this.compileExpression();
            this.process("]");
        }
        this.process("=");
        this.compileExpression();
        this.process(";");
        this.write_end("letStatement");

    }

    /**
     * This method compiles an if statment.
     * possibly with a trailing else clause.
     */
    public void compileIf() {
        this.write_start("ifStatement");
        this.process(this.token.Keyword()); // if
        // (expression)
        this.process("(");
        this.compileExpression();
        this.process(")");

        // {statement}
        this.process("{");
        this.compileStatements();
        this.process("}");

        // (else {statement})?
        if ((this.token.tokenType().equals(TokenType.Keyword)) && (this.token.Keyword().equals("else"))) {
            this.process(this.token.Keyword()); // else
            this.process("{");
            this.compileStatements();
            this.process("}");
        }
        this.write_end("ifStatement");
    }

    /**
     * This method compiles a while statment.
     */
    public void compileWhile() {
        this.write_start("whileStatement");
        this.process("while"); // while
        // (expression)
        this.process("(");
        this.compileExpression();
        this.process(")");
        // {statement}
        this.process("{");
        this.compileStatements();
        this.process("}");
        this.write_end("whileStatement");

    }

    /**
     * This method compiles a Do statment.
     */
    public void compileDo() {
        this.write_start("doStatement");
        this.process(this.token.Keyword()); // do
        // subroutineCall
        this.process(this.token.Identifier()); // subroutineName or (className|varName)
        // ( expressionList )
        if ((this.token.tokenType().equals(TokenType.Symbol)) && (this.token.Symbol() == '(')) {
            this.process("(");
            this.compileExpressionList();
            this.process(")");
        } else {
            // . subroutineName ( expressionList )
            this.process(".");
            this.process(this.token.Identifier()); // subroutineName
            this.process("(");
            this.compileExpressionList();
            this.process(")");
        }

        // back to doStatement
        this.process(";"); // synbol
        this.write_end("doStatement");
    }

    /**
     * This method compiles a return statment.
     */
    public void compileReturn() {
        this.write_start("returnStatement");
        this.process(this.token.Keyword()); // return
        // expression?
        if ((!this.token.tokenType().equals(TokenType.Symbol))
                || ((this.token.tokenType().equals(TokenType.Symbol)) && (this.token.Symbol() != ';'))) {
            this.compileExpression();
        }
        this.process("" + this.token.Symbol()); // ;
        this.write_end("returnStatement");
    }

    /**
     * This method compiles an expression.
     */
    public void compileExpression() {
        this.write_start("expression");
        this.compileTerm();
        while ((this.token.tokenType().equals(TokenType.Symbol)) && (op.contains("" + this.token.Symbol()))) {
            this.process("" + this.token.Symbol());
            this.compileTerm();
        }
        this.write_end("expression");
    }

    /**
     * This method compiles a term.
     * if the current token is an identifier, the routine must resolve it into a
     * variable, an array entry or a subroutine call.
     * 
     */
    public void compileTerm() {
        this.write_start("term");
        if (this.token.tokenType().equals(TokenType.IntegerConstant)) {
            process("" + this.token.IntVal());
        } else if (this.token.tokenType().equals(TokenType.StringConstant)) {
            process("" + this.token.StringVal());
        } else if (this.token.tokenType().equals(TokenType.Keyword)) {
            process(this.token.Keyword());
        } else if (this.token.tokenType().equals(TokenType.Identifier)) {
            process(this.token.Identifier());
            if ((this.token.tokenType().equals(TokenType.Symbol)) && (this.token.Symbol() == '[')) {
                process("[");
                compileExpression();
                process("]");
            }
            // subroutineCall:
            else if ((this.token.tokenType().equals(TokenType.Symbol)) && (this.token.Symbol() == '(')) {
                this.process("(");
                this.compileExpressionList();
                this.process(")");
            } else if ((this.token.tokenType().equals(TokenType.Symbol)) && (this.token.Symbol() == '.')) {
                this.process(".");
                this.process(this.token.Identifier()); // subroutineName
                this.process("(");
                this.compileExpressionList();
                this.process(")");
            }
        } else if (this.token.tokenType().equals(TokenType.Symbol)) {
            if (this.token.Symbol() == '(') {
                process("(");
                compileExpression();
                process(")");
            } else if ((this.token.Symbol() == '-') || (this.token.Symbol() == '~')) {
                process("" + this.token.Symbol());
                compileTerm();
            }
        }
        this.write_end("term");
    }

    /**
     * This method compiles a (possiblt empty) comma=separated list of expressions.
     * Returns the number of expressions in the list.
     */
    public int compileExpressionList() {
        this.write_start("expressionList");
        int num = 0;
        // if next token is ')' , there is no expression list
        if ((this.token.tokenType().equals(TokenType.Symbol)) && (this.token.Symbol() == ')')) {
            this.write_end("expressionList");
            return num;
        } else {
            compileExpression();
            num++;
            while ((this.token.tokenType().equals(TokenType.Symbol)) && (this.token.Symbol() == ',')) {
                process(",");
                compileExpression();
                num++;
            }
        }
        this.write_end("expressionList");
        return num;
    }

    /**
     * Helper method:
     * handles the current input token, and advances the input
     */
    public void process(String str) {
        String token_str = str;
        String str_type = typeToStr(this.token.tokenType());
        if (this.token.tokenType().equals(TokenType.Symbol)) {
            if (this.token.Symbol() == '<') {
                token_str = "&lt";
            }
            if (this.token.Symbol() == '>') {
                token_str = "&gt";
            }
            if (this.token.Symbol() == '&') {
                token_str = "&amp";
            }
            if (this.token.Symbol() == '"') {
                token_str = "&quot";
            }
        }
        // if (token.get() == str) {
        write_token(str_type, token_str);
        // } else {
        // System.out.println("syntax error");
        // }
        this.token.advance();
    }

    /**
     * Helper method:
     * handles the current input token, and advances the input
     */
    public void processT() {
        String token_str = this.token.getToken();
        String str_type = typeToStr(this.token.tokenType());
        if (this.token.tokenType().equals(TokenType.Symbol)) {
            if (this.token.Symbol() == '<') {
                token_str = "&lt";
            }
            if (this.token.Symbol() == '>') {
                token_str = "&gt";
            }
            if (this.token.Symbol() == '&') {
                token_str = "&amp";
            }
            if (this.token.Symbol() == '"') {
                token_str = "&quot";
            }
        }
        // if (token.get() == str) {
        write_token(str_type, token_str);
        // } else {
        // System.out.println("syntax error");
        // }
        this.token.advance();
    }

    public void translatorT() {

        this.write_start("tokens");
        this.token.advance();
        while (this.token.hasMoreTokens()) {
            this.processT();
        }
        this.write_end("tokens");
    }

    /**
     * Helper method:
     * write the first line in every compilexxx, and increament the indentation
     */
    public void write_start(String str) {
        String start = "    ".repeat(this.indentation) + "<" + str + ">\n";
        try {
            writer.write(start);
        } catch (IOException e) {
            e.printStackTrace();
        }
        this.indentation++;
    }

    /**
     * Helper method:
     * write the end line in every compilexxx, and decrease the indentation
     */
    public void write_end(String str) {
        this.indentation--;
        String end = "    ".repeat(this.indentation) + "</" + str + ">\n";
        try {
            writer.write(end);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Helper method:
     * write the line with the token and the type of the token
     */
    public void write_token(String type_str, String str) {
        String line = "  ".repeat(this.indentation) + "<" + type_str + "> " + str + " </" + type_str + ">\n";
        try {
            writer.write(line);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Helper method:
     * parse the token_type to str
     */
    public String typeToStr(TokenType token_type) {
        switch (token_type) {
            case Keyword:
                return "keyword";
            case Symbol:
                return "symbol";
            case Identifier:
                return "identifier";
            case IntegerConstant:
                return "integerConstant";
            case StringConstant:
                return "stringConstant";
            default:
                throw new IllegalArgumentException("Unsupported TokenType: " + token_type);
        }
    }

    public void close() {
        try {
            if (writer != null) {
                writer.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

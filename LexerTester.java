import java.util.List;

public class LexerTester {
    public static void main(String[] args) {
        Lexer lexer = new Lexer();
        // testingStrings(lexer);

        lexer.lex("num V_a, text V_b,");

        testingFiles(lexer);


        
    }

    private static void testingFiles(Lexer lexer){
        String[] fileNames = {
            "files/test1.rsl",
            "files/test2.rsl",
            "files/test3.rsl",
            "files/test4.rsl",
            "files/test5.rsl",
            "files/test6.rsl",
            "files/test7.rsl",
            "files/test8.rsl",
            "files/test9.rsl",
            "files/test10.rsl",
            "files/test11.rsl",
            "files/test12.rsl",
        };

        for (String fileName : fileNames) {
            System.out.println("===========================================================");
            try {
                List<Token> tokens = lexer.lexFile(fileName);
                System.out.println("Tokens for " + fileName + ":");
                for (Token token : tokens) {
                    System.out.println(token.getType() + ": " + token.getValue());
                }
            } catch (RuntimeException e) {
                System.out.println("Could not tokenize input. Error: " + e.getMessage());
            }
        }
        

    }


    private  static void testingStrings(Lexer lexer){
        String[] testStrings = {
            // Testing numbers
            "0",
            "0.0",
            "0.1",
            "0.123",
            "-0",
            "-0.0",
            "-0.1",
            "-0.123",
            "1",
            "1.0",
            "1.1",
            "1.123",
            "-1",
            "-1.0",
            "-1.1",
            "-1.123",
            "12345678901234567890", "1234567890.123456789",
            "-12345678901234567890", "-1234567890.123456789",


            // Testing reserved characters
            "{",
            "}",
            "(",
            ")",
            ",",
            ";",
            "<",
            "=",
            " ",

            // Testing reserved words
            "main",
            "num",
            "text",
            "if",
            "else",
            "begin",
            "end",
            "skip",
            "halt",
            "print",
            "then",
            "void",
            "not",
            "and",
            "or",
            "add",
            "sub",
            "mul",
            "div",
            "eq",
            "grt",
            "sqrt",
            "while",
            "for",
            "do",
            "random",

            // Testing variables
            "V",
            "V_",
            "V_1",
            "V_a1",
            "V_apple",
            "V_apple1",
            "V_Apple1",
            "V_appLe",

            // Testing functions
            "F",
            "F_",
            "F_1",
            "F_a1",
            "F_apple",
            "F_apple1",
            "F_Apple1",
            "F_appLe",
        };

        for (String testString : testStrings) {
            System.out.println("===========================================================");
            try {
                List<Token> tokens = lexer.lex(testString);
                System.out.println("Tokens for " + testString + ":");
                for (Token token : tokens) {
                    System.out.println(token.getType() + ": " + token.getValue());
                } 
            } catch (RuntimeException e) {
                System.out.println("Could not tokenize input. Error: " + e.getMessage());
            }
        }
    }


}

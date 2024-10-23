import java.io.*;
import java.nio.file.*;

public class Main {
    
    public static void main(String[] args) {
        try {
            // Step 1: Get user's input directory and filename
            BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
            System.out.println("Please enter the filename without extension (e.g., factorial): ");
            String infile = reader.readLine();

            // Step 2: Create the 'lib' directory if it doesn't exist
            Path libDir = Paths.get("lib");
            if (!Files.exists(libDir)) {
                Files.createDirectory(libDir);  // Creates the lib directory
                System.out.println("Created 'lib' directory.");
            } else {
                System.out.println("'lib' directory already exists.");
            }

            System.out.println("Lexical Analysis...");
            new TokenStream(infile + ".txt", "lib/lexer.xml");
            System.out.println("Lexical Analysis complete. Saved in 'lib/lexer.xml'.");
            System.out.println("Parsing...");
            Parser parser = new Parser();
            parser.parse("lib/lexer.xml", "lib/parser.xml");
            System.out.println("Parsing complete. Saved in 'lib/parser.xml'.");

            System.out.println("Scope Analysis...");
            ScopeAnalyser scopeAnalyser = new ScopeAnalyser("lib/parser.xml");
            scopeAnalyser.analyse("lib/scope");
            scopeAnalyser.printTable("lib/scope");
            System.out.println("Scope Analysis complete. Saved in 'lib/scope.xml'.");
            System.out.println("The symbol table is saved in 'lib/scope.txt'.");

            PrintXML printXML = new PrintXML("lib/scope.xml");
            printXML.analyse("lib/renamed.txt");

            System.out.println("Type Checking...");
            TypeChecker typeChecker = new TypeChecker("lib/scope");
            typeChecker.analyse("lib/typed");
            typeChecker.printTable("lib/typed");
            System.out.println("Type Checking complete. Saved in 'lib/typed.xml'.");
            System.out.println("The type table is saved in 'lib/typed.txt'.");

            System.out.println("Intermediate Code Generation...");
            IntermediateCodeGenerator intermediateCodeGenerator = new IntermediateCodeGenerator("lib/typed", "lib/intermediate");
            intermediateCodeGenerator.generate();
            System.out.println("Intermediate Code Generation complete. Saved in 'lib/intermediate.xml'.");

            System.out.println("Converting to BASIC...");
            IntermediateToBasicConverter intermediateToBasicConverter = new IntermediateToBasicConverter();
            intermediateToBasicConverter.convert(infile, "lib/intermediate");
            System.out.println("Conversion to BASIC complete. Saved in " + infile + ".bas'.");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

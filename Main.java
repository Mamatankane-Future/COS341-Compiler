public class Main {
    
    public static void main(String[] args) {
        String filename = "test3";
        TokenStream stream = new TokenStream("in/" + filename + ".txt");

        Parser parser = new Parser();
        parser.parse("lexer/" + filename + ".xml");

        ScopeAnalyser scopeAnalyser = new ScopeAnalyser("parser/" + filename + ".xml");
        scopeAnalyser.analyse(filename);
        scopeAnalyser.printTable(filename);

        PrintXML printXML = new PrintXML("scopes/" + filename + ".xml");
        printXML.analyse(filename);

        TypeChecker typeChecker = new TypeChecker(filename);
        typeChecker.analyse(filename);
        // typeChecker.printTable(filename);


    }
}

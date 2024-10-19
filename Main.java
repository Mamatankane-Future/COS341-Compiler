public class Main {
    
    public static void main(String[] args) {
        String filename = "test3";
        String outfile = "output";
        new TokenStream("in/" + filename + ".txt", outfile);

        Parser parser = new Parser();
        parser.parse("lexer/" + outfile + ".xml");

        ScopeAnalyser scopeAnalyser = new ScopeAnalyser("parser/" + outfile + ".xml");
        scopeAnalyser.analyse(outfile);
        scopeAnalyser.printTable(outfile);

        PrintXML printXML = new PrintXML("scopes/" + outfile + ".xml");
        printXML.analyse(outfile);

        TypeChecker typeChecker = new TypeChecker(outfile);
        typeChecker.analyse(outfile);
        typeChecker.printTable(outfile);


    }
}

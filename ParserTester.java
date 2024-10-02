public class ParserTester {
    public static void main(String[] args) {
        Parser parser = new Parser();
        TokenStream tokenStream = new TokenStream("files/test5.rsl");
        XMLTree xmlTree = parser.parse(tokenStream.toString());

        System.out.println(xmlTree);

    }
}

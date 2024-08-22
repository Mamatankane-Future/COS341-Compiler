import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

public class Lexer {
    private DFA dfa;

    public Lexer(DFA dfa) {
        this.dfa = dfa;
    }

    public Lexer() {
        Node[] nodes = new Node[17];

        for (int i = 0; i < 17; i++) {
            nodes[i] = new Node();
        }


        String specialChars = "{}(),;<= ";
        for (char c : specialChars.toCharArray()) {
            nodes[0].addTransition(c, nodes[0]);
        }

        nodes[0].addTransition('0', nodes[1]);
        nodes[0].addTransition('-', nodes[2]);
        for (char c = '1'; c <= '9'; c++) {
            nodes[0].addTransition(c, nodes[3]);
        }
        nodes[0].addTransition('"', nodes[13]);
        nodes[0].addTransition('F', nodes[8]);
        nodes[0].addTransition('V', nodes[7]);
        nodes[0].setType(Type.RESERVED_CHARACTER);

        nodes[1].addTransition('.', nodes[5]);
        for (char c: specialChars.toCharArray()) {
            nodes[1].addTransition(c, nodes[0]);
        }

        nodes[1].setType(Type.NUMBER);

        nodes[2].addTransition('0', nodes[4]);
        for (char c = '1'; c <= '9'; c++) {
            nodes[2].addTransition(c, nodes[3]);
        }

        nodes[3].addTransition('.', nodes[5]);
        for (char c: specialChars.toCharArray()) {
            nodes[3].addTransition(c, nodes[0]);
        }

        for (char c = '0'; c <= '9'; c++) {
            nodes[3].addTransition(c, nodes[3]);
        }

        nodes[3].setType(Type.NUMBER);

        nodes[4].addTransition('.', nodes[5]);

        nodes[5].addTransition('0', nodes[5]);

        for (char c = '1'; c <= '9'; c++) {
            nodes[5].addTransition(c, nodes[6]);
        }

        nodes[6].addTransition('0', nodes[5]);

        for (char c = '1'; c <= '9'; c++) {
            nodes[6].addTransition(c, nodes[6]);
        }

        for (char c: specialChars.toCharArray()) {
            nodes[6].addTransition(c, nodes[0]);
        }

        nodes[6].setType(Type.NUMBER);

        nodes[7].addTransition('_', nodes[9]);

        for (char c = 'a'; c <= 'z'; c++) {
            nodes[9].addTransition(c, nodes[10]);
        }

        for (char c = 'a'; c <= 'z'; c++) {
            nodes[10].addTransition(c, nodes[10]);
        }

        for (char c = '0'; c <= '9'; c++) {
            nodes[10].addTransition(c, nodes[10]);
        }

        for (char c: specialChars.toCharArray()) {
            nodes[10].addTransition(c, nodes[0]);
        }

        nodes[10].setType(Type.VARIABLE);


        nodes[8].addTransition('_', nodes[11]);

        for (char c = 'a'; c <= 'z'; c++) {
            nodes[11].addTransition(c, nodes[12]);
        }

        for (char c = '0'; c <= '9'; c++) {
            nodes[12].addTransition(c, nodes[12]);
        }

        for (char c = 'a'; c <= 'z'; c++) {
            nodes[12].addTransition(c, nodes[12]);
        }

        for (char c: specialChars.toCharArray()) {
            nodes[12].addTransition(c, nodes[0]);
        }

        nodes[12].setType(Type.FUNCTION);

        for (char c = 'A'; c <= 'Z'; c++) {
            nodes[13].addTransition(c, nodes[14]);
        }

        for (char c = 'a'; c <= 'z'; c++) {
            nodes[14].addTransition(c, nodes[14]);
        }

        nodes[14].addTransition('"', nodes[15]);


        for (char c: specialChars.toCharArray()) {
            nodes[15].addTransition(c, nodes[0]);
        }

        nodes[15].setType(Type.STRING);

        for (char c = 'a'; c <= 'z'; c++) {
            nodes[16].addTransition(c, nodes[16]);
        }

        for (char c = 'a'; c <= 'z'; c++) {
            nodes[0].addTransition(c, nodes[16]);
        }

        nodes[16].setType(Type.RESERVED_WORD);

        for (char c: specialChars.toCharArray()) {
            nodes[16].addTransition(c, nodes[0]);
        }

        dfa = new DFA(nodes[0]);
        
    }

    public DFA getDfa() {
        return dfa;
    }

    public void setDfa(DFA dfa) {
        this.dfa = dfa;
    }

    public List<Token> lex(String input) throws RuntimeException {
        return dfa.lex(input);
    }

    public List<Token> lexFile(String filename) {
        Path filePath = Paths.get(filename);
        List<Token> allTokens = new ArrayList<>();

        try {
            StringBuilder content = new StringBuilder();
            Files.lines(filePath).forEach(line -> content.append(line).append(" "));
            try {
                allTokens = lex(content.toString());
            } catch (RuntimeException e) {
                System.err.println("Error lexing file content.");
                e.printStackTrace();
            }
        } catch (IOException e) {
            System.err.println("Error reading file: " + filename);
            e.printStackTrace();
        }

        return allTokens;
    }
}

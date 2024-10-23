import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

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


        String specialChars = "{}(),;=< ";
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

class DFA {
    private Node startNode;

    private final String[] reservedWords = {
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
            "return",
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
            "input"
        };

    public DFA(Node startNode) {
        this.startNode = startNode;
    }

    public Node getStartNode() {
        return startNode;
    }

    public void setStartNode(Node startNode) {
        this.startNode = startNode;
    }

    private boolean isReservedWord(String token) throws RuntimeException {
        for (String reservedWord : reservedWords){
            if (token.equals(reservedWord)){
                return true;
            }
        }
        throw new RuntimeException("Invalid reserved word: " + token);
    }

    public List<Token> lex(String input) throws RuntimeException {
        Node currentNode = startNode;
        List<Token> tokens = new ArrayList<>();
        StringBuilder currentToken = new StringBuilder();
        for (int i = 0; i < input.length() - 1; i++) {
            char c = input.charAt(i);
            char c1 = input.charAt(i + 1);
            if (!currentNode.hasTransition(c)) {
                String message = "Invalid character: " + c + " at position: " + i +", reading token" +currentToken.toString();
                throw new RuntimeException(message);
            } else {
                currentNode = currentNode.getTransition(c);
                currentToken.append(c);
                Node nextNode = currentNode.getTransition(c1);

                if (currentNode.getType() == Type.RESERVED_CHARACTER && currentNode.isFinalState()){
                    Token token = new Token(currentNode.getType(), currentToken.toString());
                    tokens.add(token);
                    currentToken = new StringBuilder();
                } else if (nextNode != null && nextNode.getType() == Type.RESERVED_CHARACTER){
                    if ((currentNode.getType() == Type.RESERVED_WORD && isReservedWord(currentToken.toString())) || currentNode.getType() != Type.RESERVED_WORD){
                        Token token = new Token(currentNode.getType(), currentToken.toString());
                        tokens.add(token);
                        currentToken = new StringBuilder();
                    }
                }
        
            }
        }

        char c = input.charAt(input.length() - 1);
        if (!currentNode.hasTransition(c)) {
            String message = "Invalid character: " + c + " at position: " + (input.length() - 1);
            throw new RuntimeException(message);
        } else {
            currentNode = currentNode.getTransition(c);
            currentToken.append(c);

            if (currentNode.isFinalState()) {
                if (currentNode.getType() == Type.RESERVED_CHARACTER && currentNode.isFinalState()){
                    Token token = new Token(currentNode.getType(), currentToken.toString());
                    tokens.add(token);
                    currentToken = new StringBuilder();
                } else if ((currentNode.getType() == Type.RESERVED_WORD && isReservedWord(currentToken.toString())) || currentNode.getType() != Type.RESERVED_WORD){
                    Token token = new Token(currentNode.getType(), currentToken.toString());
                    tokens.add(token);
                    currentToken = new StringBuilder();
                }
            } else {
                String message = "Invalid token: " + currentToken.toString();
                throw new RuntimeException(message);
            }
        }

        return tokens;
    }

    @Override
    public String toString() {
        return "DFA{" +
                "startNode=" + startNode +
                '}';
    }
}


class Node {
    private TransitionMap transitions;

    private Type type = null;

    public Node() {
        this.type = null;
        this.transitions = new TransitionMap();
    }


    public boolean isFinalState() {
        return type != null;
    }


    public void addTransition(Character character, Node node) {
        this.transitions.addTransition(character, node);
    }

    public Node getTransition(Character character) {
        return this.transitions.getTransition(character);
    }

    public boolean hasTransition(Character character) {
        return this.transitions.hasTransition(character);
    }

    public Type getType() {
        return type;
    }

    public void setType(Type type) {
        this.type = type;
    }

    @Override
    public String toString() {
        return "Node{" +
                "transitions=" + transitions +
                ", type=" + type +
                '}';
    }
}

class Token {
    private String value;
    private Type type;
    private Integer id;

    public Token(){}

    public Token(Type type, String value) {
        this.type = type;
        this.value = value;
    }

    public Integer getId() {
        return id;
    }

    public Token(Type type, String value, Integer id) {
        this.type = type;
        this.value = value;
        this.id = id;
    }

    public String getValue() {
        return value;
    }

    public Type getType() {
        return type;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    @Override
    public String toString() {
        if (value.equals(" ")) {
            return "";
        }
        if (type == Type.VARIABLE) {
            return "V_";
        }
        if (type == Type.NUMBER) {
            return "N_";
        }
        if (type == Type.FUNCTION) {
            return "F_";
        }
        if (type == Type.STRING) {
            return "S_";
        }
        return getValue().replace(" ", "");
    }
}

final class Tuple {
    public Node node;
    public Character character;

    public Tuple(Node node, Character character) {
        this.node = node;
        this.character = character;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        Tuple tuple = (Tuple) obj;
        return Objects.equals(node, tuple.node) && Objects.equals(character, tuple.character);
    }

    @Override
    public int hashCode() {
        return Objects.hash(node, character);
    }
}

class TransitionMap {
    private static final List<Tuple> cache = new ArrayList<>();
    private List<Tuple> transitions = new ArrayList<>();

    public void addTransition(Character character, Node node) {
        Tuple existingTuple = findTupleInCache(character, node);
        
        if (existingTuple != null) {
            transitions.add(existingTuple);
        } else {
            Tuple newTuple = new Tuple(node, character);
            cache.add(newTuple);
            transitions.add(newTuple);
        }
    }

    private Tuple findTupleInCache(Character character, Node node) {
        for (Tuple tuple : cache) {
            if (tuple.character.equals(character) && tuple.node.equals(node)) {
                return tuple;
            }
        }
        return null;
    }

    public Node getTransition(Character character) {
        for (Tuple tuple : transitions) {
            if (tuple.character.equals(character)) {
                return tuple.node;
            }
        }
        return null;
    }

    public boolean hasTransition(Character character) {
        for (Tuple tuple : transitions) {
            if (tuple.character.equals(character)) {
                return true;
            }
        }
        return false;
    }
}

enum Type {
    VARIABLE,
    NUMBER,
    FUNCTION,
    STRING,
    RESERVED_WORD,
    RESERVED_CHARACTER,
    DOLLAR,
    TERMINATOR
}

class TokenStream{
    private List<Token> tokens;

    public TokenStream(String filename, String outfile) {
        Lexer lexer = new Lexer();
        tokens = lexer.lexFile(filename);
        List<Token> filteredTokens = new ArrayList<>();
        for (int i = 0; i < tokens.size(); i++) {
            String temp = tokens.get(i).getValue().replace(" ", "");
            if (!temp.equals("")) {
                if (temp.equals("<")){
                    filteredTokens.add(new Token(Type.RESERVED_WORD, "<input"));
                    String next = tokens.get(i + 1).getValue().replace(" ", "");
                    while(next.isEmpty()) {
                        i++;
                        next = tokens.get(i + 1).getValue().replace(" ", "");
                    }

                    if (!next.equals("input")) throw new RuntimeException("Invalid follow up word for <");
                    i++;
                }
                else filteredTokens.add(tokens.get(i));
            }
        }

        this.tokens = filteredTokens;
        StringBuilder sb = new StringBuilder();
        sb.append("<TOKENSTREAM>\n");
        Integer i = 1;
        for (Token token : tokens) {
            token.setId(i);
            sb.append("  <TOK>\n");
            sb.append("    <ID>" + i + "</ID>\n");
            sb.append("    <CLASS>" + token.getType() + "</CLASS>\n");
            sb.append("    <WORD>" + token.getValue() + "</WORD>\n");
            sb.append("  </TOK>\n");
            i++;
        }
        sb.append("  <TOK>\n");
        sb.append("    <ID>" + i + "</ID>\n");
        sb.append("    <CLASS>TERMINATOR</CLASS>\n");
        sb.append("    <WORD>$</WORD>\n");
        sb.append("  </TOK>\n");
        sb.append("</TOKENSTREAM>");
        try {
            Files.write(Paths.get(outfile), sb.toString().getBytes());
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public List<Token> getTokens() {
        return tokens;
    }

    public String toString() {
        StringBuilder sb = new StringBuilder();
        for (Token token : tokens) {
            sb.append(token.toString()).append(" ");
        }

        return sb.toString();
    }



}




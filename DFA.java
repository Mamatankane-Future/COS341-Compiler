import java.util.ArrayList;
import java.util.List;


public class DFA {
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

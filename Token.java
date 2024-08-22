public class Token {
    private String value;
    private Type type;

    public Token(){}

    public Token(Type type, String value) {
        this.type = type;
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    public Type getType() {
        return type;
    }
}

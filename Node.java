
public class Node {
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

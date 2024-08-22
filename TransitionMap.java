import java.util.*;

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

public class TransitionMap {
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

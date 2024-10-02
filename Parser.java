import java.io.*;
import java.util.HashMap;
import java.util.Map;
import java.util.Stack;
import java.util.List;
import java.util.ArrayList;

class XMLTree {
    String tag;
    String content;
    List<XMLTree> children;

    public XMLTree(String tag, String content) {
        this.tag = tag;
        this.content = content;
        this.children = new ArrayList<>();
    }

    public void addChild(XMLTree child) {
        this.children.add(child);
    }

    public String toString() {
        String ret = "<" + this.tag + ">";
        if (this.content != null) {
            ret += this.content;
        }
        for (XMLTree child : this.children) {
            ret += child.toString();
        }
        ret += "</" + this.tag + ">";
        return ret;
    }
}

public class Parser{

    public XMLTree parse(String input){
        DeserializeParser deserializeParser = new DeserializeParser();
        Map<String, String> parseMap = deserializeParser.parseInputFile();

        Stack<XMLTree> xmlTrees = new Stack<>();

        GrammarRules grammarRules = new GrammarRules();

        input = input + " $";

        Stack<String> tokens = new Stack<>();
        String[] tokenArray = input.split("\\s+"); 
        for (int i = tokenArray.length - 1; i >= 0; i--) {
            tokens.push(tokenArray[i]);
        }

        Stack<String> stack = new Stack<>();

        stack.push("0");

        while (true) {
            String top = stack.peek();
            String token = tokens.peek();

            if (top.equals("$") && token.equals("$")) {
                break;
            }

            String key = top + "_" + token;

            if (!parseMap.containsKey(key)) {
                throw new RuntimeException("No entry in parse table for key: " + key);
            }

            String value = parseMap.get(key);

            if (value.equals("acc")) {
                break;
            } else if (value.charAt(0) == 's') {
                stack.push(token);
                stack.push(value.substring(1));
                tokens.pop();
            } else if (value.charAt(0) == 'r') {
                int ruleIndex = Integer.parseInt(value.substring(1));
                GrammarRule rule = grammarRules.grammarRules.get(ruleIndex);


                for (int i = 0; i < 2 * rule.rhs.length; i++) {
                    if (i % 2 != 0){
                        String poppedString = stack.peek();
                        xmlTrees.add(new XMLTree(poppedString, ""));
                    }
                    stack.pop();
                }

                String newState = stack.peek();
                String lhs = rule.lhs;
                String newStateKey = newState + "_" + lhs;

                if (!parseMap.containsKey(newStateKey)) {
                    throw new RuntimeException("No entry in parse table for key: " + newStateKey);
                }

                String newStateValue = parseMap.get(newStateKey);
                stack.push(lhs);
                stack.push(newStateValue.substring(1));

                XMLTree leaf = new XMLTree(lhs, "");
                while (!xmlTrees.isEmpty()){
                    leaf.addChild(xmlTrees.pop());
                }
                xmlTrees.push(leaf);

            } else if (value.charAt(0) == 'g') {
                stack.push(token);
                stack.push(value.substring(1));
                tokens.pop();
            } else {
                throw new RuntimeException("Invalid value in parse table: " + value);
            }

        
        }

        return xmlTrees.pop();

    }

}



class DeserializeParser {

    public Map<String, String> parseInputFile() {
        String filePath = "parse_table.ser";
        Map<String, String> parseMap = new HashMap<>();

        // Deserialize the map from the file
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(filePath))) {
            // Cast the deserialized object to a Map
            parseMap = (Map<String, String>) ois.readObject();

            // Print the contents of the map
            // System.out.println("Deserialized Map Contents:");
            // for (Map.Entry<String, String> entry : parseMap.entrySet()) {
            //     System.out.println("Key: " + entry.getKey() + ", Value: " + entry.getValue());
            // }
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }

        return parseMap;
    }

}

class GrammarRules{

    List<GrammarRule> grammarRules = new ArrayList<>();
    
    public GrammarRules(){
        grammarRules.add(new GrammarRule("S", new String[]{"PROG"}));

        grammarRules.add(new GrammarRule("PROG", new String[]{"main", "GLOBVARS", "ALGO", "FUNCTIONS"}));

        grammarRules.add(new GrammarRule("GLOBVARS", new String[]{}));  // GLOBVARS → ''
        grammarRules.add(new GrammarRule("GLOBVARS", new String[]{"VTYP", "VNAME", ",", "GLOBVARS"}));

        grammarRules.add(new GrammarRule("VTYP", new String[]{"num"}));
        grammarRules.add(new GrammarRule("VTYP", new String[]{"text"}));

        grammarRules.add(new GrammarRule("VNAME", new String[]{"V_"}));

        grammarRules.add(new GrammarRule("ALGO", new String[]{"begin", "INSTRUC", "end"}));

        grammarRules.add(new GrammarRule("INSTRUC", new String[]{}));  // INSTRUC → ''
        grammarRules.add(new GrammarRule("INSTRUC", new String[]{"COMMAND", ";", "INSTRUC"}));

        grammarRules.add(new GrammarRule("COMMAND", new String[]{"skip"}));
        grammarRules.add(new GrammarRule("COMMAND", new String[]{"halt"}));
        grammarRules.add(new GrammarRule("COMMAND", new String[]{"print", "ATOMIC"}));
        grammarRules.add(new GrammarRule("COMMAND", new String[]{"ASSIGN"}));
        grammarRules.add(new GrammarRule("COMMAND", new String[]{"CALL"}));
        grammarRules.add(new GrammarRule("COMMAND", new String[]{"BRANCH"}));

        grammarRules.add(new GrammarRule("ATOMIC", new String[]{"VNAME"}));
        grammarRules.add(new GrammarRule("ATOMIC", new String[]{"CONST"}));

        grammarRules.add(new GrammarRule("CONST", new String[]{"N_"}));
        grammarRules.add(new GrammarRule("CONST", new String[]{"S_"}));

        grammarRules.add(new GrammarRule("ASSIGN", new String[]{"VNAME", "<", "input"}));
        grammarRules.add(new GrammarRule("ASSIGN", new String[]{"VNAME", "=", "TERM"}));

        grammarRules.add(new GrammarRule("CALL", new String[]{"FNAME", "(", "ATOMIC", ",", "ATOMIC", ",", "ATOMIC", ")"}));

        grammarRules.add(new GrammarRule("BRANCH", new String[]{"if", "COND", "then", "ALGO", "else", "ALGO"}));

        grammarRules.add(new GrammarRule("TERM", new String[]{"ATOMIC"}));
        grammarRules.add(new GrammarRule("TERM", new String[]{"CALL"}));
        grammarRules.add(new GrammarRule("TERM", new String[]{"OP"}));

        grammarRules.add(new GrammarRule("OP", new String[]{"UNOP", "(", "ARG", ")"}));
        grammarRules.add(new GrammarRule("OP", new String[]{"BINOP", "(", "ARG", ",", "ARG", ")"}));

        grammarRules.add(new GrammarRule("ARG", new String[]{"ATOMIC"}));
        grammarRules.add(new GrammarRule("ARG", new String[]{"OP"}));

        grammarRules.add(new GrammarRule("COND", new String[]{"SIMPLE"}));
        grammarRules.add(new GrammarRule("COND", new String[]{"COMPOSIT"}));

        grammarRules.add(new GrammarRule("SIMPLE", new String[]{"ATOMIC"}));
        grammarRules.add(new GrammarRule("SIMPLE", new String[]{"OP"}));

        grammarRules.add(new GrammarRule("COMPOSIT", new String[]{"BINOP", "(", "ATOMIC", ",", "ATOMIC", ")"}));
        grammarRules.add(new GrammarRule("COMPOSIT", new String[]{"UNOP", "(", "SIMPLE", ")"}));

        grammarRules.add(new GrammarRule("UNOP", new String[]{"not"}));
        grammarRules.add(new GrammarRule("UNOP", new String[]{"sqrt"}));

        grammarRules.add(new GrammarRule("BINOP", new String[]{"or"}));
        grammarRules.add(new GrammarRule("BINOP", new String[]{"and"}));
        grammarRules.add(new GrammarRule("BINOP", new String[]{"eq"}));
        grammarRules.add(new GrammarRule("BINOP", new String[]{"grt"}));
        grammarRules.add(new GrammarRule("BINOP", new String[]{"add"}));
        grammarRules.add(new GrammarRule("BINOP", new String[]{"sub"}));
        grammarRules.add(new GrammarRule("BINOP", new String[]{"mul"}));
        grammarRules.add(new GrammarRule("BINOP", new String[]{"div"}));

        grammarRules.add(new GrammarRule("FNAME", new String[]{"F_"}));

        grammarRules.add(new GrammarRule("FUNCTIONS", new String[]{}));  // FUNCTIONS → ''
        grammarRules.add(new GrammarRule("FUNCTIONS", new String[]{"DECL", "FUNCTIONS"}));

        grammarRules.add(new GrammarRule("DECL", new String[]{"HEADER", "BODY"}));

        grammarRules.add(new GrammarRule("HEADER", new String[]{"FTYP", "FNAME", "(", "VNAME", ",", "VNAME", ",", "VNAME", ")"}));

        grammarRules.add(new GrammarRule("FTYP", new String[]{"num"}));
        grammarRules.add(new GrammarRule("FTYP", new String[]{"void"}));

        grammarRules.add(new GrammarRule("BODY", new String[]{"PROLOG", "LOCVARS", "ALGO", "EPILOG", "SUBFUNCS", "end"}));

        grammarRules.add(new GrammarRule("PROLOG", new String[]{"{"}));
        grammarRules.add(new GrammarRule("EPILOG", new String[]{"}"}));

        grammarRules.add(new GrammarRule("LOCVARS", new String[]{"VTYP", "VNAME", ",", "VTYP", "VNAME", ",", "VTYP", "VNAME"}));

        grammarRules.add(new GrammarRule("SUBFUNCS", new String[]{"FUNCTIONS"}));


    }

    @Override
    public String toString() {
        String ret = "";
        for (int i = 0; i < grammarRules.size(); i++) {
            ret += grammarRules.get(i).toString() + "\n";
        }
        return ret;
    }
}


class GrammarRule {
    String lhs;   
    String[] rhs; 

    public GrammarRule(String lhs, String[] rhs) {
        this.lhs = lhs;
        this.rhs = rhs;
    }

    public String toString() {
        String ret = this.lhs + " -> ";
        for (int i = 0; i < rhs.length; i++) {
            ret += this.rhs[i] + " ";
        }
        return ret;
    }
}

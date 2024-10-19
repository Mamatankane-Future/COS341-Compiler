import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;
import java.util.Stack;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.List;
import java.util.ArrayList;

class XMLTree {
    String tag;
    Integer id;
    Integer parent;
    List<XMLTree> children;
    List<Integer> childrenIDs;

    public XMLTree(String tag, Integer id) {
        this.tag = tag;
        this.id = id;
        this.children = new ArrayList<>();
        this.childrenIDs = new ArrayList<>();
        parent = null;
    }

    public void addChild(XMLTree child) {
        this.children.addFirst(child);
        this.childrenIDs.addFirst(child.id);
        child.addParent(this.id);
    }

    public void addParent(Integer parent) {
        this.parent = parent;
    }

    public String toString() {
        return toString(0);
    }
    
    private String toString(int indentLevel) {
        StringBuilder ret = new StringBuilder();
        
        // Indent the opening tag
        ret.append("\n");
        ret.append(" ".repeat(indentLevel));
        ret.append("<").append("NODE id=\"").append(id).append("\" symbol=\"").append(this.tag).append("\" >");
    
    
        // Recursively add children with incremented indentation level
        for (XMLTree child : this.children) {
            ret.append(child.toString(indentLevel + 1));
        }
    
        // Indent the closing tag
        if (!this.children.isEmpty()) {
            ret.append("\n");
            ret.append(" ".repeat(indentLevel));
        } 
        ret.append("</NODE>");

        return ret.toString();
    }

    

    public String toSyntaxTreeString(XMLTree node) {
        StringBuilder root = new StringBuilder();
        StringBuilder innerNodes = new StringBuilder();
        StringBuilder leafs = new StringBuilder();
        
        Queue<XMLTree> queue = new LinkedList<>();
        queue.add(node);
        

        while (!queue.isEmpty()) {
            XMLTree currentNode = queue.poll();
    
            if (currentNode.parent == null) {
                root.append("  <ROOT>\n");
                root.append("    <UNID>").append(currentNode.id).append("</UNID>\n");
                root.append("    <SYMB>").append(currentNode.tag).append("</SYMB>\n");
                root.append("    <CHILDREN>\n");
                
                // Adding child IDs
                for (Integer childID : currentNode.childrenIDs) {
                    root.append("      <ID>").append(childID).append("</ID>\n");
                }
                
                root.append("    </CHILDREN>\n");
                root.append("  </ROOT>\n");
            }

            else if (!currentNode.children.isEmpty()) {
                innerNodes.append("    <IN>\n");
                innerNodes.append("      <PARENT>").append(currentNode.parent).append("</PARENT>\n");
                innerNodes.append("      <UNID>").append(currentNode.id).append("</UNID>\n");
                innerNodes.append("      <SYMB>").append(currentNode.tag).append("</SYMB>\n");
                innerNodes.append("      <CHILDREN>\n");

                // Adding child IDs for inner node
                for (Integer childID : currentNode.childrenIDs) {
                    innerNodes.append("        <ID>").append(childID).append("</ID>\n");
                }

                innerNodes.append("      </CHILDREN>\n");
                innerNodes.append("    </IN>\n");
            }

            // Leaf nodes processing
            else if (currentNode.children.isEmpty()) {
                leafs.append("    <LEAF>\n");
                leafs.append("      <PARENT>").append(currentNode.parent).append("</PARENT>\n");
                leafs.append("      <UNID>").append(currentNode.id).append("</UNID>\n");
                leafs.append("      <TERMINAL>\n");
                if (currentNode.tag.equals("<input")) leafs.append("        <TOKEN>").append("input").append("</TOKEN>\n");
                else leafs.append("        <TOKEN>").append(currentNode.tag).append("</TOKEN>\n");
                leafs.append("      </TERMINAL>\n");
                leafs.append("    </LEAF>\n");
            }

            // Add all children of current node to the queue for further processing
            for (XMLTree child : currentNode.children) {
                queue.add(child);
            }

        }

        // Combine root, inner nodes, and leaf nodes into the final XML structure
        StringBuilder finalXML = new StringBuilder();
        finalXML.append("<SYNTREE>\n");
        finalXML.append(root);
        finalXML.append("  <INNERNODES>\n");
        finalXML.append(innerNodes);
        finalXML.append("  </INNERNODES>\n");
        finalXML.append("  <LEAFNODES>\n");
        finalXML.append(leafs);
        finalXML.append("  </LEAFNODES>\n");
        finalXML.append("</SYNTREE>\n");

        return finalXML.toString();
    }
 
    
}

public class Parser{

    public void parse(String filename){
        File file = new File(filename);
        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            XMLTree xmlTree = parse(br);
            String [] parts = filename.split("/")[1].split("\\.");
            writeToFile("parser/" + parts[0] + ".xml", xmlTree);
            System.out.println("Parser output written to parser/" + parts[0] + ".xml");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void writeToFile(String fileName, XMLTree tree) throws IOException {
        Files.write(Paths.get(fileName), tree.toSyntaxTreeString(tree).getBytes(), StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
        System.out.println("XML written to " + fileName);
    }

    public XMLTree parse(BufferedReader br) throws IOException {
        DeserializeParser deserializeParser = new DeserializeParser();
        Map<String, String> parseMap = deserializeParser.parseInputFile();

        AtomicInteger currentUNID = new AtomicInteger(1);

        List<XMLTree> xmlTrees = new ArrayList<>();

        GrammarRules grammarRules = new GrammarRules();

        Stack<String> stack = new Stack<>();

        stack.push("0");

        String line = br.readLine();
        String id = null, type = null, word = null;
       

        while (true) {
            String top = stack.peek();
            if (id == null) {
                br.readLine();
                id = br.readLine();
                id = id.substring(id.indexOf("<ID>") + 4, id.indexOf("</ID>"));
                type = br.readLine();
                type = type.substring(type.indexOf("<CLASS>") + 7, type.indexOf("</CLASS>"));
                word = br.readLine();
                word = word.substring(word.indexOf("<WORD>") + 6, word.indexOf("</WORD>"));
            }
            

            Token token = new Token(Type.valueOf(type), word, Integer.parseInt(id));

            if (top.equals("$") && token.getType() == Type.DOLLAR) {
                break;
            }

            String key = top + "_" + token.toString();

            if (!parseMap.containsKey(key)) {
                throw new RuntimeException("No entry in parse table for key: " + key);
            }

            String value = parseMap.get(key);

            if (value.equals("acc")) {
                break;
            } else if (value.charAt(0) == 's') {
                stack.push(token.getValue());
                stack.push(value.substring(1));
                xmlTrees.add(new XMLTree(token.getValue(), currentUNID.getAndIncrement()));
                id = null;
            } else if (value.charAt(0) == 'r') {
                int ruleIndex = Integer.parseInt(value.substring(1));
                GrammarRule rule = grammarRules.grammarRules.get(ruleIndex);

                for (int i = 0; i < 2 * rule.rhs.length; i++) {
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

                XMLTree xmlTree = new XMLTree(lhs, currentUNID.getAndIncrement());

                for (int i = 0; i < rule.rhs.length; i++) {
                    XMLTree child = xmlTrees.remove(xmlTrees.size() - 1);
                    xmlTree.addChild(child);
                }

                xmlTrees.add(xmlTree);

            } else if (value.charAt(0) == 'g') {
                stack.push(token.getValue());
                stack.push(value.substring(1));
            } else {
                throw new RuntimeException("Invalid value in parse table: " + value);
            }

            if (id == null){
                br.readLine();
            }

        
        }

        XMLTree root = new XMLTree("S", currentUNID.getAndIncrement());
        root.addChild(xmlTrees.get(0));

        return root;

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
        grammarRules.add(new GrammarRule("COMMAND", new String[]{"return", "ATOMIC"}));
        grammarRules.add(new GrammarRule("COMMAND", new String[]{"ASSIGN"}));
        grammarRules.add(new GrammarRule("COMMAND", new String[]{"CALL"}));
        grammarRules.add(new GrammarRule("COMMAND", new String[]{"BRANCH"}));

        grammarRules.add(new GrammarRule("ATOMIC", new String[]{"VNAME"}));
        grammarRules.add(new GrammarRule("ATOMIC", new String[]{"CONST"}));

        grammarRules.add(new GrammarRule("CONST", new String[]{"N_"}));
        grammarRules.add(new GrammarRule("CONST", new String[]{"S_"}));

        grammarRules.add(new GrammarRule("ASSIGN", new String[]{"VNAME", "<input"}));
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

        grammarRules.add(new GrammarRule("SIMPLE", new String[]{"BINOP", "(", "ATOMIC", ",", "ATOMIC", ")"}));

        grammarRules.add(new GrammarRule("COMPOSIT", new String[]{"BINOP", "(", "SIMPLE", ",", "SIMPLE", ")"}));
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

        grammarRules.add(new GrammarRule("LOCVARS", new String[]{"VTYP", "VNAME", ",", "VTYP", "VNAME", ",", "VTYP", "VNAME", ","}));

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

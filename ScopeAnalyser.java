import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.xml.sax.InputSource;

import java.io.File;
import java.io.FileInputStream;
import java.io.Serializable;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Stack;

import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.xpath.XPathConstants;

class XPATH {
    private XPath xpath;
    private Document doc;
    public XPATH(String filename){
        try {
        
            File xmlFile = new File(filename);
            FileInputStream fileInputStream = new FileInputStream(xmlFile);

        
            DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
            doc = dBuilder.parse(new InputSource(fileInputStream));


            XPathFactory xpathFactory = XPathFactory.newInstance();
            xpath = xpathFactory.newXPath();


        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public String[] evaluate(String expression) {
        try {
            Node node = (Node) xpath.evaluate(expression, doc, XPathConstants.NODE);
            if (node != null) {
                String rawXML = nodeToString(node);
                String result = rawXML.replaceFirst("^<\\?xml.*?\\?>", "").replace(" ", "");
                String[] lines = result.split("\n");
                return lines;
            } else {
                throw new Exception("Node not found");
            }
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public void update(String expression, String value) {
        try {
            // Evaluate the XPath expression to get the node
            Node node = (Node) xpath.evaluate(expression, doc, XPathConstants.NODE);
            
            // Check if node is found
            if (node != null) {
                node.setTextContent(value);
        
            } else {
                throw new Exception("Node not found for the given XPath expression: " + expression);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    private static String nodeToString(Node node) throws Exception {
        StringWriter writer = new StringWriter();
        Transformer transformer = TransformerFactory.newInstance().newTransformer();
        transformer.transform(new DOMSource(node), new StreamResult(writer));
        return writer.toString();
    }

    public void save(String filename) {
        try {
            Transformer transformer = TransformerFactory.newInstance().newTransformer();
            transformer.transform(new DOMSource(doc), new StreamResult(new File(filename)));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

class Identifier implements Serializable{
    String name;
    String type;
    String value;
    String id;

    public void setName(String name) {
        this.name = name;
    }

    public void setType(String type) {
        this.type = type;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public void setId(String id) {
        this.id = id;
    }
}

class ScopeTable implements Serializable{
    HashMap<String, ArrayList<Identifier>> table = new HashMap<String, ArrayList<Identifier>>();
    public void insert(String scope, Identifier id) {
        if (table.containsKey(scope)) {
            table.get(scope).add(id);
        } else {
            ArrayList<Identifier> list = new ArrayList<Identifier>();
            list.add(id);
            table.put(scope, list);
        }
    }

    public boolean lookup(String scope, String name) {
        if (table.containsKey(scope)) {
            for (Identifier id : table.get(scope)) {
                if (id.name.equals(name)) {
                    return true;
                }
            }
        }
        return false;
    }

    public Identifier get(String scope, String name) {
        if (table.containsKey(scope)) {
            for (Identifier id : table.get(scope)) {
                if (id.name.equals(name)) {
                    return id;
                }
            }
        }
        return null;
    }

    public void assign(String scope, String name, String value) {
        if (table.containsKey(scope)) {
            for (Identifier id : table.get(scope)) {
                if (id.name.equals(name)) {
                    id.setValue(value);
                }
            }
        }
    }

    public ArrayList<Identifier> getKey(String value){
        for (String key : table.keySet()) {
            if (table.get(key).get(0).id.equals(value)) {
                return table.get(key);
            }
            
        }
        return null;
    }

    public void setType(String id, String type) {
        for (String scope : table.keySet()) {
            for (Identifier identifier : table.get(scope)) {
                if (identifier.id.equals(id)) {
                    identifier.setType(type);
                }
            }
        }
    }

    public void setValue(String id, String value) {
        for (String scope : table.keySet()) {
            for (Identifier identifier : table.get(scope)) {
                if (identifier.id.equals(id)) {
                    identifier.setValue(value);
                }
            }
        }
    }

    public String getType(String id) {
        for (String scope : table.keySet()) {
            for (Identifier identifier : table.get(scope)) {
                if (identifier.id.equals(id)) {
                    return identifier.type;
                }
            }
        }
        return null;
    }

    public String getValue(String id) {
        for (String scope : table.keySet()) {
            for (Identifier identifier : table.get(scope)) {
                if (identifier.id.equals(id)) {
                    return identifier.value;
                }
            }
        }
        return null;
    }


}

public class ScopeAnalyser {

    XPATH xpath;
    Stack<String> scopes;
    ScopeTable table;
    Stack<HashMap.Entry<String, String>> declaredLater = new Stack<>();
    List<String[]> names = new ArrayList<>();

    public ScopeAnalyser(String filename) {
        xpath = new XPATH(filename);
        scopes = new Stack<String>();
        table = new ScopeTable();
    }

    public void analyse(String filename) {
        try {
            handleS(); 
            while (!declaredLater.isEmpty()) {
                HashMap.Entry<String, String> entry = declaredLater.pop();
                if (!table.lookup(entry.getKey(), entry.getValue())) {
                    throw new RuntimeException("Function " + entry.getValue() + " not declared");
                }
            }
            java.io.FileOutputStream fileOut = new java.io.FileOutputStream("scopes/"+filename+".ser");
            java.io.ObjectOutputStream out = new java.io.ObjectOutputStream(fileOut);
            out.writeObject(table);
            out.close();
            fileOut.close();

            System.out.println("Scopes saved in scopes/"+filename+".ser");
            changeNames(filename);
            System.out.println("Names changed in scopes/"+filename+".xml");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void printTable(String filename) {
        try {
            java.io.FileInputStream fileIn = new java.io.FileInputStream("scopes/"+filename+".ser");
            java.io.ObjectInputStream in = new java.io.ObjectInputStream(fileIn);
            ScopeTable table = (ScopeTable) in.readObject();
            in.close();
            fileIn.close();

            java.io.FileWriter fileWriter = new java.io.FileWriter("scopes/"+filename+".txt");
            java.io.BufferedWriter writer = new java.io.BufferedWriter(fileWriter);
    
            for (String scope : table.table.keySet()) {
                writer.write("Scope: " + scope + "\n");
                for (Identifier id : table.table.get(scope)) {
                    writer.write("Name: " + id.name + ", Type: " + id.type + ", Value: " + id.value + ", ID: " + id.id + "\n");
                }
            }
    
            // Close the writer
            writer.close();
            fileWriter.close();
    
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private String getNewName(String scope, String name){
        while(!scope.equals("")){
            Identifier temp = table.get(scope, name);

            if (temp != null) {
                return temp.id;
            }
            scope = scope.substring(0, scope.lastIndexOf('.'));
        }

        return null;
    }

    private void changeNames(String filename) {
        for (String[] name : names) {
            String[] lines = xpath.evaluate("//UNID[text()='"+name[0]+"']/..");
            String oldName = lines[4].replace("<TOKEN>", "").replace("</TOKEN>", "").trim();
            String newName = getNewName(name[1], oldName);
        
            xpath.update("//UNID[text()='" + name[0] + "']/../TERMINAL/TOKEN", newName);
        }  
        
        xpath.save("scopes/"+filename+".xml");
        

    }
    

    private void handleS(){
        String temp = xpath.evaluate("//ROOT/CHILDREN/ID/text()")[0];
        scopes.push("global");
        Identifier identifier = new Identifier();
        identifier.setName("main");
        identifier.setType("n");
        identifier.setId("global");
        table.insert("global", identifier);
        handleProg(temp);
    }

    private void handleProg(String id){

        String[] lines = xpath.evaluate("//UNID[text()='"+id+"']/..");

        String temp = lines[6].replace("<ID>", "").replace("</ID>", "").trim();

        handleGlobals(temp);

        temp = lines[7].replace("<ID>", "").replace("</ID>", "").trim();

        handleAlgos(temp);

        temp = lines[8].replace("<ID>", "").replace("</ID>", "").trim();

        handleFuncs(temp);

    }

    private void handleAlgos(String id) {
        String[] lines = xpath.evaluate("//UNID[text()='"+id+"']/..");

        String temp = lines[6].replace("<ID>", "").replace("</ID>", "").trim();

        handleInstructions(temp);

    }

    private void handleInstructions(String id) {
        String[] lines = xpath.evaluate("//UNID[text()='"+id+"']/..");

        if (lines[0].trim().equals( "<LEAF>")) {
            return;
        }

        String temp = lines[5].replace("<ID>", "").replace("</ID>", "").trim();

        handleCommands(temp);

        temp = lines[7].replace("<ID>", "").replace("</ID>", "").trim();

        handleInstructions(temp);

    }

    private void handleCommands(String id){
        String[] lines = xpath.evaluate("//UNID[text()='"+id+"']/..");

        String temp = lines[5].replace("<ID>", "").replace("</ID>", "").trim();

        String temp2 = lines[6].replace("<ID>", "").replace("</ID>", "").trim();

        int leaf = handleLeafs(temp);

        if (leaf == 0) {
            if (!temp2.equals("</CHILDREN>")) {
                handleAtomics(temp2);
            }
        }
        
    }



    private void handleBranches(String id) {
        String[] lines = xpath.evaluate("//UNID[text()='"+id+"']/..");

        String temp = lines[6].replace("<ID>", "").replace("</ID>", "").trim();

        handleConditions(temp);

        temp = lines[8].replace("<ID>", "").replace("</ID>", "").trim();

        handleAlgos(temp);

        temp = lines[10].replace("<ID>", "").replace("</ID>", "").trim();

        handleAlgos(temp);

    }

    private void handleConditions(String id) {
        String[] lines = xpath.evaluate("//UNID[text()='"+id+"']/..");

        String temp = lines[5].replace("<ID>", "").replace("</ID>", "").trim();

        id = temp;

        lines = xpath.evaluate("//UNID[text()='"+temp+"']/..");

        temp = lines[3].replace("<SYMB>", "").replace("</SYMB>", "").trim();

        if (temp.equals("SIMPLE")) {
            handleSimple(id);
        }
        else {
            handleComposit(id);
        }

    }

    private void handleSimple(String id) {
        String[] lines = xpath.evaluate("//UNID[text()='"+id+"']/..");

        String temp = lines[7].replace("<ID>", "").replace("</ID>", "").trim();

        handleAtomics(temp);

        temp = lines[9].replace("<ID>", "").replace("</ID>", "").trim();

        handleAtomics(temp);

    }

    private void handleComposit(String id) {
        String[] lines = xpath.evaluate("//UNID[text()='"+id+"']/..");


        for (int i = 7; i < lines.length - 2; i+=2) {
            String temp = lines[i].replace("<ID>", "").replace("</ID>", "").trim();

            handleSimple(temp);

        }

    }

    private int handleLeafs(String id){
        String[] lines = xpath.evaluate("//UNID[text()='"+id+"']/..");

        if (lines[0].trim().equals( "<LEAF>")) {
            String temp = getToken(id);


            if (temp.equals("return")) {
                if (scopes.peek().equals("global")) throw new RuntimeException("Return statement found in main");

                String scope = scopes.peek();
                
                Identifier identifier = table.get(scope, scope.substring(scope.lastIndexOf('.')+1));

                if (identifier.type.equals("v")) {
                    throw new RuntimeException("Return statement found in void function");
                }
            }
            return 0;
        }
        
        String temp = lines[3].replace("<SYMB>", "").replace("</SYMB>", "").trim();

        switch (temp) {
            case "ASSIGN":
                handleAssignments(id);
                break;
            case "CALL":
                handleCalls(id);
                break;
            case "BRANCH":
                handleBranches(id);
                break;
        }

        return 1;
         
    }

    private void handleFuncs(String id){
        String[] lines = xpath.evaluate("//UNID[text()='"+id+"']/..");

        if (lines[0].trim().equals( "<LEAF>")) {
            return;
        }

        String temp = lines[5].replace("<ID>", "").replace("</ID>", "").trim();

        handleDeclarations(temp);

        temp = lines[6].replace("<ID>", "").replace("</ID>", "").trim();

        handleFuncs(temp);

    }

    private void handleGlobals(String id) {
        String[] lines = xpath.evaluate("//UNID[text()='"+id+"']/..");

        if (lines[0].trim().equals( "<LEAF>")) {
            return;
        }

        String temp = lines[5].replace("<ID>", "").replace("</ID>", "").trim();

        String type = handleDatatypes(temp);

        temp = lines[6].replace("<ID>", "").replace("</ID>", "").trim();

        String name = handleNames(temp);

        if (table.lookup("global", name)) {
            throw new RuntimeException("Variable " + name + " already declared");
        } 

        Identifier identifier = new Identifier();
        identifier.setName(name);
        identifier.setType(type);
        identifier.setId('v'+temp);
        if (type.equals("num")){
            identifier.setValue("0");
        }
        else {
            identifier.setValue("\"\"");
        }
        table.insert("global", identifier);

        temp = lines[8].replace("<ID>", "").replace("</ID>", "").trim();

        handleGlobals(temp);
    }

    private String handleDatatypes(String id) {
        String[] lines = xpath.evaluate("//UNID[text()='"+id+"']/..");
        String temp = lines[5].replace("<ID>", "").replace("</ID>", "").trim();
        return getToken(temp);  

    }

    private String getToken(String id) {
        String[] lines = xpath.evaluate("//UNID[text()='"+id+"']/..");
        String temp = lines[4].replace("<TOKEN>", "").replace("</TOKEN>", "").trim();

        return temp;
    }

    private String handleNames(String id) {
        String[] lines = xpath.evaluate("//UNID[text()='"+id+"']/..");
        String temp = lines[5].replace("<ID>", "").replace("</ID>", "").trim();

        id = temp;

        temp = getToken(temp);
    
        names.add(new String[]{id, scopes.peek()});
        return temp;      

    }

    private void handleDeclarations(String id) {
        String[] lines = xpath.evaluate("//UNID[text()='"+id+"']/..");

        String temp = lines[5].replace("<ID>", "").replace("</ID>", "").trim();

        handleHeaders(temp);

        temp = lines[6].replace("<ID>", "").replace("</ID>", "").trim();

        handleBody(temp);



    }

    private void handleHeaders(String id) {
        String[] lines = xpath.evaluate("//UNID[text()='"+id+"']/..");

        String temp = lines[5].replace("<ID>", "").replace("</ID>", "").trim();

        String type = handleDatatypes(temp);

        if (type.equals("void")) type = "v";
        else if (type.equals("num")) type = "n";
        else type = "u";

        temp = lines[6].replace("<ID>", "").replace("</ID>", "").trim();

        String name = handleNames(temp);

        if (table.lookup(scopes.peek(), name)) {
            throw new RuntimeException("Function " + name + " already declared");
        }

        Identifier identifier = new Identifier();
        identifier.setName(name);
        identifier.setType(type);
        identifier.setId('f'+temp);
        table.insert(scopes.peek(), identifier);

        scopes.push(scopes.peek()+"."+name);

        table.insert(scopes.peek(), identifier);

        for (int i = 8; i < lines.length - 2; i+=2) {
            temp = lines[i].replace("<ID>", "").replace("</ID>", "").trim();

            name = handleNames(temp);

            if (table.lookup(scopes.peek(), name)) {
                throw new RuntimeException("Variable " + name + " already declared");
            }

            identifier = new Identifier();
            identifier.setName(name);
            identifier.setType("num");
            identifier.setId('p'+temp);
            table.insert(scopes.peek(), identifier);
        }


    }

    private void handleBody(String id) {
        String[] lines = xpath.evaluate("//UNID[text()='"+id+"']/..");

        String temp = lines[6].replace("<ID>", "").replace("</ID>", "").trim();

        handleLocals(temp);

        temp = lines[7].replace("<ID>", "").replace("</ID>", "").trim();

        handleAlgos(temp);

        temp = lines[9].replace("<ID>", "").replace("</ID>", "").trim();

        handleSubs(temp);


        scopes.pop();

    }

    private void handleSubs(String id){
        String[] lines = xpath.evaluate("//UNID[text()='"+id+"']/..");

        String temp = lines[5].replace("<ID>", "").replace("</ID>", "").trim();

        handleFuncs(temp);

    }

    private void handleLocals(String id) {
        String[] lines = xpath.evaluate("//UNID[text()='"+id+"']/..");


        for (int i = 5; i < 14; i+=3) {

            String temp = lines[i].replace("<ID>", "").replace("</ID>", "").trim();

            String type = handleDatatypes(temp);

            temp = lines[i+1].replace("<ID>", "").replace("</ID>", "").trim();

            String name = handleNames(temp);

            if (table.lookup(scopes.peek(), name)) {
                throw new RuntimeException("Variable " + name + " already declared");
            }


            Identifier identifier = new Identifier();
            identifier.setName(name);
            identifier.setType(type);
            identifier.setId('v'+temp);
            if (type.equals("num")){
                identifier.setValue("0");
            }
            else {
                identifier.setValue("\"\"");
            }
            table.insert(scopes.peek(), identifier);
        }

    }


    private void handleCalls(String id) {
        String[] lines = xpath.evaluate("//UNID[text()='"+id+"']/..");

         String temp = lines[5].replace("<ID>", "").replace("</ID>", "").trim();

        String name = handleNames(temp);

        if (!functionLookup(scopes.peek(), name)) {
            declaredLater.push(new HashMap.SimpleEntry<>(scopes.peek(), name));
        } 

        temp = lines[7].replace("<ID>", "").replace("</ID>", "").trim();

        handleAtomics(temp);

        temp = lines[9].replace("<ID>", "").replace("</ID>", "").trim();

        handleAtomics(temp);

        temp = lines[11].replace("<ID>", "").replace("</ID>", "").trim();

        handleAtomics(temp);

        
    }

    private void handleAtomics(String id) {
        String[] lines = xpath.evaluate("//UNID[text()='"+id+"']/..");

        String temp = lines[5].replace("<ID>", "").replace("</ID>", "").trim();

        id = temp;

        lines = xpath.evaluate("//UNID[text()='"+temp+"']/..");

        temp = lines[3].replace("<SYMB>", "").replace("</SYMB>", "").trim();

        if (temp.equals("VNAME")) {
            
            temp = handleNames(id);

            if (!variableLookup(scopes.peek(), temp)) {
                throw new RuntimeException("Variable " + temp + " not declared");
            }
        
        }

    }


    private void handleAssignments(String id) {
        String[] lines = xpath.evaluate("//UNID[text()='"+id+"']/..");

        String temp = lines[5].replace("<ID>", "").replace("</ID>", "").trim();

        String name = handleNames(temp);

        if (!variableLookup(scopes.peek(), name)) {
            throw new RuntimeException("Variable " + name + " not declared");
        }

        temp = lines[6].replace("<ID>", "").replace("</ID>", "").trim();

        temp = getToken(temp);

        if (temp.equals("=")){
            temp = lines[7].replace("<ID>", "").replace("</ID>", "").trim();

            handleTerms(temp);
        }
   
    }

    private void handleTerms(String id) {
        String[] lines = xpath.evaluate("//UNID[text()='"+id+"']/..");

        String temp = lines[5].replace("<ID>", "").replace("</ID>", "").trim();

        id = temp;

        lines = xpath.evaluate("//UNID[text()='"+temp+"']/..");

        temp = lines[3].replace("<SYMB>", "").replace("</SYMB>", "").trim();

        if (temp.equals("ATOMIC")) {
            handleAtomics(id);
        }
        else if (temp.equals("CALL")) {
            handleCalls(id);
        }
        else {
            handleOps(id);
        }

    }

    private void handleOps(String id) {
        String[] lines = xpath.evaluate("//UNID[text()='"+id+"']/..");

        for (int i = 7; i < lines.length - 2; i+=2) {
            String temp = lines[i].replace("<ID>", "").replace("</ID>", "").trim();

            handleArgs(temp);

        }

    }

    private void handleArgs(String id) {
        String[] lines = xpath.evaluate("//UNID[text()='"+id+"']/..");

        String temp = lines[5].replace("<ID>", "").replace("</ID>", "").trim();

        id = temp;

        lines = xpath.evaluate("//UNID[text()='"+temp+"']/..");

        temp = lines[3].replace("<SYMB>", "").replace("</SYMB>", "").trim();

        if (temp.equals("ATOMIC")) {
            handleAtomics(id);
        }
        else {
            handleOps(id);
        }
        

    }

    private boolean variableLookup(String from, String name) {
        Stack<String> temp = new Stack<>();
    
        // Pop elements until we find 'from' or the stack is empty
        while (!scopes.isEmpty()) {
            if (from.equals(scopes.peek())) {
                break;
            }
            temp.push(scopes.pop());
        }
    
        // If we didn't find the 'from' element, restore the stack and return false
        if (scopes.isEmpty()) {
            // Restore stack in original order
            while (!temp.isEmpty()) {
                scopes.push(temp.pop());
            }
            return false;
        }
    
        // Look for 'name' in the scopes after 'from'
        boolean found = false;
        while (!scopes.isEmpty()) {
            if (table.lookup(scopes.peek(), name)) {
                found = true;
                break;
            }
            temp.push(scopes.pop());
        }
    
        // Restore stack to its original state
        while (!temp.isEmpty()) {
            scopes.push(temp.pop());
        }
    
        return found;
    }

    private boolean functionLookup(String from, String name) {
        Stack<String> temp = new Stack<>();
        
        // Pop elements until we find 'from' or the stack is empty
        while (!scopes.isEmpty()) {
            if (from.equals(scopes.peek())) {
                break;
            }
            temp.push(scopes.pop());
        }
    
        // If we didn't find the 'from' element, restore the stack and return false
        if (scopes.isEmpty()) {
            // Restore stack in original order
            while (!temp.isEmpty()) {
                scopes.push(temp.pop());
            }
            return false;
        }
    
        // Look for 'name' in the scopes after 'from'
        boolean found = false;
       
        if (table.lookup(scopes.peek(), name)) {
            found = true;
        }
        
        // Restore stack to its original state
        while (!temp.isEmpty()) {
            scopes.push(temp.pop());
        }
    
        return found;
    }
    

    
}

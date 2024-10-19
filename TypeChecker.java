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
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Stack;

import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.xpath.XPathConstants;


public class TypeChecker{

    XPATH xpath;
    ScopeTable table;

    public TypeChecker(String filename) {
        xpath = new XPATH("scopes/"+filename+".xml");
        try {
            java.io.FileInputStream fileIn = new java.io.FileInputStream("scopes/"+filename+".ser");
            java.io.ObjectInputStream in = new java.io.ObjectInputStream(fileIn);
            ScopeTable table = (ScopeTable) in.readObject();
            in.close();
            fileIn.close();
            this.table = table;

        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public void analyse(String filename) {
        try {
            handleS(); 
            java.io.FileOutputStream fileOut = new java.io.FileOutputStream("typers/"+filename+".ser");
            java.io.ObjectOutputStream out = new java.io.ObjectOutputStream(fileOut);
            out.writeObject(table);
            out.close();
            fileOut.close();
            System.out.println("Type Checked saved in typers/"+filename+".ser");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void printTable(String filename) {
        try {
            java.io.FileInputStream fileIn = new java.io.FileInputStream("typers/"+filename+".ser");
            java.io.ObjectInputStream in = new java.io.ObjectInputStream(fileIn);
            ScopeTable table = (ScopeTable) in.readObject();
            in.close();
            fileIn.close();

            java.io.FileWriter fileWriter = new java.io.FileWriter("typers/"+filename+".txt");
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

    

    private void handleS(){
        String temp = xpath.evaluate("//ROOT/CHILDREN/ID/text()")[0];
        if (!handleProg(temp))  throw new RuntimeException("Type Error");
    }

    private boolean handleProg(String id){

        String[] lines = xpath.evaluate("//UNID[text()='"+id+"']/..");

        String temp = lines[6].replace("<ID>", "").replace("</ID>", "").trim();

        boolean type = handleGlobals(temp);

        temp = lines[7].replace("<ID>", "").replace("</ID>", "").trim();

        type = type && handleAlgos(temp);

        temp = lines[8].replace("<ID>", "").replace("</ID>", "").trim();

        type = type && handleFuncs(temp);

        return type;

    }

    private boolean handleAlgos(String id) {
        String[] lines = xpath.evaluate("//UNID[text()='"+id+"']/..");

        String temp = lines[6].replace("<ID>", "").replace("</ID>", "").trim();

        return handleInstructions(temp);


    }

    private boolean handleInstructions(String id) {
        String[] lines = xpath.evaluate("//UNID[text()='"+id+"']/..");

        if (lines[0].trim().equals( "<LEAF>")) {
            return true;
        }

        String temp = lines[5].replace("<ID>", "").replace("</ID>", "").trim();

        boolean type = handleCommands(temp);

        temp = lines[7].replace("<ID>", "").replace("</ID>", "").trim();

        return type && handleInstructions(temp);

    }

    private boolean handleCommands(String id){
        String[] lines = xpath.evaluate("//UNID[text()='"+id+"']/..");

        String temp = lines[5].replace("<ID>", "").replace("</ID>", "").trim();

        String temp2 = lines[6].replace("<ID>", "").replace("</ID>", "").trim();

        Boolean type[] = {null};
        String[] arg = {null};  
        
        int leaf = handleLeafs(temp, type, arg); 

        if (leaf == 0) {
            if (!temp2.equals("</CHILDREN>")) {
                String type2 = handleAtomics(temp2);

                if (type2.equals("t") || type2.equals("n")) {
                    return true;
                }
            }

            return true;
        }

        if (type[0] != null) {
            return type[0];
        }

        if (arg[0] != null) {
            return arg[0].equals("n");
        }

        throw new RuntimeException("Type Error");
        // return false;
        
    }

    private boolean handleBranches(String id) {
        String[] lines = xpath.evaluate("//UNID[text()='"+id+"']/..");

        String temp = lines[6].replace("<ID>", "").replace("</ID>", "").trim();

        String condType = handleConditions(temp);


        temp = lines[8].replace("<ID>", "").replace("</ID>", "").trim();

        boolean algoType = handleAlgos(temp);


        temp = lines[10].replace("<ID>", "").replace("</ID>", "").trim();

        boolean algo2Type = handleAlgos(temp);

        return condType.equals("b") && algoType && algo2Type;

    }

    private String handleConditions(String id) {
        String[] lines = xpath.evaluate("//UNID[text()='"+id+"']/..");

        String temp = lines[5].replace("<ID>", "").replace("</ID>", "").trim();

        id = temp;

        lines = xpath.evaluate("//UNID[text()='"+temp+"']/..");

        temp = lines[3].replace("<SYMB>", "").replace("</SYMB>", "").trim();

        if (temp.equals("SIMPLE")) {
            return handleSimple(id);
        }
        else {
            return handleComposit(id);
        }

    }

    private String handleSimple(String id) {
        String[] lines = xpath.evaluate("//UNID[text()='"+id+"']/..");

        String temp = lines[5].replace("<ID>", "").replace("</ID>", "").trim();

        String binop = handleBinops(temp);


        temp = lines[7].replace("<ID>", "").replace("</ID>", "").trim();

        String atomic1 = handleAtomics(temp);


        temp = lines[9].replace("<ID>", "").replace("</ID>", "").trim();

        String atomic2 = handleAtomics(temp);

        if (binop.equals(atomic2) && binop.equals(atomic1) && binop.equals("b")) return  "b";
        if (atomic1.equals(atomic2) && atomic1.equals("n") && binop.equals("c")) return  "b";

        throw new RuntimeException("Type Error");
        // return "u";
    }

    private String handleComposit(String id) {
        String[] lines = xpath.evaluate("//UNID[text()='"+id+"']/..");

        String[] temp2 = lines;

        String temp = lines[5].replace("<ID>", "").replace("</ID>", "").trim();

        id = temp;

        lines = xpath.evaluate("//UNID[text()='"+temp+"']/..");

        temp = lines[3].replace("<SYMB>", "").replace("</SYMB>", "").trim();

        if (temp.equals("BINOP")){
            String binopType = handleBinops(id);
            lines = temp2;

            temp = lines[7].replace("<ID>", "").replace("</ID>", "").trim();
            String type1 = handleSimple(temp);

            temp = lines[9].replace("<ID>", "").replace("</ID>", "").trim();
            String type2 = handleSimple(temp);

            if (binopType.equals(type1) && binopType.equals(type2) && binopType.equals("b")) return  "b";


        }
        else{
            String unopType = handleUnops(id);
            lines = temp2;

            temp = lines[7].replace("<ID>", "").replace("</ID>", "").trim();
            String type = handleSimple(temp);

            if (unopType.equals(type)) return unopType;
        }

        throw new RuntimeException("Type Error");
        // return "u";
    }


    private int handleLeafs(String id, Boolean[] type, String[] arg) {
        String[] lines = xpath.evaluate("//UNID[text()='"+id+"']/..");

        if (lines[0].trim().equals( "<LEAF>")) {
            getToken(id);
            return 0;
        }
        
        String temp = lines[3].replace("<SYMB>", "").replace("</SYMB>", "").trim();

        switch (temp) {
            case "ASSIGN":
                type[0] = handleAssignments(id);
                break;
            case "CALL":
                arg[0] = handleCalls(id);
                break;
            case "BRANCH":
                type[0] = handleBranches(id);
                break;
        }

        return 1;
         
    }

    private boolean handleFuncs(String id){
        String[] lines = xpath.evaluate("//UNID[text()='"+id+"']/..");

        if (lines[0].trim().equals( "<LEAF>")) {
            return true;
        }

        String temp = lines[5].replace("<ID>", "").replace("</ID>", "").trim();

        boolean typedecl = handleDeclarations(temp);

        temp = lines[6].replace("<ID>", "").replace("</ID>", "").trim();

        boolean typeFunc = handleFuncs(temp);

        return typedecl && typeFunc;

    }

    private String typeOf(String type){
        if (type.equals("void")) return "v";
        if (type.equals("num")) return "n";
        if (type.equals("text")) return "t";
        throw new RuntimeException("Type Error");
    }

    private boolean handleGlobals(String id) {
        String[] lines = xpath.evaluate("//UNID[text()='"+id+"']/..");

        if (lines[0].trim().equals( "<LEAF>")) {
          return true;
        }

        String temp = lines[5].replace("<ID>", "").replace("</ID>", "").trim();

        String type = handleDatatypes(temp);

        temp = lines[6].replace("<ID>", "").replace("</ID>", "").trim();

        String name = handleNames(temp);

        table.setType(name, typeOf(type));
    
        temp = lines[8].replace("<ID>", "").replace("</ID>", "").trim();

        return handleGlobals(temp);
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

        return getToken(temp);       

    }

    private boolean handleDeclarations(String id) {
        String[] lines = xpath.evaluate("//UNID[text()='"+id+"']/..");

        String temp = lines[5].replace("<ID>", "").replace("</ID>", "").trim();

        boolean type1 = handleHeaders(temp);

        temp = lines[6].replace("<ID>", "").replace("</ID>", "").trim();

        boolean type2 = handleBody(temp);

        return type1 && type2;


    }

    private boolean handleHeaders(String id) {
        String[] lines = xpath.evaluate("//UNID[text()='"+id+"']/..");

        String temp = lines[5].replace("<ID>", "").replace("</ID>", "").trim();

        String type = handleDatatypes(temp);

        temp = lines[6].replace("<ID>", "").replace("</ID>", "").trim();

        String name = handleNames(temp);

        table.setType(name, typeOf(type));


        temp = lines[8].replace("<ID>", "").replace("</ID>", "").trim();

        name = handleNames(temp);

        if (!name.equals("n")) {
            throw new RuntimeException("Type Error");
            // return false;
        }

        temp = lines[10].replace("<ID>", "").replace("</ID>", "").trim();

        name = handleNames(temp);

        if (!name.equals("n")){
            throw new RuntimeException("Type Error");
            // return false;
        } 


        temp = lines[12].replace("<ID>", "").replace("</ID>", "").trim();

        name = handleNames(temp);

        if (!name.equals("n")){
            throw new RuntimeException("Type Error");
            // return false;
        }

        return true;

    }

    private boolean handleBody(String id) {
        String[] lines = xpath.evaluate("//UNID[text()='"+id+"']/..");

        String temp = lines[5].replace("<ID>", "").replace("</ID>", "").trim();

        boolean typeLogs = handleLogs(temp);

        temp = lines[6].replace("<ID>", "").replace("</ID>", "").trim();

        boolean typeLocals = handleLocals(temp);

        temp = lines[7].replace("<ID>", "").replace("</ID>", "").trim();

        boolean typeAlgos = handleAlgos(temp);

        temp = lines[8].replace("<ID>", "").replace("</ID>", "").trim();

        typeLogs = handleLogs(temp) && typeLogs;

        temp = lines[9].replace("<ID>", "").replace("</ID>", "").trim();

        boolean typeSubs = handleSubs(temp);


        return typeLogs && typeLocals && typeAlgos && typeSubs;
    }

    private boolean handleSubs(String id){
        String[] lines = xpath.evaluate("//UNID[text()='"+id+"']/..");

        String temp = lines[5].replace("<ID>", "").replace("</ID>", "").trim();

        return handleFuncs(temp);

    }

    private boolean handleLogs(String id){
        String[] lines = xpath.evaluate("//UNID[text()='"+id+"']/..");

        String temp = lines[5].replace("<ID>", "").replace("</ID>", "").trim();

        getToken(temp);

        if (temp.equals("{") || temp.equals("}")) return true;

        else 
            throw new RuntimeException("Type Error");
            // return false;

    }

    private boolean handleLocals(String id) {
        String[] lines = xpath.evaluate("//UNID[text()='"+id+"']/..");


        for (int i = 5; i < 14; i+=3) {

            String temp = lines[i].replace("<ID>", "").replace("</ID>", "").trim();

            String type = handleDatatypes(temp);

            temp = lines[i+1].replace("<ID>", "").replace("</ID>", "").trim();

            String name = handleNames(temp);

            table.setType(name, typeOf(type));

        }

        return true;

    }


    private String handleCalls(String id) {
        String[] lines = xpath.evaluate("//UNID[text()='"+id+"']/..");

        String temp = lines[5].replace("<ID>", "").replace("</ID>", "").trim();

        String funcName = handleNames(temp);

        String funcType = table.getType(funcName);


        temp = lines[7].replace("<ID>", "").replace("</ID>", "").trim();

        String name = handleAtomics(temp);

        boolean nameType = name.equals("n");


        temp = lines[9].replace("<ID>", "").replace("</ID>", "").trim();

        name = handleAtomics(temp);
        nameType = name.equals("n") && nameType;



        temp = lines[11].replace("<ID>", "").replace("</ID>", "").trim();

        name = handleAtomics(temp);
        nameType = name.equals("n") && nameType;

        if (nameType) return funcType;
        else 
            throw new RuntimeException("Type Error");
            // return "u";


        
    }

    private String handleAtomics(String id) {
        String[] lines = xpath.evaluate("//UNID[text()='"+id+"']/..");

        String temp = lines[5].replace("<ID>", "").replace("</ID>", "").trim();

        id = temp;

        lines = xpath.evaluate("//UNID[text()='"+temp+"']/..");

        temp = lines[3].replace("<SYMB>", "").replace("</SYMB>", "").trim();

        if (temp.equals("VNAME")) {
            temp = handleNames(id);
            return table.getType(temp);
        }
        else {
            temp = handleConstants(id);
            String alphabeticPattern = "^\"[a-zA-Z]+\"$";
            String numericPattern = "^[0-9]+$";
            if (temp.matches(alphabeticPattern)) return "t";
            if (temp.matches(numericPattern)) return "n";
        }

        throw new RuntimeException("Type Error");

    }

    private String handleConstants(String id) {
        String[] lines = xpath.evaluate("//UNID[text()='"+id+"']/..");

        String temp = lines[5].replace("<ID>", "").replace("</ID>", "").trim();

        return getToken(temp);

    }


    private boolean handleAssignments(String id) {
        String[] lines = xpath.evaluate("//UNID[text()='"+id+"']/..");

        String temp = lines[5].replace("<ID>", "").replace("</ID>", "").trim();

        temp = handleNames(temp);

        String type = table.getType(temp);

        temp = lines[6].replace("<ID>", "").replace("</ID>", "").trim();

        temp = getToken(temp);

        if (temp.equals("=")){
            temp = lines[7].replace("<ID>", "").replace("</ID>", "").trim();

            String type2 = handleTerms(temp);

            return type2.equals(type);
        }
        else {
            if (type.equals("n")) return true;
            else 
                throw new RuntimeException("Type Error");
                // return false;
        }
   
    }

    private String handleTerms(String id) {
        String[] lines = xpath.evaluate("//UNID[text()='"+id+"']/..");

        String temp = lines[5].replace("<ID>", "").replace("</ID>", "").trim();

        id = temp;

        lines = xpath.evaluate("//UNID[text()='"+temp+"']/..");

        temp = lines[3].replace("<SYMB>", "").replace("</SYMB>", "").trim();

        if (temp.equals("ATOMIC")) {
            return handleAtomics(id);
        }
        else if (temp.equals("CALL")) {
            return handleCalls(id);
        }
        else {
            return handleOps(id);
        }

    }

    private String handleOps(String id) {
        String[] lines = xpath.evaluate("//UNID[text()='"+id+"']/..");

        String[] temp2 = lines;

        String temp = lines[5].replace("<ID>", "").replace("</ID>", "").trim();

        id = temp;

        lines = xpath.evaluate("//UNID[text()='"+temp+"']/..");

        temp = lines[3].replace("<SYMB>", "").replace("</SYMB>", "").trim();

        if (temp.equals("BINOP")){
            String type = handleBinops(id);
            lines = temp2;


            temp = lines[7].replace("<ID>", "").replace("</ID>", "").trim();
            String arg1 = handleArgs(temp);

            temp = lines[9].replace("<ID>", "").replace("</ID>", "").trim();
            String arg3 = handleArgs(temp);

            if (type.equals(arg3) && type.equals(arg1) && (type.equals("b") || type.equals("n"))) return type;
            
            if (arg1.equals(arg3) && arg1.equals("n") && type.equals("b")) return type;

            else 
                throw new RuntimeException("Type Error");
                // return "u";
        }
        else{
            String type = handleUnops(id);
            lines = temp2;

            temp = lines[7].replace("<ID>", "").replace("</ID>", "").trim();
            String arg1 = handleArgs(temp);

            if (type.equals(arg1)) return type;
            else 
                throw new RuntimeException("Type Error");
                // return "u";

        }

    }

    private String handleBinops(String id){
        String[] lines = xpath.evaluate("//UNID[text()='"+id+"']/..");

        String temp = lines[5].replace("<ID>", "").replace("</ID>", "").trim();

        String binop = getToken(temp);

        String bools[] = {"and", "or"};

        String nums[] = {"add", "sub", "mul", "div"};

        String cs[] = {"eq", "gt"};

        if (Arrays.asList(bools).contains(binop)) {
            return "b";
        }
        
        if (Arrays.asList(nums).contains(binop)) {
            return "n";
        }

        if (Arrays.asList(cs).contains(binop)) {
            return "c";
        }

        throw new RuntimeException("Type Error");
    }

    private String handleUnops(String id){
        String[] lines = xpath.evaluate("//UNID[text()='"+id+"']/..");

        String temp = lines[5].replace("<ID>", "").replace("</ID>", "").trim();

        temp = getToken(temp);

        if (temp.equals("sqrt")) return "n";
        if (temp.equals("not")) return "b";

        throw new RuntimeException("Type Error");
    }

    private String handleArgs(String id) {
        String[] lines = xpath.evaluate("//UNID[text()='"+id+"']/..");

        String temp = lines[5].replace("<ID>", "").replace("</ID>", "").trim();

        id = temp;

        lines = xpath.evaluate("//UNID[text()='"+temp+"']/..");

        temp = lines[3].replace("<SYMB>", "").replace("</SYMB>", "").trim();

        if (temp.equals("ATOMIC")) {
            return handleAtomics(id);
        }
        else {
            return handleOps(id);
        }
        

    }

   
}

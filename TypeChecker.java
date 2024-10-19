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
        xpath = new XPATH(filename);
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

    public void analyse() {
        try {
            handleS(); 
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

        Boolean type = true;
        int leaf = handleLeafs(temp, type);

        if (leaf == 0) {
            if (!temp2.equals("</CHILDREN>")) {
               type = handleAtomics(temp2);
            }
        }

        return type;
        
    }

    private boolean handleBranches(String id) {
        String[] lines = xpath.evaluate("//UNID[text()='"+id+"']/..");

        String temp = lines[6].replace("<ID>", "").replace("</ID>", "").trim();

        String condType = handleConditions(temp);


        temp = lines[8].replace("<ID>", "").replace("</ID>", "").trim();

        String algoType = handleAlgos(temp);


        temp = lines[10].replace("<ID>", "").replace("</ID>", "").trim();

        String algo2Type = handleAlgos(temp);

        return condType.equals("b") && algoType.equals(algo2Type);

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

        return "u";
    }

    private void handleComposit(String id) {
        String[] lines = xpath.evaluate("//UNID[text()='"+id+"']/..");

        String[] temp2 = lines;

        String temp = lines[5].replace("<ID>", "").replace("</ID>", "").trim();

        id = temp;

        lines = xpath.evaluate("//UNID[text()='"+temp+"']/..");

        temp = lines[3].replace("<SYMB>", "").replace("</SYMB>", "").trim();

        if (temp.equals("BINOP")){
            handleBinops(id);
            lines = temp2;
            temp = lines[6].replace("<ID>", "").replace("</ID>", "").trim();
            getToken(temp);

            temp = lines[7].replace("<ID>", "").replace("</ID>", "").trim();
            handleSimple(temp);

            temp = lines[8].replace("<ID>", "").replace("</ID>", "").trim();
            getToken(temp);

            temp = lines[9].replace("<ID>", "").replace("</ID>", "").trim();
            handleSimple(temp);

            temp = lines[10].replace("<ID>", "").replace("</ID>", "").trim();
            getToken(temp);


        }
        else{
            handleUnops(id);
            lines = temp2;
            temp = lines[6].replace("<ID>", "").replace("</ID>", "").trim();
            getToken(temp);

            temp = lines[7].replace("<ID>", "").replace("</ID>", "").trim();
            handleSimple(temp);

            temp = lines[8].replace("<ID>", "").replace("</ID>", "").trim();
            getToken(temp);
        }

    }


    private int handleLeafs(String id, Boolean type){
        String[] lines = xpath.evaluate("//UNID[text()='"+id+"']/..");

        if (lines[0].trim().equals( "<LEAF>")) {
            getToken(id);
            type = true;
            return 0;
        }
        
        String temp = lines[3].replace("<SYMB>", "").replace("</SYMB>", "").trim();

        switch (temp) {
            case "ASSIGN":
                type = handleAssignments(id);
                break;
            case "CALL":
                type = handleCalls(id);
                break;
            case "BRANCH":
                type = handleBranches(id);
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

        System.out.print(temp+" ");

        return temp;
    }

    private String handleNames(String id) {
        String[] lines = xpath.evaluate("//UNID[text()='"+id+"']/..");
        String temp = lines[5].replace("<ID>", "").replace("</ID>", "").trim();

        return getToken(temp);       

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

        handleDatatypes(temp);

        temp = lines[6].replace("<ID>", "").replace("</ID>", "").trim();

        handleNames(temp);

        temp = lines[7].replace("<ID>", "").replace("</ID>", "").trim();

        getToken(temp);

        temp = lines[8].replace("<ID>", "").replace("</ID>", "").trim();

        handleNames(temp);

        temp = lines[9].replace("<ID>", "").replace("</ID>", "").trim();

        getToken(temp);

        temp = lines[10].replace("<ID>", "").replace("</ID>", "").trim();

        handleNames(temp);

        temp = lines[11].replace("<ID>", "").replace("</ID>", "").trim();

        getToken(temp);


        temp = lines[12].replace("<ID>", "").replace("</ID>", "").trim();

        handleNames(temp);

        temp = lines[13].replace("<ID>", "").replace("</ID>", "").trim();

        getToken(temp);

    }

    private void handleBody(String id) {
        String[] lines = xpath.evaluate("//UNID[text()='"+id+"']/..");

        String temp = lines[5].replace("<ID>", "").replace("</ID>", "").trim();

        handleLogs(temp);

        temp = lines[6].replace("<ID>", "").replace("</ID>", "").trim();

        handleLocals(temp);

        temp = lines[7].replace("<ID>", "").replace("</ID>", "").trim();

        handleAlgos(temp);

        temp = lines[8].replace("<ID>", "").replace("</ID>", "").trim();

        handleLogs(temp);

        temp = lines[9].replace("<ID>", "").replace("</ID>", "").trim();

        handleSubs(temp);

        temp = lines[10].replace("<ID>", "").replace("</ID>", "").trim();

        getToken(temp);

    }

    private void handleSubs(String id){
        String[] lines = xpath.evaluate("//UNID[text()='"+id+"']/..");

        String temp = lines[5].replace("<ID>", "").replace("</ID>", "").trim();

        handleFuncs(temp);

    }

    private void handleLogs(String id){
        String[] lines = xpath.evaluate("//UNID[text()='"+id+"']/..");

        String temp = lines[5].replace("<ID>", "").replace("</ID>", "").trim();

        getToken(temp);

    }

    private void handleLocals(String id) {
        String[] lines = xpath.evaluate("//UNID[text()='"+id+"']/..");


        for (int i = 5; i < 14; i+=3) {

            String temp = lines[i].replace("<ID>", "").replace("</ID>", "").trim();

            handleDatatypes(temp);

            temp = lines[i+1].replace("<ID>", "").replace("</ID>", "").trim();

            handleNames(temp);

            temp = lines[i+2].replace("<ID>", "").replace("</ID>", "").trim();

            getToken(temp);

        }

    }


    private String handleCalls(String id) {
        String[] lines = xpath.evaluate("//UNID[text()='"+id+"']/..");

        String temp = lines[5].replace("<ID>", "").replace("</ID>", "").trim();

        String funcName = handleNames(temp);

        String funcType = table.getType(funcName);


        temp = lines[7].replace("<ID>", "").replace("</ID>", "").trim();

        String name = handleAtomics(temp);

        boolean nameType = table.getType(name).equals("n");


        temp = lines[9].replace("<ID>", "").replace("</ID>", "").trim();

        name = handleAtomics(temp);
        nameType = table.getType(name).equals("n") && nameType;



        temp = lines[11].replace("<ID>", "").replace("</ID>", "").trim();

        name = handleAtomics(temp);
        nameType = table.getType(name).equals("n") && nameType;

        return nameType ? funcType : "u";
        
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
            String alphabeticPattern = "^[a-zA-Z]+$";
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

            String name = handleTerms(temp);

            return table.getType(name).equals(type);
        }
        else {
            if (type.equals("n")) return true;
            else return false;
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

            else return "u";
        }
        else{
            String type = handleUnops(id);
            lines = temp2;

            temp = lines[7].replace("<ID>", "").replace("</ID>", "").trim();
            String arg1 = handleArgs(temp);

            if (type.equals(arg1)) return type;
            else return "u";

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

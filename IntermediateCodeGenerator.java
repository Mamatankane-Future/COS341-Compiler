import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicInteger;

class BufferedFileWriter implements AutoCloseable {
    private static final int MAX_BUFFER_LENGTH = 1024;
    private StringBuilder stringBuilder; 
    private BufferedWriter writer;
    private String filename;

    public BufferedFileWriter(String filename) throws IOException {
        this.filename = filename + ".txt";
        this.stringBuilder = new StringBuilder();
        (new BufferedWriter(new FileWriter(this.filename))).close();
        this.writer = new BufferedWriter(new FileWriter(this.filename, true));
    }


    public void addString(String text) throws IOException {
        stringBuilder.append(text);

        if (stringBuilder.length() >= MAX_BUFFER_LENGTH) {
            writeToFile();
        }
    }

    private void writeToFile() throws IOException {
        String text = stringBuilder.toString();
        writer.write(text);
        writer.flush();
        stringBuilder.setLength(0);
    }

    public void flush() throws IOException {
        if (stringBuilder.length() > 0) {
            writeToFile();
        }
    }

    @Override
    public void close() throws IOException {
        flush();
        writer.close();
    }
}

public class IntermediateCodeGenerator {

    XPATH xpath;
    BufferedFileWriter writer;
    private AtomicInteger counter = new AtomicInteger(0);
    private AtomicInteger counter2 = new AtomicInteger(0);
    private ScopeTable scopeTable;
    private ArrayList<String> scopes = new ArrayList<>();

    public IntermediateCodeGenerator(String infile, String outfile) {
        xpath = new XPATH("lib/scope.xml");
        try {
            writer = new BufferedFileWriter(outfile);
            scopeTable = new ScopeTable();
            java.io.FileInputStream fileIn = new java.io.FileInputStream(infile+".ser");
            java.io.ObjectInputStream in = new java.io.ObjectInputStream(fileIn);
            ScopeTable table = (ScopeTable) in.readObject();
            in.close();
            fileIn.close();
            this.scopeTable = table;
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    public void generate() {
        try {
            handleS();
            writer.flush();
            System.out.println("Intermediate code generated successfully");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    

    private void handleS() throws IOException{
        String temp = xpath.evaluate("//ROOT/CHILDREN/ID/text()")[0];
        scopes.add("global");
        handleProg(temp);
    }

    private void handleProg(String id) throws IOException{

        String[] lines = xpath.evaluate("//UNID[text()='"+id+"']/..");

        String temp = lines[6].replace("<ID>", "").replace("</ID>", "").trim();

        handleGlobals(temp);

        temp = lines[7].replace("<ID>", "").replace("</ID>", "").trim();

        handleAlgos(temp);

        writer.addString("STOP\n");

        temp = lines[8].replace("<ID>", "").replace("</ID>", "").trim();

        handleFuncs(temp);

    }

    private void handleGlobals(String id) throws IOException {
        String[] lines = xpath.evaluate("//UNID[text()='"+id+"']/..");

        if (lines[0].trim().equals( "<LEAF>")) {
          return;
        }


        String temp = lines[6].replace("<ID>", "").replace("</ID>", "").trim();

        String name = handleNames(temp);

        writer.addString(name+" := "+scopeTable.getValue(name)+"\n");
    

        temp = lines[8].replace("<ID>", "").replace("</ID>", "").trim();

        handleGlobals(temp);
    }

    private void handleAlgos(String id) throws IOException {
        String[] lines = xpath.evaluate("//UNID[text()='"+id+"']/..");

        String temp = lines[6].replace("<ID>", "").replace("</ID>", "").trim();

        handleInstructions(temp);

    }

    private void handleInstructions(String id) throws IOException {
        String[] lines = xpath.evaluate("//UNID[text()='"+id+"']/..");

        if (lines[0].trim().equals( "<LEAF>")) {
            writer.addString("REM END\n");
            return;
        }

        String temp = lines[5].replace("<ID>", "").replace("</ID>", "").trim();

        handleCommands(temp);

        temp = lines[7].replace("<ID>", "").replace("</ID>", "").trim();

        writer.addString("");

        handleInstructions(temp);

    }

    private void handleCommands(String id) throws IOException{
        String[] lines = xpath.evaluate("//UNID[text()='"+id+"']/..");

        String temp = lines[5].replace("<ID>", "").replace("</ID>", "").trim();

        String temp2 = lines[6].replace("<ID>", "").replace("</ID>", "").trim();

        int leaf = handleLeafs(temp);

        if (leaf == 0) {
            if (!temp2.equals("</CHILDREN>")) {
                handleAtomics(temp2);
                writer.addString(";\n");
            }
        }

        if (leaf == 2) {
            int from = 32;
            writer.addString("M[SP + "+(from+56)+"] := ");
            handleAtomics(temp2);
            writer.addString(";\n");
        }
        
    }

    private void handleBranches(String id) throws IOException {
        String[] lines = xpath.evaluate("//UNID[text()='"+id+"']/..");

        String temp = lines[6].replace("<ID>", "").replace("</ID>", "").trim();

        String label1 = newLabel();
        String label2 = newLabel();
        String label3 = newLabel();
        handleConditions(temp, label1, label2);

        temp = lines[8].replace("<ID>", "").replace("</ID>", "").trim();

        writer.addString("LABEL "+label1+"\n");
        handleAlgos(temp);
        writer.addString("GOTO "+label3+"\n");
        temp = lines[10].replace("<ID>", "").replace("</ID>", "").trim();
        writer.addString("LABEL "+label2+"\n");
        handleAlgos(temp);
        writer.addString("LABEL "+label3+"\n");

    }

    private void handleConditions(String id, String label1, String label2) throws IOException {
        String[] lines = xpath.evaluate("//UNID[text()='"+id+"']/..");

        String temp = lines[5].replace("<ID>", "").replace("</ID>", "").trim();

        id = temp;

        lines = xpath.evaluate("//UNID[text()='"+temp+"']/..");

        temp = lines[3].replace("<SYMB>", "").replace("</SYMB>", "").trim();

        if (temp.equals("SIMPLE")) {
            writer.addString("IF ");
            handleSimple(id);
            writer.addString(" THEN ");
            writer.addString("GOTO "+label1+" ");
            writer.addString("ELSE ");
            writer.addString("GOTO "+label2+";\n");
        }
        else {
            handleComposit(id);
            writer.addString("THEN ");
            writer.addString("GOTO "+label1+" ");
            writer.addString("ELSE ");
            writer.addString("GOTO "+label2+";\n");
        }

    }

    private String newLabel(){
        return "L"+counter2.getAndIncrement();
    }

    private void handleSimple(String id) throws IOException {
        String[] lines = xpath.evaluate("//UNID[text()='"+id+"']/..");

        String temp = lines[5].replace("<ID>", "").replace("</ID>", "").trim();

        String op = handleBinops(temp);


        temp = lines[7].replace("<ID>", "").replace("</ID>", "").trim();

        handleAtomics(temp);

        writer.addString(" "+op+" ");


        temp = lines[9].replace("<ID>", "").replace("</ID>", "").trim();

        handleAtomics(temp);

    }

    private void handleComposit(String id) throws IOException {
        String[] lines = xpath.evaluate("//UNID[text()='"+id+"']/..");

        String[] temp2 = lines;

        String temp = lines[5].replace("<ID>", "").replace("</ID>", "").trim();

        id = temp;

        lines = xpath.evaluate("//UNID[text()='"+temp+"']/..");

        temp = lines[3].replace("<SYMB>", "").replace("</SYMB>", "").trim();

        if (temp.equals("BINOP")){
            String op = handleBinops(id);
            lines = temp2;
            temp = lines[6].replace("<ID>", "").replace("</ID>", "").trim();
            getToken(temp);

            temp = lines[7].replace("<ID>", "").replace("</ID>", "").trim();

            writer.addString("IF ");
            handleSimple(temp);

            writer.addString(" "+op+" ");

            temp = lines[9].replace("<ID>", "").replace("</ID>", "").trim();
            handleSimple(temp);

            writer.addString(" ");


        }
        else{
            String op = handleUnops(id);
            lines = temp2;

            writer.addString("IF "+op+" ");

            temp = lines[7].replace("<ID>", "").replace("</ID>", "").trim();
            handleSimple(temp);
            writer.addString(" ");

        }

    }


    private int handleLeafs(String id) throws IOException{
        String[] lines = xpath.evaluate("//UNID[text()='"+id+"']/..");

        if (lines[0].trim().equals( "<LEAF>")) {
            String token = getToken(id);

            if (token.equals("halt")) {
                writer.addString("STOP");
            }
            else if (token.equals("skip")) {
                writer.addString("REM DO NOTHING");
            }
            else if (token.equals("print")) {
                writer.addString("PRINT ");
            }
            else if (token.equals("return")){
                return 2;
            }
            return 0;
        }
        
        String temp = lines[3].replace("<SYMB>", "").replace("</SYMB>", "").trim();

        switch (temp) {
            case "ASSIGN":
                handleAssignments(id);
                break;
            case "CALL":
                handleCalls(id, null);
                break;
            case "BRANCH":
                handleBranches(id);
                break;
        }

        return 1;
         
    }

    private void handleFuncs(String id) throws IOException{
        String[] lines = xpath.evaluate("//UNID[text()='"+id+"']/..");

        if (lines[0].trim().equals( "<LEAF>")) {
            writer.addString("REM END\n");
            return;
        }

        String temp = lines[5].replace("<ID>", "").replace("</ID>", "").trim();

        handleDeclarations(temp);

        writer.addString("STOP\n");

        temp = lines[6].replace("<ID>", "").replace("</ID>", "").trim();

        handleFuncs(temp);

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

    private String handleNames(String id){
        String[] lines = xpath.evaluate("//UNID[text()='"+id+"']/..");
        String temp = lines[5].replace("<ID>", "").replace("</ID>", "").trim();

        temp = getToken(temp); 
        return temp; 

    }

    private void handleDeclarations(String id) throws IOException {
        String[] lines = xpath.evaluate("//UNID[text()='"+id+"']/..");

        String temp = lines[5].replace("<ID>", "").replace("</ID>", "").trim();

        handleHeaders(temp);

        temp = lines[6].replace("<ID>", "").replace("</ID>", "").trim();

        handleBody(temp);

    }

    private void handleHeaders(String id) throws IOException {
        String[] lines = xpath.evaluate("//UNID[text()='"+id+"']/..");

        String temp = lines[6].replace("<ID>", "").replace("</ID>", "").trim();

        

        String name = handleNames(temp);
        writer.addString("LABEL "+name+"\n");
        writer.addString("SP := SP - 8 * 8\n");
        int from = 32;

        writer.addString("M[SP + "+from+"] := R0\n");
        writer.addString("M[SP + "+(from+8)+"] := R1\n");
        writer.addString("M[SP + "+(from+16)+"] := R2\n");
        writer.addString("M[SP + "+(from+24)+"] := R3\n");
    

        temp = lines[8].replace("<ID>", "").replace("</ID>", "").trim();

        name = handleNames(temp);

        writer.addString(name +" := M[SP + "+(from+32)+"]\n");


        temp = lines[10].replace("<ID>", "").replace("</ID>", "").trim();

        name = handleNames(temp);

        writer.addString(name +" := M[SP + "+(from+40)+"]\n");

        temp = lines[12].replace("<ID>", "").replace("</ID>", "").trim();

        name = handleNames(temp);
        writer.addString(name +" := M[SP + "+(from+48)+"]\n");
    }

    private void handleBody(String id) throws IOException {
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

    }

    private void handleSubs(String id) throws IOException{
        String[] lines = xpath.evaluate("//UNID[text()='"+id+"']/..");

        String temp = lines[5].replace("<ID>", "").replace("</ID>", "").trim();

        handleFuncs(temp);

    }

    private void handleLogs(String id) throws IOException{
        String[] lines = xpath.evaluate("//UNID[text()='"+id+"']/..");

        String temp = lines[5].replace("<ID>", "").replace("</ID>", "").trim();

        temp = getToken(temp);

        if (temp.equals("{")) {
            writer.addString("REM BEGIN\n");
        }
        else {
            int from = 32;
            writer.addString("R0 := M[SP + "+from+"]\n");
            writer.addString("R1 := M[SP + "+(from+8)+"]\n");
            writer.addString("R2 := M[SP + "+(from+16)+"]\n");
            writer.addString("R3 := M[SP + "+(from+24)+"]\n");
            writer.addString("SP := SP + 8 * 8\n");
            writer.addString("GOTO M[SP]\n");
            writer.addString("REM END\n");
        }

    }

    private void handleLocals(String id) throws IOException {
        String[] lines = xpath.evaluate("//UNID[text()='"+id+"']/..");


        for (int i = 5; i < 14; i+=3) {

            String temp = lines[i].replace("<ID>", "").replace("</ID>", "").trim();

            handleDatatypes(temp);

            temp = lines[i+1].replace("<ID>", "").replace("</ID>", "").trim();

            String name = handleNames(temp);

            writer.addString(name+" := "+scopeTable.getValue(name)+"\n");

        }

    }

    private void handleCalls(String id, String place) throws IOException {
        String[] lines = xpath.evaluate("//UNID[text()='"+id+"']/..");


        String temp = lines[5].replace("<ID>", "").replace("</ID>", "").trim();

        String name = handleNames(temp);

        String currentScope = scopes.get(0);

        scopes.addFirst(name);

        ArrayList<Identifier> args = scopeTable.getKey(scopes.get(0));

        if (!currentScope.equals("global")){
            int k = 0;
            for (Identifier identifier : args) {
                if (identifier.id.startsWith("v")) writer.addString("M[SP + 8 * "+(k++)+"] := "+identifier.id+";\n");
            }
        }


        writer.addString("SP := SP - 8 * "+4+";\n");


        temp = lines[7].replace("<ID>", "").replace("</ID>", "").trim();

        writer.addString("M[SP + 8] := ");

        handleAtomics(temp);

        writer.addString(";\nM[SP + 16] := ");


        temp = lines[9].replace("<ID>", "").replace("</ID>", "").trim();

        handleAtomics(temp);

        writer.addString(";\nM[SP + 24] := ");

        temp = lines[11].replace("<ID>", "").replace("</ID>", "").trim();

        handleAtomics(temp);

        String label = newLabel();
        writer.addString(";\nM[SP] := "+label+";\n");
        writer.addString("GOTO "+name+";\n");
        writer.addString("LABEL "+label+";\n");
        if (place != null) {
            writer.addString(place+" := M[SP + 8];\n");
        }
        writer.addString("SP := SP + 8 * "+4+";\n");

        if (!currentScope.equals("global")){
            int k = 0;
            for (Identifier identifier : args) {
                if (identifier.id.startsWith("p")) writer.addString(identifier.id+" := M[SP + 8 * "+(k+++"];\n"));
            }
        }

        scopes.removeFirst();

        
    }

    private void handleAtomics(String id) throws IOException {
        String[] lines = xpath.evaluate("//UNID[text()='"+id+"']/..");

        String temp = lines[5].replace("<ID>", "").replace("</ID>", "").trim();

        id = temp;

        lines = xpath.evaluate("//UNID[text()='"+temp+"']/..");

        temp = lines[3].replace("<SYMB>", "").replace("</SYMB>", "").trim();

        if (temp.equals("VNAME")) {
            temp = handleNames(id);
            writer.addString(temp);
        }
        else {
            temp = handleConstants(id);
            writer.addString(temp);
        }

    }

    private void handleAtomics(String id, String place) throws IOException {
        String[] lines = xpath.evaluate("//UNID[text()='"+id+"']/..");

        String temp = lines[5].replace("<ID>", "").replace("</ID>", "").trim();

        id = temp;

        lines = xpath.evaluate("//UNID[text()='"+temp+"']/..");

        temp = lines[3].replace("<SYMB>", "").replace("</SYMB>", "").trim();

        if (temp.equals("VNAME")) {
            temp = handleNames(id);
            writer.addString(place+" := "+temp);
            writer.addString("\n");
        }
        else {
            temp = handleConstants(id);
            writer.addString(place+" := "+temp);
            writer.addString("\n");
        }

    }

    private String handleConstants(String id){
        String[] lines = xpath.evaluate("//UNID[text()='"+id+"']/..");

        String temp = lines[5].replace("<ID>", "").replace("</ID>", "").trim();

        temp = getToken(temp);
        
        return temp;

    }


    private void handleAssignments(String id) throws IOException {
        String[] lines = xpath.evaluate("//UNID[text()='"+id+"']/..");

        String id1 = lines[5].replace("<ID>", "").replace("</ID>", "").trim();

        String temp = handleNames(id1);

        String id2 = lines[6].replace("<ID>", "").replace("</ID>", "").trim();

        String temp2 = getToken(id2);

        if (temp2.equals("=")){
            id2 = lines[7].replace("<ID>", "").replace("</ID>", "").trim();
            String place = newVar();
            handleTerms(id2, place);
            writer.addString(temp+" := " + place+";\n");
        }
        else {
            writer.addString("INPUT "+temp+";\n");
        }
   
    }

    private void handleTerms(String id, String place) throws IOException {
        String[] lines = xpath.evaluate("//UNID[text()='"+id+"']/..");

        String temp = lines[5].replace("<ID>", "").replace("</ID>", "").trim();

        id = temp;

        lines = xpath.evaluate("//UNID[text()='"+temp+"']/..");

        temp = lines[3].replace("<SYMB>", "").replace("</SYMB>", "").trim();

        if (temp.equals("ATOMIC")) {
            handleAtomics(id, place);
        }
        else if (temp.equals("CALL")) {
            handleCalls(id, place);
        }
        else {
            handleOps(id, place);
        }

    }

    private void handleOps(String id, String place) throws IOException {
        String[] lines = xpath.evaluate("//UNID[text()='"+id+"']/..");

        String[] temp2 = lines;

        String temp = lines[5].replace("<ID>", "").replace("</ID>", "").trim();

        id = temp;

        lines = xpath.evaluate("//UNID[text()='"+temp+"']/..");

        temp = lines[3].replace("<SYMB>", "").replace("</SYMB>", "").trim();

        if (temp.equals("BINOP")){
            String op = handleBinops(id);
            lines = temp2;

            String place1 = newVar();
            temp = lines[7].replace("<ID>", "").replace("</ID>", "").trim();
            handleArgs(temp, place1);

            String place2 = newVar();
            temp = lines[9].replace("<ID>", "").replace("</ID>", "").trim();
            handleArgs(temp, place2);

            writer.addString(place+" := "+place1+" "+op+" "+place2+"\n");

        }
        else{
            String op = handleUnops(id);
            lines = temp2;

            String place1 = newVar();

            temp = lines[7].replace("<ID>", "").replace("</ID>", "").trim();
            handleArgs(temp, place1);

            writer.addString(place+" := "+op+" "+place1+"\n");
        }

    }

    private String newVar(){
        return "T"+counter.getAndIncrement();
    }

    private String handleBinops(String id){
        String[] lines = xpath.evaluate("//UNID[text()='"+id+"']/..");

        String temp = lines[5].replace("<ID>", "").replace("</ID>", "").trim();

        temp = getToken(temp);
        if (temp.equals("eq")) {
            temp = "=";
        }
        else if (temp.equals("grt")) {
            temp = ">";
        }
        else if (temp.equals("add")) {
            temp = "+";
        }
        else if (temp.equals("sub")) {
            temp = "-";
        }
        else if (temp.equals("mul")) {
            temp = "*";
        }
        else if (temp.equals("div")) {
            temp = "/";
        }
        else if (temp.equals("and")) {
            temp = "&&";
        }
        else if (temp.equals("or")) {
            temp = "||";
        }
     
        else throw new IllegalArgumentException("Invalid operator: " + temp);
        return temp;
    }

    private String handleUnops(String id){
        String[] lines = xpath.evaluate("//UNID[text()='"+id+"']/..");

        String temp = lines[5].replace("<ID>", "").replace("</ID>", "").trim();

        temp = getToken(temp);

        if (temp.equals("sqrt")) {
            temp = "SQR";
        }
        else if (temp.equals("not")) {
            temp = "!";
        }
        return temp;
    }

    private void handleArgs(String id, String place) throws IOException {
        String[] lines = xpath.evaluate("//UNID[text()='"+id+"']/..");

        String temp = lines[5].replace("<ID>", "").replace("</ID>", "").trim();

        id = temp;

        lines = xpath.evaluate("//UNID[text()='"+temp+"']/..");

        temp = lines[3].replace("<SYMB>", "").replace("</SYMB>", "").trim();

        if (temp.equals("ATOMIC")) {
            handleAtomics(id, place);
        }
        else {
            handleOps(id, place);
        }
        

    }

   
}

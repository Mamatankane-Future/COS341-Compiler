import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

class BufferedFileWriter implements AutoCloseable {
    private static final int MAX_BUFFER_LENGTH = 1024; 
    private StringBuffer stringBuffer;
    private BufferedWriter writer;
    private String filename;

    public BufferedFileWriter(String filename) throws IOException {
        this.filename = "out/"+filename+ ".txt";
        this.stringBuffer = new StringBuffer();
        this.writer = new BufferedWriter(new FileWriter(filename, true)); 
    }

    public void addString(String text) throws IOException {
        stringBuffer.append(text).append("\n");

        if (stringBuffer.length() >= MAX_BUFFER_LENGTH) {
            writeToFile();
        }
    }

    private void writeToFile() throws IOException {
        writer.write(stringBuffer.toString());
        stringBuffer.setLength(0); 
    }

    public void flush() throws IOException {
        if (stringBuffer.length() > 0) {
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

    public IntermediateCodeGenerator(String filename, String outputFilename) {
        xpath = new XPATH(filename);
        try {
            writer = new BufferedFileWriter(outputFilename);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void analyse() {
        try {
            handleS();
            System.out.println("Intermediate code generated successfully");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    

    private void handleS() throws IOException{
        String temp = xpath.evaluate("//ROOT/CHILDREN/ID/text()")[0];
        handleProg(temp);
    }

    private void handleProg(String id) throws IOException{

        String[] lines = xpath.evaluate("//UNID[text()='"+id+"']/..");

        String temp = lines[7].replace("<ID>", "").replace("</ID>", "").trim();

        handleAlgos(temp);

        writer.addString(" STOP ");

        temp = lines[8].replace("<ID>", "").replace("</ID>", "").trim();

        handleFuncs(temp);

    }

    private void handleAlgos(String id) throws IOException {
        String[] lines = xpath.evaluate("//UNID[text()='"+id+"']/..");

        String temp = lines[6].replace("<ID>", "").replace("</ID>", "").trim();

        handleInstructions(temp);

    }

    private void handleInstructions(String id) throws IOException {
        String[] lines = xpath.evaluate("//UNID[text()='"+id+"']/..");

        if (lines[0].trim().equals( "<LEAF>")) {
            writer.addString(" REM END ");
            return;
        }

        String temp = lines[5].replace("<ID>", "").replace("</ID>", "").trim();

        handleCommands(temp);

        temp = lines[6].replace("<ID>", "").replace("</ID>", "").trim();

        getToken(temp);

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

        String temp = lines[5].replace("<ID>", "").replace("</ID>", "").trim();

        getToken(temp);

        temp = lines[6].replace("<ID>", "").replace("</ID>", "").trim();

        handleConditions(temp);

        temp = lines[7].replace("<ID>", "").replace("</ID>", "").trim();

        getToken(temp);

        temp = lines[8].replace("<ID>", "").replace("</ID>", "").trim();

        handleAlgos(temp);

        temp = lines[9].replace("<ID>", "").replace("</ID>", "").trim();

        getToken(temp);

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

        String temp = lines[5].replace("<ID>", "").replace("</ID>", "").trim();

        handleBinops(temp);

        temp = lines[6].replace("<ID>", "").replace("</ID>", "").trim();

        getToken(temp);


        temp = lines[7].replace("<ID>", "").replace("</ID>", "").trim();

        handleAtomics(temp);

        temp = lines[8].replace("<ID>", "").replace("</ID>", "").trim();

        getToken(temp);

        temp = lines[9].replace("<ID>", "").replace("</ID>", "").trim();

        handleAtomics(temp);

        temp = lines[10].replace("<ID>", "").replace("</ID>", "").trim();

        getToken(temp);

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


    private int handleLeafs(String id){
        String[] lines = xpath.evaluate("//UNID[text()='"+id+"']/..");

        if (lines[0].trim().equals( "<LEAF>")) {
            getToken(id);
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


    private String handleDatatypes(String id) {
        String[] lines = xpath.evaluate("//UNID[text()='"+id+"']/..");
        String temp = lines[5].replace("<ID>", "").replace("</ID>", "").trim();
         
        return getToken(temp);  

    }

    private String getToken(String id) {
        String[] lines = xpath.evaluate("//UNID[text()='"+id+"']/..");
        String temp = lines[4].replace("<TOKEN>", "").replace("</TOKEN>", "").trim();

        sb.append(temp+" ");

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


    private void handleCalls(String id) {
        String[] lines = xpath.evaluate("//UNID[text()='"+id+"']/..");

        String temp = lines[5].replace("<ID>", "").replace("</ID>", "").trim();

        handleNames(temp);


        temp = lines[6].replace("<ID>", "").replace("</ID>", "").trim();

        getToken(temp);


        temp = lines[7].replace("<ID>", "").replace("</ID>", "").trim();

        handleAtomics(temp);

        temp = lines[8].replace("<ID>", "").replace("</ID>", "").trim();

        getToken(temp);

        temp = lines[9].replace("<ID>", "").replace("</ID>", "").trim();

        handleAtomics(temp);

        temp = lines[10].replace("<ID>", "").replace("</ID>", "").trim();

        getToken(temp);

        temp = lines[11].replace("<ID>", "").replace("</ID>", "").trim();

        handleAtomics(temp);

        temp = lines[12].replace("<ID>", "").replace("</ID>", "").trim();

        getToken(temp);

        
    }

    private void handleAtomics(String id) {
        String[] lines = xpath.evaluate("//UNID[text()='"+id+"']/..");

        String temp = lines[5].replace("<ID>", "").replace("</ID>", "").trim();

        id = temp;

        lines = xpath.evaluate("//UNID[text()='"+temp+"']/..");

        temp = lines[3].replace("<SYMB>", "").replace("</SYMB>", "").trim();

        if (temp.equals("VNAME")) {
            temp = handleNames(id);
        }
        else {
            temp = handleConstants(id);
        }

    }

    private String handleConstants(String id) {
        String[] lines = xpath.evaluate("//UNID[text()='"+id+"']/..");

        String temp = lines[5].replace("<ID>", "").replace("</ID>", "").trim();

        return getToken(temp);

    }


    private void handleAssignments(String id) {
        String[] lines = xpath.evaluate("//UNID[text()='"+id+"']/..");

        String temp = lines[5].replace("<ID>", "").replace("</ID>", "").trim();

        handleNames(temp);

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
            handleArgs(temp);

            temp = lines[8].replace("<ID>", "").replace("</ID>", "").trim();
            getToken(temp);

            temp = lines[9].replace("<ID>", "").replace("</ID>", "").trim();
            handleArgs(temp);

            temp = lines[10].replace("<ID>", "").replace("</ID>", "").trim();
            getToken(temp);


        }
        else{
            handleUnops(id);
            lines = temp2;
            temp = lines[6].replace("<ID>", "").replace("</ID>", "").trim();
            getToken(temp);

            temp = lines[7].replace("<ID>", "").replace("</ID>", "").trim();
            handleArgs(temp);

            temp = lines[8].replace("<ID>", "").replace("</ID>", "").trim();
            getToken(temp);
        }

    }

    private void handleBinops(String id){
        String[] lines = xpath.evaluate("//UNID[text()='"+id+"']/..");

        String temp = lines[5].replace("<ID>", "").replace("</ID>", "").trim();

        getToken(temp);
    }

    private void handleUnops(String id){
        String[] lines = xpath.evaluate("//UNID[text()='"+id+"']/..");

        String temp = lines[5].replace("<ID>", "").replace("</ID>", "").trim();

        getToken(temp);
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

   
}

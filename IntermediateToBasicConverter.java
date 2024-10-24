import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import java.util.concurrent.atomic.AtomicReference;

class AtomicCharacter {
    private final AtomicReference<Character> atomicChar;

    public AtomicCharacter(char initialValue) {
        atomicChar = new AtomicReference<>(initialValue);
    }

    public Character get() {
        return atomicChar.get();
    }

    public Character getAndIncrement() {
        return atomicChar.getAndUpdate(this::incrementChar);
    }

    public Character incrementAndGet() {
        return atomicChar.updateAndGet(this::incrementChar);
    }

    private Character incrementChar(Character current) {
        if (current == null){
            throw new RuntimeException("Too many string variables");
        }
        if (current == 'Z') {
            return null; // Return null instead of wrapping around
        }
        return (char) (current + 1);
    }

    public void set(char newValue) {
        atomicChar.set(newValue);
    }

    public Character getAndSet(char newValue) {
        return atomicChar.getAndSet(newValue);
    }
}


public class IntermediateToBasicConverter {

    ArrayList<String> stringList = new ArrayList<>();
    HashMap<String, String> lineNumbers = new HashMap<>();
    HashMap<String, Character> strings = new HashMap<>();
    AtomicCharacter atomicChar = new AtomicCharacter('A');
    

    // Converts an intermediate line to BASIC code
    private String convertLine(String intermediateLine, AtomicInteger i) {

        String basicCode = "";

        if (intermediateLine.startsWith("PRINT")){
            String[] parts = intermediateLine.split(" ");
            if (!stringList.contains(parts[1])) basicCode = intermediateLine;
            else basicCode = parts[0] + " " + strings.get(parts[1]) + "$";
        }
        else if (intermediateLine.matches("([a-zA-Z][a-zA-Z0-9]*) := \"([a-zA-Z0-9]*)\"")){
            String[] parts = intermediateLine.split(" := ");
            Character c = atomicChar.getAndIncrement();
            basicCode = "LET " + c + "$ = " + parts[1];
            strings.put(parts[0], c);
            stringList.add(parts[0]);
        }
        else if (intermediateLine.matches("([a-zA-Z][a-zA-Z0-9]*) := ([0-9]+\\.[0-9]+)")) {
            String[] parts = intermediateLine.split(" := ");
            basicCode = "LET " + parts[0] + " = " + parts[1];
        } 
        else if (intermediateLine.matches("([a-zA-Z][a-zA-Z0-9]*) := ([a-zA-Z0-9]+)")) {
            String[] parts = intermediateLine.split(" := ");
            if (!stringList.contains(parts[1])) basicCode = "LET " + parts[0] + " = " + parts[1];
            else basicCode = "LET " + parts[0] + "$ = " + parts[1] + "$";
        }
        else if (intermediateLine.matches("([a-zA-Z0-9]+)\\s*:=\\s*([a-zA-Z0-9]+)\\s*([\\+\\-\\*\\/\\&\\|]{1,2})\\s*([a-zA-Z0-9]+)")) {
            String[] parts = intermediateLine.split(" := ");
            basicCode = parts[0] + " = " + parts[1];
        }
        else if (intermediateLine.matches("([a-zA-Z0-9]+)\\s*:=\\s*SQR\\s*([a-zA-Z0-9]+)")) {
            String[] parts = intermediateLine.split(" := ");
            String[] part1 = parts[1].split(" ");
            basicCode = parts[0] +" = SQR(" +part1[1]+")";
        }
        else if (intermediateLine.matches("([a-zA-Z0-9]+)\\s*:=\\s*NOT\\s*([a-zA-Z0-9]+)")){
            String[] parts = intermediateLine.split(" := ");
            String[] part1 = parts[1].split(" ");
            basicCode = parts[0] +" = SQR(" +part1[1]+")";
        }
        else if (intermediateLine.startsWith("STOP")){
            basicCode = "END";
        }
        else if (intermediateLine.startsWith("REM")){
            basicCode = intermediateLine;
        }
        else if (intermediateLine.matches("IF\\s*!\\s*([a-zA-Z0-9]+)\\s*(=|>)\\s*([a-zA-Z0-9]+)\\s*THEN\\s*GOTO\\s*([a-zA-Z0-9]+)\\s*ELSE\\s*GOTO\\s*([a-zA-Z0-9]+)")) {
            Matcher matcher = Pattern.compile("IF\\s*!\\s*([a-zA-Z0-9]+)\\s*(=|>)\\s*([a-zA-Z0-9]+)\\s*THEN\\s*GOTO\\s*([a-zA-Z0-9]+)\\s*ELSE\\s*GOTO\\s*([a-zA-Z0-9]+)").matcher(intermediateLine);
            if (matcher.find()) {
                String conditionVar = matcher.group(1);
                String operator = matcher.group(2);
                String value = matcher.group(3);
                String thenLabel = matcher.group(4);
                String elseLabel = matcher.group(5);
                
                basicCode = "IF NOT " + conditionVar + " " + operator + " " + value + " THEN GOTO " + thenLabel + " ELSE GOTO " + elseLabel + " FI";
                
            }
        }
        else if (intermediateLine.matches("IF\\s*([-]?[a-zA-Z0-9]+)\\s*(=|>)\\s*([-]?[a-zA-Z0-9]+)\\s*THEN\\s*GOTO\\s*([a-zA-Z0-9]+)\\s*ELSE\\s*GOTO\\s*([a-zA-Z0-9]+)")) {
            Matcher matcher = Pattern.compile("IF\\s*([-]?[a-zA-Z0-9]+)\\s*(=|>)\\s*([-]?[a-zA-Z0-9]+)\\s*THEN\\s*GOTO\\s*([a-zA-Z0-9]+)\\s*ELSE\\s*GOTO\\s*([a-zA-Z0-9]+)").matcher(intermediateLine);
            if (matcher.find()) {
                String conditionVar = matcher.group(1);
                String operator = matcher.group(2);
                String value = matcher.group(3);
                String thenLabel = matcher.group(4);
                String elseLabel = matcher.group(5);
                
                basicCode = "IF " + conditionVar + " " + operator + " " + value + " THEN GOTO " + thenLabel + " ELSE GOTO " + elseLabel + " FI";
            }
        }        
        else if (intermediateLine.matches("GOTO ([a-zA-Z0-9]+)")) {
            String[] parts = intermediateLine.split(" ");
            if (!parts[1].startsWith("f")) basicCode = "GOTO "+parts[1]+" ";
            else basicCode = "GOSUB "+parts[1]+" ";
        }
        else if (intermediateLine.matches("LABEL ([a-zA-Z0-9]+)")){
            basicCode = "REM "+intermediateLine;
            String[] parts = intermediateLine.split(" ");

            lineNumbers.put(" "+parts[1]+" ", " "+String.valueOf(i.get()) + " ");

            if (parts[1].startsWith("f")) {
                basicCode = i.get()+" REM "+intermediateLine;
                i.set(i.get()+10);
                basicCode+= "\n"+i.get()+" f = f + 1";
            }
        }
        else if (intermediateLine.matches("INPUT ([a-zA-Z0-9]+)")){
            String[] parts = intermediateLine.split(" ");
            basicCode = parts[0] + " \"Please enter number: \" " + parts[1];

        }
        else if (intermediateLine.equals("GOTO M[SP]")) {
            basicCode = i.get()+" f = f - 1\n";
            i.set(i.get()+10);
            basicCode+=i.get()+" RETURN";
        }
        else {
            basicCode = storingPars(intermediateLine);
        }

        return basicCode;
    }

    private String storingPars(String intermediateLine){
        if (intermediateLine.startsWith("M[SP + 8]")){
            String[] parts = intermediateLine.split(" := ");

            return "M(1, f + 1) = " + parts[1];
        }
        else if (intermediateLine.startsWith("M[SP + 16]")){
            String[] parts = intermediateLine.split(" := ");
            return "M(2, f + 1) = " + parts[1];
        }
        else if (intermediateLine.startsWith("M[SP + 24]")){
            String[] parts = intermediateLine.split(" := ");
            return "M(3, f + 1) = " + parts[1];
        }
        else {
            return retrievingPars(intermediateLine);
        }
    }

    private String retrievingPars(String intermediateLine){
        if (intermediateLine.endsWith("M[SP + 88]")){
            String[] parts = intermediateLine.split(" := ");
            return parts[0] + " = M(1, f)";
        }
        else if (intermediateLine.endsWith("M[SP + 96]")){
            String[] parts = intermediateLine.split(" := ");
            return parts[0] + " = M(2, f)";
        }
        else if (intermediateLine.endsWith("M[SP + 104]")){
            String[] parts = intermediateLine.split(" := ");
            return parts[0] + " = M(3, f)";
        }
        else {
            return storingLocals(intermediateLine);
        }
    }

    private String storingLocals(String intermediateLine){
        if (intermediateLine.startsWith("M[SP + 8 * 0]")){
            String[] parts = intermediateLine.split(" := ");
            return "M(1, f) = " + parts[1];
        }
        else if (intermediateLine.startsWith("M[SP + 8 * 1]")){
            String[] parts = intermediateLine.split(" := ");
            return "M(2, f) = " + parts[1];
        }
        else if (intermediateLine.startsWith("M[SP + 8 * 2]")){
            String[] parts = intermediateLine.split(" := ");
            return "M(3, f) = " + parts[1];
        }
        else if (intermediateLine.startsWith("M[SP + 8 * 3]")){
            String[] parts = intermediateLine.split(" := ");
            return "M(4, f) = " + parts[1];
        }
        else if (intermediateLine.startsWith("M[SP + 8 * 4]")){
            String[] parts = intermediateLine.split(" := ");
            return "M(5, f) = " + parts[1];
        }
        else if (intermediateLine.startsWith("M[SP + 8 * 5]")){
            String[] parts = intermediateLine.split(" := ");
            return "M(6, f) = " + parts[1];
        }
        else if (intermediateLine.startsWith("M[SP + 8 * 6]")){
            String[] parts = intermediateLine.split(" := ");
            return "M(7, f) = " + parts[1];
        }
        else {
            return retrievingLocals(intermediateLine);
        }
    }

    private String retrievingLocals(String intermediateLine){
        if (intermediateLine.endsWith("M[SP + 8 * 0]")){
            String[] parts = intermediateLine.split(" := ");
            return parts[0] + " = M(1, f)";
        }
        else if (intermediateLine.endsWith("M[SP + 8 * 1]")){
            String[] parts = intermediateLine.split(" := ");
            return parts[0] + " = M(2, f)";
        }
        else if (intermediateLine.endsWith("M[SP + 8 * 2]")){
            String[] parts = intermediateLine.split(" := ");
            return parts[0] + " = M(3, f)";
        }
        else if (intermediateLine.endsWith("M[SP + 8 * 3]")){
            String[] parts = intermediateLine.split(" := ");
            return parts[0] + " = M(4, f)";
        }
        else if (intermediateLine.endsWith("M[SP + 8 * 4]")){
            String[] parts = intermediateLine.split(" := ");
            return parts[0] + " = M(5, f)";
        }
        else if (intermediateLine.endsWith("M[SP + 8 * 5]")){
            String[] parts = intermediateLine.split(" := ");
            return parts[0] + " = M(6, f)";
        }
        else if (intermediateLine.endsWith("M[SP + 8 * 6]")){
            String[] parts = intermediateLine.split(" := ");
            return parts[0] + " = M(7, f)";
        }
        else {
            return storingResult(intermediateLine);
        }
    }

    private String storingResult(String intermediateLine){
        if (intermediateLine.startsWith("M[SP + 88]")){
            String[] parts = intermediateLine.split(" := ");
            return "M(0, f - 1) = " + parts[1];
        }
        else {
            return retrievingResult(intermediateLine);
        }
       
    }

    private String retrievingResult(String intermediateLine){
        if (intermediateLine.endsWith("M[SP + 8]")){
            String[] parts = intermediateLine.split(" := ");
            return parts[0] + " = M(0, f)";
        }
        else {
            return "";
        }
    }

    // Reads from input file and converts to BASIC code in output file
    private void convertFile(String inputFilePath, String outputFilePath) {

        try (BufferedReader reader = new BufferedReader(new FileReader(inputFilePath));
             BufferedWriter writer = new BufferedWriter(new FileWriter(outputFilePath))) {
            String line;
            AtomicInteger i = new AtomicInteger(30);

            writer.write("10 DIM M(7, 100)\n20 f = 0");
            writer.newLine();
            while ((line = reader.readLine()) != null) {
                line = line.replace(";", "");
                int prev = i.get();
                String basicCode = convertLine(line, i);
                if (!basicCode.isEmpty()) {
                    if (prev == i.get()) writer.write(i.get() + " " + basicCode);
                    else writer.write(basicCode);
                    i.set(i.get()+10);
                    writer.newLine();
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        try {

            File file = new File(outputFilePath);
            BufferedReader fileReader = new BufferedReader(new FileReader(file));
            StringBuilder contentBuilder = new StringBuilder();
            String line;

            while ((line = fileReader.readLine()) != null) {
                if (line.contains("GOSUB") || line.contains("GOTO")) {
                    for (Map.Entry<String, String> entry : lineNumbers.entrySet()) {
                        String key = entry.getKey();
                        String value = entry.getValue();
                        line = line.replace(key, value);
                    }
                }
                contentBuilder.append(line).append("\n");
            }
            fileReader.close();

            BufferedWriter fileWriter = new BufferedWriter(new FileWriter(file));
            fileWriter.write(contentBuilder.toString());
            fileWriter.close();

        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public void convert(String outfile, String infile) {
        IntermediateToBasicConverter converter = new IntermediateToBasicConverter();
        

        String inputFilePath = infile + ".txt";
        String outputFilePath = outfile + ".bas";
        
    
        converter.convertFile(inputFilePath, outputFilePath);
        System.out.println("Program compiled successfully. Output saved in " + outputFilePath);
    }
}

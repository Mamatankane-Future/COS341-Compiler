import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class IntermediateToBasicConverter {

    ArrayList<String> stringList = new ArrayList<>();
    HashMap<String, String> lineNumbers = new HashMap<>();

    public void populateLineNumbers(String intermediateLine, int i){
        if (intermediateLine.matches("LABEL ([a-zA-Z0-9]+)")){
            String[] parts = intermediateLine.split(" ");
            lineNumbers.put(parts[1], String.valueOf(i));
        }
    }

    // Converts an intermediate line to BASIC code
    public String convertLine(String intermediateLine) {

        String basicCode = "";

        if (intermediateLine.matches("([a-zA-Z][a-zA-Z0-9]*) := \"([a-zA-Z0-9]*)\"")){
            String[] parts = intermediateLine.split(" := ");
            basicCode = "LET " + parts[0] + "$ = " + parts[1];
            stringList.add(parts[0]);
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
        else if (intermediateLine.startsWith("PRINT")){
            String[] parts = intermediateLine.split(" ");
            if (!stringList.contains(parts[1])) basicCode = intermediateLine;
            else basicCode = "LET " + parts[0] + "$ = " + parts[1] + "$;";
        }
        else if (intermediateLine.startsWith("STOP")){
            basicCode = intermediateLine;
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
                
                basicCode = "IF NOT " + conditionVar + " " + operator + " " + value + " THEN GOTO " + thenLabel + " ELSE GOTO " + elseLabel;
                
            }
        }
        else if (intermediateLine.matches("IF\\s*([a-zA-Z0-9]+)\\s*(=|>)\\s*([a-zA-Z0-9]+)\\s*THEN\\s*GOTO\\s*([a-zA-Z0-9]+)\\s*ELSE\\s*GOTO\\s*([a-zA-Z0-9]+)")) {
            // Extract variables from the match
            Matcher matcher = Pattern.compile("IF\\s*([a-zA-Z0-9]+)\\s*(=|>)\\s*([a-zA-Z0-9]+)\\s*THEN\\s*GOTO\\s*([a-zA-Z0-9]+)\\s*ELSE\\s*GOTO\\s*([a-zA-Z0-9]+)").matcher(intermediateLine);
            if (matcher.find()) {
                String conditionVar = matcher.group(1);
                String operator = matcher.group(2);
                String value = matcher.group(3);
                String thenLabel = matcher.group(4);
                String elseLabel = matcher.group(5);
                
                // Convert to BASIC code
                basicCode = "IF " + conditionVar + " " + operator + " " + value + " THEN GOTO " + thenLabel + " ELSE GOTO " + elseLabel;
            }
        }        
        else if (intermediateLine.matches("GOTO ([a-zA-Z0-9]+)")) {
            String[] parts = intermediateLine.split(" ");
            basicCode = "GOTO "+lineNumbers.get(parts[1]);
        }
        else if (intermediateLine.matches("LABEL ([a-zA-Z0-9]+)")){
            basicCode = "REM "+intermediateLine;
        }
        else if (intermediateLine.matches("INPUT ([a-zA-Z0-9]+)")){
            String[] parts = intermediateLine.split(" ");
            basicCode = parts[0] + " \"Please enter number: \", " + parts[1];

        }
        else if (intermediateLine.matches("IF ([a-zA-Z0-9]+) THEN ([a-zA-Z0-9]+) ELSE ([a-zA-Z0-9]+)")) {
            String[] parts = intermediateLine.split(" ");
            String condition = parts[1];
            String thenLabel = parts[3];
            String elseLabel = parts[5];
            basicCode = "IF " + condition + " THEN GOTO " + thenLabel + "\n" + "REM ELSE GOTO " + elseLabel;
        }
        else if (intermediateLine.matches("MSP\\[\\] = (.*) GOTO ([a-zA-Z0-9]+)")) {
            String[] parts = intermediateLine.split(" ");
            String returnAddress = parts[2];
            String functionName = parts[4];
            basicCode = "GOSUB " + functionName + "\nREM " + returnAddress;
        }
        else if (intermediateLine.equals("GOTO MSP[]")) {
            basicCode = "RETURN";
        } else {
            // If no pattern matched, log it
            System.out.println("No matching pattern found for: " + intermediateLine);
        }

        return basicCode;
    }

    // Reads from input file and converts to BASIC code in output file
    public void convertFile(String inputFilePath, String outputFilePath) {
        try (BufferedReader reader = new BufferedReader(new FileReader(inputFilePath))) {
            String line;
            int i = 10;

            while ((line = reader.readLine()) != null) {
                line = line.replace(";", "");
                populateLineNumbers(line, i);
                i+=10;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        try (BufferedReader reader = new BufferedReader(new FileReader(inputFilePath));
             BufferedWriter writer = new BufferedWriter(new FileWriter(outputFilePath))) {
            String line;
            int i = 10;

            // Second pass through the file
            while ((line = reader.readLine()) != null) {
                line = line.replace(";", "");
                String basicCode = convertLine(line);
                if (!basicCode.isEmpty()) {
                    writer.write(i + " " + basicCode);
                    i += 10;
                    writer.newLine();
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public static void main(String[] args) {
        IntermediateToBasicConverter converter = new IntermediateToBasicConverter();
        
        // Input and output file paths
        String inputFilePath = "out/output.txt";
        String outputFilePath = "out/output.bas";
        
        // Convert the file
        converter.convertFile(inputFilePath, outputFilePath);
        System.out.println("Conversion completed. Output saved in " + outputFilePath);
    }
}

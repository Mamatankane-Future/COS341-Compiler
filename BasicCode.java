import java.io.BufferedWriter;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class BasicCode {

    public BasicCode(String outfile) {
        String inputFilePath = "out/"+outfile+".txt";  
        String outputFilePath = "out/"+outfile+".sab";
        
        try {
            BufferedReader reader = new BufferedReader(new FileReader(inputFilePath));
            BufferedWriter writer = new BufferedWriter(new FileWriter(outputFilePath));
            
            String line;
            int lineNumber = 10; 
            while ((line = reader.readLine()) != null) {
                line = line.replace(";", "");
                String temp = convertAssignments(line);
                if (temp == null){
                    temp = convertExpressions(line);
                }
                String formattedLine = lineNumber +" "+temp;
                writer.write(formattedLine + "\n");
                lineNumber += 10;
            }
            
            reader.close();
            writer.close();
            
            System.out.println("Generated .sab file: " + outputFilePath);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private String convertAssignments(String input) {
        String pattern = "([A-Za-z][A-Za-z0-9_]*)\\s*:=\\s*(\\d+|\".*\")";
        Pattern r = Pattern.compile(pattern);
        
        Matcher m = r.matcher(input);
        
        if (m.find()) {
            String variable = m.group(1);  
            String value = m.group(2);     
            
            if (value.startsWith("\"")) {
                return "LET " + variable.charAt(0) + "$ = " + value;
            } else {
                return "LET " + variable + " = " + value;
            }
        } else {
            return null;
        }
    }

    private static String convertExpressions(String input) {
        String pattern = "([A-Za-z][A-Za-z0-9_]*)\\s*:=\\s*([A-Za-z][A-Za-z0-9_]*)\\s*([+\\-*/]||[&]{2}|[|]{2})\\s*([A-Za-z][A-Za-z0-9_]*)";
        Pattern r = Pattern.compile(pattern);
        
        Matcher m = r.matcher(input);
        
        if (m.find()) {
            String variable = m.group(1);
            String operand1 = m.group(2);
            String operator = m.group(3);
            String operand2 = m.group(4);
            return "LET " + variable + " = " + operand1 + " " + operator + " " + operand2;
        } else {
            return "No match found.";
        }
    }
    
}

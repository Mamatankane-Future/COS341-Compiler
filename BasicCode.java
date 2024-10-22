import java.io.BufferedWriter;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

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
                String formattedLine = lineNumber +" "+line;
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

    // // Convert the intermediate code line into a BASIC line
    // private static String processIntermediateCode(String line, int lineNumber) {
    //     // Basic mapping logic (you can expand this as per your specific rules)
    //     if (line.contains("PRINT")) {
    //         return lineNumber + " " + line;
    //     } else if (line.contains("GOTO")) {
    //         return lineNumber + " " + line;
    //     } else if (line.contains("LABEL")) {
    //         return lineNumber + " " + line.replace("LABEL", ""); // Convert LABELs
    //     } else if (line.contains("STOP")) {
    //         return lineNumber + " " + line;
    //     } else if (line.contains(":=")) {
    //         return lineNumber + " LET " + line.replace(":=", "="); // Convert assignments
    //     }
    //     // Default case: return as-is
    //     return lineNumber + " " + line;
    // }
    
}

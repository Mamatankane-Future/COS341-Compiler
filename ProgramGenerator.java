import java.io.FileWriter;
import java.io.IOException;
import java.util.Random;

public class ProgramGenerator {
    private static Random random = new Random();

    public static void main(String[] args) {
        for (int i = 3; i < 10; i++) {
            String generatedProgram = generateProgram();
            saveToFile(generatedProgram, "in/test" + i + ".txt");
        }
        System.out.println("Program generated and saved to in/test1.txt");
    }

    private static String generateProgram() {
        return "main " + generateGlobalVars() + generateAlgo() + generateFunctions();
    }

    private static String generateGlobalVars() {
        if (random.nextBoolean()) {
            return ""; // Empty global vars
        } else {
            return generateVtyp() + " " + generateVname() + " , " + generateGlobalVars();
        }
    }

    // Generate variable type (num or text)
    private static String generateVtyp() {
        return random.nextBoolean() ? "num" : "text";
    }

    // Generate a variable name like V_a1, V_apple, V_b9834
    private static String generateVname() {
        String randomWord = generateRandomWord(); // Generate random word-like string
        int randomNumber = random.nextInt(10000); // Generates a random number (0-9999)
        return "V_" + randomWord + randomNumber;
    }

    
    private static String generateRandomWord() {
        int wordLength = random.nextInt(10) + 1; // Random word length between 1 and 6
        StringBuilder word = new StringBuilder();
    
        // First character must be a letter (a-z)
        char firstChar = (char) (random.nextInt(26) + 'a');
        word.append(firstChar);
    
        // Remaining characters can be either a letter (a-z) or a digit (0-9)
        for (int i = 1; i < wordLength; i++) {
            if (random.nextBoolean()) {
                // Add a random letter (a-z)
                char randomChar = (char) (random.nextInt(26) + 'a');
                word.append(randomChar);
            } else {
                // Add a random digit (0-9)
                char randomDigit = (char) (random.nextInt(10) + '0');
                word.append(randomDigit);
            }
        }
    
        return word.toString();
    }
    

    // Generate algorithm (begin INSTRUC end)
    private static String generateAlgo() {
        return "begin " + generateInstruc() + "end";
    }

    // Generate instructions (COMMAND ; INSTRUC)
    private static String generateInstruc() {
        if (random.nextBoolean()) {
            return ""; // Empty instructions
        } else {
            return generateCommand() + " ; " + generateInstruc();
        }
    }

    // Generate commands
    private static String generateCommand() {
        switch (random.nextInt(6)) {
            case 0:
                return "skip";
            case 1:
                return "halt";
            case 2:
                return "print " + generateAtomic();
            case 3:
                return "return " + generateAtomic();
            case 4:
                return generateAssign();
            case 5:
                return generateCall();
            default:
                return generateBranch();
        }
    }

    // Generate atomic values (variable name or constant)
    private static String generateAtomic() {
        return random.nextBoolean() ? generateVname() : generateConst();
    }

    private static String generateConst() {
        return random.nextBoolean() ? generateNum() : generateString();
    }

    private static String generateNum() {
        return random.nextBoolean() ? generateInteger().toString() : generateDouble().toString();
    }

    private static Double generateDouble() {
        return random.nextDouble() * 100000000;
    }
    

    private static Integer generateInteger() {
        return random.nextInt();
    }

    private static String generateString() {
        int wordLength = random.nextInt(5) + 1; // Random word length between 1 and 6
        StringBuilder word = new StringBuilder();

        char randomUppercase = (char) (random.nextInt(26) + 'A');
        word.append(randomUppercase);
    
        // Generate a random word consisting of both uppercase and lowercase letters
        for (int i = 0; i < wordLength; i++) {
                // Add a random lowercase letter (a-z)
            char randomLowercase = (char) (random.nextInt(26) + 'a');
            word.append(randomLowercase);
        }
    
        return "\"word.toString()\"";
    }
    


    // Generate assignment (VNAME <input or VNAME = TERM)
    private static String generateAssign() {
        if (random.nextBoolean()) {
            return generateVname() + " <input";
        } else {
            return generateVname() + " = " + generateTerm();
        }
    }

    // Generate function call (FNAME(ATOMIC, ATOMIC, ATOMIC))
    private static String generateCall() {
        return generateFname() + "(" + generateAtomic() + " , " + generateAtomic() + " , " + generateAtomic() + ")";
    }

    // Generate branch (if COND then ALGO else ALGO)
    private static String generateBranch() {
        return "if " + generateCond() + " then " + generateAlgo() + " else " + generateAlgo();
    }

    // Generate condition (SIMPLE or COMPOSIT)
    private static String generateCond() {
        return random.nextBoolean() ? generateSimple() : generateComposit();
    }

    // Generate simple condition (BINOP(ATOMIC, ATOMIC))
    private static String generateSimple() {
        return generateBinop() + "(" + generateAtomic() + " , " + generateAtomic() + ")";
    }

    // Generate composit condition (BINOP(SIMPLE, SIMPLE) or UNOP(SIMPLE))
    private static String generateComposit() {
        if (random.nextBoolean()) {
            return generateBinop() + "(" + generateSimple() + " , " + generateSimple() + ")";
        } else {
            return generateUnop() + "(" + generateSimple() + ")";
        }
    }

    // Generate term (ATOMIC, CALL, OP)
    private static String generateTerm() {
        switch (random.nextInt(3)) {
            case 0:
                return generateAtomic();
            case 1:
                return generateCall();
            default:
                return generateOp();
        }
    }

    // Generate operation (UNOP(ARG) or BINOP(ARG, ARG))
    private static String generateOp() {
        if (random.nextBoolean()) {
            return generateUnop() + "(" + generateArg() + ")";
        } else {
            return generateBinop() + "(" + generateArg() + " , " + generateArg() + ")";
        }
    }

    // Generate argument (ATOMIC or OP)
    private static String generateArg() {
        return random.nextBoolean() ? generateAtomic() : generateOp();
    }

    // Generate unary operator (not, sqrt)
    private static String generateUnop() {
        return random.nextBoolean() ? "not" : "sqrt";
    }

    // Generate binary operator (or, and, eq, grt, add, sub, mul, div)
    private static String generateBinop() {
        String[] binops = {"or", "and", "eq", "grt", "add", "sub", "mul", "div"};
        return binops[random.nextInt(binops.length)];
    }

    // Generate a function name like F_a1, F_banana234, F_z87
    private static String generateFname() {
        String randomWord = generateRandomWord(); // Generate random word-like string
        int randomNumber = random.nextInt(10000); // Generates a random number (0-9999)
        return "F_" + randomWord + randomNumber;
    }


    // Generate functions (can be empty or a function declaration followed by others)
    private static String generateFunctions() {
        if (random.nextBoolean()) {
            return ""; // Empty functions
        } else {
            return generateDecl() + generateFunctions();
        }
    }

    // Generate function declaration (HEADER BODY)
    private static String generateDecl() {
        return generateHeader() + generateBody();
    }

    // Generate function header (FTYP FNAME (VNAME, VNAME, VNAME))
    private static String generateHeader() {
        return generateFtyp() + " " + generateFname() + "(" + generateVname() + " , " + generateVname() + " , " + generateVname() + ")";
    }

    // Generate function type (num or void)
    private static String generateFtyp() {
        return random.nextBoolean() ? "num" : "void";
    }

    // Generate function body (PROLOG LOCVARS ALGO EPILOG SUBFUNCS end)
    private static String generateBody() {
        return "{ " + generateLocvars() + " " + generateAlgo() + " } " + generateSubfuncs() + "end ";
    }

    // Generate local variables (VTYP VNAME, VTYP VNAME, VTYP VNAME)
    private static String generateLocvars() {
        return generateVtyp() + " " + generateVname() + " , " + generateVtyp() + " " + generateVname() + " , " + generateVtyp() + " " + generateVname();
    }

    // Generate sub-functions (can be empty or more functions)
    private static String generateSubfuncs() {
        return generateFunctions();
    }

    // Save the generated program to a file
    private static void saveToFile(String content, String fileName) {
        try (FileWriter fileWriter = new FileWriter(fileName)) {
            fileWriter.write(content);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

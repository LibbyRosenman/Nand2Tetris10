import java.io.File;
import java.io.IOException;
import java.io.FileNotFoundException;

public class JackAnalyzer {
    public static void main(String[] args) {
        // validate the input file
        if (args.length == 0) {
            System.out.println("Error: No command-line arguments provided");
        }
        String inputPath = args[0].trim();
        File inputFile = new File(inputPath);
        if (!inputFile.exists()) {
            System.out.println("Error: file or directory not found");
        }

        try {
            if (inputFile.isDirectory()) {
                // Input is a directory, process all files in the directory into a single file
                File[] files = inputFile.listFiles((dir, name) -> name.toLowerCase().endsWith(".jack"));
                if (files != null) {
                    // Process each VM file and write to the output ASM file
                    for (File jackFile : files) {
                        // translating the vm file to the hack-assembly language
                        processFile(jackFile);
                    }
                } else {
                    System.out.println("Error: Unable to list files in the directory");
                }
            } else if (inputFile.isFile()) {
                // Input is a single file
                processFile(inputFile);
            } else {
                System.out.println("Error: Invalid input");
            }
        } catch (IOException e) {
            System.out.println("ERROR: " + e);
        }
    }

    /**
     * this function translates a jack file to a new xml file
     * it creates a new xml file in the same folder
     * it constructs a parser to handle the input file and a code writer to handle
     * the ouputfile
     * then - iterates through the input file, parsing each line and generating code
     * from it
     * 
     * @param sourceFile
     * @throws IOException
     */
    private static void processFile(File sourceFile) throws IOException {
        // create the output file - same as the original path with .xml suffix
        String sourceAbsolutePath = sourceFile.getAbsolutePath();
        String fileName = sourceFile.getName();
        int fileNameExtensionIndex = fileName.lastIndexOf(".");
        String fileNameNoExtension = fileName.substring(0, fileNameExtensionIndex);
        int fileNameIndex = sourceFile.getAbsolutePath().indexOf(sourceFile.getName());
        String sourceDirectory = sourceAbsolutePath.substring(0, fileNameIndex);
        // version V.0 - create T.xml file
        String outputFilePath = sourceDirectory + fileNameNoExtension + ".xml";
        File outputFile = new File(outputFilePath);

        // version V.0 - calling translatorT
        translator(sourceFile, outputFile);
    }

    /**
     * This method creates a compilation engine and compile the whole file into the
     * xml output file
     * 
     * @param jackFile
     * @param xmlFile
     * @throws FileNotFoundException
     * @throws IOException
     */
    public static void translator(File jackFile, File xmlFile) throws FileNotFoundException, IOException {
        CompilationEngine compile = new CompilationEngine(jackFile, xmlFile);
        compile.compileClass();
        compile.close();
    }

    public static void translatorT(File jackFile, File xmlFile) throws FileNotFoundException, IOException {

        CompilationEngine compile = new CompilationEngine(jackFile, xmlFile);
        compile.translatorT();
        compile.close();
    }

}

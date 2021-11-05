package de.unibremen.informatik.vcs2see;

import lombok.Getter;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;

/**
 * Component to run axivion suite / baushaus on a given path.
 *
 * @author Felix Gaebler
 * @version 1.0.0
 */
public class CodeAnalyser {

    private File directory;

    private Language language;

    private String bauhausPath;

    private String cpfcsv2rfgPath;

    /**
     * Helper method to run a ProcessBuilder and output the output to the console.
     * @param processBuilder ProcessBuilder which should be executed
     * @throws IOException exception
     */
    private void run(ProcessBuilder processBuilder) throws IOException {
        Process process = processBuilder.start();
        BufferedReader input = new BufferedReader(new InputStreamReader(process.getInputStream()));

        String line;
        while ((line = input.readLine()) != null) {
            System.out.println(line);
        }
    }

    /**
     * Run the clone analysis with cpf.
     * @param directory directory in which the command is to be executed
     * @param language programming language of the source code to be analyzed
     * @throws IOException exception
     */
    private void cpf(File directory, Language language) throws IOException {
        List<String> cmd = new ArrayList<>(Arrays.asList(bauhausPath + "\\cpf", "-m", "100", "-c", "clones.cpf", "-s", "clones.csv", "-a"));
        for (String extension : language.getExtensions()) {
            cmd.add("-i");
            cmd.add("\"*." + extension + "\"");
        }
        cmd.add(".");

        ProcessBuilder processBuilder = new ProcessBuilder();
        processBuilder.command(cmd.toArray(String[]::new));
        processBuilder.directory(directory);

        run(processBuilder);
    }

    /**
     * Generate an RFG from the clone information.
     * @param directory directory in which the command is to be executed
     * @throws IOException exception
     */
    private void cpfcsv2rfg(File directory) throws IOException {
        ProcessBuilder processBuilder = new ProcessBuilder();
        processBuilder.command(bauhausPath + "\\rfgscript", cpfcsv2rfgPath + "\\cpfcsv2rfg.py", "clones.cpf", "clones.csv", "clones.rfg");
        processBuilder.directory(directory);

        run(processBuilder);
    }

    /**
     * Export the clone information in the RFG to a GXL file.
     * @param directory directory in which the command is to be executed
     * @param fileName name of the output GLX file
     * @throws IOException
     */
    private void rfgexport(File directory, String fileName, int index) throws IOException {
        // Create directory for output
        File output = new File(directory, fileName);
        output.mkdirs();

        ProcessBuilder processBuilder = new ProcessBuilder();
        processBuilder.command(bauhausPath + "\\rfgexport", "-o", "Clones", "-f", "GXL", "clones.rfg", fileName + "\\" + fileName + "-" + index + ".gxl");
        processBuilder.directory(directory);

        run(processBuilder);
    }

    /**
     *
     * @param directory directory in which the command is to be executed
     * @return number of deleted files
     */
    private long cleanup(File directory) {
        return Stream.of(new File(directory, "clones.cpf").delete(),
                        new File(directory, "clones.csv").delete(),
                        new File(directory, "clones.rfg").delete(),
                        new File(directory, "tokens.files").delete(),
                        new File(directory, "tokens.tok").delete())
                .filter(Boolean::booleanValue)
                .count();
    }

    /**
     * Initialization method for the CodeAnalyser.
     * @param path path to the repository to be analyzed
     * @param language programming language of the repository to be analyzed
     * @throws IOException exception
     */
    public void init(String path, Language language) throws IOException {
        this.directory = new File(path);
        this.language = language;

        PropertiesManager propertiesManager = new PropertiesManager();
        propertiesManager.loadProperties();
        this.bauhausPath = propertiesManager.getProperty("path.bauhaus").orElseThrow();
        this.cpfcsv2rfgPath = propertiesManager.getProperty("path.cpfcsv2rfg").orElseThrow();
    }

    /**
     * Start analysis in the repository specified during {@link #cpf(File, Language)}.
     * @param fileName name of the GLX file to be output. file extension will be appended later
     * @param index index of the file. Appended to the file name
     * @throws IOException exception
     */
    public void analyse(String fileName, int index) throws IOException {
        cpf(directory, language);
        cpfcsv2rfg(directory);
        rfgexport(directory, fileName, index);
        cleanup(directory);
    }

    /**
     * Enum of supported programming languages of Bauhaus including the file extensions of this programming language.
     */
    public enum Language {
        C("C", "i", "c", "h"),
        CPP("C++", "ii", "cpp", "cxx", "c++", "cc", "tcc", "hpp", "hxx", "h++", "hh", "C", "H", "inl", "preinc"),
        CS("C#", "cs"),
        ADA("Ada", "adb", "ads", "ada"),
        JAVA("Java", "java");

        @Getter
        private final String label;

        @Getter
        private final String[] extensions;

        Language(String label, String... extensions) {
            this.label = label;
            this.extensions = extensions;
        }
    }

}

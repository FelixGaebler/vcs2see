package de.unibremen.informatik.vcs2see;

import de.unibremen.informatik.vcs2see.predicates.CleanupPredicate;
import lombok.Getter;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
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
     * Composes relative file path from available data.
     * @param name name of the file/directory
     * @param index index of the file, relative id of the commit
     * @param ending file ending
     * @return composed file path
     */
    private String path(String name, int index, String ending) {
        return "\"" + name + "/" + name + "-" + index + ending + "\"";
    }

    /**
     * Helper method to run a ProcessBuilder and output the output to the console.
     * @param processBuilder ProcessBuilder which should be executed
     * @throws IOException exception
     */
    private void run(ProcessBuilder processBuilder) throws IOException {
        System.out.println(String.join(" ", processBuilder.command()));

        processBuilder.redirectErrorStream(true);
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
    private void cpf(File directory, Language language, String name, int index) throws IOException {
        List<String> cmd = new ArrayList<>(Arrays.asList(bauhausPath + "\\cpf", "-B", "\"src/main/java/\"", "-m", "100", "-c", path(name, index, ".cpf"), "-s", path(name, index, ".csv"), "-t", path(name, index, "")));
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
    private void cpfcsv2rfg(File directory, String name, int index) throws IOException {
        ProcessBuilder processBuilder = new ProcessBuilder();
        processBuilder.command(bauhausPath + "\\rfgscript", cpfcsv2rfgPath + "\\cpfcsv2rfg.py", (name + "-" + index + ".cpf"), (name + "-" + index + ".csv"), name + "-" + index + ".rfg");
        processBuilder.directory(directory);

        run(processBuilder);
    }

    /**
     * Export the clone information in the RFG to a GXL file.
     * @param directory directory in which the command is to be executed
     * @param name name of the output GLX file
     * @throws IOException
     * @return
     */
    private File rfgexport(File directory, String name, int index) throws IOException {
        String fileName = name + "-" + index;

        ProcessBuilder processBuilder = new ProcessBuilder();
        processBuilder.command(bauhausPath + "\\rfgexport", "-o", "Clones", "-f", "GXL", (fileName + ".rfg"), (fileName + ".gxl"));
        processBuilder.directory(directory);

        run(processBuilder);
        return new File(directory, (fileName + ".gxl"));
    }

    /**
     * Cleans the working directory from unnecessary files.
     * @param directory directory in which the command is to be executed
     */
    private void cleanup(File directory) {
        Arrays.stream(Objects.requireNonNull(directory.listFiles(new CleanupPredicate())))
                .forEach(File::delete);
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
     * Start analysis in the repository specified during {@link #cpf(File, Language, String, int)}.
     * @param projectName name of the GLX file to be output. file extension will be appended later
     * @param index index of the file. Appended to the file name
     * @return generated GLX file
     * @throws IOException exception
     */
    public File analyse(String projectName, int index) throws IOException {
        // Create directory for output.
        File output = new File(directory, projectName);
        output.mkdirs();

        // Run bauhaus commands.
        cpf(directory, language, projectName, index);
        cpfcsv2rfg(output, projectName, index);
        File file = rfgexport(output, projectName, index);
        cleanup(output);

        return file;
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

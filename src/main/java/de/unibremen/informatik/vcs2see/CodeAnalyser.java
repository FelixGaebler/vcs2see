package de.unibremen.informatik.vcs2see;

import de.unibremen.informatik.vcs2see.data.EnvironmentData;
import de.unibremen.informatik.vcs2see.data.RepositoryData;
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

/**
 * Component to run axivion suite / baushaus on a given path.
 *
 * @author Felix Gaebler
 * @version 1.0.0
 */
public class CodeAnalyser {

    private final File directory;

    private final EnvironmentData environmentData;

    private final RepositoryData repositoryData;

    /**
     * Initialization for the CodeAnalyser.
     * @param path path to the repository to be analyzed
     * @param repositoryData all the required information about the repository
     * @param environmentData all the required information about the environment
     */
    public CodeAnalyser(String path, RepositoryData repositoryData, EnvironmentData environmentData) {
        this.directory = new File(path);
        this.repositoryData = repositoryData;
        this.environmentData = environmentData;
    }

    /**
     * Composes relative file path from available data.
     * @param index index of the file, relative id of the commit
     * @param ending file ending
     * @return composed file path
     */
    private String path(int index, String ending) {
        return "\"" + repositoryData.getName() + "/" + repositoryData.getName() + "-" + index + ending + "\"";
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
     * @param revision index of the revision to be analyzed
     * @throws IOException exception
     */
    private void cpf(File directory, int revision) throws IOException {
        PropertiesManager propertiesManager = new PropertiesManager();
        propertiesManager.loadProperties();

        List<String> cmd = new ArrayList<>(Arrays.asList(environmentData.getBauhausPath() + "\\cpf", "-B", "\"" + repositoryData.getBasePath() + "\"", "-m", "100", "-c", path(revision, ".cpf"), "-s", path(revision, ".csv"), "-t", path(revision, "")));
        for (String extension : repositoryData.getLanguage().getExtensions()) {
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
     * @param revision index of the revision to be analyzed
     * @throws IOException exception
     */
    private void cpfcsv2rfg(File directory, int revision) throws IOException {
        String fileName = repositoryData.getName() + "-" + revision;

        ProcessBuilder processBuilder = new ProcessBuilder();
        processBuilder.command(environmentData.getBauhausPath() + "\\rfgscript", environmentData.getCpfcsv2rfgPath() + "\\cpfcsv2rfg.py", fileName + ".cpf", fileName + ".csv", fileName + ".rfg");
        processBuilder.directory(directory);

        run(processBuilder);
    }

    /**
     * Export the clone information in the RFG to a GXL file.
     * @param directory directory in which the command is to be executed
     * @param revision index of the revision to be analyzed
     * @throws IOException exception
     * @return generated file
     */
    private File rfgexport(File directory, int revision) throws IOException {
        String fileName = repositoryData.getName() + "-" + revision;

        ProcessBuilder processBuilder = new ProcessBuilder();
        processBuilder.command(environmentData.getBauhausPath() + "\\rfgexport", "-o", "Clones", "-f", "GXL", fileName + ".rfg", fileName + ".gxl");
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
     * Start analysis in the repository.
     * @param revision index of the revision to be analyzed
     * @return generated GLX file
     * @throws IOException exception
     */
    public File analyse(int revision) throws IOException {
        // Create directory for output.
        File output = new File(directory, repositoryData.getName());
        output.mkdirs();

        // Run bauhaus commands.
        cpf(directory, revision);
        cpfcsv2rfg(output, revision);
        File file = rfgexport(output, revision);
        cleanup(output);

        return file;
    }

    /**
     * Enum of supported programming languages of Bauhaus including the file extensions of this programming language.
     */
    public enum Language {
        C("i", "c", "h"),
        CPP("ii", "cpp", "cxx", "c++", "cc", "tcc", "hpp", "hxx", "h++", "hh", "C", "H", "inl", "preinc"),
        CS("cs"),
        ADA("adb", "ads", "ada"),
        JAVA("java");

        @Getter
        private final String[] extensions;

        Language(String... extensions) {
            this.extensions = extensions;
        }

        public String regex() {
            return ".*\\" + Arrays.stream(extensions)
                    .map(extension -> "." + extension)
                    .collect(Collectors.joining("|"));
        }
    }

}

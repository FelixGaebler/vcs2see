package de.unibremen.informatik.vcs2see;

import de.unibremen.informatik.vcs2see.data.EnvironmentData;
import de.unibremen.informatik.vcs2see.data.RepositoryData;
import de.unibremen.informatik.vcs2see.predicates.BauhausPathPredicate;
import de.unibremen.informatik.vcs2see.predicates.CpfCsv2RfgPathPredicate;
import de.unibremen.informatik.vcs2see.predicates.RepositoryPathPredicate;
import org.xml.sax.SAXException;

import java.io.IOException;
import java.util.Arrays;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * Vsc2See.
 * Prepares a folder with GLX files for SEE to visualize a version control history.
 *
 * @author Felix Gaebler
 * @version 1.0.0
 */
public class Vsc2See {

    private final PropertiesManager propertiesManager;

    private final ConsoleManager consoleManager;

    private EnvironmentData environmentData;

    private RepositoryData repositoryData;

    public Vsc2See() {
        this.propertiesManager = new PropertiesManager();
        this.consoleManager = new ConsoleManager();
    }

    /**
     * Outputs the logo in the console.
     */
    public void welcome() {
        consoleManager.printLogo();
    }

    /**
     * Runs the setup in the console. Environment and repository settings are queried.
     * @throws IOException exception
     */
    public void setup() throws IOException {
        propertiesManager.loadProperties();
        consoleManager.printLine();

        this.environmentData = setupEnvironment();
        this.repositoryData = setupRepository();

        propertiesManager.saveProperties();
    }

    /**
     * Queries the paths to required applications.
     * @return environment data object
     * @throws IOException exception
     */
    private EnvironmentData setupEnvironment() throws IOException {
        EnvironmentData environmentData = new EnvironmentData();

        consoleManager.print(" ENVIRONMENT");
        consoleManager.printLine();

        environmentData.setBauhausPath(propertySetupStep("path.bauhaus", new BauhausPathPredicate()));
        environmentData.setCpfcsv2rfgPath(propertySetupStep("path.cpfcsv2rfg", new CpfCsv2RfgPathPredicate()));

        return environmentData;
    }

    /**
     * Queries the required data about the repository.
     * @return repository data object
     * @throws IOException exception
     */
    private RepositoryData setupRepository() throws IOException {
        RepositoryData repositoryData = new RepositoryData();

        consoleManager.print(" REPOSITORY");
        consoleManager.printLine();

        boolean valid;
        String input;

        // Repository name.
        do {
            input = consoleManager.readLine("Export name : ");
            valid = !input.isBlank();
        } while (!valid);
        repositoryData.setName(input);

        // Repository type.
        do {
            String types = Arrays.stream(RepositoryCrawler.Type.values())
                    .map(RepositoryCrawler.Type::name)
                    .collect(Collectors.joining(", "));
            input = consoleManager.readLine("Repository type (" + types + "): ");
            try {
                repositoryData.setType(RepositoryCrawler.Type.valueOf(input));
                valid = true;
            } catch (IllegalArgumentException e) {
                consoleManager.print("Invalid type");
                valid = false;
            }
        } while (!valid);

        // Repository programming language.
        do {
            String languages = Arrays.stream(CodeAnalyser.Language.values())
                    .map(CodeAnalyser.Language::name)
                    .collect(Collectors.joining(", "));
            input = consoleManager.readLine("Language of repository (" + languages + "): ");
            try {
                repositoryData.setLanguage(CodeAnalyser.Language.valueOf(input));
                valid = true;
            } catch (IllegalArgumentException e) {
                consoleManager.print("Invalid language");
                valid = false;
            }
        } while (!valid);

        // Repository path.
        do {
            input = consoleManager.readLine("Path to repository: ");
            RepositoryPathPredicate predicate = new RepositoryPathPredicate(repositoryData.getType());
            valid = predicate.test(input);
        } while (!valid);
        repositoryData.setPath(input);

        return repositoryData;
    }

    /**
     * Starts crawling and analysing the repository.
     * @throws IOException exception
     * @throws SAXException exception
     */
    public void run() throws IOException, SAXException {
        // Start crawling the repository.
        RepositoryCrawler repositoryCrawler = new RepositoryCrawler(repositoryData);
        repositoryCrawler.crawl();

        consoleManager.printLine();
    }

    /**
     * Process a single step of the setup.
     * @param key property key
     * @param predicate a condition that must be met for the input
     * @throws IOException exception
     */
    private String propertySetupStep(String key, Predicate<String> predicate) throws IOException {
        // Load and print current value
        Optional<String> optional = propertiesManager.getProperty(key);
        consoleManager.print(key + ": " + optional.orElse("<not set>"));

        boolean valid;
        do {
            String prompt = optional.isEmpty()
                    ? "New value: "
                    : "New value or empty for old value: ";

            String line = consoleManager.readLine(prompt);

            // If a value is present, then no input is required
            if (optional.isPresent() && line.isBlank()) {
                valid = true;
                continue;
            }

            // If a value was entered and it is incorrect, the query is repeated.
            // Errors are output by the precondition.
            if(!predicate.test(line)) {
                valid = false;
                continue;
            } else {
                propertiesManager.setProperty(key, line);
            }

            valid = true;
        } while (!valid);

        consoleManager.printLine();
        return propertiesManager.getProperty(key).orElseThrow();
    }

    /**
     * The entry point of the application.
     * Perform steps of the sequence one by one.
     *
     * @param args ignored
     */
    public static void main(String[] args) throws IOException, SAXException {
        Vsc2See software = new Vsc2See();
        software.welcome();
        software.setup();
        software.run();
    }

}

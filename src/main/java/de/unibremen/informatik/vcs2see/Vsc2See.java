package de.unibremen.informatik.vcs2see;

import de.unibremen.informatik.vcs2see.predicates.BasePathPredicate;
import de.unibremen.informatik.vcs2see.predicates.BauhausPathPredicate;
import de.unibremen.informatik.vcs2see.predicates.CpfCsv2RfgPathPredicate;
import de.unibremen.informatik.vcs2see.predicates.RepositoryPathPredicate;
import org.xml.sax.SAXException;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
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
     * Runs the setup in the console. Application paths and other settings are queried.
     * @throws IOException exception
     */
    public void setup() throws IOException {
        propertiesManager.loadProperties();
        consoleManager.printLine();

        consoleManager.print(" SETUP");
        consoleManager.printLine();

        setupStep("path.bauhaus", new BauhausPathPredicate());
        setupStep("path.cpfcsv2rfg", new CpfCsv2RfgPathPredicate());
        setupStep("path.base", new BasePathPredicate());

        propertiesManager.saveProperties();
    }

    /**
     * Queries data about the repository and then starts crawling the repository.
     * @throws IOException exception
     */
    public void crawl() throws IOException, SAXException {
        consoleManager.print(" CRAWLER");
        consoleManager.printLine();

        boolean valid;
        String line;
        String name;
        CodeAnalyser.Language language = null;
        RepositoryCrawler.Type type = null;

        // Name of the exported files.
        do {
            name = consoleManager.readLine("Export name : ");
            valid = !name.isBlank();
        } while (!valid);

        // Type of repository to crawl.
        do {
            String types = Arrays.stream(RepositoryCrawler.Type.values())
                    .map(RepositoryCrawler.Type::name)
                    .collect(Collectors.joining(", "));
            line = consoleManager.readLine("Repository type (" + types + "): ");
            try {
                type = RepositoryCrawler.Type.valueOf(line);
                valid = true;
            } catch (IllegalArgumentException e) {
                consoleManager.print("Invalid type");
                valid = false;
            }
        } while (!valid);

        // Programming language of repository to crawl.
        do {
            String languages = Arrays.stream(CodeAnalyser.Language.values())
                    .map(CodeAnalyser.Language::name)
                    .collect(Collectors.joining(", "));
            line = consoleManager.readLine("Language of repository (" + languages + "): ");
            try {
                language = CodeAnalyser.Language.valueOf(line);
                valid = true;
            } catch (IllegalArgumentException e) {
                consoleManager.print("Invalid language");
                valid = false;
            }
        } while (!valid);

        // Path to repository to crawl.
        do {
            line = consoleManager.readLine("Path to repository: ");
            RepositoryPathPredicate predicate = new RepositoryPathPredicate(type);
            valid = predicate.test(line);
        } while (!valid);

        consoleManager.print("Repository found! Starting crawler...");

        // Start crawling the repository.
        RepositoryCrawler repositoryCrawler = new RepositoryCrawler();
        repositoryCrawler.init(name, line, type, language);
        repositoryCrawler.crawl();

        consoleManager.printLine();
    }

    /**
     * Process a single step of the setup.
     * @param key property key
     * @param predicate a condition that must be met for the input
     * @throws IOException exception
     */
    private void setupStep(String key, Predicate<String> predicate) throws IOException {
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
    }

    /**
     * The entry point of the application.
     * Performs steps of the sequence one by one.
     *
     * @param args ignored
     */
    public static void main(String[] args) throws IOException, SAXException {
        Vsc2See software = new Vsc2See();
        software.welcome();
        software.setup();
        software.crawl();
    }

}

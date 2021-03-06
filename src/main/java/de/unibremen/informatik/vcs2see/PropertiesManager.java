package de.unibremen.informatik.vcs2see;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Optional;
import java.util.Properties;

/**
 * Component which handles reading and writing of a properties file.
 *
 * @author Felix Gaebler
 * @version 1.0.0
 */
public class PropertiesManager {

    private Properties properties;

    private File file;

    /**
     * Load the properties file of the software.
     * @throws IOException exception
     */
    public void loadProperties() throws IOException {
        file = new File("vsc2see.properties");
        if(!file.exists() && !file.createNewFile()) {
            System.err.println("Failed to create properties file.");
            System.exit(1);
        }

        properties = new Properties();
        properties.load(new FileInputStream(file));
    }

    /**
     * Returns a property from the properties file as {@link Optional<String>}.
     * @param key key to the property
     * @return value as optional
     */
    public Optional<String> getProperty(String key) {
        return Optional.ofNullable(properties.getProperty(key));
    }

    /**
     * Sets a property in the properties file.
     * @param key key to the property
     * @param value value of the property
     */
    public void setProperty(String key, String value) {
        properties.setProperty(key, value);
    }

    /**
     * Saves the changes in the properties file.
     * @throws IOException exception
     */
    public void saveProperties() throws IOException {
        properties.store(new FileOutputStream(file), "Vsc2See Properties");
    }

}

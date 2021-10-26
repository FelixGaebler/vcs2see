package de.unibremen.informatik.vcs4see;

import java.io.IOException;

/**
 * Fragen:
 * - Welche Daten sollen visualisiert werden?
 * - Skala der Evolution (Zeit oder Commits) -> Zeit weil sinnvoll und mehrere Autoren gleichzeitig
 * - Große Repositories über viele Jahre -> Evolution in Tagen/Wochen/Monaten
 *
 */
public class Main {

    public static void main(String[] args) throws IOException {
        String NAME = args[0];
        String REPOSITORY = args[1];

        Vcs2See vcs2see = new Vcs2See(NAME, REPOSITORY);
    }

}

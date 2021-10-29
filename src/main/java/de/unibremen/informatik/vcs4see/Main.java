package de.unibremen.informatik.vcs4see;

import net.sourceforge.gxl.GXLDocument;
import net.sourceforge.gxl.GXLGraph;
import net.sourceforge.gxl.GXLNode;

import java.io.File;
import java.io.IOException;
import java.net.URI;

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

        GXLDocument document = new GXLDocument();

        GXLGraph graph = new GXLGraph("yGraph");

        GXLNode node = new GXLNode("N1");
        node.setType(URI.create("Commit"));
        graph.add(node);

        document.getDocumentElement().add(graph);
        document.write(new File("TEST.gxl"));

        Vcs2See vcs2see = new Vcs2See(NAME, REPOSITORY);
    }

}

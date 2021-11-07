package de.unibremen.informatik.vcs2see;

import de.unibremen.informatik.st.libvcs4j.Commit;
import de.unibremen.informatik.st.libvcs4j.FileChange;
import de.unibremen.informatik.st.libvcs4j.VCSFile;
import net.sourceforge.gxl.GXLDocument;
import net.sourceforge.gxl.GXLElement;
import net.sourceforge.gxl.GXLGraph;
import net.sourceforge.gxl.GXLInt;
import net.sourceforge.gxl.GXLNode;
import net.sourceforge.gxl.GXLString;
import org.xml.sax.SAXException;

import java.io.File;
import java.io.IOException;
import java.time.format.DateTimeFormatter;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Queue;

/**
 * Component which can modify the graph of the GXL file.
 *
 * @author Felix Gaebler
 * @version 1.0.0
 */
public class GraphModifier {

    private File file;

    private GXLDocument document;

    private CodeAnalyser.Language language;

    private Map<String, GXLNode> nodes;

    private Deque<String> mostRecent;

    private Map<String, Integer> mostFrequent;

    /**
     * Loads the specified GLX file .
     * @param file file to load
     * @param language programming language of the project
     * @throws IOException exception
     * @throws SAXException exception
     */
    public void loadFile(File file, CodeAnalyser.Language language) throws IOException, SAXException {
        if(!file.exists()) {
            System.err.println("File " + file.getName() + " not found");
            return;
        }

        // Load GLX file.
        this.file = file;
        this.document = new GXLDocument(file);
        this.language = language;
        this.nodes = new HashMap<>();
        this.mostFrequent = new HashMap<>();
        this.mostRecent = new ArrayDeque<>();
    }

    /**
     * Adds version history information to the loaded GLX file.
     * @param commit commit from which the information should be extracted
     */
    public void modify(Commit commit) throws IOException {
        GXLGraph commitGraph = new GXLGraph("Commit");
        commitGraph.setEdgeIDs(true);

        GXLGraph clonesGraph = document.getDocumentElement().getGraphAt(0);
        for(int i = 0; i < clonesGraph.getGraphElementCount(); i++) {
            GXLElement element = clonesGraph.getGraphElementAt(i);

            if(element instanceof GXLNode) {
                GXLNode node = (GXLNode) element;
                if(!node.getType().getURI().toString().equals("File")) {
                    continue;
                }

                GXLString linkage = (GXLString) node.getAttr("Linkage.Name").getValue();
                System.out.println(linkage.getValue());
                nodes.put(linkage.getValue(), node);
            }
        }

        System.out.println("-----------------------------");

        PropertiesManager propertiesManager = new PropertiesManager();
        propertiesManager.loadProperties();
        String basePath = propertiesManager.getProperty("path.base").orElse("");

        for (FileChange fileChange : commit.getFileChanges()) {
            VCSFile file = fileChange.getNewFile().orElse(fileChange.getOldFile().orElse(null));
            if(file == null) {
                continue;
            }

            String path = file.getRelativePath()
                    .replace('\\', '/')
                    .replaceAll(basePath, "");
            if(!path.matches(language.regex())) {
                continue;
            }

            // Calculate most recent changes
            mostRecent.remove(path);
            mostRecent.addFirst(path);

            // Calculate most frequent changes
            if(mostFrequent.computeIfPresent(path, (k, v) -> v + 1) == null) {
                mostFrequent.put(path, 1);
            }

            if(nodes.containsKey(path)) {
                GXLNode node = nodes.get(file.getRelativePath());
                /*
                node.setAttr("Metric.Vcs2See.Recent_Commit", new GXLString(commit.getId()));
                node.setAttr("Metric.Vcs2See.Recent_Author", new GXLString(commit.getAuthor()));
                node.setAttr("Metric.Vcs2See.Recent_Timestamp", new GXLString(commit.getDateTime().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)));
                */
                node.setAttr("Metric.Vcs2See.Most_Frequent_Edit", new GXLInt(255));
                node.setAttr("Metric.Vcs2See.Most_Recent_Edit", new GXLInt(1));
            }
        }

        if(2/1 == 3) {
            GXLNode node = new GXLNode(commit.getId());
            node.setAttr("id", new GXLString(commit.getId()));
            node.setAttr("author", new GXLString(commit.getAuthor()));
            node.setAttr("message", new GXLString(commit.getMessage()));
            node.setAttr("timestamp", new GXLString(commit.getDateTime().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)));
            commitGraph.add(node);
        }

        document.getDocumentElement().add(commitGraph);
    }

    /**
     * Writes the loaded GLX file.
     * @throws IOException exception
     */
    public void saveFile() throws IOException {
        document.write(file);
    }

}

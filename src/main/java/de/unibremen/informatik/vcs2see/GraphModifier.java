package de.unibremen.informatik.vcs2see;

import de.unibremen.informatik.st.libvcs4j.Commit;
import de.unibremen.informatik.st.libvcs4j.FileChange;
import de.unibremen.informatik.st.libvcs4j.VCSFile;
import de.unibremen.informatik.vcs2see.data.RepositoryData;
import net.sourceforge.gxl.*;
import org.xml.sax.SAXException;

import java.io.File;
import java.io.IOException;
import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * Component which can modify the graph of the GXL file.
 *
 * @author Felix Gaebler
 * @version 1.0.0
 */
public class GraphModifier {

    private final RepositoryData repositoryData;

    private File file;

    private GXLDocument document;

    private Map<String, GXLNode> nodes;

    private final Deque<String> mostRecent;

    private final Map<String, Integer> mostFrequent;

    public GraphModifier(RepositoryData repositoryData) {
        this.repositoryData = repositoryData;
        this.mostFrequent = new HashMap<>();
        this.mostRecent = new LinkedList<>();
    }

    /**
     * Loads the specified GLX file .
     * @param file file to load
     * @throws IOException exception
     * @throws SAXException exception
     */
    public void loadFile(File file) throws IOException, SAXException {
        if(!file.exists()) {
            System.err.println("File " + file.getName() + " not found");
            return;
        }

        // Load GLX file.
        this.file = file;
        this.document = new GXLDocument(file);
    }

    public void loadNodes() {
        this.nodes = new HashMap<>();

        System.out.println("Nodes:");

        // Load nodes.
        GXLGraph clonesGraph = document.getDocumentElement().getGraphAt(0);
        for(int i = 0; i < clonesGraph.getGraphElementCount(); i++) {
            GXLElement element = clonesGraph.getGraphElementAt(i);

            if(element instanceof GXLNode) {
                GXLNode node = (GXLNode) element;
                if(!node.getType().getURI().toString().equals("File")) {
                    continue;
                }

                GXLString linkage = (GXLString) node.getAttr("Linkage.Name").getValue();
                System.out.println(" - " + linkage.getValue());
                nodes.put(linkage.getValue(), node);
            }
        }
    }

    /**
     * Adds version history information to the loaded GLX file.
     * @param commit commit from which the information should be extracted
     */
    public void queryCommitData(Commit commit) throws IOException {
        PropertiesManager propertiesManager = new PropertiesManager();
        propertiesManager.loadProperties();
        String basePath = propertiesManager.getProperty("path.base").orElse("");

        System.out.println("Changes:");
        for (FileChange fileChange : commit.getFileChanges()) {
            VCSFile file = fileChange.getNewFile().orElse(fileChange.getOldFile().orElse(null));
            if(file == null) {
                System.err.println("No file");
                continue;
            }

            String path = file.getRelativePath()
                    .replace('\\', '/')
                    .replaceAll(basePath, "");
            if(!path.matches(repositoryData.getLanguage().regex())) {
                continue;
            }

            System.out.println("- " + path);

            // Calculate most recent changes
            mostRecent.remove(path);
            mostRecent.addFirst(path);

            // Calculate most frequent changes
            if(mostFrequent.computeIfPresent(path, (k, v) -> v + 1) == null) {
                mostFrequent.put(path, 1);
            }
        }
    }

    public void populateNodes() {
        List<String> list = (LinkedList<String>) mostRecent;
        for(int i = 0; i < list.size(); i++) {
            String path = list.get(i);
            if(nodes.containsKey(path)) {
                GXLNode node = nodes.get(path);
                node.setAttr("Metric.Vcs2See.Most_Recent_Edit", new GXLInt(interpolateMostRecent(list.size(), i)));
            }
        }
    }

    public void addCommitGraph(Commit commit) {
        GXLGraph commitGraph = new GXLGraph("Commit");
        commitGraph.setEdgeIDs(true);

        GXLNode node = new GXLNode(commit.getId());
        node.setAttr("id", new GXLString(commit.getId()));
        node.setAttr("author", new GXLString(commit.getAuthor()));
        node.setAttr("message", new GXLString(commit.getMessage()));
        node.setAttr("timestamp", new GXLString(commit.getDateTime().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)));

        commitGraph.add(node);
        document.getDocumentElement().add(commitGraph);
    }

    private int interpolateMostRecent(int size, int index) {
        int step = 255 / (size - 1);
        return 255 - (step  * index);
    }

    private float interpolateMostFrequent(int value) {
        float min = mostFrequent.values().stream().min(Integer::compareTo).orElse(0);
        float max = mostFrequent.values().stream().max(Integer::compareTo).orElse(255);
        return (255 / (max - min)) * (value - min);
    }

    /**
     * Writes the loaded GLX file.
     * @throws IOException exception
     */
    public void saveFile() throws IOException {
        document.write(file);
    }

}

package de.unibremen.informatik.vcs4see;

import de.unibremen.informatik.st.libvcs4j.*;
import lombok.Getter;
import lombok.extern.log4j.Log4j2;
import net.sourceforge.gxl.*;
import org.xml.sax.SAXException;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Vcs4See.
 * Converts data of a version control system into GXL files for visualisation in SEE.
 *
 * @author Felix Gaebler
 * @version 1.0.0
 * @see <a href="https://www.baeldung.com/properties-with-spring">Custom property source (Baeldung)</a>
 */
@Log4j2
public class Vcs4See {

    private final String REPOSITORY = "C:\\Users\\Felix Gaebler\\Documents\\Universität Bremen\\Bachelorarbeit\\vcs4see\\example\\hellogitworld";

    public static void main(String[] args) throws IOException, SAXException {
        Vcs4See vcs4See = new Vcs4See();
    }

    @Getter
    private final Map<String, FileInfo> fileInfos;

    private final Map<String, GXLNode> nodeMap;

    private GXLDocument document;
    private GXLGraph graph;

    private VCSEngine vcs;

    public Vcs4See() throws IOException, SAXException {
        this.fileInfos = new HashMap<>();
        this.nodeMap = new HashMap<>();
        load();
        collect();
        print();
        importGxl();
        exportGxl();
    }

    private void load() {
        log.info("Loading repository...");
        vcs = VCSEngineBuilder
                .ofGit(REPOSITORY)
                .build();
        log.info("Repository loaded successfully.");
    }


    private void collect() {
        for (RevisionRange range : vcs) {

            for(Commit commit : range.getCommits()) {
                String author = commit.getAuthor();
                //TODO: Timeline by date or by commit id

                for(FileChange fileChange : commit.getFileChanges()) {
                    VCSFile file = fileChange.getNewFile().orElse(fileChange.getOldFile().orElse(null));

                    if(file == null) {
                        throw new RuntimeException("Commit has no changes. #" + commit.getId());
                    }

                    fileInfos.putIfAbsent(file.getRelativePath(), new FileInfo());
                    FileInfo fileInfo = fileInfos.get(file.getRelativePath());
                    fileInfo.addEdit(author, commit.getDateTime());
                }

            }

        }

    }

    private void print() {
        fileInfos.entrySet().stream()
                .sorted(Comparator.comparingInt(a -> a.getValue().getTotalEdits()))
                .forEach(entry -> {
                    String authors = entry.getValue().getEditsPerAuthor().entrySet().stream()
                            .map(e -> e.getKey() + " (" + e.getValue() + ")")
                            .collect(Collectors.joining(", "));
                    System.out.println(entry.getKey() + ":");
                    System.out.println(" - Edits: " + entry.getValue().getTotalEdits());
                    System.out.println(" - Authors: " + authors);
                });
    }

    private void importGxl() throws IOException, SAXException {
        this.document = new GXLDocument(new File("work/input.gxl"));
        GXLGXL gxl = document.getDocumentElement();

        for(int a = 0; a < gxl.getGraphCount(); a++) {
            GXLGraph graph = gxl.getGraphAt(a);

            if(!graph.getID().equals("vcs4see")) {
                return;
            }

            this.graph = graph;

            for (int b = 0; b < graph.getGraphElementCount(); b++) {
                GXLElement element = graph.getGraphElementAt(b);
                if(element instanceof GXLNode) {
                    GXLNode node = (GXLNode) element;
                    nodeMap.put(node.getID(), node);
                }
            }
        }

        if(graph == null) {
            graph = new GXLGraph("vcs4see");
            document.getDocumentElement().add(graph);
        }
    }

    private void exportGxl() throws IOException {
        fileInfos.entrySet().stream()
                .sorted(Comparator.comparingInt(a -> a.getValue().getTotalEdits()))
                .forEach(entry -> {
                    FileInfo fileInfo = entry.getValue();

                    // Create node with hashed path as key
                    String hash = entry.getKey();//Base64.getEncoder().encodeToString(entry.getKey().getBytes(StandardCharsets.UTF_8));
                    GXLNode node = getNode(hash);

                    // Set node type
                    node.setType(URI.create("File"));

                    // Set node latest change
                    node.setAttr("LatestChange", new GXLString(fileInfo.getLatestChange().toString()));

                    // Add authors to node
                    GXLSet authors = new GXLSet();
                    fileInfo.getEditsPerAuthor().forEach((key, value) -> {
                        GXLTup tuple = new GXLTup();
                        tuple.add(new GXLString(key));
                        tuple.add(new GXLInt(value));
                        authors.add(tuple);
                    });
                    node.setAttr("Authors", authors);
                });



        document.write(new File("work/output.gxl"));
    }

    private GXLNode getNode(final String id) {
        GXLNode node = nodeMap.getOrDefault(id, new GXLNode(id));
        if(!nodeMap.containsKey(id)) {
            graph.add(node);
        }

        return node;
    }

}

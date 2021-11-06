package de.unibremen.informatik.vcs2see;

import de.unibremen.informatik.st.libvcs4j.Commit;
import de.unibremen.informatik.st.libvcs4j.FileChange;
import de.unibremen.informatik.st.libvcs4j.VCSFile;
import net.sourceforge.gxl.GXLDocument;
import net.sourceforge.gxl.GXLElement;
import net.sourceforge.gxl.GXLGraph;
import net.sourceforge.gxl.GXLNode;
import net.sourceforge.gxl.GXLString;
import org.xml.sax.SAXException;

import java.io.File;
import java.io.IOException;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

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
    }

    /**
     * Adds version history information to the loaded GLX file.
     * @param commit commit from which the information should be extracted
     */
    public void modify(Commit commit) {
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

        System.out.println("-----------------------------------------");

        for (FileChange fileChange : commit.getFileChanges()) {
            VCSFile file = fileChange.getNewFile().orElse(fileChange.getOldFile().orElse(null));
            if(file == null) {
                System.out.println("file null");
                continue;
            }

            if(!file.getRelativePath().matches(language.regex())) {
                System.out.println("regex unmatch");
                continue;
            }

            System.out.println(file.getRelativePath());
        }

        GXLNode node = new GXLNode(commit.getId());
        node.setAttr("id", new GXLString(commit.getId()));
        node.setAttr("author", new GXLString(commit.getAuthor()));
        node.setAttr("message", new GXLString(commit.getMessage()));
        node.setAttr("timestamp", new GXLString(commit.getDateTime().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)));
        commitGraph.add(node);

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

package de.unibremen.informatik.vcs2see;

import de.unibremen.informatik.st.libvcs4j.Commit;
import net.sourceforge.gxl.GXLAttr;
import net.sourceforge.gxl.GXLDocument;
import net.sourceforge.gxl.GXLGraph;
import net.sourceforge.gxl.GXLNode;
import net.sourceforge.gxl.GXLString;
import org.xml.sax.SAXException;

import java.io.DataInput;
import java.io.File;
import java.io.IOException;
import java.time.format.DateTimeFormatter;

/**
 * Component which can modify the graph of the GXL file.
 *
 * @author Felix Gaebler
 * @version 1.0.0
 */
public class GraphModifier {

    private File file;

    private GXLDocument document;

    /**
     * Initialization method for the GraphModifier.
     * @param name name of the graph
     */
    public void init(String name) {
       //TODO this.name = name;
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

    /**
     * Adds version history information to the loaded GLX file.
     * @param commit commit from which the information should be extracted
     */
    public void modify(Commit commit) {
        GXLGraph graph = new GXLGraph("Commit");
        graph.setEdgeIDs(true);

        GXLNode node = new GXLNode(commit.getId());
        node.setAttr("id", new GXLString(commit.getId()));
        node.setAttr("author", new GXLString(commit.getAuthor()));
        node.setAttr("message", new GXLString(commit.getMessage()));
        node.setAttr("timestamp", new GXLString(commit.getDateTime().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)));
        graph.add(node);

        document.getDocumentElement().add(graph);
    }

    /**
     * Writes the loaded GLX file.
     * @throws IOException exception
     */
    public void saveFile() throws IOException {
        document.write(file);
    }

}

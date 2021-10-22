package de.unibremen.informatik.vcs4see;

import de.unibremen.informatik.st.libvcs4j.Commit;
import de.unibremen.informatik.st.libvcs4j.RevisionRange;
import de.unibremen.informatik.st.libvcs4j.VCSEngine;
import de.unibremen.informatik.st.libvcs4j.VCSEngineBuilder;
import lombok.extern.log4j.Log4j2;
import net.sourceforge.gxl.GXLDocument;
import net.sourceforge.gxl.GXLGraph;
import net.sourceforge.gxl.GXLNode;
import net.sourceforge.gxl.GXLString;
import org.apache.regexp.RE;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.text.SimpleDateFormat;

@Log4j2
public class Vcs2See {

    private VCSEngine engine;

    public Vcs2See(String name, Boolean evolution, String repository) throws IOException {
        load(repository);
        crawl(name, evolution);
    }

    private void load(String repository) {
        log.info("Loading repository...");
        engine = VCSEngineBuilder
                .ofGit(repository)
                .build();
        log.info("Repository loaded successfully.");
    }

    private void crawl(String name, Boolean evolution) throws IOException {
        int index = 0;

        for(RevisionRange revision : engine) {
            // Throw exception if commit amount is anything but 1
            if(revision.getCommits().size() != 1) {
                throw new RuntimeException("Invalid commit amount per revision: " + revision.getCommits().size());
            }

            Commit commit = revision.getCommits().get(0);
            String author = commit.getAuthor();


            if(evolution) {
                write(name, index++);
            }
        }

        if(!evolution) {
            write(name);
        }
    }

    private void process() {

    }

    private void write(String name) throws IOException {
        write(name, null);
    }

    private void write(String name, Integer index) throws IOException {
        process();

        // Calculate path from index
        String path = "output/" + name;
        if(index != null) {
            path += "-" + index;
        }
        path += ".gxl";

        // Prepare gxl file
        File file = new File(path);
        file.getParentFile().mkdirs();
        GXLDocument document = new GXLDocument();
        GXLGraph graph = new GXLGraph("vcs2see");

        // Create nodes
        for(int i = 0; i < 1; i++) {//TODO
            GXLNode node = new GXLNode("N");
            node.setType(URI.create("File"));
            graph.add(node);
        }

        // Persist gxl file
        document.getDocumentElement().add(graph);
        document.write(file);
    }

}

package de.unibremen.informatik.vcs4see;

import de.unibremen.informatik.st.libvcs4j.Commit;
import de.unibremen.informatik.st.libvcs4j.FileChange;
import de.unibremen.informatik.st.libvcs4j.RevisionRange;
import de.unibremen.informatik.st.libvcs4j.VCSEngine;
import de.unibremen.informatik.st.libvcs4j.VCSEngineBuilder;
import lombok.extern.log4j.Log4j2;
import net.sourceforge.gxl.GXLBag;
import net.sourceforge.gxl.GXLDocument;
import net.sourceforge.gxl.GXLGraph;
import net.sourceforge.gxl.GXLNode;
import net.sourceforge.gxl.GXLString;
import net.sourceforge.gxl.GXLTup;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Log4j2
public class Vcs2See {

    private LocalDateTime start;

    private LocalDateTime end;

    private VCSEngine engine;

    private Map<LocalDate, List<Commit>> commitMap = new HashMap<>();

    public Vcs2See(String name, String repository) throws IOException {
        load(repository);
        crawl();
        process(name);
    }

    private void load(String repository) {
        log.info("Loading repository...");
        engine = VCSEngineBuilder
                .ofGit(repository)
                .build();
        log.info("Repository loaded successfully.");
    }

    private void crawl() throws IOException {
        for(RevisionRange revision : engine) {
            // Throw exception if commit amount is anything but 1
            if(revision.getCommits().size() != 1) {
                throw new RuntimeException("Invalid commit amount per revision: " + revision.getCommits().size());
            }

            // Get commit from revision
            Commit commit = revision.getCommits().get(0);

            // Get the lowest date from commits
            if(this.start == null || commit.getDateTime().isBefore(start)) {
                this.start = commit.getDateTime();
            }

            // Get the highest date from commits
            if(this.end == null || commit.getDateTime().isAfter(end)) {
                this.end = commit.getDateTime();
            }
            
            LocalDate date = commit.getDateTime().toLocalDate();
            commitMap.put(date, new ArrayList<>(revision.getCommits()));
        }
    }

    private void process(String name) throws IOException {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy");
        long days = ChronoUnit.DAYS.between(this.start.toLocalDate(), this.end.toLocalDate());
        System.out.println("Repository period: " + this.start.format(formatter) + " - " + this.end.format(formatter) + " (" + days + " days)");

        for(int day = 0; day <= days; day++) {
            LocalDate date = ChronoUnit.DAYS.addTo(this.start, day).toLocalDate();

            if(commitMap.containsKey(date)) {
                System.out.println(day);
                List<Commit> commits = commitMap.get(date);
                write(name, day, commits);
                continue;
            }

            write(name, day, new ArrayList<>());
        }
    }

    private void write(String name, int index, List<Commit> commits) throws IOException {
        String path = "output/" + name + "/" + name + "-" + index + ".gxl";

        // Prepare gxl file
        File file = new File(path);

        // Clear directory
        File dir = file.getParentFile();
        if(dir.exists()) {
            dir.delete();
        }
        file.getParentFile().mkdirs();

        GXLDocument document = new GXLDocument();
        GXLGraph graph = new GXLGraph(name);

        // Create nodes
        for(Commit commit : commits) {
            GXLNode node = new GXLNode(commit.getId());
            node.setType(URI.create("Commit"));
            node.setAttr("timestamp", new GXLString(commit.getDateTime().format(DateTimeFormatter.ofPattern("dd.MM.yyyy, hh:mm"))));
            node.setAttr("author", new GXLString(commit.getAuthor()));
            node.setAttr("message", new GXLString(commit.getMessage()));

            GXLBag changes = new GXLBag();
            for (FileChange fileChange : commit.getFileChanges()) {
                GXLTup change = new GXLTup();
                change.add(new GXLString(fileChange.getType().name()));
                change.add(new GXLString(fileChange.getOldFile().isPresent() ? fileChange.getOldFile().get().getRelativePath() : "NO"));
                change.add(new GXLString(fileChange.getNewFile().isPresent() ? fileChange.getNewFile().get().getRelativePath() : "NO"));
                changes.add(change);
            }
            node.setAttr("changes", changes);
            
            graph.add(node);
        }

        document.getDocumentElement().add(graph);
        document.write(file);

        /*
        GXLDocument document = new GXLDocument();
        GXLGraph graph = new GXLGraph(name);

        // Create nodes
        for(Commit commit : commits) {
            GXLNode node = new GXLNode(commit.getId());
            node.setType(URI.create("Commit"));
            node.setAttr("timestamp", new GXLString(commit.getDateTime().format(DateTimeFormatter.ofPattern("dd.MM.yyyy, hh:mm"))));
            graph.add(node);
        }

        // Persist gxl file
        document.getDocumentElement().add(graph);
        document.write(file);
         */
    }

}

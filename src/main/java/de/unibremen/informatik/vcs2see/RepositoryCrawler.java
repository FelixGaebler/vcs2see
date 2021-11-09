package de.unibremen.informatik.vcs2see;

import de.unibremen.informatik.st.libvcs4j.Commit;
import de.unibremen.informatik.st.libvcs4j.RevisionRange;
import de.unibremen.informatik.st.libvcs4j.VCSEngine;
import de.unibremen.informatik.st.libvcs4j.VCSEngineBuilder;
import de.unibremen.informatik.vcs2see.data.EnvironmentData;
import de.unibremen.informatik.vcs2see.data.RepositoryData;
import org.apache.commons.io.FileUtils;
import org.xml.sax.SAXException;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Component which uses LibVCS4j to traverse the repository and run an analysis on each revision.
 *
 * @author Felix Gaebler
 * @version 1.0.0
 */
public class RepositoryCrawler {

    private final VCSEngine engine;

    private final RepositoryData repositoryData;

    private final EnvironmentData environmentData;

    /**
     * Initialization for the RepositoryCrawler.
     * @param repositoryData all the required information about the repository
     * @param environmentData all the required information about the environment
     */
    public RepositoryCrawler(RepositoryData repositoryData, EnvironmentData environmentData) {
        this.repositoryData = repositoryData;
        this.environmentData = environmentData;

        switch (repositoryData.getType()) {
            case GIT:
                engine = VCSEngineBuilder.ofGit(repositoryData.getPath()).build();
                break;

            case HG:
                engine = VCSEngineBuilder.ofHG(repositoryData.getPath()).build();
                break;

            case SVN:
                engine = VCSEngineBuilder.ofSVN(repositoryData.getPath()).build();
                break;

            default:
                engine = VCSEngineBuilder.of(repositoryData.getPath()).build();
                break;
        }
    }

    /**
     * Start crawling the repository. This may take a few minutes.
     * @throws IOException exception
     * @throws SAXException exception
     */
    public void crawl() throws IOException, SAXException {
        String temp = engine.getOutput().toAbsolutePath().toString();

        CodeAnalyser codeAnalyser = new CodeAnalyser(temp, repositoryData, environmentData);
        GraphModifier graphModifier = new GraphModifier(repositoryData);
        ConsoleManager consoleManager = new ConsoleManager();

        int index = 1;
        for (RevisionRange revision : engine) {
            for(Commit commit : revision.getCommits()) {
                consoleManager.print("Commit: " + commit.getId());
                File file = codeAnalyser.analyse(index);

                graphModifier.loadFile(file);
                graphModifier.loadNodes();
                graphModifier.queryCommitData(commit);
                graphModifier.populateNodes();
                graphModifier.addCommitGraph(commit);
                graphModifier.saveFile();

                index++;
                consoleManager.printLine();
            }
        }

        // Copy result from temp directory to execution path.
        File folder = new File(repositoryData.getName());
        FileUtils.deleteDirectory(folder);
        FileUtils.copyDirectory(new File(temp, repositoryData.getName()), folder);
    }

    /**
     * Type of repository. All types supported by LibVCS4j.
     */
    public enum Type {
        GIT, HG, SVN;
    }

}

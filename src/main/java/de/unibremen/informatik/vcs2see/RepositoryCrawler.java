package de.unibremen.informatik.vcs2see;

import de.unibremen.informatik.st.libvcs4j.Commit;
import de.unibremen.informatik.st.libvcs4j.RevisionRange;
import de.unibremen.informatik.st.libvcs4j.VCSEngine;
import de.unibremen.informatik.st.libvcs4j.VCSEngineBuilder;
import de.unibremen.informatik.vcs2see.data.EnvironmentData;
import de.unibremen.informatik.vcs2see.data.RepositoryData;
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

        int index = 1;
        for (RevisionRange revision : engine) {
            for(Commit commit : revision.getCommits()) {
                File file = codeAnalyser.analyse(index);

                graphModifier.loadFile(file);
                graphModifier.loadNodes();
                graphModifier.queryCommitData(commit);
                graphModifier.populateNodes();
                graphModifier.addCommitGraph(commit);
                graphModifier.saveFile();

                index++;
            }
        }

        // Copy result from temp directory to execution path.
        copyDirectory(new File(temp, repositoryData.getName()).toPath(), ".");
    }

    /**
     * Copies directory from source to destination.
     * @param source source directory
     * @param destination destination directory
     * @throws IOException exception
     */
    private void copyDirectory(Path source, String destination) throws IOException {
        Files.walk(source).forEach(src -> {
            Path dest = Paths.get(destination, src.toString().substring(destination.length()));
            try {
                Files.copy(src, dest);
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    }

    /**
     * Type of repository. All types supported by LibVCS4j.
     */
    public enum Type {
        GIT, HG, SVN;
    }

}

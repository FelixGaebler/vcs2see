package de.unibremen.informatik.vcs2see;

import de.unibremen.informatik.st.libvcs4j.Commit;
import de.unibremen.informatik.st.libvcs4j.RevisionRange;
import de.unibremen.informatik.st.libvcs4j.VCSEngine;
import de.unibremen.informatik.st.libvcs4j.VCSEngineBuilder;
import org.xml.sax.SAXException;

import java.io.File;
import java.io.IOException;

/**
 * Component which uses LibVCS4j to traverse the repository and run an analysis on each revision.
 *
 * @author Felix Gaebler
 * @version 1.0.0
 */
public class RepositoryCrawler {

    private VCSEngine engine;

    private String name;

    private String path;

    private CodeAnalyser.Language language;

    /**
     * Initialization method for the RepositoryCrawler.
     * @param name name of the GLX files (without index) which are output by the {@link CodeAnalyser} (see {@link CodeAnalyser#analyse(String, int)}).
     * @param path path to the repository which should be crawled
     * @param type type of repository to be crawled
     * @param language programming language of repository to be crawled
     */
    public void init(String name, String path, Type type, CodeAnalyser.Language language) {
        this.name = name;
        this.path = path;
        this.language = language;

        switch (type) {
            case GIT:
                engine = VCSEngineBuilder.ofGit(path).build();
                break;

            case HG:
                engine = VCSEngineBuilder.ofHG(path).build();
                break;

            case SVN:
                engine = VCSEngineBuilder.ofSVN(path).build();
                break;

            default:
                engine = VCSEngineBuilder.of(path).build();
                break;
        }
    }

    /**
     * Start crawling the repository. This may take a few minutes.
     * @throws IOException exception
     */
    public void crawl() throws IOException, SAXException {
        String temp = engine.getOutput().toAbsolutePath().toString();

        CodeAnalyser codeAnalyser = new CodeAnalyser();
        codeAnalyser.init(temp, language);

        GraphModifier graphModifier = new GraphModifier();

        int index = 1;
        for (RevisionRange revision : engine) {
            for(Commit commit : revision.getCommits()) {
                File file = codeAnalyser.analyse(name, index);

                graphModifier.loadFile(file, language);
                graphModifier.modify(commit);
                graphModifier.saveFile();

                index++;
            }
        }
    }

    /**
     * Type of repository. All types supported by LibVCS4j.
     */
    public enum Type {
        GIT, HG, SVN;
    }

}

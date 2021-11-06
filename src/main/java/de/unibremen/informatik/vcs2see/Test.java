package de.unibremen.informatik.vcs2see;

import de.unibremen.informatik.st.libvcs4j.Commit;
import de.unibremen.informatik.st.libvcs4j.RevisionRange;
import de.unibremen.informatik.st.libvcs4j.VCSEngine;
import de.unibremen.informatik.st.libvcs4j.VCSEngineBuilder;
import org.xml.sax.SAXException;

import java.io.File;
import java.io.IOException;

public class Test {

    public static void main(String[] args) throws IOException, SAXException {
        VCSEngine engine = VCSEngineBuilder
                .ofGit("C:\\Users\\Felix Gaebler\\Documents\\Universität Bremen\\Bachelorarbeit\\vcs2see")
                .build();

        GraphModifier graphModifier = new GraphModifier();
        graphModifier.loadFile(new File("test/vcs2see-1.gxl"), CodeAnalyser.Language.JAVA);

        int index = 1;
        for (RevisionRange revision : engine) {
            for(Commit commit : revision.getCommits()) {
                graphModifier.modify(commit);
                if(index++ == 2) {
                    return;
                }
            }
        }

        graphModifier.saveFile();
    }

}

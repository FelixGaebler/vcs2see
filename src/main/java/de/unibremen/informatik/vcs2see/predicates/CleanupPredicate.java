package de.unibremen.informatik.vcs2see.predicates;

import java.io.File;
import java.io.FilenameFilter;

public class CleanupPredicate implements FilenameFilter {

    @Override
    public boolean accept(File dir, String name) {
        return name.endsWith(".cpf")
                || name.endsWith(".files")
                || name.endsWith(".tok")
                || name.endsWith(".csv")
                || name.endsWith(".rfg");
    }

}

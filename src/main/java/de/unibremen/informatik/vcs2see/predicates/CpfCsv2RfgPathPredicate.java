package de.unibremen.informatik.vcs2see.predicates;

import java.io.File;
import java.util.function.Predicate;

public class CpfCsv2RfgPathPredicate implements Predicate<String> {

    @Override
    public boolean test(String path) {
        File directory = new File(path);

        if(!directory.exists()) {
            System.out.println("Path does not exist");
            return false;
        }

        if(!directory.isDirectory()) {
            System.out.println("Path is not a directory");
            return false;
        }

        if(!new File(directory, "cpfcsv2rfg.py").exists()) {
            System.out.println("cpfcsv2rfg.py not found in path");
            return false;
        }

        return true;
    }

}

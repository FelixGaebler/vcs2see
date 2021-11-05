package de.unibremen.informatik.vcs2see.predicates;

import java.io.File;
import java.util.function.Predicate;

public class BauhausPathPredicate implements Predicate<String> {

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

        if(!new File(directory, "cpf").exists()) {
            System.out.println("cpf not found in path");
            return false;
        }

        if(!new File(directory, "rfgscript").exists()) {
            System.out.println("rfgscript not found in path");
            return false;
        }

        if(!new File(directory, "rfgexport").exists()) {
            System.out.println("rfgexport not found in path");
            return false;
        }

        return true;
    }

}

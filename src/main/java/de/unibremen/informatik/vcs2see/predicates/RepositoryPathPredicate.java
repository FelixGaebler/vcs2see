package de.unibremen.informatik.vcs2see.predicates;

import de.unibremen.informatik.vcs2see.RepositoryCrawler;

import java.io.File;
import java.util.function.Predicate;

public class RepositoryPathPredicate implements Predicate<String> {

    private final RepositoryCrawler.Type type;

    public RepositoryPathPredicate(RepositoryCrawler.Type type) {
        this.type = type;
    }

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

        // Preconditions by type of the repository.
        switch (type) {
            case GIT:
                if(!new File(directory, ".git").exists()) {
                    System.out.println(".git not found in directory");
                    return false;
                }

                if(!new File(directory, ".git").isDirectory()) {
                    System.out.println(".git is not a directory");
                    return false;
                }
                break;

            case HG:
                break;

            case SVN:
                break;
        }

        return true;
    }

}

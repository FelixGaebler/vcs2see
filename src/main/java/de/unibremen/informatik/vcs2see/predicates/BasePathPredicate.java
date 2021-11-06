package de.unibremen.informatik.vcs2see.predicates;

import java.util.function.Predicate;

public class BasePathPredicate implements Predicate<String> {

    @Override
    public boolean test(String path) {
        return true;
    }

}

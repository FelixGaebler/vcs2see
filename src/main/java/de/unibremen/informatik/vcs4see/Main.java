package de.unibremen.informatik.vcs4see;

import java.io.IOException;

public class Main {

    public static void main(String[] args) throws IOException {
        String NAME = args[0];
        Boolean EVOLUTION = Boolean.parseBoolean(args[1]);
        String REPOSITORY = args[2];

        Vcs2See vcs2see = new Vcs2See(NAME, EVOLUTION, REPOSITORY);
    }

}

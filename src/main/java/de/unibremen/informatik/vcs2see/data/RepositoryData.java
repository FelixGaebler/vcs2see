package de.unibremen.informatik.vcs2see.data;

import de.unibremen.informatik.vcs2see.CodeAnalyser;
import de.unibremen.informatik.vcs2see.RepositoryCrawler;
import lombok.Data;

@Data
public class RepositoryData {

    private String name;

    private RepositoryCrawler.Type type;

    private CodeAnalyser.Language language;

    private String path;

    private String basePath;

}

package de.unibremen.informatik.vcs4see;

import lombok.Getter;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class FileInfo {

    private final Map<String, Integer> editsPerAuthor;

    @Getter
    private int totalEdits;

    @Getter
    private LocalDateTime latestChange;

    public FileInfo() {
        this.editsPerAuthor = new HashMap<>();
        this.totalEdits = 0;
    }

    public void addEdit(final String author, final LocalDateTime latestChange) {
        this.editsPerAuthor.putIfAbsent(author, 0);
        this.editsPerAuthor.put(author, this.editsPerAuthor.get(author) + 1);
        this.totalEdits++;
        this.latestChange = latestChange;
    }

    public Map<String, Integer> getEditsPerAuthor() {
        return sortByValue(this.editsPerAuthor);
    }

    private Map<String, Integer> sortByValue(Map<String, Integer> unsortedMap) {
        List<Map.Entry<String, Integer>> list = new ArrayList<>(unsortedMap.entrySet());

        list.sort((o1, o2) -> o1.getValue().compareTo(o2.getValue()) == 0
                ? o1.getKey().compareTo(o2.getKey())
                : o1.getValue().compareTo(o2.getValue()));

        return list.stream().collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (a, b) -> b, HashMap::new));

    }

}

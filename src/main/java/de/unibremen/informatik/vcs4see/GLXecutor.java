package de.unibremen.informatik.vcs4see;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class GLXecutor {

    private final String PATH = "C:\\Program Files (x86)\\Bauhaus\\bin\\";

    public GLXecutor(Language language) throws IOException {
        File directory = new File("");
        cpf(directory, language);
        cpfcsv2rfg(directory);
        rfgexport(directory);
        long files = cleanup(directory);
        System.out.println("Cleaned up " + files + "files");
    }

    private void run(ProcessBuilder processBuilder) throws IOException {
        Process process = processBuilder.start();
        BufferedReader input = new BufferedReader(new InputStreamReader(process.getInputStream()));
        String line;
        while ((line = input.readLine()) != null)
            System.out.println(line);
    }

    private void cpf(File directory, Language language) throws IOException {
        List<String> cmd = new ArrayList<>(Arrays.asList(PATH + "cpf", "-m", "100", "-c", "clones.cpf", "-s", "clones.csv", "-a"));
        for (String extension : language.getExtensions()) {
            cmd.add("-i");
            cmd.add("\"." + extension + "\"");
        }
        cmd.add(".");

        ProcessBuilder processBuilder = new ProcessBuilder();
        processBuilder.command(cmd.toArray(String[]::new));
        processBuilder.directory(directory);

        run(processBuilder);
    }

    private void cpfcsv2rfg(File directory) throws IOException {
        ProcessBuilder processBuilder = new ProcessBuilder();
        processBuilder.command("cpfcsv2rfg", "clones.cpf", "clones.csv", "clones.rfg");
        processBuilder.directory(directory);

        run(processBuilder);
    }

    private void rfgexport(File directory) throws IOException {
        ProcessBuilder processBuilder = new ProcessBuilder();
        processBuilder.command("rfgexport", "-o", "Clones", "-f", "GXL", "clones.rfg", "clones.gxl");
        processBuilder.directory(directory);

        run(processBuilder);
    }

    private long cleanup(File directory) {
        return Stream.of(new File(directory, "clones.cpf").delete(),
                new File(directory, "clones.csv").delete(),
                new File(directory, "clones.rfg").delete(),
                new File(directory, "tokens.files").delete(),
                new File(directory, "tokens.tok").delete())
                .filter(Boolean::booleanValue)
                .count();
    }

    public enum Language {
        C("C", "i", "c", "h"),
        CPP("C++", "ii", "cpp", "cxx", "c++", "cc", "tcc", "hpp", "hxx", "h++", "hh", "C", "H", "inl", "preinc"),
        CS("C#", "cs"),
        ADA("Ada", "adb", "ads", "ada"),
        JAVA("Java", "java");

        @Getter
        private final String label;

        @Getter
        private final String[] extensions;

        Language(String label, String... extensions) {
            this.label = label;
            this.extensions = extensions;
        }
    }

}

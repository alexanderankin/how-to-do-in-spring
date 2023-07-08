package info.ankin.how.exp.tfe.gp.test;

import info.ankin.how.exp.tfe.gp.gp2.GoLexer;
import info.ankin.how.exp.tfe.gp.gp2.GoParser;
import lombok.SneakyThrows;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;

import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

public class GpTest {
    public static void main(String[] args) {
        // parser("var abc IPRanges = (*ipRanges)(nil)").varDecl();
        // System.out.println(parser("var i").varDecl().getText());
        // System.out.println(parser("const i").constDecl().getText());
        // System.out.println(parser("i := 1").shortVarDecl().DECLARE_ASSIGN());
        // var x = parser("var i = 1").varDecl();
        // System.out.println(x);

        System.out.println(parser("int").type_().getText());
        System.out.println(parser("=").assign_op().getText());
        System.out.println(parser("i").expressionList().getText());
        System.out.println(parser("1").expressionList().getText());
        GoParser.AssignmentContext assignment = parser("i = 1 + 1").assignment();
        System.out.println(assignment.getText());
        System.out.println(assignment.children.get(0).getClass().getSimpleName() + ": " + assignment.children.get(0).getText());
        System.out.println(assignment.children.get(1).getClass().getSimpleName() + ": " + assignment.children.get(1).getText());
        System.out.println(assignment.children.get(2).getClass().getSimpleName() + ": " + assignment.children.get(2).getText());
        System.out.println(assignment.children.size());
    }

    public static void main2(String[] args) {
        GoParser.SourceFileContext parse = parse("""
                package tfe;

                // abc hi
                var abc IPRanges = (*ipRanges)(nil)
                """);

        System.out.println(parse.toStringTree());
    }

    public static void main1(String[] args) {
        try (ZipArchive zipArchive = new ZipArchive(Path.of(System.getProperty("user.home"), "resources", "go-tfe.zip"))) {
            List<ZipEntry> goFiles = zipArchive.files().stream()
                    .filter(e -> e.getName().endsWith(".go"))
                    .filter(e -> !e.getName().contains("test"))
                    .toList();

            for (ZipEntry goFile : goFiles) {
                String contents = zipArchive.read(goFile);
                GoParser.SourceFileContext parsed = parse(contents);
                System.out.println(parsed.toStringTree());
            }
        }

    }

    private static GoParser.SourceFileContext parse(String contents) {
        GoParser goParser = parser(contents);
        return goParser.sourceFile();
    }

    private static GoParser parser(String contents) {
        var stream = CharStreams.fromString(contents);
        var lexer = new GoLexer(stream);
        var tokens = new CommonTokenStream(lexer);
        return new GoParser(tokens);
    }

    static class ZipArchive implements AutoCloseable {
        private final Path path;
        private ZipFile zipFile;
        private List<ZipEntry> files;

        ZipArchive(Path path) {
            this.path = path;
        }

        private ZipFile getZipFile() {
            if (zipFile == null) open();
            return zipFile;
        }

        @SneakyThrows
        ZipArchive open() {
            zipFile = new ZipFile(path.toFile());
            return this;
        }

        List<ZipEntry> files() {
            if (files == null) {
                files = new ArrayList<>();
                getZipFile().entries().asIterator().forEachRemaining(files::add);
            }

            return files;
        }

        @SneakyThrows
        String read(ZipEntry zipEntry) {
            return new String(zipFile.getInputStream(zipEntry).readAllBytes(), StandardCharsets.UTF_8);
        }

        @SneakyThrows
        @Override
        public void close() {
            if (zipFile != null) zipFile.close();
        }
    }
}

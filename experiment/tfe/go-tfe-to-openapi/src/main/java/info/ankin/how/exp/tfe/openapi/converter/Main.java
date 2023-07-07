package info.ankin.how.exp.tfe.openapi.converter;

import info.ankin.how.exp.tfe.openapi.converter.generated.GoLexer;
import info.ankin.how.exp.tfe.openapi.converter.generated.GoParser;
import lombok.SneakyThrows;
import org.antlr.v4.runtime.*;
import org.springframework.util.StreamUtils;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.AbstractMap;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

public class Main {
    public static void main(String[] args) {
        new Main().run();
    }

    @SneakyThrows
    private void run() {
        String goLexerFile = StreamUtils.copyToString(getClass().getResourceAsStream("/info/ankin/how/exp/tfe/openapi/converter/GoLexer.g4"), StandardCharsets.UTF_8);
        String goParserFile = StreamUtils.copyToString(getClass().getResourceAsStream("/info/ankin/how/exp/tfe/openapi/converter/GoParser.g4"), StandardCharsets.UTF_8);

        // System.out.println(goLexerFile);
        // System.out.println(goParserFile);

        // wget 'https://github.com/hashicorp/go-tfe/raw/main/run.go'
        String goFile = Files.readString(Path.of("/resources/open-source/tfe/go-tfe/run.go"));
        // System.out.println(goFile);

        CodePointCharStream cPCS = CharStreams.fromString(goFile);

        Lexer lexer = new GoLexer(cPCS);

        CommonTokenStream commonTokenStream = new CommonTokenStream(lexer);

        GoParser goParser = new GoParser(commonTokenStream);

        GoParser.SourceFileContext sourceFileContext = goParser.sourceFile();

        String text = sourceFileContext.packageClause().packageName.getText();
        System.out.println(text);

        Set<Class<? extends ParserRuleContext>> filter = Set.of(
                GoParser.DeclarationContext.class,
                GoParser.MethodDeclContext.class,
                GoParser.FunctionDeclContext.class
        );

        List<ParserRuleContext> contexts = sourceFileContext.children.stream()
                .filter(ParserRuleContext.class::isInstance)
                .map(ParserRuleContext.class::cast)
                .filter(c -> filter.contains(c.getClass())).toList();

        System.out.println(contexts.stream().map(e -> e.getClass().getSimpleName().toString()).toList().toString());



        if (2 > 1) return;

        List<GoParser.DeclarationContext> declaration = sourceFileContext.declaration();
        System.out.println(declaration.size());
        for (GoParser.DeclarationContext declarationContext : declaration) {
            GoParser.TypeDeclContext typeCtx = declarationContext.typeDecl();
            GoParser.ConstDeclContext constCtx = declarationContext.constDecl();
            GoParser.VarDeclContext varCtx = declarationContext.varDecl();

            List<String> list = Arrays.asList(
                            new AbstractMap.SimpleEntry<String, ParserRuleContext>("typeCtx", typeCtx),
                            new AbstractMap.SimpleEntry<String, ParserRuleContext>("constCtx", constCtx),
                            new AbstractMap.SimpleEntry<String, ParserRuleContext>("varCtx", varCtx))
                    .stream().filter(e -> e.getValue() != null)
                    .map(AbstractMap.SimpleEntry::getValue).map(e -> e.getText()).toList();
            if (list.size() == 1) {
                System.out.println("\n--------------\nnew Rule:\n\n\n");
                System.out.println(list.get(0));
            } else
                System.out.println(String.join("------------\nNew Rule:\n\n\n", list));
        }
    }
}

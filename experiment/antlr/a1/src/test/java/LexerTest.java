import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.atn.ATNConfigSet;
import org.antlr.v4.runtime.dfa.DFA;
import org.antlr.v4.runtime.misc.Interval;
import org.junit.jupiter.api.Test;

import java.util.BitSet;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;

class LexerTest {

    @Test
    void test_package() {
        MyLexer myLexer = new MyLexer(CharStreams.fromString("package 'abc'"));
        myLexer.removeErrorListeners();
        myLexer.addErrorListener(new StrictListener());
        List<String> expected = List.of("package", " ", "'abc'");
        List<? extends Token> tokens = myLexer.getAllTokens();
        List<String> actual = tokens.stream().map(Token::getText).toList();

        assertEquals(expected, actual);
    }

    MyLexer lexer(String input) {
        return Optional.of(input).map(CharStreams::fromString).map(MyLexer::new).stream()
                .peek(Recognizer::removeErrorListeners)
                .peek(m -> m.addErrorListener(new StrictListener()))
                .findAny().orElseThrow();
    }

    MyParser parser(String input) {
        MyParser myParser = new MyParser(new CommonTokenStream(lexer(input)));
        myParser.removeErrorListeners();
        myParser.addErrorListener(new StrictListener());
        return myParser;
    }

    @Test
    void test_imports() {
        var tokens = lexer("import").getAllTokens();
        assertEquals(1, tokens.size());
        assertEquals(List.of("import"), tokens.stream().map(Token::getText).toList());
        assertEquals(List.of(MyLexer.IMPORT), tokens.stream().map(Token::getType).toList());

        assertEquals(1, parser("import \"abc\"").importSpec().importStatement().size());
        assertEquals("\"abc\"", parser("import \"abc\"").importSpec().importStatement(0).importSingle().getText());
        assertEquals(List.of("\"fmt\"", "\"io\""),
                parser("import (\n  \"fmt\"\n  \"io\")").importSpec().importStatement(0).importMultiple()
                        .importIdentifier().stream().map(RuleContext::getText).toList());
        assertEquals(List.of("\"fmt\"", "\"io\""),
                parser("import (\"fmt\"; \"io\")").importSpec().importStatement(0).importMultiple()
                        .importIdentifier().stream().map(RuleContext::getText).toList());
        assertEquals(List.of("f \"fmt\"", "\"io\""),
                parser("import (f \"fmt\"; \"io\")").importSpec().importStatement(0).importMultiple()
                        .importIdentifier().stream().map(RuleContext::getText).toList());
        assertEquals("f", parser("import (f \"fmt\"; \"io\")").importSpec().importStatement(0).importMultiple().importIdentifier(0).IDENTIFIER().getText());
        assertEquals("\"fmt\"", parser("import (f \"fmt\"; \"io\")").importSpec().importStatement(0).importMultiple().importIdentifier(0).DOUBLE_STRING().getText());
    }

    @Test
    void test_comments() {
        assertEquals(List.of("//", "\n", "//abc"),
                lexer("//\n//abc").getAllTokens().stream().map(Token::getText).toList());
        assertEquals(List.of(
                        MyLexer.COMMENT_LINE,
                        MyLexer.NEW_LINE,
                        MyLexer.COMMENT_LINE),
                lexer("//\n//abc").getAllTokens().stream().map(Token::getType).toList());

        assertEquals("// abc", parser("// abc").commentsSpec().getText());
        assertEquals("// abc\n// def", parser("// abc\n// def").commentsSpec().getText());
        assertEquals(1, parser("// abc").commentsSpec().comment().size());

        assertEquals(List.of("// abc", "// def"), parser("""
                // abc
                // def
                """).commentsSpec().comment().stream()
                .map(RuleContext::getText).toList());
    }

    @Test
    void test_blockComments() {
        String example = """
                /*
                abc def
                */
                // abc
                /* some other text */
                """;
        assertEquals(3, parser(example).commentsSpec().comment().size());
        assertEquals("/*\nabc def\n*/", parser(example).commentsSpec().comment(0).getText());
        assertEquals("// abc", parser(example).commentsSpec().comment(1).getText());
        assertEquals("/* some other text */", parser(example).commentsSpec().comment(2).getText());
    }

    // more of an end-to-end test - this should always use the sourceFile starting point
    @Test
    void test_parsing() {
        MyParser.SourceFileContext sourceFileContext = parser("""
                // Hello world
                // welcome to tfe

                package tfe

                import (
                    "context"
                    "fmt"
                    "net/url"
                )
                """)
                .sourceFile();
        System.out.println(sourceFileContext);
    }

    private static class StrictListener extends DiagnosticErrorListener {
        @Override
        public void syntaxError(Recognizer<?, ?> recognizer, Object offendingSymbol, int line, int charPositionInLine, String msg, RecognitionException e) {
            throw new RuntimeException("syntaxError: " + msg);
        }

        @Override
        public void reportAmbiguity(Parser recognizer, DFA dfa, int startIndex, int stopIndex, boolean exact, BitSet ambigAlts, ATNConfigSet configs) {
            String format = "reportAmbiguity d=%s: ambigAlts=%s, input='%s'";
            String decision = getDecisionDescription(recognizer, dfa);
            BitSet conflictingAlts = getConflictingAlts(ambigAlts, configs);
            String text = recognizer.getTokenStream().getText(Interval.of(startIndex, stopIndex));
            String message = String.format(format, decision, conflictingAlts, text);
            // throw new RuntimeException("reportAmbiguity: " + message);
        }

        @Override
        public void reportAttemptingFullContext(Parser recognizer, DFA dfa, int startIndex, int stopIndex, BitSet conflictingAlts, ATNConfigSet configs) {
            String format = "reportAttemptingFullContext d=%s, input='%s'";
            String decision = getDecisionDescription(recognizer, dfa);
            String text = recognizer.getTokenStream().getText(Interval.of(startIndex, stopIndex));
            String message = String.format(format, decision, text);
            // throw new RuntimeException("reportAttemptingFullContext: " + message);
        }

        @Override
        public void reportContextSensitivity(Parser recognizer, DFA dfa, int startIndex, int stopIndex, int prediction, ATNConfigSet configs) {
            String format = "reportContextSensitivity d=%s, input='%s'";
            String decision = getDecisionDescription(recognizer, dfa);
            String text = recognizer.getTokenStream().getText(Interval.of(startIndex, stopIndex));
            String message = String.format(format, decision, text);
            recognizer.notifyErrorListeners(message);
            throw new RuntimeException("reportContextSensitivity: " + message);
        }
    }
}

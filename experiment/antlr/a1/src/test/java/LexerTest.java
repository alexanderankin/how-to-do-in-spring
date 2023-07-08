import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.atn.ATNConfigSet;
import org.antlr.v4.runtime.dfa.DFA;
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

    @Test
    void test_imports() {
        var tokens = lexer("import").getAllTokens();
        assertEquals(1, tokens.size());
        assertEquals(List.of("import"), tokens.stream().map(Token::getText).toList());
        assertEquals(List.of(MyLexer.IMPORT), tokens.stream().map(Token::getType).toList());
    }

    private static class StrictListener implements ANTLRErrorListener {
        @Override
        public void syntaxError(Recognizer<?, ?> recognizer, Object offendingSymbol, int line, int charPositionInLine, String msg, RecognitionException e) {
            throw new RuntimeException("syntaxError: " + msg);
        }

        @Override
        public void reportAmbiguity(Parser recognizer, DFA dfa, int startIndex, int stopIndex, boolean exact, BitSet ambigAlts, ATNConfigSet configs) {
            throw new RuntimeException("reportAmbiguity");
        }

        @Override
        public void reportAttemptingFullContext(Parser recognizer, DFA dfa, int startIndex, int stopIndex, BitSet conflictingAlts, ATNConfigSet configs) {
            throw new RuntimeException("reportAttemptingFullContext");
        }

        @Override
        public void reportContextSensitivity(Parser recognizer, DFA dfa, int startIndex, int stopIndex, int prediction, ATNConfigSet configs) {
            throw new RuntimeException("reportContextSensitivity");
        }
    }
}

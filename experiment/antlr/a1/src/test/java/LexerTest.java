import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.Token;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class LexerTest {

    @Test
    void test_package() {
        MyLexer myLexer = new MyLexer(CharStreams.fromString("package 'abc'"));
        List<String> expected = List.of("package", "'", "abc", "'");
        List<? extends Token> tokens = myLexer.getAllTokens();
        List<String> actual = tokens.stream().map(Token::getText).toList();

        assertEquals(expected, actual);
    }

}

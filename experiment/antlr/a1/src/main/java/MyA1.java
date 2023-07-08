import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.Token;

public class MyA1 {
    public static void main(String[] args) {
        MyLexer myLexer = new MyLexer(CharStreams.fromString("package 'hi'"));
        Token token;
        do {
            token = myLexer.nextToken();
            System.out.println(token);
        } while (token.getType() != Token.EOF);
    }
}

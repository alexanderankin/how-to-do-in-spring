import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.Token;
import org.antlr.v4.runtime.tree.ParseTree;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;

public class MyA1 {
    public static void main(String[] args) {
        MyLexer myLexer = new MyLexer(CharStreams.fromString("import \"fmt\""));
        var map = constants(myLexer.getClass());
        for (Token token : myLexer.getAllTokens()) {
            System.out.printf("%s: '%s'%n", map.get(token.getType()), token.getText());
        }

        System.out.println(map);
        {
            var p = new MyParser(new CommonTokenStream(new MyLexer(CharStreams.fromString("import \"fmt\""))));
            for (var child : p.importSpec().importStatement()) {
                System.out.println(child.importSingle().importIdentifier().getText());
            }
        }
        {
            String multi = """
                    import (
                    "fmt"
                    "other"
                    )""";
            System.out.println(multi);
            var p = new MyParser(new CommonTokenStream(new MyLexer(CharStreams.fromString(multi))));
            for (var child : p.importSpec().importStatement()) {
                for (MyParser.ImportIdentifierContext ii : child.importMultiple().importIdentifier()) {
                    System.out.println(ii.getText());
                }
            }
        }
    }

    private static Map<Integer, String> constants(Class<?> aClass) {
        return Arrays.stream(aClass.getDeclaredFields())
                .filter(f -> Modifier.isStatic(f.getModifiers()))
                .filter(f -> Character.isUpperCase(f.getName().charAt(0)))
                .filter(f -> f.getType() == Integer.class || f.getType() == Integer.TYPE)
                .collect(Collectors.toMap(
                        f -> {
                            try {
                                return (Integer) f.get(null);
                            } catch (Exception e) {
                                throw new RuntimeException(e);
                            }
                        },
                        Field::getName
                ));
    }
}

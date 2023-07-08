import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.Token;

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

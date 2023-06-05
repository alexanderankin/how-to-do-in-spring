package test;

public class Example {
    public static String capitalize1(String input) {
        return input.toUpperCase();
    }

    public static String capitalize2(String input) {
        return input.chars()
                .map(Character::toUpperCase)
                .collect(StringBuilder::new, (stringBuilder, value) -> stringBuilder.append((char) value), StringBuilder::append)
                .toString();
    }
}

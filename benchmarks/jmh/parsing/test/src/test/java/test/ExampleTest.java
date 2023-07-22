package test;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ExampleTest {

    @Test
    void test() {
        assertEquals("ABC", Example.capitalize1("abc"));
        assertEquals("ABC", Example.capitalize2("abc"));
    }

}

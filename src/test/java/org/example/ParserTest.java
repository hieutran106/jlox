package org.example;

import org.junit.Before;
import org.junit.Test;

import java.util.List;

public class ParserTest {
    private Parser underTest;

    @Before
    public void setUp() {

    }

    @Test
    public void testCase1() {
        String source = "var x = \"global\"; { var a = 1;}";
        Scanner scanner = new Scanner(source);

        List<Token> tokens = scanner.scanTokens();
        Parser parser = new Parser(tokens);
    }
}

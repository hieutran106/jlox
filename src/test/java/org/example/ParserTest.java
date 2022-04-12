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
        String source = "2+3*4";
        Scanner scanner = new Scanner(source);

        List<Token> tokens = scanner.scanTokens();
        Parser parser = new Parser(tokens);
        Expr expr = parser.parse();

        AstPrinter printer = new AstPrinter();

        System.out.println(printer.print(expr));
    }
}

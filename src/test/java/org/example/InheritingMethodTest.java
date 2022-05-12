package org.example;

import org.apache.commons.io.IOUtils;
import org.junit.Assert;
import org.junit.Test;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;

public class InheritingMethodTest {
    @Test
    public void testCase1() throws IOException {
        String source =
                "class Doughnut {" +
                "cook() {" +
                "print \"Aha\";" +
                "}" +
                "}" +
                "class BostonCream < Doughnut {}" +
                        "BostonCream().cook();";

        Scanner scanner = new Scanner(source);

        List<Token> tokens = scanner.scanTokens();
        Parser parser = new Parser(tokens);
        List<Stmt> stmts = parser.parse();

        Interpreter interpreter = new Interpreter();

        Resolver resolver = new Resolver(interpreter);
        resolver.resolve(stmts);

        interpreter.interpret(stmts);
    }
}

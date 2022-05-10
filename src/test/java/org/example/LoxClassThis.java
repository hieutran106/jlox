package org.example;

import org.apache.commons.io.IOUtils;
import org.junit.Test;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;

public class LoxClassThis {
    @Test
    public void testCase1() throws IOException {
        InputStream inputStream = getClass().getClassLoader().getResourceAsStream("class_this.lox");
        String source = IOUtils.toString(inputStream, StandardCharsets.UTF_8);
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

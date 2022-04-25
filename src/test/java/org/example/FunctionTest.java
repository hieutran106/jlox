package org.example;

import org.apache.commons.io.IOUtils;
import org.junit.Assert;
import org.junit.Test;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;

public class FunctionTest {
    @Test
    public void testCase1() throws IOException {
        InputStream inputStream = getClass().getClassLoader().getResourceAsStream("function.lox");
        String source = IOUtils.toString(inputStream, StandardCharsets.UTF_8);
        Scanner scanner = new Scanner(source);

        List<Token> tokens = scanner.scanTokens();
        Parser parser = new Parser(tokens);
        List<Stmt> stmts = parser.parse();

        Interpreter interpreter = new Interpreter();
        interpreter.interpret(stmts);

        Environment global = interpreter.getEnvironment();
        LoxFunction foo = (LoxFunction) global.getValues().get("add");
        Assert.assertEquals("<fn add>", foo.toString());
    }

    @Test
    public void testCase3() throws IOException {
        InputStream inputStream = getClass().getClassLoader().getResourceAsStream("functionClosure.lox");
        String source = IOUtils.toString(inputStream, StandardCharsets.UTF_8);
        Scanner scanner = new Scanner(source);

        List<Token> tokens = scanner.scanTokens();
        Parser parser = new Parser(tokens);
        List<Stmt> stmts = parser.parse();

        Interpreter interpreter = new Interpreter();
        interpreter.interpret(stmts);

        Double x = (Double) interpreter.getEnvironment().getValues().get("x");
        Assert.assertNotNull(x);
        Assert.assertEquals(x.doubleValue(), 2, 0.0001);

        Double y = (Double) interpreter.getEnvironment().getValues().get("y");
        Assert.assertNotNull(y);
        Assert.assertEquals(y.doubleValue(), 1, 0.0001);
    }

    @Test
    public void testCase4() throws IOException {
        InputStream inputStream = getClass().getClassLoader().getResourceAsStream("functionClosure2.lox");
        String source = IOUtils.toString(inputStream, StandardCharsets.UTF_8);
        Scanner scanner = new Scanner(source);

        List<Token> tokens = scanner.scanTokens();
        Parser parser = new Parser(tokens);
        List<Stmt> stmts = parser.parse();

        Interpreter interpreter = new Interpreter();
        interpreter.interpret(stmts);
    }
}

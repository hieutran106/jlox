package org.example;

import org.junit.Test;

import java.util.List;

public class InterpreterTest {
    @Test
    public void testCase1() {
        String source = "var a = 1; var b = 2; print a + b;";
        Scanner scanner = new Scanner(source);

        List<Token> tokens = scanner.scanTokens();
        Parser parser = new Parser(tokens);
        List<Stmt> stmts = parser.parse();

        Interpreter interpreter = new Interpreter();
        interpreter.interpret(stmts);


    }
}

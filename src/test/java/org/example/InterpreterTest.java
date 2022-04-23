package org.example;

import org.junit.Test;

import java.util.List;

public class InterpreterTest {
    @Test
    public void testCase1() {
        String source = "{ var b = 1; }";
        Scanner scanner = new Scanner(source);

        List<Token> tokens = scanner.scanTokens();
        Parser parser = new Parser(tokens);
        List<Stmt> stmts = parser.parse();

//        Interpreter interpreter = new Interpreter();
//        interpreter.interpret(stmts);


    }
}

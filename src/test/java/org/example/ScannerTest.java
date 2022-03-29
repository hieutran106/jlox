package org.example;

import org.junit.Assert;
import org.junit.Test;

import java.util.List;

import static org.junit.Assert.*;

public class ScannerTest {

    @Test
    public void testScanTokens() {
        String source = "= \"Hello World\" + \n 13.4 while ordina;";
        Scanner scanner = new Scanner(source);
        List<Token> result= scanner.scanTokens();

        // Test second token
        Token stringLiteral = result.get(1);
        Assert.assertEquals(stringLiteral.type, TokenType.STRING);
        Assert.assertEquals(stringLiteral.lexeme, "\"Hello World\"");
        Assert.assertEquals(stringLiteral.literal, "Hello World");

        // Test the third token
        Token plus = result.get(2);
        Assert.assertEquals(plus.type, TokenType.PLUS);

        // Test the 4th token
        Token number = result.get(3);
        Assert.assertEquals(number.type, TokenType.NUMBER);
        Assert.assertEquals(((Double)number.literal).doubleValue(), 13.4, 0.01);
        Assert.assertEquals(number.line, 2);

        // Test 5th token
        Token keyword = result.get(4);
        Assert.assertEquals(keyword.type, TokenType.WHILE);

        // Test 6th token
        Token identifier = result.get(5);
        Assert.assertEquals(identifier.type, TokenType.IDENTIFIER);
        Assert.assertEquals(identifier.lexeme, "ordina");

    }

    @Test
    public void testScanTokens2() {
        String source = "= \"Hello";
        Scanner scanner = new Scanner(source);
        List<Token> result= scanner.scanTokens();

        Assert.assertEquals(result.size(), 2);
    }
}
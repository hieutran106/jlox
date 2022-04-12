package org.example;

import java.util.List;

import static org.example.TokenType.*;

public class Parser {
    private final List<Token> tokens;
    private int current = 0;

    public Parser(List<Token> tokens) {
        this.tokens = tokens;
    }

    public Expr parse() {
        return expression();
    }

    private Expr expression() {
        return equality();
    }

    private Expr equality() {
        Expr left = comparision();
        while (match(BANG_EQUAL, EQUAL_EQUAL)) {
            Token operator = previous();
            Expr right = comparision();
            left = new Expr.Binary(left, operator, right);
        }
        return left;
    }


    private Expr comparision() {
        Expr left = term();
        while (match(GREATER, GREATER_EQUAL, LESS, LESS_EQUAL)) {
            Token operator = previous();
            Expr right = term();
            left = new Expr.Binary(left, operator, right);
        }
        return left;
    }

    private Expr term() {
        Expr left = factor();
        while (match(PLUS, MINUS)) {
            Token operator = previous();
            Expr right = factor();
            left = new Expr.Binary(left, operator, right);
        }
        return left;
    }

    private Expr factor() {
        Expr left = unary();
        while (match(SLASH, STAR)) {
            Token operator = previous();
            Expr right = unary();
            left = new Expr.Binary(left, operator, right);
        }
        return left;
    }

    private Expr unary() {
        if (match(BANG, MINUS)) {
            Token operator = previous();
            Expr right = unary();
            Expr result = new Expr.Unary(operator, right);
            return result;
        }
        return primary();
    }

    private Expr primary() {
        if (match(TRUE)) return new Expr.Literal(true);
        if (match(FALSE)) return new Expr.Literal(false);
        if (match(NIL)) return new Expr.Literal(null);

        if (match(NUMBER, STRING)) {
            Token token = previous();
            return new Expr.Literal(token.literal);
        }

        if (match(LEFT_PARENT)) {
            Expr expression = expression();
            consume(RIGHT_PARENT, "Expect ')' after expression.");
            return new Expr.Grouping(expression);
        }
        // else handle error
        return null;
    }

    /**
     * My implementation
     * @param type
     * @param msg
     */
    private void consume(TokenType type, String msg) {
        if (check(type)) {
            advance();
            return;
        }
        Lox.error(-1, msg);
    }


    /**
     * Consume a token if match
     * @param types
     * @return
     */
    private boolean match(TokenType... types) {
        for (TokenType type: types) {
            if (check(type)) {
                advance();
                return true;
            }
        }
        return false;
    }

    private Token advance() {
        if (!isAtEnd()) current++;
        return previous();
    }

    /* return true if the current token is of the given type
     */
    private boolean check(TokenType type) {
        if (isAtEnd()) {
            return false;
        }
        return peek().type == type;
    }

    private boolean isAtEnd() {
        return peek().type == EOF;
    }

    private Token peek() {
        return tokens.get(current);
    }

    private Token previous() {
        return tokens.get(current - 1);
    }
}

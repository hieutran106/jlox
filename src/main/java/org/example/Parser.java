package org.example;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.example.TokenType.*;

public class Parser {
    private static class ParseError extends RuntimeException {

    }

    private final List<Token> tokens;
    private int current = 0;

    public Parser(List<Token> tokens) {
        this.tokens = tokens;
    }

    public List<Stmt> parse() {
        List<Stmt> statements = new ArrayList<>();
        while (!isAtEnd()) {
            statements.add(declaration());
        }
        return statements;
    }

    private Stmt declaration() {
       try {
           if (match(CLASS)) return classDeclaration();
           if (match(FUN)) return function("function");
           if (match(VAR)) {
               return varDeclaration();
           }
           return statement();
       } catch (ParseError error) {
           synchronize();
           return null;
       }
    }

    private Stmt classDeclaration() {
        Token name = consume(IDENTIFIER, "Expect class name.");
        consume(LEFT_BRACE, "Expect '{' before class body.");
        List<Stmt.Function> methods = new ArrayList<>();
        while (!check(RIGHT_BRACE) && !isAtEnd()) {
            Stmt.Function method = function("method");
            methods.add(method);
        }
        consume(RIGHT_BRACE, "Expect '}' after class body.");
        // abcd
        return new Stmt.Class(name, methods);

    }

    private Stmt.Function function(String kind) {
        Token name = consume(IDENTIFIER, "Expect " + kind + " name.");
        consume(LEFT_PARENT, "Expect '(' after " + kind + " name.");
        List<Token> parameters = new ArrayList<>();
        if (!check(RIGHT_PARENT)) {
            do {
                if (parameters.size() >= 255) {
                    error(peek(), "Can't have more than 255 parameters.");
                }
                Token param = consume(IDENTIFIER, "Expect parameter name.");
                parameters.add(param);
            } while (match(COMMA));
        }
        consume(RIGHT_PARENT, "Expect ')' after parameters.");
        consume(LEFT_BRACE, "Expect '{' before " + kind + " body.");
        List<Stmt> body = block();
        return new Stmt.Function(name, parameters, body);
    }

    private Stmt varDeclaration() {
        Token name = consume(IDENTIFIER, "Expect variable name.");

        Expr initializer = null;
        if (match(EQUAL)) {
            initializer = expression();
        }
        consume(SEMICOLON, "Expect ';' after variable declaration.");
        return new Stmt.Var(name, initializer);
    }

    private Stmt statement() {
        if (match(RETURN)) return returnStatement();
        if (match(FOR)) return forStatement();
        if (match(IF)) return ifStatement();
        if (match(PRINT)) return printStatement();
        if (match(WHILE)) return whileStatement();
        // parse block
        if (match(LEFT_BRACE)) return new Stmt.Block(block());
        return expressionStatement();
    }

    private Stmt returnStatement() {
        Token keyword = previous();
        Expr value = null;
        if (!check(SEMICOLON)) {
            value = expression();
        }
        consume(SEMICOLON, "Expect ';' after return value.");
        return new Stmt.Return(keyword, value);
    }

    private Stmt forStatement() {
        consume(LEFT_PARENT, "Expect '(' after 'for'.");
        Stmt initializer = null;
        if (match(SEMICOLON)) {
            initializer = null;
        } else if (match(VAR)) {
            initializer = varDeclaration();
        } else {
            initializer = expressionStatement();
        }
        Expr condition = null;

        if (!check(SEMICOLON)) {
            condition = expression();
        }
        consume(SEMICOLON, "Expect ';' after loop condition.");

        Expr increment = null;
        if (!check(RIGHT_PARENT)) {
            increment = expression();
        }
        consume(RIGHT_PARENT, "Expect ')' after for clauses.");

        Stmt body = statement();

        // desugar if statement
        if (increment != null) {
            body = new Stmt.Block(Arrays.asList(
                    body,
                    new Stmt.Expression(increment)
            ));
        }

        if (condition == null) {
            condition = new Expr.Literal(true);
        }
        body = new Stmt.While(condition, body);

        if (initializer != null) {
            body = new Stmt.Block(Arrays.asList(initializer, body));
        }
        return body;
    }

    private Stmt whileStatement() {
        consume(LEFT_PARENT, "Expect '(' after 'while'.");
        Expr condition = expression();
        consume(RIGHT_PARENT, "Expect ')' after while condition.");
        Stmt body = statement();

        Stmt.While whileStmt = new Stmt.While(condition, body);
        return whileStmt;
    }

    private Stmt ifStatement() {
        consume(LEFT_PARENT, "Expect '(' after 'if'.");
        Expr condition = expression();
        consume(RIGHT_PARENT, "Expect ')' after if condition");
        Stmt thenBranch = statement();

        Stmt elseBranch = null;
        if (match(ELSE)) {
            elseBranch = statement();
        }
        return new Stmt.If(condition, thenBranch, elseBranch);

    }

    private List<Stmt> block() {
        List<Stmt> statements = new ArrayList<>();
        while (!check(RIGHT_BRACE) && !isAtEnd()) {
            Stmt stmt = declaration();
            statements.add(stmt);
        }
        consume(RIGHT_BRACE, "Expect '}' after block.");
        return statements;
    }

    private Stmt printStatement() {
        Expr value = expression();
        consume(SEMICOLON, "Expect ';' after value.");
        return new Stmt.Print(value);
    }

    private Stmt expressionStatement() {
        Expr expr = expression();
        consume(SEMICOLON, "Expect ';' after value.");
        return new Stmt.Expression(expr);
    }

    private Expr expression() {
        return assignment();
    }

    private Expr assignment() {
        Expr expr = or();

        if (match(EQUAL)) {
            Token equals = previous();
            Expr value = assignment();

            if (expr instanceof Expr.Variable) {
                Token name = ((Expr.Variable) expr).name;
                Expr.Assign assignmentExpr = new Expr.Assign(name, value);
                return assignmentExpr;
            } else if (expr instanceof Expr.Get) {
                Expr.Get getExpression = (Expr.Get) expr;
                Expr getObject = getExpression.object;
                Token name = getExpression.name;
                Expr.Set set = new Expr.Set(getObject, name, value);
                return set;
            }

            error(equals, "Invalid assignment target.");
        }
        return expr;
    }

    private Expr or() {
        Expr expr = and();

        while (match(OR)) {
            Token operator = previous();
            Expr right = and();
            expr = new Expr.Logical(expr, operator, right);
        }

        return expr;
    }

    private Expr and() {
        Expr expr = equality();
        while (match(AND)) {
            Token operator = previous();
            Expr right = equality();
            expr = new Expr.Logical(expr, operator, right);
        }
        return expr;
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
        return call();
    }

    private Expr call() {
        Expr expr  = primary();
        while (true) {
            // allow match a series of calls like fn(1)(2)(3)
            if (match(LEFT_PARENT)) {
                expr = finishCall(expr);
            } else if (match(DOT)) {
                Token name = consume(IDENTIFIER,
                        "Expect property name after '.'.");
                expr = new Expr.Get(expr, name);
            } else {
                break;
            }
        }
        return expr;
    }

    private Expr finishCall(Expr callee) {
        List<Expr> arguments = new ArrayList<>();
        if (!check(RIGHT_PARENT)) {
            do {
                // Maximum argument counts
                if (arguments.size() >= 255) {
                    // Note that the code here reports an error if it encounters too many arguments, but it doesn’t throw the error.
                    error(peek(), "Can't have more than 255 arguments.");
                }
                arguments.add(expression());
            } while (match(COMMA));
        }

        Token paren = consume(RIGHT_PARENT, "Expect ')' after arguments.");
        return new Expr.Call(callee, paren, arguments);
    }


    private Expr primary() {
        if (match(TRUE)) return new Expr.Literal(true);
        if (match(FALSE)) return new Expr.Literal(false);
        if (match(NIL)) return new Expr.Literal(null);

        if (match(NUMBER, STRING)) {
            Token token = previous();
            return new Expr.Literal(token.literal);
        }

        if (match(THIS)) {
            return new Expr.This(previous());
        }

        if (match(IDENTIFIER)) {
            Token token = previous();
            return new Expr.Variable(token);
        }

        if (match(LEFT_PARENT)) {
            Expr expression = expression();
            consume(RIGHT_PARENT, "Expect ')' after expression.");
            return new Expr.Grouping(expression);
        }
        // None of primary() match, throw an error
        throw error(peek(), "Expect expression.");
    }

    private void synchronize() {
        advance();

        while (!isAtEnd()) {
            if (previous().type == SEMICOLON) return;

            switch (peek().type) {
                case CLASS:
                case FUN:
                case VAR:
                case FOR:
                case IF:
                case WHILE:
                case PRINT:
                case RETURN:
                    return;
            }

            advance();
        }
    }

    /**
     * Enter panic mode
     * @param type
     * @param msg
     */
    private Token consume(TokenType type, String msg) {
        if (check(type)) {
            return advance();
        }

        throw error(peek(), msg);
    }

    private ParseError error(Token token, String msg) {
        Lox.error(token, msg);
        return new ParseError();
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

package org.example;

import java.util.ArrayList;
import java.util.List;

import static org.example.TokenType.*;

class Scanner {
    private final String source;
    private final List<Token> tokens = new ArrayList<>();

    // Variables to keep track of where the scanner is in the source code
    private int start = 0;
    private int current = 0;
    private int line = 1;

    Scanner(String source) {
        this.source = source;
    }

    List<Token> scanTokens() {
        while (!isAtEnd()) {
            // We are at the beginning of the next lexeme
            start = current;
            // add token to list
            scanToken();
        }

        // Append one final "end of file" token
        tokens.add(new Token(EOF, "", null, line));
        return this.tokens;
    }

    private void scanToken() {
        char c = advance();
        switch (c) {
            // Sing-character lexemes
            case '(':
                addToken(LEFT_PARENT);
                break;
            case ')':
                addToken(RIGHT_PARENT);
                break;
            case '{':
                addToken(LEFT_BRACE);
                break;
            case '}':
                addToken(RIGHT_BRACE);
                break;
            case ',':
                addToken(COMMA);
                break;
            case '.':
                addToken(DOT);
                break;
            case '-':
                addToken(MINUS);
                break;
            case '+':
                addToken(PLUS);
                break;
            case ';':
                addToken(SEMICOLON);
                break;
            case '*':
                addToken(STAR);
                break;
            // Operators
            case '!':
                addToken(match('=') == true ? BANG_EQUAL : BANG );
                break;
            case '=':
                addToken(match('=') == true ? EQUAL_EQUAL: EQUAL );
                break;
            case '>':
                addToken(match('=') == true ? GREATER_EQUAL: GREATER);
                break;
            case '<':
                addToken(match('=') == true ? LESS_EQUAL: LESS);
                break;
            case '/':
                // handle division operator and comment
                if (match('/')) {
                    // A comment goes until the end of the line
                    while (peek() != '\n' && !isAtEnd()) {
                        advance();
                    }
                } else {
                    addToken(SLASH);
                }
                break;
            case ' ':
            case '\r':
            case '\t':
                // Ignore whitespace
                break;
            case '\n':
                line++;
                break;
            case '"':
                string();
                break;
            default:
                if (isDigit(c)) {
                    number();
                } else {
                    Lox.error(line, "Unexpected character.");
                }
                break;

        }
    }

    private void number() {
        while (isDigit(peek())) {
            advance();
        }

        // Look for fractional part
        if (peek() == '.' && isDigit(peekNext())) {
            // consume the "."
            advance();
            // consume the rest
            while (isDigit(peek())) {
                advance();
            }
        }

        Double number = Double.parseDouble(source.substring(start, current));
        addToken(NUMBER, number);

    }

    private void string() {
        while (peek() != '"' && !isAtEnd()) {
            // Lox support multi-line strings
            if (peek() == '\n') line++;
            advance();
        }

        if (isAtEnd()) {
            Lox.error(line, "Unterminated string.");
            return;
        }

        // The closing ".
        advance();

        // Trim the surrounding quotes.
        String value = source.substring(start + 1, current - 1);
        addToken(STRING, value);
    }

    private boolean match(char expected) {
        if (isAtEnd()) {
            return false;
        }
        if (source.charAt(current) != expected) {
            return false;
        }

        current ++;
        return true;
    }

    private char peek() {
        if (isAtEnd()) {
            return '\0';
        }
        return source.charAt(current);
    }

    private char peekNext() {
        if (current + 1 >= source.length()) {
            return '\0';
        }
        return source.charAt(current + 1);
    }

    private boolean isDigit(char c) {
        return c >= '0' && c <='9';
    }

    private boolean isAtEnd() {
        return current >= source.length();
    }

    private char advance() {
        return source.charAt(current++);
    }

    private void addToken(TokenType type) {
        addToken(type, null);
    }

    private void addToken(TokenType type, Object literal) {
        String text = source.substring(start, current);
        tokens.add(new Token(type, text, literal, line));
    }
}

package com.cristian.app.lox;

import com.cristian.app.Lox;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.cristian.app.lox.TokenType.*;

public class Scanner {
    private final String source;
    private final List<Token> tokens = new ArrayList<>();
    private int start;
    private int current;
    private int line = 1;
    private static final Map<String, TokenType> keywords;

    static {
        keywords = new HashMap<>();
        keywords.put("and", AND);
        keywords.put("class", CLASS);
        keywords.put("else", ELSE);
        keywords.put("false", FALSE);
        keywords.put("true", TRUE);
        keywords.put("if", IF);
        keywords.put("while", WHILE);
        keywords.put("for", FOR);
        keywords.put("nil", NIL);
        keywords.put("or", OR);
        keywords.put("print", PRINT);
        keywords.put("return", RETURN);
        keywords.put("super", SUPER);
        keywords.put("this", THIS);
        keywords.put("var", VAR);
        keywords.put("fun", FUN);
        keywords.put("break", BREAK);
    }

    public Scanner(String source) {
        this.source = source;
    }


    public List<Token> scan() {
        while (!isAtEnd()) {
            scanToken();
        }

        tokens.add(new Token(EOF, "", null, line));
        return tokens;
    }

    private void scanToken() {
        skipWhitespace();
        if (isAtEnd()) return;

        char c = advance();

        switch (c) {
            case '(' -> addToken(LEFT_PAREN);
            case ')' -> addToken(RIGHT_PAREN);
            case '{' -> addToken(LEFT_BRACE);
            case '}' -> addToken(RIGHT_BRACE);
            case ',' -> addToken(COMMA);
            case '.' -> addToken(DOT);
            case ';' -> addToken(SEMICOLON);
            case '-' -> addToken(MINUS);
            case '+' -> addToken(PLUS);
            case '/' -> {
                if (match('/')) while (peek() != '\n' && !isAtEnd()) advance();
                else addToken(SLASH);
            }
            case '*' -> addToken(STAR);
            case '!' -> addToken(match('=') ? BANG_EQUAL : BANG);
            case '<' -> addToken(match('=') ? LESS_EQUAL : LESS);
            case '>' -> addToken(match('=') ? GREATER_EQUAL : GREATER);
            case '=' -> addToken(match('=') ? EQUAL_EQUAL : EQUAL);
            case '"' -> string();
            default -> {
                if (Character.isDigit(c)) number();
                else if (Character.isLetter(c)) identifier();
                else Lox.error(line, "Unexpected character.");
            }
        }
        start = current;
    }

    private void identifier() {
        while (Character.isLetterOrDigit(peek())) advance();
        String text = source.substring(start, current);
        TokenType type = keywords.get(text);
        if (type == null) type = IDENTIFIER;
        addToken(type);
    }

    private void number() {
        while (Character.isDigit(peek())) advance();
        if (peek() == '.' && Character.isDigit(peekNext())) {
            advance();
            while (Character.isDigit(peek())) advance();
        }
        Double value = Double.parseDouble(source.substring(start, current));
        addToken(NUMBER, value);
    }

    private char peekNext() {
        if (current + 1 > source.length()) return '\0';
        return source.charAt(current + 1);
    }

    private void string() {
        while (peek() != '"' && !isAtEnd()) {
            if (peek() == '\n') line++;
            advance();
        }
        if (isAtEnd()) {
            Lox.error(line, "String missing '\"'");
            return;
        }
        advance();
        String value = source.substring(start + 1, current - 1);
        addToken(STRING, value);
    }

    private boolean match(char c) {
        if (!isAtEnd() && peek() == c) {
            current++;
            return true;
        }
        return false;
    }

    private char peek() {
        if (isAtEnd()) return '\0';
        return source.charAt(current);
    }

    private void addToken(TokenType tokenType) {
        addToken(tokenType, null);
    }

    private void addToken(TokenType tokenType, Object o) {
        String lexeme = source.substring(start, current);
        tokens.add(new Token(tokenType, lexeme, o, line));
    }

    private boolean isAtEnd() {
        return current >= source.length();
    }

    private void skipWhitespace() {
        while (!isAtEnd() && Character.isWhitespace(source.charAt(start))) {
            if (source.charAt(start) == '\n') line++;
            start++;
            current = start;
        }
    }

    private char advance() {
        return source.charAt(current++);
    }
}

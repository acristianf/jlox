package com.cristian.app.lox;

import com.cristian.app.Lox;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static com.cristian.app.lox.TokenType.*;

public class Parser {
    private static class ParseError extends RuntimeException {
    }

    private final List<Token> tokens;
    private int current = 0;
    private int loopDepth = 0;

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
            if (match(VAR)) return varDeclaration();
            return statement();
        } catch (ParseError error) {
            synchronize();
            return null;
        }
    }

    private Stmt varDeclaration() {
        Token identifier = consume(IDENTIFIER, "Expected variable identifier.");
        Expr initializer = null;
        if (match(EQUAL)) {
            initializer = expression();
        }
        consume(SEMICOLON, "Expected ';' after declaration.");
        return new Stmt.Var(identifier, initializer);
    }

    private Stmt statement() {
        if (match(IF)) return ifStatement();
        if (match(WHILE)) return whileStatement();
        if (match(FOR)) return forStatement();
        if (match(PRINT)) return printStatement();
        if (match(LEFT_BRACE)) return new Stmt.Block(block());
        if (match(FUN)) return funStatement();
        if (match(RETURN)) return returnStatement();
        if (match(BREAK)) {
            if (loopDepth > 0) return breakStatement();
            else throw new ParseError();
        }
        return expressionStatement();
    }

    private Stmt returnStatement() {
        Token keyword = previous();
        Expr value = null;
        if (!check(SEMICOLON)) {
            value = expression();
        }
        consume(SEMICOLON, "Expected ';' after return value.");
        return new Stmt.Return(keyword, value);
    }

    private Stmt funStatement() {
        Token identifier = new Token(IDENTIFIER, "", null, tokens.get(current).line);
        if (match(IDENTIFIER)) {
            identifier = previous();
        }
        consume(LEFT_PAREN, "Expect '(' after function identifier.");
        List<Token> params = new ArrayList<>();
        while (match(IDENTIFIER)) {
            Token param = previous();
            params.add(param);
            match(COMMA);
        }
        consume(RIGHT_PAREN, "Expect ')' after param list.");
        Stmt body = statement();
        return new Stmt.Function(identifier, params, body);
    }

    private Stmt breakStatement() {
        Stmt stmt = new Stmt.Break();
        consume(SEMICOLON, "Expected ';' after break.");
        return stmt;
    }

    private Stmt forStatement() {
        consume(LEFT_PAREN, "Expected '(' after for.");
        Stmt initializer;
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
        consume(SEMICOLON, "Expected ';' after loop condition.");

        Expr lastExpression = null;
        if (!check(RIGHT_PAREN)) {
            lastExpression = expression();
        }
        consume(RIGHT_PAREN, "Expected ')' after for statement.");

        Stmt body = statement();
        if (lastExpression != null) {
            body = new Stmt.Block(Arrays.asList(body, new Stmt.Expression(lastExpression)));
        }
        if (condition == null) condition = new Expr.Literal(true);
        body = new Stmt.While(condition, body);
        if (initializer != null) {
            body = new Stmt.Block(Arrays.asList(initializer, body));
        }
        return body;
    }

    private Stmt whileStatement() {
        loopDepth++;
        consume(LEFT_PAREN, "Expected '(' after while.");
        Expr condition = expression();
        consume(RIGHT_PAREN, "Expected ')' after while condition.");
        Stmt body = statement();
        loopDepth--;
        return new Stmt.While(condition, body);
    }

    private Stmt ifStatement() {
        consume(LEFT_PAREN, "Expected '(' after if.");
        Expr condition = expression(); // NOTE: Maybe handle null
        consume(RIGHT_PAREN, "Expected ')' after if condition.");
        Stmt thenStatement = statement();
        Stmt elseStatement = null;
        if (match(ELSE)) {
            elseStatement = statement();
        }
        return new Stmt.If(condition, thenStatement, elseStatement);
    }

    private List<Stmt> block() {
        List<Stmt> statements = new ArrayList<>();
        while (!check(RIGHT_BRACE) && !isAtEnd()) {
            statements.add(declaration());
        }
        consume(RIGHT_BRACE, "Expected '}' after block.");
        return statements;
    }

    private Stmt expressionStatement() {
        Expr value = expression();
        consume(SEMICOLON, "Expect ';' after expression.");
        return new Stmt.Expression(value);
    }

    private Stmt printStatement() {
        Expr value = expression();
        consume(SEMICOLON, "Expect ';' after value.");
        return new Stmt.Print(value);
    }

    private Expr expression() {
        return assignment();
    }

    private Expr assignment() {
        Expr expr = logical_or();
        if (match(EQUAL)) {
            Token equals = previous();
            Expr value = assignment();
            if (expr instanceof Expr.Variable) {
                Token identifier = ((Expr.Variable) expr).identifier;
                return new Expr.Assign(identifier, value);
            }
            throw error(equals, "Invalid assignment target.");
        }
        return expr;
    }

    private Expr logical_or() {
        Expr expr = logical_and();
        while (match(OR)) {
            Token operator = previous();
            Expr right = logical_and();
            expr = new Expr.Logical(expr, operator, right);
        }
        return expr;
    }

    private Expr logical_and() {
        Expr expr = equality();
        while (match(AND)) {
            Token operator = previous();
            Expr right = equality();
            expr = new Expr.Logical(expr, operator, right);
        }
        return expr;
    }

    private Expr equality() {
        Expr expr = comparison();

        while (match(BANG_EQUAL, EQUAL_EQUAL)) {
            Token operator = previous();
            Expr right = comparison();
            expr = new Expr.Binary(expr, operator, right);
        }

        return expr;
    }

    private Expr comparison() {
        Expr expr = term();

        while (match(LESS, LESS_EQUAL, GREATER, GREATER_EQUAL)) {
            Token operator = previous();
            Expr right = term();
            expr = new Expr.Binary(expr, operator, right);
        }

        return expr;
    }

    private Expr term() {
        Expr expr = factor();

        while (match(PLUS, MINUS)) {
            Token operator = previous();
            Expr right = factor();
            expr = new Expr.Binary(expr, operator, right);
        }

        return expr;
    }

    private Expr factor() {
        Expr expr = unary();

        while (match(STAR, SLASH)) {
            Token operator = previous();
            Expr right = unary();
            expr = new Expr.Binary(expr, operator, right);
        }

        return expr;
    }

    private Expr unary() {
        if (match(MINUS, BANG)) {
            Token operator = previous();
            Expr right = primary();
            return new Expr.Unary(operator, right);
        }

        return primary();
    }

    private Expr funExpression(Token identifier) {
        List<Expr> params = new ArrayList<>();
        while (!isAtEnd() && !check(RIGHT_PAREN)) {
            params.add(expression());
            match(COMMA);
        }
        consume(RIGHT_PAREN, "Expected ')' after param list.");
        return new Expr.Function(identifier, params);
    }

    private Expr primary() {
        if (match(FALSE)) return new Expr.Literal(false);
        if (match(TRUE)) return new Expr.Literal(true);
        if (match(NIL)) return new Expr.Literal(null);
        if (match(NUMBER, STRING)) return new Expr.Literal(previous().literal);
        if (match(IDENTIFIER)) {
            Token identifier = previous();
            if (match(LEFT_PAREN)) {
                return funExpression(identifier);
            }
            return new Expr.Variable(identifier);
        }
        if (match(LEFT_PAREN)) {
            Expr expr = expression();
            consume(RIGHT_PAREN, "Expect ')' after expression.");
            return new Expr.Grouping(expr);
        }
        throw error(peek(), "Expect expression.");
    }

    private Token consume(TokenType tokenType, String errMsg) {
        if (check(tokenType)) return advance();
        throw error(peek(), errMsg);
    }

    private ParseError error(Token token, String errMsg) {
        Lox.error(token, errMsg);
        return new ParseError();
    }

    private boolean match(TokenType... types) {
        for (TokenType type : types) {
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

    private boolean check(TokenType type) {
        if (isAtEnd()) return false;
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

    private void synchronize() {
        advance();
        while (!isAtEnd()) {
            if (previous().type == SEMICOLON) return;
            switch (peek().type) {
                case CLASS, FUN, VAR, FOR, IF, WHILE, PRINT, RETURN -> {
                    return;
                }
            }
            advance();
        }
    }
}

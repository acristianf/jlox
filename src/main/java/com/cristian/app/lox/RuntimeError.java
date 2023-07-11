package com.cristian.app.lox;

public class RuntimeError extends RuntimeException {
    final Token token;

    public Token getToken() {
        return token;
    }

    RuntimeError(Token token, String err) {
        super(err);
        this.token = token;
    }
}

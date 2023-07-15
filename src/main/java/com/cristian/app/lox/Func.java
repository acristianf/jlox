package com.cristian.app.lox;

import java.util.List;

public class Func {
    public Environment environment;
    public Environment closure;
    public final List<Token> params;
    public final Stmt body;
    private final String name;

    public Func(String name, List<Token> params, Stmt body, Environment closure) {
        this.name = name;
        this.params = params;
        this.body = body;
        this.closure = closure;
    }

    @Override
    public String toString() {
        return "<fn " + this.name + ">";
    }
}

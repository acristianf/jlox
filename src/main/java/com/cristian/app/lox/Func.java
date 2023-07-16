package com.cristian.app.lox;

import java.util.List;

public class Func implements LoxCallable {
    public Environment environment;
    public Environment closure;
    public final List<Token> params;
    public final List<Stmt> body;
    private final String name;

    public Func(String name, List<Token> params, List<Stmt> body, Environment closure) {
        this.name = name;
        this.params = params;
        this.body = body;
        this.closure = closure;
    }

    @Override
    public String toString() {
        return "<fn " + this.name + ">";
    }

    @Override
    public Object call(Interpreter interpreter, List<Object> arguments) {
        environment = new Environment(closure);
        for (int i = 0; i < params.size(); i++) {
            environment.define(params.get(i).lexeme, arguments.get(i));
        }
        try {
            interpreter.executeBlock(body, environment);
        } catch (Return r) {
            return r.value;
        }
        return null;
    }
}

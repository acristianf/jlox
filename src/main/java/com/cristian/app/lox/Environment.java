package com.cristian.app.lox;

import java.util.HashMap;
import java.util.Set;

public class Environment {
    Environment outer;
    private final HashMap<String, Object> values = new HashMap<>();

    public Environment() {
        this.outer = null;
    }

    public Environment(Environment outer) {
        this.outer = outer;
    }


    public void define(String identifier, Object value) {
        values.put(identifier, value);
    }

    public void assign(Token identifier, Object value) {
        if (values.containsKey(identifier.lexeme)) {
            values.put(identifier.lexeme, value);
            return;
        }
        if (this.outer != null) {
            outer.assign(identifier, value);
            return;
        }
        throw new RuntimeError(identifier, "Undefined variable '" + identifier.lexeme + "'.");
    }

    @Override
    public String toString() {
        String outerString = null;
        if (outer != null) {
            outerString = outer.toString();
        }
        return "Environment{" +
                "outer=" + outerString +
                ", values=" + values +
                '}';
    }

    public Object get(Token identifier) {
        if (values.containsKey(identifier.lexeme)) {
            return values.get(identifier.lexeme);
        }
        if (this.outer != null) {
            return outer.get(identifier);
        }
        throw new RuntimeError(identifier, "Undefined identifier '" + identifier.lexeme + "'.");
    }

    public Object getAt(Integer distance, String name) {
        return ancestor(distance).values.get(name);
    }

    private Environment ancestor(Integer distance) {
        Environment environment = this;
        for (int i = 0; i < distance; i++) {
            environment = environment.outer;
        }
        return environment;
    }

    public void assignAt(Integer distance, Token identifier, Object value) {
        ancestor(distance).values.put(identifier.lexeme, value);
    }
}

package com.cristian.app.lox;

import java.util.Map;

public class LoxClass {
    public final String identifier;
    public final Map<String, Func> methods;

    public LoxClass(String identifier, Map<String, Func> methods) {
        this.identifier = identifier;
        this.methods = methods;
    }

    @Override
    public String toString() {
        return "LoxClass{" +
                "identifier='" + identifier + '\'' +
                '}';
    }

    public Func findMethod(String lexeme) {
        if (methods.containsKey(lexeme)) return methods.get(lexeme);
        return null;
    }
}

package com.cristian.app.lox;

public class LoxClass {
    public final String identifier;

    public LoxClass(String identifier) {
        this.identifier = identifier;
    }

    @Override
    public String toString() {
        return "LoxClass{" +
                "identifier='" + identifier + '\'' +
                '}';
    }
}

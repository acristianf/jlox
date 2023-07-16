package com.cristian.app.lox;

public class LoxInstance {
    final LoxClass klass;

    public LoxInstance(LoxClass klass) {
        this.klass = klass;
    }

    @Override
    public String toString() {
        return "LoxInstance{" +
                "klass=" + klass +
                '}';
    }
}

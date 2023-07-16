package com.cristian.app.lox;

import java.util.HashMap;
import java.util.Map;

public class LoxInstance {
    final LoxClass klass;
    private final Map<String, Object> fields = new HashMap<>();

    public LoxInstance(LoxClass klass) {
        this.klass = klass;
    }

    @Override
    public String toString() {
        return "LoxInstance{" +
                "klass=" + klass +
                '}';
    }

    public Object get(Token identifier) {
        if (fields.containsKey(identifier.lexeme)) {
            return fields.get(identifier.lexeme);
        }
        Func method = klass.findMethod(identifier.lexeme);
        if (method != null) return method.bind(this);
        throw new RuntimeError(identifier, "Undefined property '" + identifier.lexeme + "'.");
    }

    public void set(Token identifier, Object value) {
        fields.put(identifier.lexeme, value);
    }
}
